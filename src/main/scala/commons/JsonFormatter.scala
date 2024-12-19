package commons

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.model.{ Record, RecordsList }
import spray.json.{
  DefaultJsonProtocol,
  DeserializationException,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  RootJsonFormat
}

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

  implicit val recordsListFormat: RootJsonFormat[RecordsList] = jsonFormat1(RecordsList.apply)
}
