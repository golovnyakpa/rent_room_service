package my.meetings_room_renter

import my.meetings_room_renter.TestContainer.{cleanSchema, dockerContainerLayer, testDataSourceLayer}
import my.meetings_room_renter.dao.entities.UserDb
import my.meetings_room_renter.dao.repositories.{UserRepository, UserRepositoryLive}
import zio._
import zio.test._

object UserRepoSpec extends ZIOSpecDefault {

  val layer = dockerContainerLayer >+> testDataSourceLayer ++ UserRepositoryLive.layer

  override def spec =
    suite("UserRepo spec")(
      suite("Register new user")(
        test("Registration of new user works correctly") {
          for {
            repo  <- ZIO.service[UserRepository]
            _     <- repo.registerNewUser(UserDb("Vasya", "qwerty"))
            _     <- repo.registerNewUser(UserDb("Petya", "12345678"))
            _     <- repo.registerNewUser(UserDb("Sidor", "Scalarulez!"))
            users <- repo.showAllUsers
          } yield assertTrue(users.size == 3)
        } @@ cleanSchema,
        test("Users registration with the same login is impossible") {
          (for {
            repo <- ZIO.service[UserRepository]
            _    <- repo.registerNewUser(UserDb("Vasya", "qwerty"))
            res  <- repo.registerNewUser(UserDb("Vasya", "12345678"))
          } yield res).fold(_ => assertTrue(true), _ => assertTrue(false))
        } @@ cleanSchema
      )

    ).provideShared(layer) @@ TestAspect.sequential

}
