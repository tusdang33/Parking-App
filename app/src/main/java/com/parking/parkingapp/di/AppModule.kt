package com.parking.parkingapp.di


import com.parking.parkingapp.domain.usecase.ValidateEmailUseCase
import com.parking.parkingapp.domain.usecase.ValidatePasswordUseCase
import com.parking.parkingapp.domain.usecase.ValidateRetypePasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideValidateEmail(): ValidateEmailUseCase = ValidateEmailUseCase()

    @Singleton
    @Provides
    fun provideValidatePassword(): ValidatePasswordUseCase = ValidatePasswordUseCase()

    @Singleton
    @Provides
    fun provideValidateRetypePassword(): ValidateRetypePasswordUseCase =
        ValidateRetypePasswordUseCase()

}