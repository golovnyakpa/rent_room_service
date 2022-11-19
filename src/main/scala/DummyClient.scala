package my.meetings_room_renter

import zhttp.http.{Body, Headers, Method, Response}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._

//  val body: Body = Body.fromString(
//    """{"dttmStart": "2022-11-02T13:44:50.198984", "dttmEnd": "2022-11-02T13:44:50.198984", "renter": "qwerrry"}"""
//  )

object DummyClient extends ZIOAppDefault {

  val env: ZLayer[Any, Nothing, ChannelFactory with EventLoopGroup] = ChannelFactory.auto ++ EventLoopGroup.auto()

  def register(login: String, password: String): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url  = "http://localhost:9090/user/register"
    val body = Body.fromString(s"""{"login": "$login","password": "$password"}""")
    Client.request(url, method = Method.POST, content = body)
  }

  def getJwtToken(login: String, password: String): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url        = "http://localhost:9090/user/login"
    val authHeader = Headers.basicAuthorizationHeader(login, password)
    Client.request(url, headers = authHeader, method = Method.POST)
  }

  def showAllRooms(jwt: String): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url                 = "http://localhost:9090/rents"
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.GET)
  }

  val program: ZIO[EventLoopGroup with ChannelFactory, Throwable, Unit] = {
    val url  = "http://localhost:9090/user/register"
    val body = Body.fromString(s"""{"login": "qwerty","password": "qwerty"}""")

    for {
//      registerResp   <- register("test_user", "test_password")
//      registerStatus <- registerResp.body.asString
      jwtResp        <- getJwtToken("test_user", "test_password")
      jwt            <- jwtResp.body.asString
      _              <- Console.printLine(s"Receive jwt: $jwt, code: ${jwtResp.status}, $jwtResp")
      rentsResp      <- showAllRooms(jwt)
      rents          <- rentsResp.body.asString
      _              <- Console.printLine(s"Rents: $rents, $rentsResp")
    } yield ()
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provideLayer(env).exitCode

}
