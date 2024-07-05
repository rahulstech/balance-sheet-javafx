package rahulstech.jfx.balancesheet.util;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public class ChartUtil {

    // TODO: use better indicator
    // current implementation uses tooltip need something that persists
    // also does not interfare with chart representation
    public static void setChartIndicator(Node node, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(node,tooltip);
    }
}
