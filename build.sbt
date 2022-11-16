ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val ZioHttpVersion = "2.0.0-RC11"
lazy val CirceVersion   = "0.14.2"

lazy val root = (project in file("."))
  .settings(
    name             := "meeting_rooms_renter",
    idePackagePrefix := Some("my.meetings_room_renter"),
    scalacOptions += "-Ymacro-annotations",
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
      "org.postgresql"        % "postgresql"                      % "42.3.1",
      "org.liquibase"         % "liquibase-core"                  % "4.17.1",
      "com.dimafeng"         %% "testcontainers-scala-mysql"      % "0.36.0"  % Test,
      "com.dimafeng"         %% "testcontainers-scala-postgresql" % "0.40.11" % Test,
      "com.dimafeng"         %% "testcontainers-scala-scalatest"  % "0.40.11" % Test,
      "org.scalatest"        %% "scalatest"                       % "3.2.14"  % Test,
      "org.slf4j"             % "slf4j-simple"                    % "2.0.3"   % Test
    )
  )
