package my.meetings_room_renter
package auth_microservice

import cats.data.EitherT
import cats.effect._
import my.meetings_room_renter.auth_microservice.Models._
import my.meetings_room_renter.auth_microservice.db_access.{getUserByLogin, registerUser}
import org.http4s.{EntityDecoder, Request}


package object requests_handlers {

  private def checkPassword(passwordHashFromDb: String, passwordFromRequest: String): IO[Either[String, String]] =
    IO.pure(
      if (passwordHashFromDb == hashPassword(passwordFromRequest)) Right("User logged in!")
      else Left("Incorrect password")
    )

  private def parseRequest[T](req: Request[IO])(implicit d: EntityDecoder[IO, T]): EitherT[IO, String, T] =
    EitherT(req.as[T].redeem(e => Left(e.getMessage), u => Right(u)))


  def handleRegisterRequest(req: Request[IO]): EitherT[IO, String, String] =
    for {
      user <- parseRequest[User](req)
      _    <- EitherT(registerUser(user))
      resp <- EitherT.liftF[IO, String, String](IO.pure(s"User ${user.login} successfully registered"))
    } yield resp

  def handleLoginRequest(req: Request[IO]): EitherT[IO, String, String] =
    for {
      user <- parseRequest[User](req)
      usersFromDb <- EitherT(getUserByLogin(user.login))
      res <- usersFromDb match {
               case userFromDb :: Nil =>
                 EitherT(
                   checkPassword(userFromDb.password, user.password)
                 )
               case Nil => EitherT.left(IO.pure("Incorrect login"))
               case _   => EitherT.left(IO.pure("Few users with the same login. WTF???"))
             }
    } yield res

}
