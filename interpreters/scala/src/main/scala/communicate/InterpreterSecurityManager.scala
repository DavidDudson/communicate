package communicate

import java.io.{File, FilePermission}
import java.net.NetPermission
import java.security.{Permission, SecurityPermission}
import java.util.PropertyPermission

/**
  * Security manager is taken directly from OlegYch/multibot
  */
object InterpreterSecurityManager extends SecurityManager {
  System.setProperty("actors.enableForkJoin", "false")

  private val lock = new Object
  @volatile private var sm: Option[SecurityManager] = None
  @volatile private var activated = false

  def hardenPermissions[T](f: => T): T = lock.synchronized {
    try {
      activate
      f
    } finally deactivate
  }

  override def checkPermission(perm: Permission) {
    if (activated) try {
      deactivate
      doChecks(perm)
    } finally activate
    else {
      //don't use closures here to avoid SOE
      if (sm.isDefined && sm.get != this) {
        sm.get.checkPermission(perm)
      }
    }
  }

  private def doChecks(perm: Permission) {
    val read = perm.getActions == "read"
    val readWrite = perm.getActions == "read,write"
    val allowedMethods = Seq(
      "accessDeclaredMembers", "suppressAccessChecks", "createClassLoader",
      "accessClassInPackage.sun.reflect", "getStackTrace", "getClassLoader", "closeClassLoader",
      "setIO", "getProtectionDomain", "setContextClassLoader", "getClassLoader", "accessClassInPackage.sun.misc", "accessClassInPackage.sun.util.resources.en"
    ).contains(perm.getName)
    val getenv = perm.getName.startsWith("getenv")
    val file = perm.isInstanceOf[FilePermission]
    val property = perm.isInstanceOf[PropertyPermission]
    val security = perm.isInstanceOf[SecurityPermission]
    val net = perm.isInstanceOf[NetPermission]

    def notExistingFile = !new File(perm.getName).exists()

    val allowedFiles =
      Seq( """.*\.class""", """.*\.jar""", """.*classes.*""", """.*\.properties""",
        """.*src/main/scala.*""", """.*/?target""", ".", """.*\$line.*""")
    val isClass = allowedFiles.exists(allowed => perm.getName.replaceAll( """\""" + """\""", "/").matches(allowed) || new File(perm.getName).getAbsolutePath == new File(allowed).getAbsolutePath)

    val readClass = file && isClass && read
    val readMissingFile = file && notExistingFile && read
    def allowedClass(trace: Array[StackTraceElement]) = trace.exists { element =>
      val name = element.getFileName
      //todo apply more robust checks
      List("BytecodeWriters.scala", "Settings.scala", "PathResolver.scala", "JavaMirrors.scala", "ForkJoinPool.java", "Using.scala", "TimeZone.java", "ScriptSecurityManager.scala")
        .contains(name)
    }

    val allow = readMissingFile || readClass || (read && !file) || allowedMethods ||
      (property && readWrite) || (security && perm.getName.startsWith("getProperty.")) ||
      (net && perm.getName.startsWith("specifyStreamHandler")) ||
      allowedClass(new Throwable().getStackTrace)
    if (!allow || getenv) {
      val exception = new SecurityException(perm.toString)
      exception.printStackTrace()
      throw exception
    }
  }

  private def deactivate {
    activated = false
    if (System.getSecurityManager == this) sm.foreach(System.setSecurityManager)
  }

  private def activate {
    val manager = System.getSecurityManager
    if (manager != this) {
      sm = Option(manager)
      System.setSecurityManager(this)
    }
    activated = true
  }
}
