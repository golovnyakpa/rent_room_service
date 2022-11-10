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

  // database tables
  // rooms: room_id | room_title
  // past_rents: rent_id | room_id | start_time | end_time | renter
  // future_rents: rent_id | room_id | start_time | end_time | renter
  // попозже: пользователи и их авторизация
  //
  // Сегодня сделать:
  // 1. DDL для таблиц
  // 2. Вставка нового бронирования (проверка, что это время ещё не занято)
  // 3. Эндпоинт с листингом всех бронирований из future_rents
  // 4. Эндпоинт с листингом всех бронирований из past_rents
  // 5. Перемещение записей из future_rents в past_rents
  // 6. Листинг всех комнат
  // 7. Добавление новой комнаты +

}
