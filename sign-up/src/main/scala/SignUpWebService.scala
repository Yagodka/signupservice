import java.util.Properties

import actor.KafkaActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RejectionHandler
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import route.SignUpRoutes
import service.SignUpServiceImpl
import util.Rejection

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object SignUpWebService extends App {

  implicit val system: ActorSystem = ActorSystem("sign-up-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val rejectHandler: RejectionHandler = Rejection.rejectionHandler

  val conf = ConfigFactory.load()
  val webHost = conf.getString("web.host")
  val webPort = conf.getInt("web.port")

  val kafkaHost: String = conf.getString("akka.kafka.host")
  val kafkaPort: String = conf.getString("akka.kafka.port")

  val kafkaProps = new Properties()
  kafkaProps.put("bootstrap.servers", s"$kafkaHost:$kafkaPort")
  kafkaProps.put("acks", "all")
  kafkaProps.put("retries", "0")
  kafkaProps.put("batch.size", "16384")
  kafkaProps.put("linger.ms", "1")
  kafkaProps.put("buffer.memory", "33554432")
  kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val kafkaActor = system.actorOf(KafkaActor.props(kafkaProps), "KafkaActor")

  val persistenceActor = system.actorSelection("akka.tcp://persistence-service@localhost:2553/user/persistActor")

  val service = new SignUpServiceImpl(kafkaActor, persistenceActor)
  val routes = new SignUpRoutes(service).routes
  val bindingFuture = Http().bindAndHandle(routes, webHost, webPort)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}