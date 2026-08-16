package vn.megacitymc.megaantispoof.nms.v1_21;
import vn.megacitymc.megaantispoof.nms.AbstractRangeAdapter;
public final class Adapter121 extends AbstractRangeAdapter {
    public Adapter121() { super("1.21-1.21.8"); }
    public boolean supports(String v) { return v.matches("1\\.21(?:\\.[0-8])?"); }
}
