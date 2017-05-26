package org.multibot

object Sanitizer {

  def sanitizeOutput(out: String): String = {
    val sanitized = out.replace("`", "\'").trim
    val output = sanitized.split("\n")
      .map(quoteString)
      .mkString("\n")

    s"```\n$output\n```"
  }

  def sanitizeInput(in: String): String = {
    val tripleBackquoted = "^```\\s*(.*?)\\s*```$".r
    val scalaSyntaxBackquoted  = "(?s)^```scala\\s*(.*)\\s*```$".r
    val singleBackquoted = "^`(.*)`$".r

    in match {
      case scalaSyntaxBackquoted(s) => s
      case tripleBackquoted(s) => s
      case singleBackquoted(s) => s
      case s => s
    }
  }

  val quoteString: Function[String, String] = {
    case s if s.startsWith("String = ") =>
      val stringBody = s.stripPrefix("String = ")
      "String = \"" + stringBody + "\""
    case s => s
  }
}

