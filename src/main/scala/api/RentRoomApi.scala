package my.meetings_room_renter
package api

import zhttp.http._
import zhttp.http.{Http, Request}
import zio._

object RentRoomApi {

  val rentRoomEndpoint: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> Path.root => Response.text("You are welcome!")
  }

  val helloEndpoint: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> Path.root / "hello" => Random.nextIntBetween(42, 84).map(n => Response.text(s"Your num: $n"))
  }

  val rentRoomApi: Http[Any, Nothing, Request, Response] = rentRoomEndpoint ++ helloEndpoint
}
