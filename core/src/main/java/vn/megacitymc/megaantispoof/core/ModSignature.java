package vn.megacitymc.megaantispoof.core;

import java.util.Locale;

public record ModSignature(String id, String displayName, String translationKey, Mode mode) {
    public ModSignature {
        if (id == null || id.isBlank() || translationKey == null || translationKey.isBlank())
            throw new IllegalArgumentException("id/translationKey không được rỗng");
        id = id.toLowerCase(Locale.ROOT);
    }
    public enum Mode { TRANSLATE, KEYBIND }
}
