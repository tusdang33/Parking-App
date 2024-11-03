package com.parking.parkingapp.data.repository

import com.parking.parkingapp.data.model.AutoCompleteModel
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getSearchSuggestion(query: String) : Flow<List<AutoCompleteModel>?>
}