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

lazy val root =
  aggregateProjects(
    coreInterpreter,
    scalaInterpreter
  )