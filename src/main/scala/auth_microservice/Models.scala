package my.meetings_room_renter
package auth_microservice

import cats.effect._
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._

object Models {
  case class User(login: String, password: String)
  implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
}
