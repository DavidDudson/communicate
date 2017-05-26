package org.multibot

import java.util

import com.amatkivskiy.gitter.sdk.Constants
import com.amatkivskiy.gitter.sdk.api.builder.GitterApiBuilder
import com.amatkivskiy.gitter.sdk.converter.UserJsonDeserializer
import com.amatkivskiy.gitter.sdk.model.request.{ChatMessagesRequestParams, UnreadRequestParam, UpdateRoomRequestParam, UserAccountType}
import com.amatkivskiy.gitter.sdk.model.response._
import com.amatkivskiy.gitter.sdk.model.response.ban.BanResponse
import com.amatkivskiy.gitter.sdk.model.response.group.GroupResponse
import com.amatkivskiy.gitter.sdk.model.response.message.{MessageResponse, UnReadMessagesResponse}
import com.amatkivskiy.gitter.sdk.model.response.room.RoomResponse
import com.amatkivskiy.gitter.sdk.model.response.room.welcome.{WelcomeMessageContainer, WelcomeResponse}
import com.amatkivskiy.gitter.sdk.sync.api.SyncGitterApi
import com.amatkivskiy.gitter.sdk.sync.client.SyncGitterApiClient
import com.google.gson.{Gson, GsonBuilder}
import retrofit.converter.GsonConverter
import com.amatkivskiy.gitter.sdk.util.RequestUtils.convertChatMessagesParamsToMap
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
  * Gitter client always returning Try
  */
class SafeSyncGitterApiClient(api: SyncGitterApi) {

  private final val LOGGER = LoggerFactory.getLogger(SafeSyncGitterApiClient.getClass)

  /**
    * Just ensure all errors are logged.
    * We don't actually want the app to crash because of these errors.
    */
  def exceptionHandling[A]: PartialFunction[Throwable, Try[A]] = { case e =>
    LOGGER.warn("Rest api threw exception", e)
    Failure(e)
  }

  // User API
  def getCurrentUser: Try[UserResponse] =
    Try(api.getCurrentUser)
      .recoverWith(exceptionHandling)

  def getUserOrgs(userId: String): Try[List[OrgResponse]] =
    Try(api.getUserOrgs(userId).asScala.toList)
      .recoverWith(exceptionHandling)

  def getUserRepos(userId: String): Try[List[RepoResponse]] =
    Try(api.getUserRepos(userId).asScala.toList)
      .recoverWith(exceptionHandling)

  def searchUsers(`type`: UserAccountType, searchTerm: String): Try[List[UserResponse]] =
    Try(api.searchUsers(`type`, searchTerm).results.asScala.toList)
      .recoverWith(exceptionHandling)

  def searchUsers(searchTerm: String): Try[List[UserResponse]] =
    Try(api.searchUsers(searchTerm).results.asScala.toList)
      .recoverWith(exceptionHandling)

  def getCurrentUserRooms: Try[List[RoomResponse]] =
    Try(api.getCurrentUserRooms.asScala.toList)
      .recoverWith(exceptionHandling)

  // Rooms API
  def getUserRooms(userId: String): Try[RoomResponse] =
    Try(api.getUserRooms(userId))
      .recoverWith(exceptionHandling)


  def getRoomUsers(roomId: String): Try[List[UserResponse]] =
    Try(api.getRoomUsers(roomId).asScala.toList)
      .recoverWith(exceptionHandling)

  def joinRoom(userId: String, roomId: String): Try[RoomResponse] =
    Try(api.joinRoom(userId, roomId))
      .recoverWith(exceptionHandling)

  def updateRoom(roomId: String, params: UpdateRoomRequestParam): Try[RoomResponse] =
    Try(api.updateRoom(roomId, params))
      .recoverWith(exceptionHandling)

  /**
    * Removes specified user from the room. It can be used to leave room.
    *
    * @param roomId Id of the room.
    * @param userId Id of the user to remove.
    * @return true if successful.
    */
  def leaveRoom(roomId: String, userId: String): Try[BooleanResponse] =
    Try(api.leaveRoom(roomId, userId))
      .recoverWith(exceptionHandling)

  def searchRooms(searchTerm: String): Try[List[RoomResponse]] =
    Try(api.searchRooms(searchTerm).results.asScala.toList)
      .recoverWith(exceptionHandling)

  /**
    * @return suggested rooms for the current user.
    */
  def getSuggestedRooms: Try[List[RoomResponse]] =
    Try(api.getSuggestedRooms.asScala.toList)
      .recoverWith(exceptionHandling)

