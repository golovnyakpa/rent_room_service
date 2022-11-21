package my.meetings_room_renter
package api

import my.meetings_room_renter.authentication.{checkCredentialsBasicAuth, giveJwt}
import my.meetings_room_renter.dao.entities.User
import my.meetings_room_renter.dao.repositories.UserRepository
import my.meetings_room_renter.serde._
import my.meetings_room_renter.utils.RequestHandlers.parseRequest
import my.meetings_room_renter.utils.ResponseMakers
import zhttp.http._


import java.sql.SQLException
import javax.sql.DataSource

object AuthenticationApi {

   def registerEndpoint: Http[DataSource with UserRepository, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "user" / "register" =>
      parseRequest[User](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        user => ResponseMakers.registerNewUser(user)
      )
  }

   def loginEndpoint: Http[DataSource with UserRepository, SQLException, Request, Response] = Http.collect[Request] {
    case Method.POST -> Path.root / "user" / "login" =>
      Response.text(giveJwt)
  } @@ Middleware.basicAuthZIO(checkCredentialsBasicAuth)

}
