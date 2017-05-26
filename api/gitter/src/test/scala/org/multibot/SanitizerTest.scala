package org.multibot

import org.scalatest.{Assertion, FlatSpec}

class SanitizerTest extends FlatSpec {

  def ensureSanitizedInput(in: String, expected: String): Assertion =
    assert(Sanitizer.sanitizeInput(in).trim === expected.trim)

  def ensureSanitizedOutput(out: String, expected: String): Assertion =
    assert(Sanitizer.sanitizeOutput(out).trim === expected.trim)

  def ensureNoSanitization(in: String) : Assertion =
    ensureSanitizedInput(in, in)

  "Inputs" should "be sanitized" in {
    ensureSanitizedInput("```foo```", "foo")
    ensureSanitizedInput("```\nfoo\n```", "foo")
    ensureSanitizedInput("``` \nfoo\n ```", "foo")
    ensureSanitizedInput("``` \nfoo \n```", "foo")
    ensureSanitizedInput("`foo`", "foo")
    ensureSanitizedInput("```scala\nfoo```", "foo")
    ensureSanitizedInput("```scala \nfoo```", "foo")
    ensureSanitizedInput("```scala \nfoo\n```", "foo")
    ensureSanitizedInput("```scala\n! class Foo {\n}\n```", "! class Foo {\n}")

    // I have no idea why anyone would do this, but make sure the result is expected
    ensureSanitizedInput("````foo````", "`foo`")
    ensureSanitizedInput("``foo``", "`foo`")
  }

  it should "not be sanitized" in {
    ensureNoSanitization("foo")
  }

  "Outputs" should "be sanitized" in {
    ensureSanitizedOutput("1", "```\n1\n```")
    ensureSanitizedOutput("String = foo", "```\nString = \"foo\"\n```")
    ensureSanitizedOutput(
      """1
        |String = foo""".stripMargin,
      """```
        |1
        |String = "foo"
        |```""".stripMargin)
    ensureSanitizedOutput(
      """String = foo
        |1
      """.stripMargin,
      """```
        |String = "foo"
        |1
        |```""".stripMargin)

    ensureSanitizedOutput("\"I `really` like cats\"", "```\n\"I 'really' like cats\"\n```")
  }
}
