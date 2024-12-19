package infrastructure.model

import domain.model.Record

import java.time.LocalDateTime

case class RecordsRow(
  id: Option[Long],
  name: String,
  phoneNum: String,
  amount: BigDecimal,
  processedAt: Option[LocalDateTime],
  createdAt: LocalDateTime
) {
  def toDomain: Record = Record(name, phoneNum, amount, createdAt)
}

object RecordsRow {
  def fromDomain(record: Record): RecordsRow =
    RecordsRow(
      id          = None,
      name        = record.name,
      phoneNum    = record.phoneNum,
      amount      = record.amount,
      processedAt = None,
      createdAt   = record.createdAt
    )

  def tupled: ((Option[Long], String, String, BigDecimal, Option[LocalDateTime], LocalDateTime)) => RecordsRow =
    (RecordsRow.apply _).tupled
}
