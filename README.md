# 🛡️ MegaAntiSpoof

<div align="center">

![Java Version](https://img.shields.io/badge/Java-25%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Minecraft Versions](https://img.shields.io/badge/Minecraft-1.21%20--%2026.x-52A535?style=for-the-badge&logo=minecraft&logoColor=white)
![Platform Support](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Folia-1877F2?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)

**Giải pháp phát hiện và ngăn chặn Hacked Client / Cheat Mod tiên tiến từ phía máy chủ (Server-side) cho Minecraft Paper, Purpur & Folia.**

[Tính năng](#-tính-năng-nổi-bật) •
[Danh sách Mod bị chặn](#-danh-sách-mod--client-được-hỗ-trợ) •
[Cài đặt](#-cài-đặt--yêu-cầu-hệ-thống) •
[Lệnh & Quyền hạn](#-lệnh--quyền-hạn) •
[Cấu hình](#-hướng-dẫn-cấu-hình) •
[API cho Developer](#-api-cho-lập-trình-viên)

</div>

---

## 🌟 Giới thiệu

**MegaAntiSpoof** là một plugin phòng vệ máy chủ hiệu năng cao, ứng dụng cơ chế **Sign Translation Challenge** (dựa trên hành vi phân giải bản địa hóa ngôn ngữ MC-265322 của Minecraft Client).

Plugin hoạt động hoàn toàn từ phía máy chủ (**100% Server-Side**), không yêu cầu người chơi phải cài đặt bất kỳ mod xác thực nào, nhưng vẫn có khả năng nhận diện chính xác các Hacked Client phổ biến nhất hiện nay như **Meteor, Wurst, LiquidBounce, ThunderHack, Baritone, Freecam, AutoClicker, AutoFish, AutoSwitch, AntiAFK,...**

---

## ⚡ Tính năng nổi bật

- **🕵️ Phát hiện Server-Side không cần Mod**: Gửi gói tin thử thách ngầm (Sign GUI ảo) để buộc client tự động phân giải các khóa dịch (`translate`) và phím tắt (`keybind`) độc quyền của mod gian lận.
- **🚀 Đa nền tảng & Hỗ trợ Folia**: Tương thích hoàn toàn với kiến trúc đa luồng của **Folia**, đồng thời hoạt động mượt mà trên **Paper**, **Purpur** từ phiên bản **1.21 đến 26.x**.
- **🔨 Cơ chế Xử phạt Tự động & Auto-Ban**: Tự động đếm và lưu vết số lần vi phạm (`violations.yml`). Nếu người chơi cố chấp vào lại server mà không gỡ mod quá số lần quy định (mặc định 3 lần), hệ thống sẽ tự động thực hiện **Ban vĩnh viễn (Permanent Ban)** và đính kèm thông tin liên hệ Discord.
- **📊 Báo cáo Discord Webhook chuyên nghiệp**: Tích hợp gửi thông báo chi tiết về Discord Embed theo thời gian thực (Bao gồm: Tên tài khoản, UUID, Địa chỉ IP, Tên Server, Kết quả PASSED/FAILED và bảng chi tiết từng mod được quét).
- **🛡️ Cơ chế Retry & Anti-Spoof thông minh**: Hỗ trợ timeout, cấu hình khoảng thời gian gửi lại gói tin và tự động xử lý khi client cố tình không phản hồi thử thách.
- **⚡ Tự động tải Dependency (Auto Bootstrap)**: Hỗ trợ tự động tải thư viện cần thiết khi khởi động nếu máy chủ chưa có sẵn.
- **🔌 Module NMS tách biệt**: Kiến trúc đa module (`api`, `core`, `nms-common`, `nms-v1_21`, `nms-v1_21_9`, `nms-v26`, `plugin`) giúp dễ dàng mở rộng và bảo trì theo từng phiên bản Minecraft NMS.

---

## 📋 Danh sách Mod / Client được hỗ trợ

MegaAntiSpoof hiện được cấu hình mặc định để nhận diện và ngăn chặn **16 mod / hack client**:

| STT | Tên Mod / Client | Khóa nhận diện (`Key`) | Chế độ | Phân loại / Mục đích |
| :---: | :--- | :--- | :---: | :--- |
| 1 | **Meteor Client** | `key.meteor-client.open-gui` | `KEYBIND` | Hack Client Anarchy / PvP phổ biến |
| 2 | **Wurst Client** | `key.wurst.zoom` | `KEYBIND` | Bộ Hack Client kinh điển |
| 3 | **LiquidBounce** | `liquidbounce.command.bind.description` | `TRANSLATE` | Hack Client đa tính năng |
| 4 | **ThunderHack-Recode** | `descriptions.combat.autocrystal` | `TRANSLATE` | Hack Client chuyên dụng Crystal PvP |
| 5 | **Baritone** | `baritone.settings` | `TRANSLATE` | Bot AI đào khoáng, tự động di chuyển (X-Ray pathing) |
| 6 | **Freecam** | `key.freecam.toggle` | `KEYBIND` | Camera tự do soi lòng đất, căn cứ ngầm |
| 7 | **Auto Clicker** | `key.category.autoclicker.keybinding-title` | `TRANSLATE` | Tự động nhấp chuột siêu tốc |
| 8 | **XPlus AutoFish** | `key.autofish.open_gui` | `KEYBIND` | Tự động câu cá (Auto-fishing) |
| 9 | **AutoSwitch** | `key.autoswitch.toggle` | `KEYBIND` | Tự động chuyển đổi công cụ tối ưu |
| 10 | **AntiAFK** | `key.antiafk.toggle` | `KEYBIND` | Tự động tránh bị kích hoạt cơ chế AFK |
| 11 | **World Downloader (WDL)** | `wdl.gui.title` | `TRANSLATE` | Mod sao chép/tải trộm bản đồ server |
| 12 | **Coffee Client** | `key.coffee.clickgui` | `KEYBIND` | Hack Client Fabric |
| 13 | **Water Client V3** | `key.waterclient.clickgui` | `KEYBIND` | Hack Client PvP |
| 14 | **4E Client** | `key.4e-client.clickgui` | `KEYBIND` | Hack Client |
| 15 | **Nebula Core** | `key.nebulacore.menu` | `KEYBIND` | Cheat Menu / Hack Client |
| 16 | **Inventory Profiles Next** | `key.inventoryprofilesnext.open_gui` | `KEYBIND` | Tự động thao tác & dọn rương siêu tốc |

*(Bạn có thể dễ dàng thêm bớt bất kỳ mod nào khác trực tiếp trong file `config.yml`)*.

---

## 🔧 Cài đặt & Yêu cầu hệ thống

### Yêu cầu
- **Java Runtime**: Java 25 trở lên.
- **Server Software**: Paper, Purpur, hoặc Folia (1.21.x – 26.x).
- **ProtocolLib**: Khuyến nghị ProtocolLib 5.4.0+ hoặc PacketEvents.

### Hướng dẫn cài đặt
1. Tải bản build mới nhất từ mục [Releases](https://github.com/Huutri091/MegaAntiSpoof/releases).
2. Đặt file `MegaAntiSpoof-x.x.x.jar` vào thư mục `plugins/` của máy chủ.
3. Khởi động lại máy chủ để plugin tự động tạo cấu hình mặc định.
4. Chỉnh sửa cấu hình trong file `plugins/MegaAntiSpoof/config.yml` (ví dụ: gắn link Discord Webhook) và sử dụng `/mas reload`.

---

## 💻 Lệnh & Quyền hạn

| Lệnh | Mô tả | Quyền hạn (`Permission`) |
| :--- | :--- | :--- |
| `/mas check <player> [mod1,mod2]` | Thực hiện quét tức thì một người chơi với tất cả hoặc một số mod chỉ định. | `megaantispoof.admin` |
| `/mas debug <on\|off\|status>` | Bật/tắt chế độ debug hiển thị phản hồi chi tiết của sign packet trong console. | `megaantispoof.admin` |
| `/mas reload` | Tải lại toàn bộ tệp cấu hình `config.yml` và `messages.yml`. | `megaantispoof.admin` |

* **`megaantispoof.alert`**: Quyền nhận thông báo cảnh báo trong game khi có người chơi bị phát hiện sử dụng client gian lận.

---

## ⚙️ Hướng dẫn cấu hình

File cấu hình [`config.yml`](plugin/src/main/resources/config.yml) được chú thích rõ ràng bằng Tiếng Việt:

```yaml
# Bật/tắt chế độ debug sign packet
debug:
  sign-response: false

# Cấu hình tự động tải dependencies khi khởi động
dependencies:
  auto-download: true
  sha256: ""

# Cấu hình quét tự động khi người chơi tham gia máy chủ
check-on-join:
  enabled: true
  delay-ticks: 40 # Độ trễ trước khi gửi thử thách (20 ticks = 1 giây)

# Cấu hình chu kỳ thử thách và giới hạn gửi lại
challenge:
  timeout-ticks: 100
  interval-ticks: 20
  max-retries: 2
  retry-interval-ticks: 20
  close-gui-delay-ticks: 2

# Hành động xử lý khi phát hiện
actions:
  kick: true
  kick-on-no-response: false

# Cấu hình xử phạt & Ban vĩnh viễn người chơi cố chấp
punishment:
  ban-enabled: true
  max-violations: 3
  reset-on-pass: true
  discord-invite: "https://discord.gg/megacitymc"
  ban-commands:
    - "ban {player} Cố chấp sử dụng mod cấm ({mods}) quá {max_violations} lần. Kháng cáo: {discord}"


# Cấu hình Discord Webhook báo cáo chi tiết
webhook:
  enabled: true
  server-name: "Survival Main"
  url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"

# Danh sách chữ ký các mod cần quét và chặn
blocked-mods:
  meteor-client:
    name: "Meteor Client"
    key: "key.meteor-client.open-gui"
    mode: KEYBIND
  wurst:
    name: "Wurst Client"
    key: "key.wurst.zoom"
    mode: KEYBIND
  liquidbounce:
    name: "LiquidBounce"
    key: "liquidbounce.command.bind.description"
    mode: TRANSLATE
```

---

## 📡 Discord Webhook Preview

Khi người chơi bị phát hiện, một thông báo Embed trực quan sẽ được gửi về Discord:

```text
🚨 PHÁT HIỆN MOD / CLIENT BỊ CẤM

• Tài khoản: Steve
• UUID: 069a79f4-44e9-4726-a5be-fca90e38aaf5
• IP: 127.0.0.1
• Máy chủ: Survival Main
• Kết quả: FAILED

📋 Chi tiết Quét (2/16 mod Phát hiện):
- Meteor Client: Phát hiện
- Wurst Client: Không phát hiện
- LiquidBounce: Phát hiện
...
```

---

## 🛠️ Biên dịch từ mã nguồn (Build from Source)

Dự án sử dụng **Gradle 9+** và **Java 25 Toolchain**.

```bash
# Clone repository
git clone https://github.com/Huutri091/MegaAntiSpoof.git
cd MegaAntiSpoof

# Build toàn bộ project
./gradlew clean test build
```

File JAR hoàn chỉnh (Shadow Jar) sẽ được xuất ra tại:
`plugin/build/libs/MegaAntiSpoof-<version>.jar`

---

## 🔌 API cho Lập trình viên

MegaAntiSpoof cung cấp API độc lập thông qua module `api`:

### Gradle
```kotlin
dependencies {
    compileOnly("vn.megacitymc:megaantispoof-api:1.0.8")
}
```

### Lắng nghe kết quả phát hiện
```java
public class DetectionListener implements Listener {
    @EventHandler
    public void onDetection(DetectionResult result) {
        if (!result.passed()) {
            String player = result.playerName();
            List<String> detectedMods = result.detectedMods();
            Bukkit.getLogger().warning(player + " đang sử dụng: " + String.join(", ", detectedMods));
        }
    }
}
```

---

## ⚖️ Giấy phép & Tuyên bố miễn trừ

- **License**: Dự án được phát hành dưới giấy phép [MIT License](LICENSE).
- **Tuyên bố**: Plugin này chỉ nên được sử dụng trên các máy chủ mà bạn có quyền quản trị hợp pháp. Vui lòng tuân thủ các quy định về quyền riêng tư và thông báo cho người chơi về việc kiểm tra tính toàn vẹn của client.
