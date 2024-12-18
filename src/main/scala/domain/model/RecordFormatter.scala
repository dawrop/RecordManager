package domain.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

object RecordFormatter extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val recordFormat: RootJsonFormat[Record] = jsonFormat3(Record.apply)
}
