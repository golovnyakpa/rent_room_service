package my.meetings_room_renter

import my.meetings_room_renter.configuration.{jwtSecretKey, jwtSignatureAlgo}
import my.meetings_room_renter.dao.repositories.UserRepository
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import zhttp.http.middleware.Auth.Credentials
import zio._
import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder}

import java.security.MessageDigest
import java.sql.SQLException
import javax.sql.DataSource
import scala.util.{Failure, Success, Try}

package object authentication {
  def hashPassword(password: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(password.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString

  def checkCredentialsBasicAuth(credentials: Credentials): ZIO[DataSource with UserRepository, SQLException, Boolean] =
    for {
      userRepo   <- ZIO.service[UserRepository]
      userFromDb <- userRepo.getUserByLogin(credentials.uname)
    } yield userFromDb.exists(user => user.password == hashPassword(credentials.upassword))

  private object ParseJwtPlaceholders {
    case class User(sub: String)
    implicit val encoderRoom: JsonDecoder[User] =
      DeriveJsonDecoder.gen[User]
  }

  def parseJwt(token: String): Try[Json] =
    JwtZIOJson.decodeJson(token, jwtSecretKey, Seq(JwtAlgorithm.HS256))

  def checkJwt(token: String): Boolean = {
    import ParseJwtPlaceholders._ // todo seems it's possible to do it without case class
    parseJwt(token) match {
      case Failure(_) => false
      case Success(value) =>
        value.as[User] match {
          case Left(_)  => false
          case Right(_) => true
        }
    }
  }

  def extractLoginFromJwt(token: Option[String]): String = {
    import ParseJwtPlaceholders._ // todo seems it's possible to do it without case class
    (for {
      t           <- token
      parsedToken <- parseJwt(t).toOption
      user        <- parsedToken.as[User].toOption
    } yield user.sub).getOrElse(throw new RuntimeException("Critical defect"))
  }

  def giveJwt: String = {
    val claim = JwtClaim(
      subject = Some("top_secret_user")
    )
    JwtZIOJson.encode(claim, jwtSecretKey, jwtSignatureAlgo)
  }

  // jwt structure:
  // 1.Header
  // 2.Payload
  // 3.Signature
  // header = { "alg": "HS256", "typ": "JWT"}
  // payload = { "userId": "b08f86af-35da-48f2-8fab-cef3904660bd" }  полезные данные (aka claim)
  // const SECRET_KEY = 'cAtwa1kkEy'
  // const unsignedToken = base64urlEncode(header) + '.' + base64urlEncode(payload)
  // const signature = HMAC-SHA256(unsignedToken, SECRET_KEY)
  // const token = encodeBase64Url(header) + '.' + encodeBase64Url(payload) + '.' + encodeBase64Url(signature)

}
