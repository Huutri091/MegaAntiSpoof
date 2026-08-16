package vn.megacitymc.megaantispoof.core;

import java.util.*;

public final class ResponseClassifier {
    public Set<String> detect(List<ModSignature> signatures, List<String> response) {
        return detect(signatures, response, null);
    }

    public Set<String> detect(List<ModSignature> signatures, List<String> response, String unknownKeybindControl) {
        Set<String> found = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(signatures.size(), response.size()); i++) {
            String value = normalize(Objects.requireNonNullElse(response.get(i), ""));
            ModSignature signature = signatures.get(i);
            // Mọi chữ ký đều được gửi dưới dạng translate. Unknown key phải trở về chính key đó.
            // Không suy luận từ KEYBIND vì client mới có thể trả tên từng unknown key khác nhau.
            // Client/protocol mới có thể bọc unknown translation bằng whitespace hoặc JSON.
            // Chỉ phát hiện nếu khóa gốc biến mất hoàn toàn khỏi phản hồi đã chuẩn hóa.
            boolean detected = !value.isEmpty() && !value.contains(normalize(signature.translationKey()));
            if (detected) found.add(signature.id());
        }
        return Set.copyOf(found);
    }

    public String normalize(String value) {
        return value.replaceAll("(?i)§[0-9A-FK-ORX]", "")
                .replace("\\\"", "\"")
                .strip();
    }
}
