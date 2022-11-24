package my

import my.meetings_room_renter.authentication.extractLoginFromJwt
import zhttp.http.{Headers, Middleware, Request}
import zio._

package object meetings_room_renter {
  val logLevel = ZIO.logLevel(LogLevel.Debug)
  val addLoginHeader = Middleware.collect[Request] { r =>
    Middleware.addHeaders(Headers("X-User-Login", extractLoginFromJwt(r.headers.bearerToken)))
  }
}
