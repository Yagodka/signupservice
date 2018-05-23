package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import reactivemongo.bson.BSONObjectID
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

final case class Account(_id: BSONObjectID, email: String, password: String)

object Protocols extends SprayJsonSupport with DefaultJsonProtocol{
  implicit object BSONObjectIDFormat extends JsonFormat[BSONObjectID] {
    def write(uid: BSONObjectID) = JsString(uid.stringify)
    def read(value: JsValue): BSONObjectID = {
      value match {
        case JsString(uid) => BSONObjectID.parse(uid).getOrElse(BSONObjectID.generate())
        case _              => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
  implicit val accountFormat = jsonFormat3(Account.apply)
}
