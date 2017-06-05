package communicate.intepreter.core

import monix.eval.Task

trait Interpreter {
  def interpret(input: String): Task[String]

  def id: String
}
