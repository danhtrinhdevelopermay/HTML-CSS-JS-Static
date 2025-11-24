# Equalizer FX - Android App Project

## Tổng quan dự án
Ứng dụng Android chuyên nghiệp về xử lý âm thanh với đầy đủ tính năng equalizer, bass boost, visualizer và các hiệu ứng 3D/8D audio.

## Thông tin dự án
- **Ngôn ngữ**: Kotlin
- **Framework**: Jetpack Compose
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 7.0 (API 24)
- **Build System**: Gradle 8.2

## Tính năng chính
1. **20-Band Equalizer**: Điều chỉnh 20 dải tần số âm thanh
2. **Bass Boost**: Tăng cường âm trầm (0-1000)
3. **Treble Boost**: Tăng cường âm cao
4. **Reverb**: 5 preset reverb effects
5. **3D Effect**: Virtualizer cho âm thanh 3D
6. **8D Effect**: Hiệu ứng âm thanh xoay vòng
7. **Audio Visualizer**: 
   - Waveform visualization
   - Bass bars (10 bands)
   - Treble bars (10 bands)
   - Frequency bands (20 bands)
8. **Media Player**: Phát MP3/MP4 và điều chỉnh âm thanh real-time

## Cấu trúc code
```
app/src/main/java/com/equalizerfx/app/
├── MainActivity.kt              # Main activity với Compose UI
├── audio/
│   ├── AudioEngine.kt          # Quản lý equalizer, bass, treble, effects
│   └── AudioVisualizer.kt      # Xử lý visualizer data
├── player/
│   └── MediaPlayerManager.kt   # Media player manager
├── service/
│   └── AudioService.kt         # Foreground service
└── ui/components/              # Compose UI components
    ├── EqualizerView.kt
    ├── VisualizerView.kt
    ├── EffectsControls.kt
    └── PlayerControls.kt
```

## GitHub Actions
- File workflow: `.github/workflows/build.yml`
- Tự động build APK khi push lên main/master
- Output: Debug APK và Release APK (unsigned)
- Download từ Actions > Artifacts

## Build Commands
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
```

## Thay đổi gần đây
- 2025-11-24: Khởi tạo dự án với đầy đủ tính năng audio processing
- Setup GitHub Actions cho automated APK builds
- Implement 20-band equalizer với virtual bands
- Thêm 4 loại visualizer (waveform, bass, treble, frequency)
- Implement 3D và 8D audio effects

## Ghi chú kỹ thuật
- Sử dụng Android Audio Framework native (audiofx)
- Virtual bands để đạt 20 bands (hardware thường chỉ hỗ trợ 5-10 bands)
- 8D effect được implement bằng circular panning algorithm
- Visualizer sử dụng FFT data cho frequency analysis
