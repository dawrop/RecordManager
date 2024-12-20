package commons

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.model.{ PersonReport, PhoneReport, Record, Report }
import spray.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JsonFormatter extends SprayJsonSupport with DefaultJsonProtocol {
  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  implicit val localDateTimeFormat: RootJsonFormat[LocalDateTime] = new RootJsonFormat[LocalDateTime] {
    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(value) => LocalDateTime.parse(value, dateFormatter)
      case _ => throw DeserializationException("Expected ISO_LOCAL_DATE_TIME formatted string for LocalDateTime")
    }

    override def write(obj: LocalDateTime): JsValue = JsString(obj.format(dateFormatter))
  }

  implicit val recordFormat: RootJsonFormat[Record] = new RootJsonFormat[Record] {
    override def read(json: JsValue): Record = {
      val fields    = json.asJsObject.fields
      val name      = fields("name").convertTo[String]
      val phoneNum  = fields("phoneNum").convertTo[String]
      val amount    = fields("amount").convertTo[BigDecimal]
      val createdAt = fields.get("createdAt").map(_.convertTo[LocalDateTime]).getOrElse(LocalDateTime.now())
      Record(name, phoneNum, amount, createdAt)
    }

    override def write(obj: Record): JsObject = JsObject(
      "name" -> JsString(obj.name),
      "phoneNum" -> JsString(obj.phoneNum),
      "amount" -> JsNumber(obj.amount),
      "createdAt" -> JsString(obj.createdAt.format(dateFormatter))
    )
  }

  implicit val personReportFormat: RootJsonFormat[PersonReport] = new RootJsonFormat[PersonReport] {
    override def read(json: JsValue): PersonReport =
      json.asJsObject.getFields("name", "totalAmount", "latestRecordDate") match {
        case Seq(JsString(name), JsNumber(totalAmount), JsString(latestRecordDateStr)) =>
          PersonReport(name, totalAmount, LocalDateTime.parse(latestRecordDateStr, dateFormatter))
        case _ => throw DeserializationException("PersonReport expected fields: name, totalAmount, latestRecordDate")
      }

    override def write(personReport: PersonReport): JsValue = JsObject(
      "name" -> JsString(personReport.name),
      "totalAmount" -> JsNumber(personReport.totalAmount),
      "latestRecordDate" -> JsString(personReport.latestRecordDate.format(dateFormatter))
    )
  }

  implicit val phoneReportFormat: RootJsonFormat[PhoneReport] = new RootJsonFormat[PhoneReport] {
    override def read(json: JsValue): PhoneReport = json.asJsObject.getFields("number", "personReports") match {
      case Seq(JsString(number), JsArray(personReports)) =>
        val personReportObjs = personReports.map(_.convertTo[PersonReport])
        PhoneReport(number, personReportObjs)
      case _ => throw DeserializationException("PhoneReport expected fields: number, personReports")
    }

    override def write(phoneReport: PhoneReport): JsValue = JsObject(
      "number" -> JsString(phoneReport.number),
      "personReports" -> JsArray(phoneReport.personReports.map(_.toJson).toVector)
    )
  }

  implicit val reportFormat: RootJsonFormat[Report] = new RootJsonFormat[Report] {
    override def read(json: JsValue): Report = json.asJsObject.getFields("phoneReports") match {
      case Seq(JsArray(phoneReports)) =>
        val phoneReportObjs = phoneReports.map(_.convertTo[PhoneReport])
        Report(phoneReportObjs)
      case _ => throw DeserializationException("Report expected field: phoneReports")
    }

    override def write(report: Report): JsValue = JsObject(
      "phoneReports" -> JsArray(report.phoneReports.map(_.toJson).toVector)
    )
  }
}
