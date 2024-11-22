package com.parking.parkingapp.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class FireCollectionRef(val collectionRef: CollectionRef)

enum class CollectionRef(val value: String) {
    PARK("park"),
    USER("user"),
    INFO("info"),
    MY_PARK("my_park"),
}