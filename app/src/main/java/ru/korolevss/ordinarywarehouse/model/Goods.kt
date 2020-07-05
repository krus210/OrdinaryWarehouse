package ru.korolevss.ordinarywarehouse.model

data class Goods(
    val id: Long,
    val name: String,
    val price: Double,
    val attachmentImage: String,
    val coordinates: Coordinates
)

data class Coordinates(var lat: Double, var lon: Double) {

}
