package online.aeck.customfish;

/**
 * FishStats — bảng tỉ lệ hợp lý giữa chiều dài, cân nặng và giá cá.
 *
 * Thực tế:
 *  - Cá common: 15-40cm, 0.1-2kg   → cá nhỏ thông thường (cá rô, cá mè nhỏ...)
 *  - Cá rare:   30-65cm, 0.5-5kg   → cá vừa (cá lóc, cá chép vừa...)
 *  - Cá epic:   55-100cm, 2-15kg   → cá lớn (cá trê, cá chép to...)
 *  - Cá legendary: 80-150cm, 8-30kg → cá cực lớn (cá hô, cá tra khổng lồ...)
 *  - Cá mythic: 120-220cm, 20-70kg  → cá khổng lồ (cá mập, cá ngừ đại dương...)
 *  - Cá godly:  180-350cm, 50-200kg → cá thần thoại
 *
 * Giá = basePrice × (0.7 + 0.6 × weightFraction) — cùng tier, cá nặng hơn đắt hơn.
 */
public final class FishStats {

    private FishStats() {}

    /** Phạm vi chiều dài [min, max] theo cm */
    public static double[] lengthRange(String quality) {
        return switch (quality.toLowerCase()) {
            case "godly"     -> new double[]{180, 350};
            case "mythic"    -> new double[]{120, 220};
            case "legendary" -> new double[]{80,  150};
            case "epic"      -> new double[]{55,  100};
            case "rare"      -> new double[]{30,   65};
            default          -> new double[]{15,   40};
        };
    }

    /** Phạm vi cân nặng [min, max] theo kg */
    public static double[] weightRange(String quality) {
        return switch (quality.toLowerCase()) {
            case "godly"     -> new double[]{50,  200};
            case "mythic"    -> new double[]{20,   70};
            case "legendary" -> new double[]{8,    30};
            case "epic"      -> new double[]{2,    15};
            case "rare"      -> new double[]{0.5,   5};
            default          -> new double[]{0.1,   2};
        };
    }

    /** Giá cơ bản ($) tại cân nặng trung bình */
    public static double basePrice(String quality) {
        return switch (quality.toLowerCase()) {
            case "godly"     -> 6000;
            case "mythic"    -> 2500;
            case "legendary" -> 900;
            case "epic"      -> 350;
            case "rare"      -> 120;
            default          -> 35;
        };
    }

    /**
     * Tính giá thực tế dựa vào cân nặng trong tier:
     * giá = basePrice × (0.7 + 0.6 × weightFraction)
     * → cá nhẹ nhất của tier = 70% giá base; cá nặng nhất = 130% giá base.
     */
    public static double calcPrice(String quality, double weight, double priceMultiplier) {
        double[] wRange = weightRange(quality);
        double minW = wRange[0], maxW = wRange[1];
        double fraction = (maxW > minW) ? Math.max(0, Math.min(1, (weight - minW) / (maxW - minW))) : 0.5;
        double price = basePrice(quality) * (0.7 + 0.6 * fraction);
        return price * priceMultiplier;
    }

    /**
     * Chiều dài hợp lý ứng với cân nặng (tuyến tính nội suy):
     * cá nặng hơn thường dài hơn.
     */
    public static double estimateLength(String quality, double weight) {
        double[] wRange = weightRange(quality);
        double[] lRange = lengthRange(quality);
        double fraction = (wRange[1] > wRange[0])
                ? Math.max(0, Math.min(1, (weight - wRange[0]) / (wRange[1] - wRange[0])))
                : 0.5;
        // Thêm noise ±10%
        double baseLng = lRange[0] + fraction * (lRange[1] - lRange[0]);
        double noise   = baseLng * 0.10 * (Math.random() * 2 - 1);
        return Math.max(lRange[0], Math.min(lRange[1], baseLng + noise));
    }

    /** HP combat theo phẩm chất */
    public static double calcHp(String quality, double weight) {
        double base = switch (quality.toLowerCase()) {
            case "godly"     -> 380;
            case "mythic"    -> 260;
            case "legendary" -> 180;
            case "epic"      -> 120;
            case "rare"      -> 80;
            default          -> 50;
        };
        return base + weight * 1.5;
    }
}
