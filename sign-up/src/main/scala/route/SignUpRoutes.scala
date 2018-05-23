package route

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.{Protocols, SignUpCmd, SignUpCmdValidator}
import service.SignUpService
import util.ValidationDirective._

import scala.util.{Failure, Success}


class SignUpRoutes(service: SignUpService) extends Protocols {

  implicit val signUpCmdValidator = SignUpCmdValidator

  def routes: Route = logRequestResult("sign-up-service") {
    pathPrefix("api" / "v1") {
      path("sign-up") {
        (post & entity(as[SignUpCmd])) { dto =>
          validateModel(dto).apply { validatedDto =>
            onComplete(service.signUp(validatedDto)) {
              case Success(Right(r)) => complete(OK -> r)
              case Success(Left(e)) => complete(BadRequest -> e)
              case Failure(e) => complete(InternalServerError -> e)
            }
          }
        }
      } ~
        path("query") {
          get {
            parameters('uuid) { uid =>
              onComplete(service.queryByUuid(uid)) {
                case Success(Right(Some(account))) => complete(OK -> account)
                case Success(Right(None)) => complete(NotFound)
                case Success(Left(e)) => complete(BadRequest -> e)
                case Failure(e) => complete(InternalServerError -> e)
              }
            }
          }
        } ~
        path("ping") {
          get {
            complete(OK -> "pong")
          }
        }
    }
  }
}
