package domain

import cats.data.NonEmptyList
import cats.implicits._
import commons.validation.{ AmountError, NameDigitsError, NameError, PhoneNumError }
import domain.model.Record
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class RecordsValidationSpec extends AnyFlatSpec with Matchers {

  "validate" should "return valid for a correct Record" in {
    val record = Record("John", "+48123456789", 100.0, LocalDateTime.of(2024, 12, 19, 12, 30))
    val result = Record.validate(record)
    result shouldEqual record.validNel
  }

  it should "return a single error for a Record with wrong name" in {
    val record = Record("John123", "+48123456789", 100.0, LocalDateTime.of(2024, 12, 19, 12, 30))
    val result = Record.validate(record)
    result shouldEqual NameDigitsError.invalidNel
  }

  it should "return a single error for a Record with wrong phone number format" in {
    val record = Record("John", "123456", 100.0, LocalDateTime.of(2024, 12, 19, 12, 30))
    val result = Record.validate(record)
    result shouldEqual PhoneNumError.invalidNel
  }

  it should "return a single error for a Record with wrong amount" in {
    val record = Record("John", "+48123456789", -100.0, LocalDateTime.of(2024, 12, 19, 12, 30))
    val result = Record.validate(record)
    result shouldEqual AmountError.invalidNel
  }

  it should "accumulate errors for an invalid Record" in {
    val record   = Record("", "12345", -10.0, LocalDateTime.of(2024, 12, 19, 12, 30))
    val result   = Record.validate(record)
    val expected = NonEmptyList.of(NameError, PhoneNumError, AmountError).invalid
    result shouldEqual expected
  }
}
