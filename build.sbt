lazy val ui = (project in file("."))
  .settings(
    name := "stego-ui",
    version := "0.1.0",
    scalaVersion := "2.12.1",
    libraryDependencies += "stego-core" % "stego-core_2.11" % "0.1.0"
  )
