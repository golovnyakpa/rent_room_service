package my.meetings_room_renter

import api.RentRoomApi.rentRoomApi

import zhttp.service.Server
import zio._

object App {
  lazy val server: ZIO[Any, Throwable, ExitCode] = for {
    authedUsers <- Ref.make(List.empty[String])
    _          <- Console.printLine(s"Server starting at http://localhost:${configuration.configuration.port}")
    _          <- Server.start(configuration.configuration.port, rentRoomApi(authedUsers))
  } yield ExitCode.success
}
