package net.sapfii.staffnotifsplus.features.screens;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.sapfii.staffnotifsplus.features.screens.widgets.LogScroll;

import java.util.ArrayList;
import java.util.List;

public class LogScreen extends Screen {
    private LogScroll logScroll;
    String logType;
    public LogScreen(Text title, String log) {
        super(title);
        logType = log;
    }

    List<Text> lines = new ArrayList<>();
    public void addMsg(Text text) {
        logScroll.addLine(text);
        addSelectableChild(logScroll);
        lines.add(text);
    }

    public void refresh() {
        this.logScroll = new LogScroll(client, width, height-90, 50, 10);
        addSelectableChild(logScroll);
        for (Text text : lines) {
            logScroll.addLine(text);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        refresh();
    }

    @Override
    protected void init() {
        logScroll = new LogScroll(client, width, height-90, 50, 10);
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Window window = Flint.getClient().getWindow();
        int titleX = window.getScaledWidth()/2-this.textRenderer.getWidth(logType)/2;
        logScroll.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, logType, titleX, 30, 0xFFFFFFFF, true);
    }
}
