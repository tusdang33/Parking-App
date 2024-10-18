package com.parking.parkingapp.domain.usecase

data class ValidationResult(
	val successful: Boolean,
	val errorMessage: String? = null
)