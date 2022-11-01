package my.meetings_room_renter

import my.meetings_room_renter.App.server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main extends ZIOAppDefault{
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    server
}
