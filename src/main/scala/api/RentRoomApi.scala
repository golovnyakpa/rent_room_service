package my.meetings_room_renter
package api

import dao.entities.Rent
import serde._

import zhttp.http._
import zio._
import zio.json._

object RentRoomApi {

  def rentRoomEndpoint(authed: Ref[List[String]]): Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rent" =>
      for {
        body   <- req.body.asString.orDie
        res    <- ZIO.succeed(body.fromJson[Rent])
        answer <- ZIO.succeed(Response.text(res.toString))
      } yield answer

  }

  val helloEndpoint: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> Path.root / "hello" => Random.nextIntBetween(42, 84).map(n => Response.text(s"Your num: $n"))
  }

  def rentRoomApi(authedUsers: Ref[List[String]]): Http[Any, Nothing, Request, Response] =
    rentRoomEndpoint(authedUsers) ++ helloEndpoint
}
