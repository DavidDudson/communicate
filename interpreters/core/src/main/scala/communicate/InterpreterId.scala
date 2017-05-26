package communicate


/**
  * The idea behind this is you may want multiple interpreters
  * Eg. a scala 2.11 interpreter and scala 2.12 interpreter in the same channel
  * Or scalaJVM and scalaJS etc.
  */
case class InterpreterId(language: String, variation: String, style: String)
