package infrastructure.model

import domain.model.Record

case class RecordsRow(id: Option[Long], name: String, phoneNum: String, amount: BigDecimal) {
  def toDomain: Record = Record(name, phoneNum, amount)
}

object RecordsRow {
  def fromDomain(record: Record): RecordsRow =
    RecordsRow(id = None, name = record.name, phoneNum = record.phoneNum, amount = record.amount)

  def tupled: ((Option[Long], String, String, BigDecimal)) => RecordsRow = Function.tupled(RecordsRow.apply)
}
