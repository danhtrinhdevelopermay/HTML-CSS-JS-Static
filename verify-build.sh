#!/bin/bash

echo "====================================="
echo "  EQUALIZER FX - Build Verification"
echo "====================================="
echo ""

echo "✓ Checking project structure..."
if [ -f "build.gradle.kts" ] && [ -f "app/build.gradle.kts" ]; then
    echo "  Project structure is valid"
else
    echo "  ✗ Project structure incomplete"
    exit 1
fi

echo ""
echo "✓ Checking source files..."
SOURCE_FILES=(
    "app/src/main/java/com/equalizerfx/app/MainActivity.kt"
    "app/src/main/java/com/equalizerfx/app/audio/AudioEngine.kt"
    "app/src/main/java/com/equalizerfx/app/audio/AudioVisualizer.kt"
    "app/src/main/java/com/equalizerfx/app/player/MediaPlayerManager.kt"
    "app/src/main/AndroidManifest.xml"
)

for file in "${SOURCE_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file"
    else
        echo "  ✗ $file missing"
        exit 1
    fi
done

echo ""
echo "✓ Checking GitHub Actions workflow..."
if [ -f ".github/workflows/build.yml" ]; then
    echo "  GitHub Actions configured"
else
    echo "  ✗ GitHub Actions workflow missing"
    exit 1
fi

echo ""
echo "✓ Checking Gradle wrapper..."
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    echo "  Gradle wrapper is ready"
else
    echo "  ✗ Gradle wrapper not executable"
    exit 1
fi

echo ""
echo "====================================="
echo "  PROJECT READY FOR BUILD!"
echo "====================================="
echo ""
echo "📱 This is an Android application project"
echo ""
echo "🔨 To build APK locally:"
echo "   ./gradlew assembleDebug"
echo "   ./gradlew assembleRelease"
echo ""
echo "☁️  To build using GitHub Actions:"
echo "   1. Push this code to your GitHub repository"
echo "   2. Go to Actions tab"
echo "   3. Download APK from Artifacts"
echo ""
echo "📖 Read README.md for detailed instructions"
echo ""
