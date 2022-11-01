ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val ZioVersion = "1.0.17"
val ZioHttpVersion = "2.0.0-RC11"


lazy val root = (project in file("."))
  .settings(
    name := "meeting_rooms_renter",
    idePackagePrefix := Some("my.meetings_room_renter"),
    libraryDependencies ++= Seq(
      "io.d11" %% "zhttp" % ZioHttpVersion
    )
  )
