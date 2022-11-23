package my.meetings_room_renter
package auth_microservice

import cats.data.EitherT
import cats.effect._
import my.meetings_room_renter.auth_microservice.Models._
import my.meetings_room_renter.auth_microservice.db_access.{getUserByLogin, registerUser}
import org.http4s.{EntityDecoder, Request}

package object requests_handlers {

  private def checkPassword(user: User, passwordHashFromDb: String): IO[Either[String, User]] =
    IO.pure(
      if (passwordHashFromDb == hashPassword(user.password)) Right(user)
      else Left("Incorrect password")
    )

  private def parseRequest[T](req: Request[IO])(implicit d: EntityDecoder[IO, T]): IO[Either[String, T]] =
    req.as[T].redeem(e => Left(e.getMessage), u => Right(u))

  def handleRegisterRequest(req: Request[IO]): EitherT[IO, String, String] =
    for {
      user <- EitherT(parseRequest[User](req))
      _    <- EitherT(registerUser(user))
      resp <- EitherT.liftF[IO, String, String](IO.pure(s"User ${user.login} successfully registered"))
    } yield resp

  def handleLoginRequest(req: Request[IO]): EitherT[IO, String, User] =
    for {
      encodedCreds <- EitherT(IO.pure(getAuthorizationHeader(req)))
      user         <- EitherT(IO.pure(parseBasicAuthCredentials(encodedCreds)))
      usersFromDb  <- EitherT(getUserByLogin(user.login))
      res <- usersFromDb match {
               case userFromDb :: Nil =>
                 EitherT(
                   checkPassword(user, userFromDb.password)
                 )
               case Nil => EitherT.left(IO.pure("Incorrect login"))
               case _   => EitherT.left(IO.pure("Few users with the same login. WTF???"))
             }
    } yield res

}
