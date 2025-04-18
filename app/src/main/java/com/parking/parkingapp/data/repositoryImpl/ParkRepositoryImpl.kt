package com.parking.parkingapp.data.repositoryImpl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parking.parkingapp.common.CollectionRef
import com.parking.parkingapp.common.FireCollectionRef
import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.common.parkStub
import com.parking.parkingapp.data.entity.ParkEntity
import com.parking.parkingapp.data.entity.RentParkEntity
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.model.RentStatus
import com.parking.parkingapp.data.model.toMyRentedPark
import com.parking.parkingapp.data.model.toParkModel
import com.parking.parkingapp.data.model.toRentParkEntity
import com.parking.parkingapp.data.repository.ParkRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkRepositoryImpl @Inject constructor(
    @FireCollectionRef(CollectionRef.PARK) private val parkCollectionRef: CollectionReference,
    @FireCollectionRef(CollectionRef.USER) private val userCollectionRef: CollectionReference,
): ParkRepository {
    override fun getPark(): Flow<Resource<List<ParkModel>>> =
        callbackFlow {
            runCatching {
                parkCollectionRef.addSnapshotListener { value, error ->
                    error?.let {
                        trySend(Resource.Fail(it.message))
                    }
                    value?.let { snapShots ->
                        trySend(Resource.Success(snapShots.documents.mapNotNull {
                            it.toObject(ParkEntity::class.java)?.toParkModel()
                        }))
                    }
                }
            }.onFailure {
                send(Resource.Fail(it.message))
            }
            awaitClose()
        }

    override suspend fun getParkById(parkId: String): Resource<ParkModel?> {
        return try {
            val result = parkCollectionRef
                .document(parkId)
                .get().await().toObject(ParkEntity::class.java)?.toParkModel()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override suspend fun rentPark(myRentedPark: MyRentedPark): Resource<MyRentedPark> {
        return try {
            val result: MyRentedPark
            userCollectionRef
                .document(myRentedPark.userId)
                .collection(CollectionRef.MY_PARK.value)
                .document().apply {
                    result = myRentedPark.copy(id = this.id)
                    set(result.toRentParkEntity()).await()
                }
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override fun getMyRentedPark(userId: String): Flow<Resource<List<MyRentedPark>>> =
        callbackFlow {
            runCatching {
                userCollectionRef
                    .document(userId)
                    .collection(CollectionRef.MY_PARK.value)
                    .get().addOnSuccessListener { snapShots ->
                        trySend(Resource.Success(snapShots.documents.mapNotNull {
                            it.toObject(RentParkEntity::class.java)?.toMyRentedPark()
                        }))
                    }.addOnFailureListener {
                        trySend(Resource.Fail(it.message))
                    }
            }.onFailure {
                send(Resource.Fail(it.message))
            }
            awaitClose()
        }

    override fun upDate() {
        Firebase.firestore.runTransaction { transaction ->
            parkStub.forEach { park ->
                transaction.set(
                    parkCollectionRef.document(park.id),
                    park,
                    SetOptions.merge()
                )
            }
        }
    }

    override suspend fun addRentTime(
        myParkId: String,
        userId: String,
        newTotalPay: Int,
        newEndTime: String
    ): Resource<Unit> {
        return try {
            userCollectionRef
                .document(userId)
                .collection(CollectionRef.MY_PARK.value)
                .document(myParkId).update(
                    mapOf(
                        "endTime" to newEndTime,
                        "totalPay" to newTotalPay
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override suspend fun checkin(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit> {
        return try {
            userCollectionRef
                .document(userId)
                .collection(CollectionRef.MY_PARK.value)
                .document(myRentedParkId).update(
                    mapOf(
                        "status" to RentStatus.CHECKED_IN.value,
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override suspend fun cancel(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit> {
        return try {
            userCollectionRef
                .document(userId)
                .collection(CollectionRef.MY_PARK.value)
                .document(myRentedParkId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override suspend fun stopRent(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit> {
        return try {
            userCollectionRef
                .document(userId)
                .collection(CollectionRef.MY_PARK.value)
                .document(myRentedParkId).update(
                    mapOf(
                        "status" to RentStatus.RENTED.value,
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }

    override suspend fun updateOvertimeRent(list: List<MyRentedPark>): Resource<Unit> {
        return try {
            Firebase.firestore.runTransaction { transaction ->
                list.forEach { myRentedPark ->
                    transaction.update(
                        userCollectionRef
                            .document(myRentedPark.userId)
                            .collection(CollectionRef.MY_PARK.value)
                            .document(myRentedPark.id),
                        "status",
                        RentStatus.RENTED.value
                    )
                }
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Fail(e.message)
        }
    }
}