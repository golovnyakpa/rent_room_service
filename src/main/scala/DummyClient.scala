package my.meetings_room_renter

import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._

object DummyClient extends ZIOAppDefault {

  val env: ZLayer[Any, Nothing, ChannelFactory with EventLoopGroup] = ChannelFactory.auto ++ EventLoopGroup.auto()

  private def register(
    login: String,
    password: String
  ): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url  = "http://localhost:9091/user/register"
    val body = Body.fromString(s"""{"login": "$login","password": "$password"}""")
    Client.request(url, method = Method.POST, content = body)
  }

  private def getJwtToken(
    login: String,
    password: String
  ): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url        = "http://localhost:9091/user/login"
    val authHeader = Headers.basicAuthorizationHeader(login, password)
    Client.request(url, headers = authHeader, method = Method.POST)
  }

  private def showAllRooms(jwt: String): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url                 = "http://localhost:9090/rents"
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.GET)
  }

  private def logAndFailIfNotExpectedStatus(resp: Response, expectedStatus: Status, logMsg: String) =
    if (resp.status != expectedStatus) ZIO.logError(logMsg) *> ZIO.fail(new RuntimeException(logMsg))
    else ZIO.succeed(resp)

  val program: ZIO[EventLoopGroup with ChannelFactory, Throwable, Unit] =
    for {
      registerResp   <- register("pavel", "test_password")
      registerStatus <- registerResp.body.asString
      jwtResp <- getJwtToken("pavel", "test_password").flatMap(r =>
                   logAndFailIfNotExpectedStatus(r, Status.Ok, "Get jwt: Not expected status")
                 )
      jwt <- jwtResp.body.asString
      _   <- ZIO.logInfo(s"Receive jwt: $jwt, code: ${jwtResp.status}")
      rentsResp <-
        showAllRooms(jwt).flatMap(r => logAndFailIfNotExpectedStatus(r, Status.Ok, "Get rooms: Not expected status"))
      rents <- rentsResp.body.asString
      _     <- ZIO.logInfo(s"Rents: $rents, $rentsResp")
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provideLayer(env).exitCode

}
