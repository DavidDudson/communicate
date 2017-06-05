package communicate.api.gitter

import java.util
import java.util.concurrent.TimeUnit

import com.amatkivskiy.gitter.sdk.async.faye.client.{AsyncGitterFayeClient, AsyncGitterFayeClientBuilder}
import com.amatkivskiy.gitter.sdk.async.faye.listeners.RoomMessagesChannel
import com.amatkivskiy.gitter.sdk.async.faye.model.MessageEvent
import com.amatkivskiy.gitter.sdk.model.response.message.MessageResponse
import com.amatkivskiy.gitter.sdk.model.response.room.RoomResponse
import com.google.common.cache.{Cache, CacheBuilder}
import communicate.api.core._
import communicate.intepreter.core.Interpreter
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

import scala.util.Try
import monix.execution.Scheduler.Implicits.global

case class GitterBot(interpreter: Interpreter, accountToken: String, roomsToJoin: List[String]) {

  private final val LOGGER = LoggerFactory.getLogger(GitterBot.getClass)
  var debugMode: Boolean = false

  var currentUserId: String = ""

  /**
    * Input => Output id cache
    * We use this to keep a mapping of which messages we have replied to.
    * This is so if they are updated we can update our response
    */
  private val recentMessageIdCache: Cache[String, String] =
    CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(10, TimeUnit.MINUTES) // Same as gitter edit time
      .build[String, String]()

  val rest: SafeSyncGitterApiClient = new SafeSyncGitterApiClient.Builder()
    .withAccountToken(accountToken)
    .build()

  val faye: AsyncGitterFayeClient = new AsyncGitterFayeClientBuilder()
    .withAccountToken(accountToken)
    .withFailListener(e => LOGGER.warn("Faye issue", e))
    .withOnDisconnected(() => start())
    .withOkHttpClient(new OkHttpClient())
    .build()

  def connectToChannel(id: String): Unit = {

    faye.subscribe(new RoomMessagesChannel(id) {

      override def onSubscribed(channel: String, messagesSnapshot: util.List[MessageResponse]): Unit =
        LOGGER.info(s"subscribed")

      override def onFailed(channel: String, ex: Exception): Unit =
        LOGGER.warn(s"subscribeFailed to $ex")

      def isMessageIdInCache(messageId: String): Boolean =
        recentMessageIdCache.getIfPresent(messageId) != null

      @Override
      def onMessage(channel: String, message: MessageEvent) {
        // We must ignore all messages from the bot
        // Also, apparently the event can be null?
        val user = message.message.fromUser
        Option(user).foreach { u =>
          if (!currentUserId.contains(u.id)) {
            time("processing time", () => processMessage(message))
          }
        }
      }

      def processMessage(message: MessageEvent): Unit = {
        val messageId = message.message.id
        message.message.text match {
          case "ping" =>
            rest.sendMessage(id, "pong")
          case "debug on" =>
            debugMode = true
            rest.sendMessage(id, "debug enabled")
          case "debug off" =>
            debugMode = false
            rest.sendMessage(id, "debug disabled")
          case PlainInterpretableMessage(input) =>
            updateIncomingMessage(messageId, input)
            create(messageId, input)
          case InterpretableMessage(input) if isCreate(message) =>
            updateIncomingMessage(messageId, input)
            create(messageId, input)
          case InterpretableMessage(input) if isUpdateOfCommand(message, messageId) =>
            update(messageId, input)
          case InterpretableMessage(input) if isUpdateOfNonCommand(message, messageId) && isLastMessage(messageId) =>
            update(messageId, input)
          case _ if isUpdateOfCommand(message, messageId) =>
            delete(messageId)
          case _ if isRemove(message) =>
            delete(messageId)
          case _ =>
        }
      }

      def time(part: String, f: () => Unit): Unit = {
        if (debugMode) {
          val start = System.nanoTime()
          f()
          val end = System.nanoTime()
          val total = (end - start) / 1e6
          rest.sendMessage(id, s"$part took $total ms")
        } else {
          f()
        }
      }

      /**
        * If someone were to write
        * ! 1 + 1
        *
        * It would be wrapped like
        *
        * ```scala
        * ! 1 + 1
        * ```
        *
        * Note this only works when the bot OWNS the room.
        * Admin privileges is not enough.
        *
        * It actually suprises me that this works at all, since it is not possible via the web GUI
        */
      private def updateIncomingMessage(messageId: String, input: String) = {
        rest.getCurrentUserRooms
          .filter(_.contains((r: RoomResponse) => r.id == id))
          .foreach { _ =>
            LOGGER.info("Wrapping plain message")
            rest.updateMessage(id, messageId, Sanitizer.sanitizeOutput(input))
          }
      }

      /**
        * Means that the message was updated, and we have already interpreted it before
        */
      def isUpdateOfNonCommand(message: MessageEvent, messageId: String): Boolean =
        !isMessageIdInCache(messageId) && message.operation == "update"

      /**
        * Means that the message was updated, and we have not interpreted it before
        */
      def isUpdateOfCommand(message: MessageEvent, messageId: String): Boolean =
        isMessageIdInCache(messageId) && message.operation == "update"

      def isCreate(message: MessageEvent): Boolean =
        message.operation == "create"

      private def isRemove(message: MessageEvent) =
        message.operation == "remove"

      /**
        * Means that the message is the last message in the channel
        */
      def isLastMessage(messageId: String): Boolean =
        rest.getRoomMessages(id)
          .filter(_.last.id == messageId)
          .isSuccess
    })

    /** Create a new message by interpreting the input in this room */
    def create(messageId: String, input: String) =
      rest.sendMessage(id, "Interpreting... Please wait")
        .flatMap(r => rest.updateMessage(id, r.id, unsafeIntepretAndSanitize(input)))
        .foreach { r =>
          recentMessageIdCache.put(messageId, r.id)
        }

    /** Updating a message with a blank string is gitters way of deleting things */
    def delete(messageId: String) =
      Option(recentMessageIdCache.getIfPresent(messageId))
        .foreach { i =>
          rest.updateMessage(id, i, "")
          recentMessageIdCache.invalidate(i)
        }

    def unsafeIntepretAndSanitize(input: String) = {
      Sanitizer.sanitizeOutput(interpreter.interpret(input).coeval.value.getOrElse(""))
    }

    /** Update an existing response in this room. */
    def update(messageId: String, input: String) =
      Option(recentMessageIdCache.getIfPresent(messageId))
        .flatMap(i => rest.updateMessage(id, i, "Interpreting... Please wait").toOption)
        .flatMap(r => rest.updateMessage(id, r.id, unsafeIntepretAndSanitize(input)).toOption)
        .foreach { r =>
          recentMessageIdCache.put(messageId, r.id)
        }
  }

  def start(): Unit = {
    Try {
      faye.connect(() => {
        rest.getCurrentUser.map { u =>
          currentUserId = u.id
          LOGGER.info(s"connected as ${u.displayName}")
        } getOrElse restRecovery

        roomsToJoin
          .map(rest.searchRooms(_, 1).getOrElse(Nil))
          .map(_.head)
          .map(_.id)
          .foreach(connectToChannel)
      })
    } recover fayeRecovery
  }

  def fayeRecovery: PartialFunction[Throwable, Unit] = {
    case e =>
      LOGGER.warn("restarting faye", e)
      start()
  }

  def restRecovery: PartialFunction[Throwable, Unit] = {
    case e =>
      faye.disconnect()
      LOGGER.warn("rest error", e)
      start()
  }
}
