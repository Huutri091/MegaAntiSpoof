package vn.megacitymc.megaantispoof.nms.v1_21_9;
import vn.megacitymc.megaantispoof.nms.AbstractRangeAdapter;
public final class Adapter1219 extends AbstractRangeAdapter {
    public Adapter1219() { super("1.21.9-1.21.11"); }
    public boolean supports(String v) { return v.matches("1\\.21\\.(?:9|10|11)"); }
}
