package service

import model.Account
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document}

import scala.concurrent.{ExecutionContext, Future}

trait DataBaseService {

  def insert(account: Account): Future[Unit]

  def findById(_id: BSONObjectID): Future[Option[Account]]

}

class DataBaseServiceImpl(db: Future[DefaultDB])(implicit ec: ExecutionContext) extends DataBaseService {

  implicit def accountWriter: BSONDocumentWriter[Account] = Macros.writer[Account]
  implicit def accountReader: BSONDocumentReader[Account] = Macros.reader[Account]

  def accountCollection: Future[BSONCollection] = db.map(d => d.collection("accounts"))

  override def insert(account: Account): Future[Unit] =
    accountCollection.flatMap(_.insert(account).map(_ => {}))

  override def findById(_id: BSONObjectID): Future[Option[Account]] =
    accountCollection.flatMap { _.find(document("_id" -> _id)).one[Account] }
}
