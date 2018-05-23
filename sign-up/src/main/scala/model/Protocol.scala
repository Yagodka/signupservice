package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import reactivemongo.bson.BSONObjectID
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}
import util.{FieldErrorInfo, Validator}

final case class SignUpCmd(email: String, password: String)

final case class OK(uuid: String)
final case class KO(error: String)
final case class Account(_id: BSONObjectID, email: String, password: String)


object SignUpCmdValidator extends Validator[SignUpCmd] {
  private def emailRule(email: String) = """\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\z""".r.findFirstMatchIn(email).isEmpty

  private def nameRule(name: String) = name.length < 8

  override def apply(model: SignUpCmd): Seq[FieldErrorInfo] = {
    val emailErrorOpt: Option[FieldErrorInfo] = validationStage(emailRule(model.email), "email", "email must be valid")

    val passwordErrorOpt: Option[FieldErrorInfo] = validationStage(nameRule(model.password), "password",
      "password length must be minimum 8 characters")

    (emailErrorOpt :: passwordErrorOpt :: Nil).flatten
  }
}

trait Protocols extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val signUpCmdFormat = jsonFormat2(SignUpCmd.apply)
  implicit val okFormat = jsonFormat1(OK.apply)
  implicit val koFormat = jsonFormat1(KO.apply)

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
