package util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol

object ValidationDirective extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val fieldErrorInfoFormat = jsonFormat2(FieldErrorInfo)
  implicit val modelValidationRejectionFormat = jsonFormat1(ModelValidationRejection)

  def validateModel[T](model: T)(implicit validator: Validator[T]): Directive1[T] = {
    validator(model) match {
      case Nil => provide(model)
      case errors: Seq[FieldErrorInfo] => reject(ModelValidationRejection(errors))
    }
  }

}