package my.meetings_room_renter

import zio.Layer

package object configuration {
  case class Configuration(host: String, port: Int)
  lazy val configuration: Configuration = Configuration(
    host = "localhost",
    port = 9090
  )

  case class Config(api: Api, liquibase: LiquibaseConfig, db2: DbConfig)

  case class LiquibaseConfig(changeLog: String)

  case class Api(host: String, port: Int)

  case class DbConfig(driver: String, url: String, user: String, password: String)

  val sqlStateToTextMapping: Map[String, String] = Map(
    "23503" -> "Rent update failed. Such room or rent doesn't exists"
  )

}
