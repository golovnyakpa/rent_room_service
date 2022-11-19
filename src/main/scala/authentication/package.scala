package my.meetings_room_renter

import my.meetings_room_renter.configuration.{jwtSecretKey, jwtSignatureAlgo}
import my.meetings_room_renter.dao.repositories.UserRepository
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import zhttp.http.Middleware.basicAuth
import zhttp.http.middleware.Auth.Credentials

import java.security.MessageDigest
import scala.util.{Failure, Success, hashing}
import zio._
import zio.json.ast.Json

import java.sql.SQLException
import javax.sql.DataSource

package object authentication {
  def hashPassword(password: String): String = {
    MessageDigest.getInstance("SHA-256")
      .digest(password.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

  def checkCredentialsBasicAuth(credentials: Credentials): ZIO[DataSource with UserRepository, SQLException, Boolean] =
    for {
      userRepo <- ZIO.service[UserRepository]
      userFromDb <- userRepo.getUserByLogin(credentials.uname)
    } yield userFromDb.exists(user => user.password == hashPassword(credentials.upassword))


  def checkJwt(token: String): Boolean = {
    JwtZIOJson.decodeJson(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
      case Failure(exception) => false
      case Success(value) => true
    }
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
