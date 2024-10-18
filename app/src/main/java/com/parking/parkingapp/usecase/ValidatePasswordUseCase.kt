package com.parking.parkingapp.domain.usecase

class ValidatePasswordUseCase {
	operator fun invoke(password: String): ValidationResult {
		if(password.length < 6) {
			return ValidationResult(
				successful = false,
				errorMessage = "The password needs 6 characters at least "
			)
		}
		return ValidationResult(
			successful = true
		)
	}
}