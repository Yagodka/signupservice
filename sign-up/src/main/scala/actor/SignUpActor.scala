package actor

import java.util.Properties

import akka.actor.{Actor, ActorLogging, Props}
import model.{Account, Protocols}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import util.FutureConverter

import scala.util.{Failure, Success}

case class WrongMessage()

class KafkaActor(props: Properties) extends Actor with ActorLogging with Protocols {

  import context._

  val producer = new KafkaProducer[String, String](props)

  override def receive: Receive = {
    case a: Account =>
      val s = sender()
      FutureConverter.toScala {
        producer.send(new ProducerRecord[String, String]("signUp", a._id.stringify, accountFormat.write(a).compactPrint))
      }.onComplete {
        case Success(_) => s ! Right(a)
        case Failure(err) =>
          log.error(s"Error on the Kafka server: ${err.getMessage}" )
          s ! Left(s"Internal server error")
      }
    case _ =>
      log.error("Received unknown message")
      sender () ! Left(s"Internal server error")
  }
}

object KafkaActor {
  def props(props: Properties): Props = Props(new KafkaActor(props))
}