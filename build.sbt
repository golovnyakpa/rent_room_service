ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val ZioHttpVersion = "2.0.0-RC11"
lazy val CirceVersion   = "0.14.2"

val http4sVersion = "0.23.16"

lazy val CatsDependencies = Seq(
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-circe"        % http4sVersion,
  "io.circe"   %% "circe-generic"       % "0.14.3",
  "io.circe"   %% "circe-literal"       % "0.14.3"
)

lazy val DoobieDependencies = Seq(
  "org.tpolecat"          %% "doobie-core"      % "1.0.0-RC1",
  "org.tpolecat"          %% "doobie-hikari"    % "1.0.0-RC1", // HikariCP transactor.
  "org.tpolecat"          %% "doobie-postgres"  % "1.0.0-RC1", // Postgres driver 42.3.1 + type mappings.
  "org.tpolecat"          %% "doobie-specs2"    % "1.0.0-RC1" % "test", // Specs2 support for typechecking statements.
  "org.tpolecat"          %% "doobie-scalatest" % "1.0.0-RC1" % "test", // ScalaTest support for typechecking statements.
  "com.github.pureconfig" %% "pureconfig"       % "0.17.2"
)

lazy val root = (project in file("."))
  .settings(
    name             := "meeting_rooms_renter",
    idePackagePrefix := Some("my.meetings_room_renter"),
    scalacOptions ++= Seq("-Ymacro-annotations", "-deprecation"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Test / fork := true,
    libraryDependencies ++= Seq(
      "io.d11"               %% "zhttp"                           % ZioHttpVersion,
      "io.getquill"          %% "quill-jdbc-zio"                  % "4.6.0",
      "dev.zio"              %% "zio-test"                        % "2.0.3"   % Test,
      "dev.zio"              %% "zio-test-sbt"                    % "2.0.3"   % Test,
      "dev.zio"              %% "zio-test-magnolia"               % "2.0.3"   % Test,
      "io.github.kitlangton" %% "zio-magic"                       % "0.3.12",
      "dev.zio"              %% "zio-json"                        % "0.3.0",
      "dev.zio"              %% "zio-macros"                      % "2.0.4",
      "dev.zio"              %% "zio-config"                      % "3.0.2",
      "dev.zio"              %% "zio-config-typesafe"             % "3.0.2",
      "dev.zio"              %% "zio-config-magnolia"             % "3.0.2",
      "org.postgresql"        % "postgresql"                      % "42.3.1",
      "org.liquibase"         % "liquibase-core"                  % "4.17.1",
      "com.dimafeng"         %% "testcontainers-scala-mysql"      % "0.36.0"  % Test,
      "com.dimafeng"         %% "testcontainers-scala-postgresql" % "0.40.11" % Test,
      "com.dimafeng"         %% "testcontainers-scala-scalatest"  % "0.40.11" % Test,
      "org.scalatest"        %% "scalatest"                       % "3.2.14"  % Test,
      "org.slf4j"             % "slf4j-simple"                    % "2.0.3"   % Test,
      "com.github.jwt-scala" %% "jwt-zio-json"                    % "9.1.2"
    ) ++ CatsDependencies ++ DoobieDependencies
  )
