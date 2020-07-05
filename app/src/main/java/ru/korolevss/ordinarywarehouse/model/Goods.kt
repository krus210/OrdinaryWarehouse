package ru.korolevss.ordinarywarehouse.model

data class Goods(
    val id: Long,
    val name: String,
    val price: Double,
    val attachmentImage: String
)