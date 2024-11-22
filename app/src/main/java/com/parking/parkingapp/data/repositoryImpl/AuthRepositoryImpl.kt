package com.parking.parkingapp.data.repositoryImpl

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.parking.parkingapp.common.CollectionRef
import com.parking.parkingapp.common.FireCollectionRef
import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.entity.FireUserEntity
import com.parking.parkingapp.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @FireCollectionRef(CollectionRef.USER) private val fireStoreUserCollection: CollectionReference,
    private val firebaseAuth: FirebaseAuth,
): AuthRepository {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> getCurrentUser(): Resource<T> {
        return try {
            Resource.Success(result = firebaseAuth.currentUser as T)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun login(
        email: String,
        pass: String
    ): Resource<FireUserEntity> {
        return try {
            val fireUser = firebaseAuth.signInWithEmailAndPassword(email, pass).await().user!!
            val user = FireUserEntity(fireUser.uid, fireUser.displayName, email, null)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun register(
        email: String,
        pass: String
    ): Resource<FireUserEntity> {
        return try {
            val fireUser = firebaseAuth.createUserWithEmailAndPassword(email, pass).await().user!!
            val user = FireUserEntity(fireUser.uid, "", email, null)
            fireStoreUserCollection.document(fireUser.uid)
                .collection(CollectionRef.INFO.value)
                .document(fireUser.uid)
                .set(user)
                .await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun updatePass(pass: String): Resource<Unit> {
        return try {
            firebaseAuth.currentUser!!.updatePassword(pass).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun updateProfile(
        name: String?,
        email: String?,
        image: String?
    ): Resource<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser!!
            val profileUpdates = userProfileChangeRequest {
                name?.let {
                    displayName = it
                }
                image?.let {
                    photoUri = it.toUri()
                }
            }
            currentUser.updateProfile(profileUpdates).await()
            email?.let { fEmail ->
                currentUser.verifyBeforeUpdateEmail(fEmail).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(errorMessage = e.message)
        }
    }
}