name := "communicate"

version := "0.1"

scalaVersion := "2.12.2"

lazy val coreInterpreter =
  project.in(file("interpreters/core"))
      .settings(
        libraryDependencies += "io.monix" %% "monix" % "2.3.0",

        // Used only for caching
        libraryDependencies += "com.google.guava" % "guava" % "22.0"
      )

lazy val scalaInterpreter =
  project.in(file("interpreters/scala"))
      .settings(
        libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    .dependsOn(coreInterpreter)

lazy val gitterApi =
  project.in(file("api/gitter"))
    .settings(
      dependencyOverrides += "com.squareup.okio" % "okio" % "1.11.0",
      libraryDependencies += "com.github.amatkivskiy" % "gitter.sdk.async" % "1.6.0",
      libraryDependencies += "com.github.amatkivskiy" % "gitter.sdk.sync" % "1.6.0",
      libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      libraryDependencies += "com.google.guava" % "guava" % "22.0",
      libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.10"
    )
    // Todo break this dependency
    .dependsOn(scalaInterpreter)


lazy val root =
  aggregateProjects(
    coreInterpreter,
    scalaInterpreter
  )