  def searchRooms(searchTerm: String, limit: Int): Try[List[RoomResponse]] =
    Try(api.searchRooms(searchTerm, limit).results.asScala.toList)
      .recoverWith(exceptionHandling)

  def deleteRoom(roomId: String): Try[BooleanResponse] =
    Try(api.deleteRoom(roomId))
      .recoverWith(exceptionHandling)

  // Messages API
  def sendMessage(roomId: String, text: String): Try[MessageResponse] =
    Try(api.sendMessage(roomId, text))
      .recoverWith(exceptionHandling)


  // Dont recover here, it already logged
  def getRoomMessages(roomId: String): Try[List[MessageResponse]] =
    getRoomMessages(roomId, null)

  def getRoomMessages(roomId: String, params: ChatMessagesRequestParams): Try[List[MessageResponse]] =
    Try(api.getRoomMessages(roomId, convertChatMessagesParamsToMap(params)).asScala.toList)
      .recoverWith(exceptionHandling)

  def getRoomMessageById(roomId: String, messageId: String): Try[MessageResponse] =
    Try(api.getRoomMessageById(roomId, messageId))
      .recoverWith(exceptionHandling)

  def updateMessage(roomId: String, chatMessageId: String, text: String): Try[MessageResponse] =
    Try(api.updateMessage(roomId, chatMessageId, text))
      .recoverWith(exceptionHandling)

  def markReadMessages(userId: String, roomId: String, chatIds: List[String]): Try[BooleanResponse] =
    Try(api.markReadMessages(userId, roomId, new UnreadRequestParam(chatIds.asJava)))
      .recoverWith(exceptionHandling)

  def getUnReadMessages(userId: String, roomId: String): Try[UnReadMessagesResponse] =
    Try(api.getUnReadMessages(userId, roomId))
      .recoverWith(exceptionHandling)

  // Groups API
  def getCurrentUserGroups: Try[List[GroupResponse]] =
    Try(api.getCurrentUserGroups(null).asScala.toList)
      .recoverWith(exceptionHandling)

  def getCurrentUserAdminGroups: Try[List[GroupResponse]] =
    Try(api.getCurrentUserGroups("admin").asScala.toList)
      .recoverWith(exceptionHandling)

  def getGroupById(groupId: String): Try[GroupResponse] =
    Try(api.getGroupById(groupId))
      .recoverWith(exceptionHandling)

  def getGroupRooms(groupId: String): Try[List[RoomResponse]] =
    Try(api.getGroupRooms(groupId).asScala.toList)
      .recoverWith(exceptionHandling)

  // Ban API
  def getBannedUsers(roomId: String): Try[List[BanResponse]] =
    Try(api.getBannedUsers(roomId).asScala.toList)
      .recoverWith(exceptionHandling)

  /**
    * Ban user of the specific room. Be careful when banning user in the private room:
    * BanResponse.user and BanResponse.bannedBy will be null.
    *
    * @param roomId   id of the room.
    * @param username name of the user.
    * @return ban request response
    */
  def banUser(roomId: String, username: String): Try[BanResponse] =
    Try(api.banUser(roomId, username))
      .recoverWith(exceptionHandling)

  def unBanUser(roomId: String, username: String): Try[BooleanResponse] =
    Try(api.unBanUser(roomId, username))
      .recoverWith(exceptionHandling)

  // Welcome API
  def getRoomWelcome(roomId: String): Try[WelcomeResponse] =
    Try(api.getRoomWelcome(roomId))
      .recoverWith(exceptionHandling)

  def setRoomWelcome(roomId: String, message: String): Try[WelcomeMessageContainer] =
    Try(api.setRoomWelcome(roomId, message))
      .recoverWith(exceptionHandling)
}

object SafeSyncGitterApiClient {
  class Builder extends GitterApiBuilder[SafeSyncGitterApiClient.Builder, SafeSyncGitterApiClient] {
    override def build: SafeSyncGitterApiClient = {
      prepareDefaultBuilderConfig()
      val gson: Gson = new GsonBuilder().registerTypeAdapter(classOf[UserResponse], new UserJsonDeserializer).create
      restAdapterBuilder.setConverter(new GsonConverter(gson))
      val api: SyncGitterApi = restAdapterBuilder.build.create(classOf[SyncGitterApi])
      new SafeSyncGitterApiClient(api)
    }

    override protected def getFullEndpointUrl: String = Constants.GitterEndpoints.GITTER_API_ENDPOINT + "/" + apiVersion + "/"
  }
}
