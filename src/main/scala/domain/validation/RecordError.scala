package domain.validation

sealed trait RecordError {
  def message: String
}

case object NameError extends RecordError {
  override def message: String = "Name cannot be empty"
}

case object NameDigitsError extends RecordError {
  override def message: String = "Name cannot contain numbers"
}

case object PhoneNumError extends RecordError {
  override def message: String = "Phone number must be in a valid format"
}

case object AmountError extends RecordError {
  override def message: String = "Amount must be greater than zero"
}
