package my.meetings_room_renter

import my.meetings_room_renter.api.RentRoomApi.rentRoomApi
import zhttp.service.Server
import zio._

object App {
  lazy val server: ZIO[Any, Throwable, ExitCode] = for {
    _ <- Console.printLine(s"Server starting at http://localhost:${configuration.configuration.port}")
    _ <- Server.start(configuration.configuration.port, rentRoomApi)
  } yield ExitCode.success
}
