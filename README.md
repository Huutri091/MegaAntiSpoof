# MegaAntiSpoof 1.0.0-SNAPSHOT

Plugin phòng vệ cho Paper/Folia/Canvas, Java 25, Minecraft 1.21–26.x. Dự án gồm `api`, `core`, ba adapter phiên bản, và `plugin`.

## Build

```powershell
.\gradlew.bat clean build
```

JAR đầu ra: `plugin/build/libs/MegaAntiSpoof-1.0.0-SNAPSHOT.jar`.

Máy chủ cần ProtocolLib 5.4.0 và khuyến nghị PacketEvents 2.13.0. Chỉ dùng trên mạng máy chủ mà quản trị viên có quyền kiểm tra; thông báo người chơi về hoạt động kiểm tra client và chính sách dữ liệu.

## Lệnh

- `/mas check <player> [mod1,mod2]`
- `/mas debug <on|off|status>`
- `/mas reload`
