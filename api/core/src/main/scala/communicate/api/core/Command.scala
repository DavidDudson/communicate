package communicate.api.core

import scala.util.matching.Regex

object InterpretableMessage {
  def unapply(input: String): Option[String] =
    input match {
      case EmbeddedCommand(i) => Some(i)
      case PrefixedCommand(i) => Some(i)
      case _ => None
    }
}

/**
  * Implies that the message is not in a code block
  */
object PlainInterpretableMessage {
  def unapply(input: String): Option[String] =
    Option(input)
      .filter(CommandUtils.isCommand)
      .filter(s => Sanitizer.sanitizeInput(s) == s)
      .map(_.drop(2))
}

object CommandUtils {
  val commandRegex: Regex = "^!\\s+.*".r
  def isCommand(s: String): Boolean =
    commandRegex.findFirstIn(s).isDefined
}

/**
  * Examples
  * `! foo`
  * ```! foo```
  * ```scala\n! foo\n```
  */
object EmbeddedCommand {
  def unapply(input: String): Option[String] =
    Option(input)
      .map(Sanitizer.sanitizeInput)
      .filter(CommandUtils.isCommand)
      .map(_.drop(2))
}

/**
  * Examples
  * ! foo
  * ! `foo`
  * ! ```foo```
  * ! ```scala\nfoo\n```
  */
object PrefixedCommand {
  def unapply(input: String): Option[String] =
    Option(input)
      .filter(CommandUtils.isCommand)
      .map(_.drop(2))
      .map(Sanitizer.sanitizeInput)
}