package com.suyzissgram.domain.model

/**
 * Local policy for the offline build. A server must enforce paid entitlements and
 * incoming-message rules before this can be considered secure across devices.
 */
object PremiumPolicy {
    const val OWNER_ID = "700200800"
    const val OWNER_USERNAME = "suyziss"

    fun isOwner(userId: String): Boolean = userId == OWNER_ID

    fun isPremium(userId: String, purchasedPremium: Boolean = false): Boolean =
        isOwner(userId) || purchasedPremium

    fun favoritesLimitGb(userId: String, purchasedPremium: Boolean = false): Int = when {
        isOwner(userId) -> 100
        isPremium(userId, purchasedPremium) -> 30
        else -> 5
    }

    /** SuyzissGram price is 66% of the corresponding Telegram reference price. */
    fun discountedPrice(telegramPriceMinorUnits: Long): Long =
        ((telegramPriceMinorUnits.coerceAtLeast(0L) * 66L) + 50L) / 100L

    /**
     * Existing conversations are always allowed through the premium-only filter.
     * Only a new conversation from a non-premium sender is rejected.
     */
    fun shouldAcceptIncomingMessage(
        onlyPremiumEnabled: Boolean,
        senderIsPremium: Boolean,
        conversationAlreadyStarted: Boolean
    ): Boolean = !onlyPremiumEnabled || senderIsPremium || conversationAlreadyStarted
}
