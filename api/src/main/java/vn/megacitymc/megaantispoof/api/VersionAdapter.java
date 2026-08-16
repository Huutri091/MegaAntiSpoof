package vn.megacitymc.megaantispoof.api;

public interface VersionAdapter {
    boolean supports(String minecraftVersion);
    String family();
    int maxLinesPerChallenge();
}
