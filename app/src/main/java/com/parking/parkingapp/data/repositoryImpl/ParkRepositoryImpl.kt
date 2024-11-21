package com.parking.parkingapp.data.repositoryImpl

import com.google.firebase.firestore.CollectionReference
import com.parking.parkingapp.common.CollectionRef
import com.parking.parkingapp.common.FireCollectionRef
import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.entity.Park
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.model.toParkModel
import com.parking.parkingapp.data.repository.ParkRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ParkRepositoryImpl @Inject constructor(
    @FireCollectionRef(CollectionRef.PARK) private val parkCollectionRef: CollectionReference
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
                            it.toObject(Park::class.java)?.toParkModel()
                        }))
                    }
                }
            }.onFailure {
                send(Resource.Fail(it.message))
            }
            awaitClose()
        }
}