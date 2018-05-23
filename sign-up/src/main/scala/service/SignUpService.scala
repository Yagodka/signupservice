package service

import akka.actor.{ActorRef, ActorSelection}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import model.{Account, KO, OK, SignUpCmd}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait SignUpService {
  def signUp(dto: SignUpCmd): Future[Either[KO, OK]]

  def queryByUuid(uuidStr: String): Future[Either[String, Option[Account]]]
}

class SignUpServiceImpl(kafka: ActorRef, persistenceActor: ActorSelection)(implicit ex: ExecutionContext, m: ActorMaterializer) extends SignUpService {

  implicit val timeout = Timeout(5 seconds)

  override def signUp(dto: SignUpCmd): Future[Either[KO, OK]] = {
    val _id = BSONObjectID.generate()
    (kafka ? Account(_id, dto.email, dto.password)).mapTo[Either[String, Account]].map {
      case Right(a) => Right(OK(a._id.stringify))
      case Left(err) => Left(KO(err))
    }
  }

  override def queryByUuid(uid: String): Future[Either[String, Option[Account]]] = {
    BSONObjectID.parse(uid) match {
      case Success(id) =>
        persistenceActor.ask(id.stringify).mapTo[Either[String, Option[Account]]]
      case Failure(_) =>
        Future.successful(Left("Wrong parameter value UID"))
    }
  }
}
