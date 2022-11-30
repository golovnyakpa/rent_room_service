package my.meetings_room_renter

import cats.data.Kleisli
import cats.effect.IO
import my.meetings_room_renter.auth_microservice.Models._
import my.meetings_room_renter.configuration.{jwtSecretKey, jwtSignatureAlgo}
import org.http4s.headers.Authorization
import org.http4s.{HttpRoutes, Request}
import pdi.jwt.{JwtClaim, JwtZIOJson}

import java.security.MessageDigest

package object auth_microservice {

  def logRequestMiddleware(route: HttpRoutes[IO]): HttpRoutes[IO] =
    Kleisli { req =>
      println(s"> ${req.uri}")
      req.headers.foreach(h => println(s">>> ${h.name}: ${h.value}"))
      println()
      route(req)
    }

  case class Credentials(login: String, password: String)

  def hashPassword(password: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(password.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString

  def giveJwt(user: String): String = {
    val claim = JwtClaim(
      subject = Some(user)
    )
    JwtZIOJson.encode(claim, jwtSecretKey, jwtSignatureAlgo)
  }

  def decodeBase64(str: String): Array[Byte] =
    java.util.Base64.getDecoder.decode(str)

  def parseBasicAuthCredentials(encodedCreds: String): Either[String, User] = {
    val decodedCreds = new String(decodeBase64(encodedCreds))
    val splitted     = decodedCreds.split(":")
    (for {
      login  <- splitted.headOption
      passwd <- splitted.tail.headOption
    } yield User(login, passwd)) match {
      case Some(value) => Right(value)
      case None        => Left("No login/password provided")
    }
  }

  def getAuthorizationHeader(req: Request[IO]): Either[String, String] =
    (for {
      auth         <- req.headers.get[Authorization]
      encodedCreds <- auth.credentials.toString.split(" ").tail.headOption
    } yield encodedCreds) match {
      case Some(value) => Right(value)
      case None        => Left("No authorization header provided")
    }

}
