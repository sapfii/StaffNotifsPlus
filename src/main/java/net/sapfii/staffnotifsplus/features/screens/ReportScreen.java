package net.sapfii.staffnotifsplus.features.screens;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.sapfii.staffnotifsplus.features.screens.widgets.LogScroll;
import net.sapfii.staffnotifsplus.features.screens.widgets.ReportScroll;

import java.util.ArrayList;
import java.util.List;

public class ReportScreen extends Screen{
    private ReportScroll scrollList;
    public ReportScreen(Text title) {
        super(title);
    }

    List<Text> lines = new ArrayList<>();
    public void addReport(Text text) {
        scrollList.addEntry(text);
        addSelectableChild(scrollList);
        lines.add(text);
    }

    public void refresh() {
        this.scrollList = new ReportScroll(client, width, height-90, 50, 10);
        addSelectableChild(scrollList);
        for (Text text : lines) {
            scrollList.addEntry(text);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        refresh();
    }

    @Override
    protected void init() {
        scrollList = new ReportScroll(client, width, height-90, 50, 10);
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Window window = Flint.getClient().getWindow();
        super.render(context, mouseX, mouseY, delta);
        int titleX = window.getScaledWidth()/2-this.textRenderer.getWidth("Reports")/2;
        scrollList.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "Reports", titleX, 30, 0xFFFFFFFF, true);
    }
}
