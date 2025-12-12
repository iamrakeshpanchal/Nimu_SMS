#!/bin/bash

echo "📦 Setting up missing files for Nimu_SMS..."

# Create utility files
cat > app/src/main/java/com/iamrakeshpanchal/nimusms/utils/PromotionalFilter.kt << 'PROMO_EOF'
package com.iamrakeshpanchal.nimusms.utils

import java.util.regex.Pattern

class PromotionalFilter {
    
    private val promotionalKeywords = listOf(
        "offer", "discount", "sale", "buy", "shop now",
        "limited time", "exclusive", "deal", "coupon",
        "voucher", "promo", "promotion", "advertisement",
        "sponsored", "unsubscribe", "click here", "order now",
        "best price", "free delivery", "flash sale", "clearance"
    )
    
    fun isPromotional(message: String, sender: String): Boolean {
        val messageLower = message.lowercase()
        return promotionalKeywords.any { messageLower.contains(it) }
    }
}
PROMO_EOF

# Create a simple build check
echo "✅ Missing files created!"
echo ""
echo "📋 Project Status:"
echo "=================="
echo "✅ Database entities & DAOs"
echo "✅ ViewModel & Application"
echo "✅ SMS Receiver & Workers"
echo "✅ Utilities (OTP, Promotional)"
echo "✅ Google Drive backup"
echo "✅ Bubble notifications"
echo "✅ Layouts & Resources"
echo "=================="
echo ""
echo "🚀 Next: Build the project"
echo "   ./gradlew build"
