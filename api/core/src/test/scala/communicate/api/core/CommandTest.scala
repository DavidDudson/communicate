package communicate.api.core

import org.scalatest.FlatSpec

class CommandTest extends FlatSpec {

  "Plain interpretable messages" should "be valid" in {
    ensurePlainInterpretableMessage("! 1 + 1")
  }

  "Negated cases of interpretable messages" should "not be valid" in {
    ensureNotPrefixedCommand(
      """```scala
        |! 1 + 1
        |```""".stripMargin)
    ensureNotEmbeddedCommand(
      raw"""!
           |```scala
           |1 + 1
           |   1 + 1
           |1 + 1
           | 1 + 1
           |```""".stripMargin)
  }

  "Embedded interpretable messages" should "be valid" in {
    ensureEmbeddedCommand("`! 1 + 1`")
    ensureEmbeddedCommand("```! 1 + 1```")
    ensureEmbeddedCommand("``` ! 1 + 1 ```")
    ensureEmbeddedCommand(
      """```scala
        |! 1 + 1
        |```""".stripMargin)
    ensureEmbeddedCommand(
      """```scala
        |! 1 + 1
        |   1 + 1
        |1 + 1
        | 1 + 1
        |```""".stripMargin)
  }

  "Prefixed interpretable messages" should "be valid" in {
    ensurePrefixedCommand("! `1 + 1`")
    ensurePrefixedCommand("! ```1 + 1```")
    ensurePrefixedCommand("! ``` 1 + 1 ```")
    ensurePrefixedCommand(
      raw"""!
        |```scala
        |1 + 1
        |```""".stripMargin)
    ensurePrefixedCommand(
      raw"""!
        |```scala
        |1 + 1
        |   1 + 1
        |1 + 1
        | 1 + 1
        |```""".stripMargin)
  }

  "Interpretable Commands" should "be valid" in {
    ensureInterpretableMessage(
      raw"""!
           |```scala
           |1 + 1
           |```""".stripMargin)
    ensureInterpretableMessage(
      raw"""!
           |```scala
           |1 + 1
           |   1 + 1
           |1 + 1
           | 1 + 1
           |```""".stripMargin)
    ensureInterpretableMessage(
      raw"""```scala
           |!
           |1 + 1
           |```""".stripMargin)
    ensureInterpretableMessage(
      raw"""```scala
           |!
           | 1 + 1
           |   1 + 1
           |1 + 1
           | 1 + 1
           |```""".stripMargin)
  }

  def ensurePlainInterpretableMessage(s: String): Unit = {
    withClue(s) {
      s match {
        case PlainInterpretableMessage(_) => succeed
        case _ => fail("Was not a plain interpretable message")
      }
    }
  }

  def ensurePrefixedCommand(s: String): Unit = {
    withClue(s) {
      s match {
        case PrefixedCommand(_) => succeed
        case _ => fail("Was not a prefixed command")
      }
    }
  }

  def ensureNotPrefixedCommand(s: String): Unit = {
    withClue(s) {
      s match {
        case PrefixedCommand(_) => fail("Was a prefixed command")
        case _ => succeed
      }
    }
  }

  def ensureEmbeddedCommand(s: String): Unit = {
    withClue(s) {
      s match {
        case EmbeddedCommand(_) => succeed
        case _ => fail("Was not an embedded command")
      }
    }
  }

  def ensureNotEmbeddedCommand(s: String): Unit = {
    withClue(s) {
      s match {
        case EmbeddedCommand(_) => fail("Was an embedded command")
        case _ => succeed
      }
    }
  }

  def ensureInterpretableMessage(s: String): Unit = {
    withClue(s) {
      s match {
        case InterpretableMessage(_) => succeed
        case _ => fail("Was not interpretable")
      }
    }
  }
}