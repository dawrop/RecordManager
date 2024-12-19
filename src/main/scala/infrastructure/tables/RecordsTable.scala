package infrastructure.tables

import infrastructure.model.RecordsRow
import slick.jdbc.PostgresProfile.api._

class RecordsTable(tag: Tag) extends Table[RecordsRow](tag, "records") {
  def id          = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name        = column[String]("name")
  def phoneNumber = column[String]("phoneNumber")
  def amount      = column[BigDecimal]("amount")

  override def * = (id.?, name, phoneNumber, amount) <> (RecordsRow.tupled, RecordsRow.unapply)
}

object RecordsTable {
  val records = TableQuery[RecordsTable]

}
