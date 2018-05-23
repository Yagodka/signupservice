package util

import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import Directives._

object Rejection {

  import ValidationDirective._

  def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle { case e@ModelValidationRejection(_) =>
        complete(BadRequest -> e)
      }
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete(MethodNotAllowed -> s"Can't do that! Supported: ${names mkString " or "}!")
      }
      .handleNotFound {
        complete(NotFound -> "Not here!")
      }
      .result()
}
