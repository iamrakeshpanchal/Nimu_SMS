package com.iamrakeshpanchal.nimusms.utils

class PromotionalFilter {
    
    private val promotionalKeywords = listOf(
        "offer", "discount", "sale", "buy", "shop now"
    )
    
    fun isPromotional(message: String, sender: String): Boolean {
        val messageLower = message.lowercase()
        return promotionalKeywords.any { messageLower.contains(it) }
    }
}
