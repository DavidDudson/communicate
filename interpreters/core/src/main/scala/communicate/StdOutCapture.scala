package communicate

import java.io.{ByteArrayOutputStream, PrintStream}

object StdOutCapture {

  /**
    * Redirect all std out to the
    */
  private def captureOutput[T](block: => T): T = {
    val conOut = new ByteArrayOutputStream
    val conOutStream = new PrintStream(conOut)
    try {
      System.setOut(conOutStream)
      System.setErr(conOutStream)
      Console.withOut(conOutStream) {
        Console.withErr(conOutStream) {
          block
        }
      }
    } finally {
      System setOut System.out
      System setErr System.err
      conOut.flush()
      conOut.reset()
    }
  }
}
