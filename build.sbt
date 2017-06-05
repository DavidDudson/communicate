name := "communicate"

scalaVersion := "2.12.2"

val doNotPublishSettings = Seq(publish := {})

// Only publish to bintray as a snapshot currently
val publishSettings =
  Seq(
    publishTo := Some("Artifactory Realm" at "http://oss.jfrog.org/artifactory/oss-snapshot-local"),
    bintrayReleaseOnPublish := false,
    credentials := List(Path.userHome / ".bintray" / ".artifactory")
      .filter(_.exists)
      .map(Credentials(_))
  )

lazy val gitterDependencies =
  Seq(
    dependencyOverrides += "com.squareup.okio" % "okio" % "1.11.0",
    libraryDependencies += "com.github.amatkivskiy" % "gitter.sdk.async" % "1.6.0",
    libraryDependencies += "com.github.amatkivskiy" % "gitter.sdk.sync" % "1.6.0"
  )

lazy val cacheDependencies =
  Seq(
    libraryDependencies += "com.google.guava" % "guava" % "22.0"
  )

lazy val testDependencies =
  Seq(
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )

lazy val loggerDependencies =
  Seq(
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.10"
  )

lazy val asyncDependencies =
  Seq(
    libraryDependencies += "io.monix" %% "monix" % "2.3.0"
  )

lazy val scalaCompilerDependencies =
  Seq(
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )


val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.2",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val coreInterpreter =
  project.in(file("interpreters/core"))
    .settings(
      commonSettings,
      asyncDependencies,
      cacheDependencies,
      doNotPublishSettings
    )

lazy val gitterApi =
  project.in(file("api/gitter"))
    .settings(
      commonSettings,
      gitterDependencies,
      cacheDependencies,
      testDependencies,
      loggerDependencies,
      doNotPublishSettings
    )
    .dependsOn(coreInterpreter)

lazy val root =
  aggregateProjects(
    coreInterpreter,
    gitterApi
  )