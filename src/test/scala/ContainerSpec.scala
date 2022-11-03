package my.meetings_room_renter

import com.dimafeng.testcontainers.{ForAllTestContainer, MySQLContainer, PostgreSQLContainer}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.{Connection, DriverManager}

class ContainerSpec extends AnyFlatSpec with ForAllTestContainer with Matchers{

  override val container = PostgreSQLContainer()

  "insert" should "do insertion" in {
    Class.forName(container.driverClassName)
    val connection: Connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
    assert(1 == 1)
  }

  it should "create table and list Table" in {

    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(container.jdbcUrl,
      container.username, container.password)

    val createTableStatement = connection.prepareStatement("create table test(a  Int)")
    createTableStatement.execute()

    val preparedStatement = connection.prepareStatement("show tables")
    val result = preparedStatement.executeQuery()

    while (result.next()) {

      val tableName = result.getString(1)
      tableName shouldEqual "test"
    }
  }

}
