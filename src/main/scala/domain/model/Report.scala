package domain.model

import java.time.LocalDateTime

case class PersonReport(name: String, totalAmount: BigDecimal, latestRecordDate: LocalDateTime)
case class PhoneReport(number: String, personReports: Seq[PersonReport])
case class Report(phoneReports: Seq[PhoneReport])
