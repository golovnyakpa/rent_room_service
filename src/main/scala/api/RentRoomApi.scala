package my.meetings_room_renter
package api

import zhttp.http._
import zhttp.http.{Http, Request}
import zio._
import zio.json._
import serde._

import my.meetings_room_renter.dao.entities.Rent

object RentRoomApi {

  private def isXAuthInHeaders(headers: Map[String, String], authed: Ref[List[String]]): ZIO[Any, Nothing, Boolean] = {
    authed.get.map { users =>
      headers.contains("X_AUTH") && headers.get("X_AUTH").exists(user => users.contains(user))
    }
  }

  def auth(authed: Ref[List[String]]): Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "auth" =>
      authed.update(lst => lst :+ req.headers.toList.toMap.getOrElse("Auth-Me", "dummy"))
        .zipRight(ZIO.succeed(Response.text("Authed!")))
  }


  def rentRoomEndpoint(authed: Ref[List[String]]): Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rent" =>
      isXAuthInHeaders(req.headers.toList.toMap, authed).flatMap {cond =>
        if (cond) {
          for {
            body <- req.body.asString.orDie
            res <- ZIO.succeed(body.fromJson[Rent])
            answer <- ZIO.succeed(Response.text(res.toString))
          } yield answer
        } else {
          ZIO.succeed(Response.text("Not Authorized").setStatus(Status.NonAuthoritiveInformation))
        }
      }
  }

  val helloEndpoint: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> Path.root / "hello" => Random.nextIntBetween(42, 84).map(n => Response.text(s"Your num: $n"))
  }

  def rentRoomApi(authedUsers: Ref[List[String]]): Http[Any, Nothing, Request, Response] =
    rentRoomEndpoint(authedUsers) ++ auth(authedUsers)
}
