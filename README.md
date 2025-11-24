# Equalizer FX - Android Audio Processing App

Ứng dụng Android điều chỉnh âm thanh chuyên nghiệp với đầy đủ tính năng equalizer, bass boost, visualizer và các hiệu ứng 3D/8D.

## Tính năng

### 🎛️ Audio Processing
- **20-Band Equalizer**: Điều chỉnh chính xác 20 dải tần số từ 31Hz đến 16kHz
- **Bass Boost**: Tăng cường âm trầm mạnh mẽ (0-1000)
- **Treble Boost**: Tăng cường âm cao rõ ràng
- **Reverb**: Hiệu ứng vang với 5 preset (None, Small Room, Medium Room, Large Room, Plate)
- **3D Audio Effect**: Hiệu ứng âm thanh 3D sống động (Virtualizer)
- **8D Audio Effect**: Hiệu ứng âm thanh xoay vòng 8D độc đáo

### 📊 Audio Visualizer
- **Waveform**: Sóng nhạc thời gian thực
- **Bass Bars**: 10 cột hiển thị mức bass
- **Treble Bars**: 10 cột hiển thị mức treble  
- **Frequency Bands**: 20 cột hiển thị từng dải tần số

### 🎵 Media Player
- Phát file MP3, MP4, và các định dạng audio khác
- Điều chỉnh âm thanh trực tiếp trên file đang phát
- Hỗ trợ điều chỉnh âm thanh hệ thống (system audio)

## Yêu cầu

- Android 7.0 (API 24) trở lên
- Quyền truy cập: RECORD_AUDIO, MODIFY_AUDIO_SETTINGS, READ_MEDIA_AUDIO

## Build Instructions

### Local Build

```bash
# Clone repository
git clone <your-repo-url>
cd EqualizerFX

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# APK output location:
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

### GitHub Actions Build

Ứng dụng được cấu hình để tự động build APK thông qua GitHub Actions khi push code lên repository.

**Cách sử dụng:**

1. Push code lên GitHub repository của bạn:
```bash
git add .
git commit -m "Initial commit"
git push origin main
```

2. Truy cập tab **Actions** trên GitHub repository

3. Workflow "Android CI - Build APK" sẽ tự động chạy

4. Sau khi build xong, tải APK từ **Artifacts**:
   - `app-debug.apk`: Bản debug
   - `app-release-unsigned.apk`: Bản release (chưa ký)

**Trigger Build:**
- Tự động khi push lên branch `main` hoặc `master`
- Tự động khi tạo Pull Request
- Thủ công: Tab Actions > Android CI - Build APK > Run workflow

## Cấu trúc dự án

```
app/
├── src/main/
│   ├── java/com/equalizerfx/app/
│   │   ├── MainActivity.kt              # Activity chính
│   │   ├── audio/
│   │   │   ├── AudioEngine.kt          # Xử lý equalizer & effects
│   │   │   └── AudioVisualizer.kt      # Xử lý visualizer
│   │   ├── player/
│   │   │   └── MediaPlayerManager.kt   # Quản lý media player
│   │   ├── service/
│   │   │   └── AudioService.kt         # Foreground service
│   │   └── ui/components/              # UI components
│   │       ├── EqualizerView.kt
│   │       ├── VisualizerView.kt
│   │       ├── EffectsControls.kt
│   │       └── PlayerControls.kt
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Công nghệ sử dụng

- **Kotlin**: Ngôn ngữ lập trình chính
- **Jetpack Compose**: UI framework hiện đại
- **Android Audio Framework**: 
  - `android.media.audiofx.Equalizer`
  - `android.media.audiofx.BassBoost`
  - `android.media.audiofx.Virtualizer`
  - `android.media.audiofx.PresetReverb`
  - `android.media.audiofx.Visualizer`
- **Coroutines & Flow**: Xử lý bất đồng bộ
- **Material Design 3**: Giao diện Material Design

## Hướng dẫn sử dụng

1. **Chọn file audio**: Nhấn nút folder để chọn file MP3/MP4
2. **Phát nhạc**: Nhấn nút Play
3. **Điều chỉnh Equalizer**: Kéo các thanh trượt 20 band để điều chỉnh âm thanh
4. **Bass/Treble Boost**: Điều chỉnh mức bass và treble
5. **Effects**: Bật/tắt các hiệu ứng Reverb, 3D, 8D
6. **Visualizer**: Xem sóng nhạc và frequency bars real-time

## License

Copyright © 2024 Equalizer FX
