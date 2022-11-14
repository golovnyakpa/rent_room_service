package my.meetings_room_renter

import com.zaxxer.hikari.HikariDataSource
import io.getquill._
import io.getquill.util.LoadConfig
import zio._

import javax.sql.DataSource

package object db {

  object Ctx extends PostgresZioJdbcContext(NamingStrategy(SnakeCase, LowerCase))

  def hikariDS: HikariDataSource = JdbcContextConfig(LoadConfig("db")).dataSource

  val zioDS: ZLayer[Any, Throwable, DataSource] = ZLayer.succeed(hikariDS)

}
