package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.GiftWallet
import kotlinx.coroutines.flow.Flow

interface GiftRepository {
    fun observeWallet(): Flow<GiftWallet>
    suspend fun sendGift(giftId: String)
}
