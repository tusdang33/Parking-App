package com.parking.parkingapp.di

import android.app.Application
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.parking.parkingapp.domain.usecase.ValidateEmailUseCase
import com.parking.parkingapp.domain.usecase.ValidatePasswordUseCase
import com.parking.parkingapp.domain.usecase.ValidateRetypePasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    @Singleton
    @Provides
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient =
        Places.createClient(context)

    @Singleton
    @Provides
    fun provideAutocompleteSessionToken(): AutocompleteSessionToken =
        AutocompleteSessionToken.newInstance()
}