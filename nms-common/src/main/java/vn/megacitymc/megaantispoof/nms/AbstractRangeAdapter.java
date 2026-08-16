package vn.megacitymc.megaantispoof.nms;

import vn.megacitymc.megaantispoof.api.VersionAdapter;

public abstract class AbstractRangeAdapter implements VersionAdapter {
    private final String family;
    protected AbstractRangeAdapter(String family) { this.family = family; }
    @Override public String family() { return family; }
    @Override public int maxLinesPerChallenge() { return 4; }
}
