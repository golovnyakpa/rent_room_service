package my.meetings_room_renter

import zhttp.http.{Body, Headers, Method}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._

object DummyClient extends ZIOAppDefault {

  val env: ZLayer[Any, Nothing, ChannelFactory with EventLoopGroup] = ChannelFactory.auto ++ EventLoopGroup.auto()
  val url: String                                                   = "http://localhost:9090/rent"
  val headers: Headers                                              = Headers.host("sports.api.decathlon.com")
  val body: Body = Body.fromString(
    """{"dttmStart": "2022-11-02T13:44:50.198984", "dttmEnd": "2022-11-02T13:44:50.198984", "renter": "qwerrry"}"""
  )

  val program: ZIO[EventLoopGroup with ChannelFactory, Throwable, Unit] = for {
    res  <- Client.request(url, headers = headers, method = Method.POST, content = body)
    data <- res.body.asString
    _    <- Console.printLine(data)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provideLayer(env).exitCode

}
