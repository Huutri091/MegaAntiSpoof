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

    @Test void detectsLiquidBounceAndThunderHack() {
        var signatures = List.of(
                new ModSignature("liquidbounce", "LiquidBounce", "liquidbounce.command.bind.description", ModSignature.Mode.TRANSLATE),
                new ModSignature("thunderhack", "ThunderHack", "descriptions.combat.autocrystal", ModSignature.Mode.TRANSLATE)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("Binds a module to a key.", "descriptions.combat.autocrystal"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("liquidbounce"));
        assertFalse(detected.contains("thunderhack"));
    }

    @Test void detectsWurstClient() {
        var signatures = List.of(
                new ModSignature("wurst", "Wurst Client", "key.wurst.zoom", ModSignature.Mode.KEYBIND)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("V"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("wurst"));

        var notDetected = new ResponseClassifier().detect(signatures, List.of("key.wurst.zoom"));
        assertTrue(notDetected.isEmpty());
    }

    @Test void detectsXPlusAutoFish() {
        var signatures = List.of(
                new ModSignature("xplus-autofish", "XPlus AutoFish", "key.autofish.open_gui", ModSignature.Mode.KEYBIND)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("Open XPlus Autofish GUI"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("xplus-autofish"));

        var notDetected = new ResponseClassifier().detect(signatures, List.of("key.autofish.open_gui"));
        assertTrue(notDetected.isEmpty());
    }

    @Test void detectsAutoSwitch() {
        var signatures = List.of(
                new ModSignature("autoswitch", "AutoSwitch", "key.autoswitch.toggle", ModSignature.Mode.KEYBIND)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("AutoSwitch Toggle Key"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("autoswitch"));

        var notDetected = new ResponseClassifier().detect(signatures, List.of("key.autoswitch.toggle"));
        assertTrue(notDetected.isEmpty());
    }

    @Test void detectsAntiAfk() {
        var signatures = List.of(
                new ModSignature("antiafk", "AntiAFK", "key.antiafk.toggle", ModSignature.Mode.KEYBIND)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("AntiAfk Toggle"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("antiafk"));

        var notDetected = new ResponseClassifier().detect(signatures, List.of("key.antiafk.toggle"));
        assertTrue(notDetected.isEmpty());
    }

    @Test void detectsLitematica() {
        var signatures = List.of(
                new ModSignature("litematica", "Litematica", "litematica.gui.title.schematic_browser", ModSignature.Mode.TRANSLATE)
        );
        var detected = new ResponseClassifier().detect(signatures, List.of("Schematic Browser"));
        assertEquals(1, detected.size());
        assertTrue(detected.contains("litematica"));

        var notDetected = new ResponseClassifier().detect(signatures, List.of("litematica.gui.title.schematic_browser"));
        assertTrue(notDetected.isEmpty());
    }
}
