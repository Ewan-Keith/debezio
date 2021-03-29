ThisBuild / scalaVersion     := "2.13.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "debezio",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.4-2",
      "dev.zio" %% "zio-test" % "1.0.4-2" % Test,
      "dev.zio" %% "zio-streams" % "1.0.4-2",
      "io.debezium" % "debezium-api" % "1.4.2.Final",
      "io.debezium" % "debezium-embedded" % "1.4.2.Final",
      "io.debezium" % "debezium-connector-postgres" % "1.4.2.Final"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
