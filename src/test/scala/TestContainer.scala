package my.meetings_room_renter

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.testcontainers.utility.MountableFile
import zio.test.TestAspect
import zio.{Console, TaskLayer, ZIO, ZLayer}

object TestContainer {

  lazy val dockerContainerLayer: TaskLayer[PostgreSQLContainer] =
    ZLayer.fromZIO {
      for {
        pg <- ZIO.attempt(new PostgreSQLContainer())
        _ <- ZIO.attempt(
               pg.container.withCopyFileToContainer(
                 MountableFile.forHostPath("C:\\Users\\Synaps\\IdeaProjects\\meeting_rooms_renter\\ddl.sql"),
                 "/docker-entrypoint-initdb.d/init.sql"
               )
             )
        _ <- ZIO.attempt(
               pg.container.withCopyFileToContainer(
                 MountableFile.forHostPath("C:\\Users\\Synaps\\IdeaProjects\\meeting_rooms_renter\\ddl.sql"),
                 "/"
               )
             )
        _ <- ZIO.attempt(pg.start()).onError(e => Console.printLine(e.prettyPrint).orDie)
      } yield pg
    }

  lazy val testDataSourceLayer: ZLayer[PostgreSQLContainer, Throwable, HikariDataSource] =
    ZLayer {
      for {
        pg <- ZIO.service[PostgreSQLContainer]
        conf <- ZIO.attempt {
                  val hc = new HikariConfig()
                  hc.setUsername(pg.container.getUsername)
                  hc.setPassword(pg.container.getPassword)
                  hc.setJdbcUrl(pg.container.getJdbcUrl)
                  hc.setDriverClassName(pg.container.getDriverClassName)
                  hc
                }
        ds <- ZIO.attempt(new HikariDataSource(conf))
      } yield ds
    }

  def cleanSchema() = {
    val cmd = raw"psql -U %s -c 'DROP SCHEMA public CASCADE;' -c 'CREATE SCHEMA public;' -c '\i /ddl.sql'"
    val cmdArr = Array(
      "psql",
      "-U",
      "test",
      "-c",
      "DROP SCHEMA public CASCADE;",
      "-c",
      "CREATE SCHEMA public;",
      "-c",
      "\\i /ddl.sql"
    )
    TestAspect.before(
      ZIO
        .service[PostgreSQLContainer]
        .flatMap(pg =>
          ZIO
//            .attempt(pg.container.execInContainer(cmd.format(pg.container.getDatabaseName)))
            .attempt(pg.container.execInContainer(cmdArr: _*))
            .flatMap(res => ZIO.debug(res.toString))
        )
        .zipLeft(ZIO.debug("Clean schema"))
    )
  }
}
