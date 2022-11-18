package my.meetings_room_renter

import my.meetings_room_renter.dao.repositories.UserRepository
import zhttp.http.Middleware.basicAuth
import zhttp.http.middleware.Auth.Credentials

import java.security.MessageDigest
import scala.util.hashing
import zio._

import java.sql.SQLException
import javax.sql.DataSource

package object authentication {
  def checkCredentialsDebug(credentials: Credentials): Boolean = {
    if (credentials.uname == "admin" && credentials.uname == "admin") true else false // todo make it adequate
  }

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

}
