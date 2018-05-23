package actor

import actor.DataBaseActor.{FindById, Save}
import akka.actor.{Actor, ActorLogging, Props}
import model.Account
import reactivemongo.bson.BSONObjectID
import service.DataBaseService

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.language.postfixOps

class DataBaseActor(db: DataBaseService) extends Actor with ActorLogging {
  import context._

  override def receive: Receive = {
    case Save(account) =>
      db.insert(account).onComplete {
      case Success(_) =>
        log.debug(s"Entity ${account._id.stringify} successfully recorded")
      case Failure(err) =>
        log.error(s"Error writing to database: ${err.getMessage}")
    }
    case id: String =>
      val s = sender()
      BSONObjectID.parse(id) match {
        case Success(uid) =>
          db.findById(uid).map(account => s ! Right(account))
        case Failure(err) =>
          Future.successful(Left(err.getMessage))

      }
  }
}

object DataBaseActor {
  def props(db: DataBaseService) = Props(new DataBaseActor(db))

  case class Save(account: Account)
  case class FindById(_id: BSONObjectID)
}