package vn.megacitymc.megaantispoof.nms.v26;
import vn.megacitymc.megaantispoof.nms.AbstractRangeAdapter;
public final class Adapter26 extends AbstractRangeAdapter {
    public Adapter26() { super("26.x"); }
    public boolean supports(String v) { return v.matches("26(?:\\.\\d+){0,2}"); }
}
