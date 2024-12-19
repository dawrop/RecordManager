package domain.model

import cats.data._
import cats.implicits._
import commons.validation.{ AmountError, NameDigitsError, NameError, PhoneNumError, RecordError }

import java.time.LocalDateTime

case class Record(name: String, phoneNum: String, amount: BigDecimal, createdAt: LocalDateTime)

object Record {
  private def validateName(value: String): ValidatedNel[RecordError, String] =
    (value.isEmpty, value.exists(_.isDigit)) match {
      case (true, _) => NameError.invalidNel
      case (_, true) => NameDigitsError.invalidNel
      case _         => value.validNel
    }

  private def validatePhoneNum(value: String): ValidatedNel[RecordError, String] =
    Validated.cond(value.matches("^(\\+?[0-9]{1,3})?[0-9]{9}$"), value, PhoneNumError).toValidatedNel

  private def validateAmount(value: BigDecimal): ValidatedNel[RecordError, BigDecimal] =
    Validated.cond(value >= BigDecimal(0.0), value, AmountError).toValidatedNel

  def validate(record: Record): ValidatedNel[RecordError, Record] =
    (
      validateName(record.name),
      validatePhoneNum(record.phoneNum),
      validateAmount(record.amount),
      record.createdAt.validNel
    ).mapN(Record.apply)
}
