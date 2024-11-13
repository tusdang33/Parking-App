package com.parking.parkingapp.di

import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parking.parkingapp.common.APIConst
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.DirectionsApi
import com.parking.parkingapp.data.repository.MapRepository
import com.parking.parkingapp.data.repositoryImpl.AuthRepositoryImpl
import com.parking.parkingapp.data.repositoryImpl.MapRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetWorkModule {
    @Singleton
    @Provides
    fun provideFireStoreUserCollection(): CollectionReference =
        Firebase.firestore.collection("user")

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Singleton
    @Provides
    fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository =
        authRepositoryImpl

    @Singleton
    @Provides
    fun provideDirectionRepository(
        directionsApi: DirectionsApi,
    ): MapRepository =
        MapRepositoryImpl(
            directionsApi = directionsApi
        )

    @Singleton
    @Provides
    fun provideDirectionApi(): DirectionsApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Change to Level.BASIC or Level.HEADERS if you prefer less detail
            })
            .build()
        return Retrofit.Builder()
            .baseUrl(APIConst.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(DirectionsApi::class.java)
    }
}