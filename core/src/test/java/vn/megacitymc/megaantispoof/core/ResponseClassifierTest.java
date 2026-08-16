package vn.megacitymc.megaantispoof.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ResponseClassifierTest {
    @Test void detectsResolvedTranslationOnly() {
        var signatures = List.of(new ModSignature("meteor", "Meteor", "key.meteor.open-gui", ModSignature.Mode.KEYBIND));
        assertEquals(1, new ResponseClassifier().detect(signatures, List.of("Mở Meteor"), null).size());
        assertTrue(new ResponseClassifier().detect(signatures, List.of("key.meteor.open-gui"), null).isEmpty());
    }

    @Test void unresolvedTranslateIsNotDetected() {
        var signatures = List.of(new ModSignature("baritone", "Baritone", "baritone.settings", ModSignature.Mode.TRANSLATE));
        assertTrue(new ResponseClassifier().detect(signatures, List.of("baritone.settings"), "Unknown Key").isEmpty());
        assertTrue(new ResponseClassifier().detect(signatures,
                List.of("[{\\\"translate\\\":\\\"baritone.settings\\\"}]"), null).isEmpty());
    }
}
