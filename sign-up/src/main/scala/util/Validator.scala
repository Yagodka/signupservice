package util

import akka.http.scaladsl.server._

final case class FieldErrorInfo(name: String, error: String)
final case class ModelValidationRejection(invalidFields: Seq[FieldErrorInfo]) extends Rejection

trait Validator[T] extends (T => Seq[FieldErrorInfo]) {

  protected def validationStage(rule: Boolean, fieldName: String, errorText: String): Option[FieldErrorInfo] =
    if (rule) Some(FieldErrorInfo(fieldName, errorText)) else None

}