import actor.DataBaseActor
import actor.DataBaseActor.Save
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, KafkaConsumerActor, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.typesafe.config.ConfigFactory
import model.Protocols.accountFormat
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import service.DataBaseServiceImpl
import spray.json.JsonParser

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.Try

object PersistenceService extends App {

  implicit val system: ActorSystem = ActorSystem("persistence-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  val conf = ConfigFactory.load()
  val kafkaHost: String = conf.getString("akka.kafka.host")
  val kafkaPort: String = conf.getString("akka.kafka.port")
  val kafkaGroupId: String = conf.getString("akka.kafka.groupId")


  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(s"$kafkaHost:$kafkaPort")
    .withGroupId(kafkaGroupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

  val consumer: ActorRef = system.actorOf(KafkaConsumerActor.props(consumerSettings))

  val mongoUri = "mongodb://localhost:27017/signup_db?authMode=scram-sha1"
  val driver = MongoDriver()
  val parsedUri = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(driver.connection)

  val futureConnection = Future.fromTry(connection)
  def db: Future[DefaultDB] = futureConnection.flatMap(_.database("signup_db"))

  val dbService = new DataBaseServiceImpl(db)
  val dbActor = system.actorOf(DataBaseActor.props(dbService), "persistActor")

  val control: Consumer.Control = Consumer
    .plainExternalSource[String, String](consumer, Subscriptions.assignment(new TopicPartition(kafkaGroupId, 0)))
    .via(Flow[ConsumerRecord[String, String]])
    .map { msg =>
      Try{
        val account = accountFormat.read(JsonParser.apply(msg.value()))
        dbActor ! Save(account)
      }
      msg
    }
    .to(Sink.ignore)
    .run()

  println(s"Service online\nPress RETURN to stop...")
  StdIn.readLine()
  system.terminate()
}
