# Hướng dẫn kích hoạt System Audio Mode

## Vấn đề

Android không cho phép ứng dụng thường (third-party apps) điều chỉnh âm thanh toàn hệ thống vì lý do bảo mật. Khi app cố gắng attach audio effects vào session 0 (global audio session), sẽ nhận được `SecurityException`.

## Giải pháp

### Option 1: Root Device (Khuyến nghị cho người dùng cá nhân)

**Yêu cầu:**
- Thiết bị Android đã root (Magisk, SuperSU, etc.)
- Module Magisk hoặc Xposed framework

**Các bước:**

1. Root thiết bị Android của bạn:
   - Sử dụng Magisk (khuyến nghị): https://github.com/topjohnwu/Magisk
   - Hoặc các công cụ root khác phù hợp với thiết bị

2. Cài đặt module để cấp quyền:
   ```
   # Sử dụng Magisk Manager
   - Tải module "Audio Modification Library" hoặc tương tự
   - Cài đặt và reboot
   ```

3. Cấp quyền root cho app khi được yêu cầu

4. App sẽ có thể attach effects vào system audio

### Option 2: Custom ROM (Cho developer)

**Yêu cầu:**
- Build Android từ source (AOSP, LineageOS, etc.)
- Ký app với platform key

**Các bước:**

1. Build ROM từ source:
   ```bash
   repo init -u https://android.googlesource.com/platform/manifest
   repo sync
   ```

2. Thêm quyền vào `app/src/main/AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.MODIFY_AUDIO_ROUTING" 
                    tools:ignore="ProtectedPermissions" />
   ```

3. Ký app với platform key:
   ```bash
   jarsigner -keystore platform.keystore \
             -signedjar signed.apk unsigned.apk platform
   ```

4. Cài đặt signed APK lên custom ROM

### Option 3: System App Installation

**Yêu cầu:**
- Thiết bị đã root
- Quyền truy cập /system partition

**Các bước:**

1. Build APK release
2. Remount /system với quyền write:
   ```bash
   adb root
   adb remount
   ```

3. Push app vào /system/app:
   ```bash
   adb push app-release.apk /system/app/EqualizerFX.apk
   adb shell chmod 644 /system/app/EqualizerFX.apk
   ```

4. Reboot thiết bị

## Kiểm tra quyền

Để kiểm tra xem app có quyền system audio hay không:

```kotlin
try {
    val equalizer = Equalizer(0, 0) // session 0
    equalizer.enabled = true
    // Thành công - có quyền
    equalizer.release()
} catch (e: SecurityException) {
    // Không có quyền - cần root/system
    Log.e(TAG, "No system audio permission")
}
```

## Lưu ý

- **Bảo mật**: Root thiết bị hoặc cài system app có thể gây rủi ro bảo mật
- **Bảo hành**: Root có thể làm mất bảo hành thiết bị
- **Tùy chọn an toàn**: Sử dụng File Playback mode trên thiết bị non-root

## Tài liệu tham khảo

- Android AudioEffect: https://developer.android.com/reference/android/media/audiofx/AudioEffect
- Magisk: https://topjohnwu.github.io/Magisk/
- AOSP Build Guide: https://source.android.com/setup/build/building
