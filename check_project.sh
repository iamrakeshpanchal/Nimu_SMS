#!/bin/bash

echo "🔍 Checking Nimu_SMS Project Structure..."
echo "========================================"

# Check directories
echo "📁 Directories:"
[ -d "app/src/main/java/com/iamrakeshpanchal/nimusms" ] && echo "✅ Java source directory exists"
[ -d "app/src/main/res/layout" ] && echo "✅ Layout directory exists"
[ -d "gradle/wrapper" ] && echo "✅ Gradle wrapper exists"

# Check essential files
echo ""
echo "📄 Essential Files:"
[ -f "gradlew" ] && echo "✅ gradlew script exists"
[ -f "build.gradle.kts" ] && echo "✅ Root build.gradle.kts exists"
[ -f "app/build.gradle.kts" ] && echo "✅ App build.gradle.kts exists"
[ -f "app/src/main/AndroidManifest.xml" ] && echo "✅ AndroidManifest.xml exists"

# Count Kotlin files
kotlin_count=$(find app/src/main/java -name "*.kt" | wc -l)
echo "✅ Found $kotlin_count Kotlin files"

echo ""
echo "🚀 Ready to build? Run: ./gradlew build"
echo "========================================"
