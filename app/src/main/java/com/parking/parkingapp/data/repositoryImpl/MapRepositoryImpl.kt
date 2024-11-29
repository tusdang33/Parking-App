package com.parking.parkingapp.data.repositoryImpl

import android.util.Log
import com.parking.parkingapp.common.APIConst
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.data.model.toAutoCompleteModel
import com.parking.parkingapp.data.repository.DirectionsApi
import com.parking.parkingapp.data.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MapRepositoryImpl(
    private val directionsApi: DirectionsApi,
): MapRepository {
    override fun getSearchSuggestion(query: String): Flow<List<AutoCompleteModel>?> = flow {
        try {
            val response = directionsApi.getAutoCompleteAddress(
                query = query,
                country = "vn",
                proximity = "ip",
                autocomplete = true,
                accessToken = APIConst.MAP_API_KEY
            )
            if (response.isSuccessful) {
                emit(response.body()?.features?.map { it.toAutoCompleteModel() })
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }
}