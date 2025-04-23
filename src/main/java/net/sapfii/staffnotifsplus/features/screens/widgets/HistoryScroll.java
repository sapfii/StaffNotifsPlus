package net.sapfii.staffnotifsplus.features.screens.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;

public class HistoryScroll extends EntryListWidget<ReportScroll.Entry> {

    public HistoryScroll(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    public void addLine(Text text) {
        this.addEntry(new ReportScroll.Entry(text));
    }

    @Override
    public int getRowWidth() {
        return 240;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
