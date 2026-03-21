# aeckCustomFishing - Minecraft Paper Plugin

## Project Overview

Plugin câu cá tùy chỉnh cho Minecraft Paper server (API 1.21). Phát triển bởi AECK.ONLINE. Java 21, Maven.

## Build

```bash
JAVA_HOME=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6 \
PATH=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6/bin:$PATH \
mvn -f aeckfishing1.0/pom.xml clean package
```

Output: `aeckfishing1.0/target/customfish-1.1-RELEASE.jar`

## Tính năng

### Cơ chế câu cá + chiến đấu (FishCatchListener + FishCombatManager)

**Flow câu cá:**
1. Quăng cần → cá cắn → `CAUGHT_FISH` fires
2. Plugin hủy sự kiện gốc, sinh thông số cá (quality, weight, HP, price...)
3. Kiểm tra **dây câu** (maxWeight): cá quá nặng → đứt dây, thông báo
4. Khởi động **trận chiến**: BossBar HP xuất hiện trên màn hình

**Cơ chế chiến đấu (FishCombatManager + FishCombatListener):**
- **Chuột Trái** (cầm cần câu) = 1 lượt tấn công (cooldown 0.8s)
- **Chuột Phải** khi combat → bị chặn, hiển thị hướng dẫn
- BossBar hiển thị HP cá: 🟢 >60% | 🟡 >30% | 🔴 <30%
- Crit: rod.critChance % để nhân sát thương × rod.critDamage
- Power skill cộng thêm 50% bonus vào sát thương
- Cá KHÔNG tự thoát; chỉ kết thúc khi HP=0 hoặc player thoát server
- HP cá: common=50, rare=80, epic=120, legendary=180, mythic=260, godly=380 + weight×2

**Lưỡi câu** — 2 tác dụng:
- `damage`: cộng vào sát thương combat, tăng range cân nặng cá
- `fish-types`: chỉ câu được loại cá trong danh sách

**Mồi câu** — `rare-bonus` (0–30%): giảm ngưỡng chất lượng, dễ câu cá hiếm

**Dây câu** — `maxWeight`: cá nặng hơn ngưỡng → đứt dây trước khi combat

**Crit** — cần câu có critChance/critDamage tăng giá bán và EXP khi combat

### /fishinventory (Admin only — aeck.admin)
GUI 54 ô, hiển thị tất cả vật phẩm trong plugin:
- Hàng 1: Cá mẫu 6 phẩm chất (common → godly) — click lấy 1, Shift+click lấy 64
- Hàng 2: Cá đặc biệt từ `fishspecial.yml` (có tỉ lệ xuất hiện)
- Hàng 3: Tất cả cần câu — click để trang bị ngay
- Hàng 4: Tất cả dây câu — click để trang bị
- Hàng 5: Tất cả lưỡi câu — click để trang bị
- Hàng 6: Tất cả mồi câu — click để trang bị
Item đang trang bị phát sáng (enchant glow), click refresh GUI để cập nhật.

### Bảng tỉ lệ cá thực tế (FishStats)
| Phẩm chất  | Chiều dài   | Cân nặng     | Giá cơ bản |
|------------|-------------|--------------|------------|
| Common     | 15–40 cm    | 0.1–2 kg     | $35        |
| Rare       | 30–65 cm    | 0.5–5 kg     | $120       |
| Epic       | 55–100 cm   | 2–15 kg      | $350       |
| Legendary  | 80–150 cm   | 8–30 kg      | $900       |
| Mythic     | 120–220 cm  | 20–70 kg     | $2,500     |
| Godly      | 180–350 cm  | 50–200 kg    | $6,000     |

Chiều dài tương quan với cân nặng (cá nặng → dài hơn, ±10% noise).
Giá cuối = basePrice × (0.7 + 0.6 × weightFraction) × multiplier × critBonus.

### Trang bị (/nangcapcan) - RodUpgradeGUI
- GUI 54 ô, 4 hàng chọn trang bị: Cần / Dây / Lưỡi / Mồi
- Item đang trang bị phát sáng (enchant glow)
- Nhấn để trang bị ngay, refresh GUI tức thì

### Cây kỹ năng (/kynangcauca) - SkillTreeGUI
- **Thuần Thục** (Mastery, slot 2): +5%/cấp giá bán cá, tối đa 5 cấp
- **Sức Mạnh** (Power, slot 6): +10%/cấp EXP, tối đa 5 cấp
- Slot 4: thông tin cấp, EXP, điểm kỹ năng, trang bị hiện tại

### Nhiệm vụ (/nhiemvucauca, /nvcauca)
- Hỗ trợ `minQuality` — chỉ tính khi câu cá đủ phẩm chất
- Nhận thưởng EXP + item khi hoàn thành
- Tiến độ hiển thị sau mỗi lần câu

### Kinh tế (/banca)
- Bán cá qua GUI, tích hợp Vault
- Fallback reflection tìm plugin economy khác

### Mùa (/muacauca)
- 4 mùa: XUAN, HA, THU, DONG
- Tự động đổi mỗi 30 phút (36000 ticks)
- Admin set tay được

### Giải đấu (tự động)
- Bắt đầu mỗi 2 giờ, kéo dài 10 phút
- Ai câu cá nặng nhất thắng, thông báo toàn server

### EXP & Level (/kinhnghiemcauca)
- Lên cấp tự động, nhận điểm kỹ năng

### Bảng xếp hạng (/topcauca)
- Top 10 cá nặng nhất từ MySQL

### MySQL
- Log mọi lượt câu cá (async)
- Truy vấn top fish

## Thuộc tính trang bị

| Loại | Thuộc tính chính |
|------|-----------------|
| Cần câu | damage, speed, critChance, critDamage |
| Dây câu | maxWeight (kg tối đa) |
| Lưỡi câu | damage (chiến đấu), fish-types (lọc loại cá) |
| Mồi câu | rare-bonus (0-30% tăng tỉ lệ cá hiếm), effect |

## Cấu hình quan trọng

- `config.yml`: exp-per-quality, quality-thresholds, monster-chance, mysql, price-multiplier
- `fishspecial.yml`: cá đặc biệt, chance, fixed-quality
- `fishingrod.yml`: tất cả trang bị với stats đầy đủ
- `quests.yml`: nhiệm vụ với minQuality support
