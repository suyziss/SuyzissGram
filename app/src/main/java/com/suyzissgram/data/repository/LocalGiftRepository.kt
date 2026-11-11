package com.suyzissgram.data.repository

import android.content.SharedPreferences
import com.suyzissgram.domain.model.GiftCatalog
import com.suyzissgram.domain.model.GiftHistoryEntry
import com.suyzissgram.domain.model.GiftWallet
import com.suyzissgram.domain.repository.GiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalGiftRepository(
    private val preferences: SharedPreferences
) : GiftRepository {
    private val mutex = Mutex()
    private val wallet = MutableStateFlow(loadWallet())

    override fun observeWallet(): Flow<GiftWallet> = wallet.asStateFlow()

    override suspend fun sendGift(giftId: String) {
        val gift = GiftCatalog.items.firstOrNull { it.id == giftId }
            ?: throw IllegalArgumentException("Unknown gift")

        mutex.withLock {
            val current = wallet.value
            if (current.starsBalance < gift.price) {
                throw IllegalStateException("NOT_ENOUGH_STARS")
            }

            val entry = GiftHistoryEntry(
                id = UUID.randomUUID().toString(),
                giftId = gift.id,
                giftNameRu = gift.nameRu,
                giftNameEn = gift.nameEn,
                price = gift.price,
                createdAt = System.currentTimeMillis()
            )
            wallet.value = current.copy(
                starsBalance = current.starsBalance - gift.price,
                history = (listOf(entry) + current.history).take(MAX_HISTORY)
            )
            persistWallet()
        }
    }

    private fun loadWallet(): GiftWallet {
        val history = runCatching {
            val array = JSONArray(preferences.getString(KEY_HISTORY, "[]"))
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        GiftHistoryEntry(
                            id = item.getString("id"),
                            giftId = item.getString("giftId"),
                            giftNameRu = item.getString("giftNameRu"),
                            giftNameEn = item.getString("giftNameEn"),
                            price = item.getInt("price"),
                            createdAt = item.getLong("createdAt")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())

        return GiftWallet(
            starsBalance = preferences.getInt(KEY_BALANCE, DEMO_START_BALANCE).coerceAtLeast(0),
            history = history
        )
    }

    private fun persistWallet() {
        val history = JSONArray()
        wallet.value.history.forEach { entry ->
            history.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("giftId", entry.giftId)
                    .put("giftNameRu", entry.giftNameRu)
                    .put("giftNameEn", entry.giftNameEn)
                    .put("price", entry.price)
                    .put("createdAt", entry.createdAt)
            )
        }
        preferences.edit()
            .putInt(KEY_BALANCE, wallet.value.starsBalance)
            .putString(KEY_HISTORY, history.toString())
            .apply()
    }

    private companion object {
        const val KEY_BALANCE = "demo_stars_balance"
        const val KEY_HISTORY = "demo_gift_history"
        const val DEMO_START_BALANCE = 100
        const val MAX_HISTORY = 50
    }
}
