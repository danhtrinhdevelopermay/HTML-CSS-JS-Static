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
   - Waveform visualization with smooth bezier curves
   - Bass bars (10 bands) with gradient colors and glow effects
   - Treble bars (10 bands)
   - Frequency bands (20 bands)
   - Enhanced bass sensitivity (2.2x boost)
   - Smooth interpolation animations
8. **Image Pulse Visualizer**: Upload hình ảnh và xem nó pulse theo bass level
   - Scale effects (1.0x to 1.4x) synced với bass rhythm
   - Radial glow effects when bass is strong
   - Smooth spring animations
   - Real-time bass level indicator
9. **Media Player**: Phát MP3/MP4 và điều chỉnh âm thanh real-time

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
    ├── ImagePulseVisualizer.kt # Image pulse with bass sync
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
- Implement dual-mode: File Playback (default) và System Audio (root only)
- Thêm auto-fallback khi System Audio mode fails (SecurityException)
- Thêm user feedback với toast messages
- **Enhanced visualizers** (2025-11-24):
  - Bass bars: 1.5x height với gradient colors và glow effects
  - Waveform: Smooth bezier curves với gradient coloring
  - Bass sensitivity: 2.2x boost với frequency averaging
  - Image Pulse Visualizer: Upload images với bass-synced pulse effects

## Ghi chú kỹ thuật
- Sử dụng Android Audio Framework native (audiofx)
- Virtual bands để đạt 20 bands (hardware thường chỉ hỗ trợ 5-10 bands)
- 8D effect được implement bằng circular panning algorithm
- Visualizer sử dụng FFT data cho frequency analysis
- Default mode: FILE_PLAYBACK (hoạt động trên mọi thiết bị)
- System Audio mode chỉ hoạt động trên thiết bị root/custom ROM do Android security restrictions

## Giới hạn Android Security
- System Audio mode (session 0) yêu cầu MODIFY_AUDIO_ROUTING permission
- Permission này chỉ cấp cho system apps hoặc apps signed với platform key
- File Playback mode hoạt động trên tất cả thiết bị Android (non-root)
- Xem SYSTEM_AUDIO_GUIDE.md cho hướng dẫn chi tiết về root/custom ROM
