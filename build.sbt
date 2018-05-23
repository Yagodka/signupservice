import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerBaseImage
import com.typesafe.sbt.packager.docker._
import sbt.Keys.libraryDependencies

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

val akkaV = "2.5.12"
val akkaHttpV = "10.1.1"
val kafkaV = "1.1.0"

lazy val commonSettings = Seq(
  organization := "com.new-age-solutions",
  version := "0.1",
  scalaVersion := "2.12.6",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
    "org.apache.kafka" %% "kafka" % kafkaV,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
    "org.reactivemongo" %% "reactivemongo" % "0.13.0"
  )
)

dockerBaseImage := "openjdk:8-jre-alpine"
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "apk add --update bash && rm -rf /var/cache/apk/*")
)

lazy val signUpService = (project in file("sign-up"))
  .settings(name := "SignUpService")
  .settings(commonSettings)
  .settings(
    mainClass in Compile := Some("SignUpWebService")
  )

lazy val persistenceService = (project in file("persistence"))
  .settings(name := "PersistenceService")
  .settings(commonSettings)
  .settings(
    mainClass in Compile := Some("PersistenceService")
  )

