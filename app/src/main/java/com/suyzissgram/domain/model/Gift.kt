package com.suyzissgram.domain.model

data class GiftDefinition(
    val id: String,
    val nameRu: String,
    val nameEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val price: Int,
    val accentColor: Long
)

data class GiftHistoryEntry(
    val id: String,
    val giftId: String,
    val giftNameRu: String,
    val giftNameEn: String,
    val price: Int,
    val createdAt: Long
)

data class GiftWallet(
    val starsBalance: Int = 100,
    val history: List<GiftHistoryEntry> = emptyList()
)

object GiftCatalog {
    val items = listOf(
        GiftDefinition(
            id = "starlight",
            nameRu = "Звёздный свет",
            nameEn = "Starlight",
            descriptionRu = "Небольшой знак внимания",
            descriptionEn = "A small sign of appreciation",
            price = 15,
            accentColor = 0xFF9472FFL
        ),
        GiftDefinition(
            id = "rose",
            nameRu = "Роза",
            nameEn = "Rose",
            descriptionRu = "Классический подарок",
            descriptionEn = "A classic little gift",
            price = 30,
            accentColor = 0xFFFF6488L
        ),
        GiftDefinition(
            id = "crown",
            nameRu = "Корона",
            nameEn = "Crown",
            descriptionRu = "Особенный жест",
            descriptionEn = "A special gesture",
            price = 50,
            accentColor = 0xFFFFC15BL
        ),
        GiftDefinition(
            id = "crystal",
            nameRu = "Кристалл",
            nameEn = "Crystal",
            descriptionRu = "Редкий сияющий подарок",
            descriptionEn = "A rare sparkling gift",
            price = 75,
            accentColor = 0xFF52C9D8L
        )
    )
}
