package my.meetings_room_renter

package object configuration {
  case class Configuration(host: String, port: Int)
  lazy val configuration: Configuration = Configuration(
    host = "localhost",
    port = 9090
  )
}
