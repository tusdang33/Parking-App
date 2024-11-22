package com.parking.parkingapp.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.parking.parkingapp.common.CollectionRef
import com.parking.parkingapp.common.FireCollectionRef
import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.entity.ParkEntity
import com.parking.parkingapp.data.entity.RentParkEntity
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
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

    override suspend fun rentPark(myRentedPark: MyRentedPark): Resource<Unit> {
        return try {
            userCollectionRef
                .document(myRentedPark.userId)
                .collection(CollectionRef.MY_PARK.value)
                .document().apply {
                    set(myRentedPark.copy(id = this.id).toRentParkEntity()).await()
                }
            Resource.Success(Unit)
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
                        Log.e("tudm", "getMyRentedPark:${it.message} ", )
                    }
            }.onFailure {
                send(Resource.Fail(it.message))
            }
            awaitClose()
        }
}