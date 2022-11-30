package my.meetings_room_renter

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.testcontainers.utility.MountableFile
import zio.test.TestAspect
import zio.{Console, TaskLayer, ZIO, ZLayer}

import scala.reflect.ClassTag

object TestContainer {

  lazy val dockerContainerLayer: TaskLayer[PostgreSQLContainer] =
    ZLayer {
      for {
        pg <- ZIO.attempt(new PostgreSQLContainer())
        _ <- ZIO.attempt(
               pg.container.withCopyFileToContainer(
                 MountableFile.forClasspathResource("ddl.sql"),
                 "/docker-entrypoint-initdb.d/init.sql"
               )
             )
        _ <- ZIO.attempt(pg.container.withCopyFileToContainer(MountableFile.forClasspathResource("ddl.sql"), "/"))
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

  private def insertIntoSeq[T: ClassTag](lst: Array[T], i: Int, el: T): Array[T] = {
    val (front, back) = lst.splitAt(i)
    front ++ Array(el) ++ back
  }

  def cleanSchema = {
    val cmd =
      Array("psql", "-U", "-c", "DROP SCHEMA public CASCADE;", "-c", "CREATE SCHEMA public;", "-c", raw"\i /ddl.sql")

    TestAspect.before(
      for {
        pg <- ZIO.service[PostgreSQLContainer]
        execRes <-
          ZIO.attempt {
            val usernamePosition = 2
            val finalCmd =
              insertIntoSeq(cmd, usernamePosition, pg.container.getUsername) // todo not very general, don't like it
            pg.container.execInContainer(finalCmd: _*)
          }
        _ <- ZIO.logDebug(execRes.toString)
      } yield ()
    )
  }
}
