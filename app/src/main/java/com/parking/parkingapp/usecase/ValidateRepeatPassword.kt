package com.parking.parkingapp.domain.usecase

class ValidateRetypePasswordUseCase {
	operator fun invoke(password: String, retypedPassword: String): ValidationResult {
		if(password != retypedPassword) {
			return ValidationResult(
				successful = false,
				errorMessage = "Not match Password"
			)
		}
		return ValidationResult(
			successful = true
		)
	}
}