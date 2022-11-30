package my.meetings_room_renter
package auth_microservice.configuration

import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

case class DbConf(
  driver: String,
  url: String,
  user: String,
  pass: String
)

case class HttpConf(
  host: String,
  port: Int
)

object Configuration {
  private implicit def hint[A]: ProductHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))
  val dbConf: DbConf                           = ConfigSource.resources("auth-application.conf").at("db").loadOrThrow[DbConf]
  val httpConf: HttpConf                       = ConfigSource.resources("auth-application.conf").at("http").loadOrThrow[HttpConf]
}
