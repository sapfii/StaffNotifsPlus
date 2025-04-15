package net.sapfii.staffnotifsplus.features.screens.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class ReportScroll extends EntryListWidget<ReportScroll.Entry> {

    public ReportScroll(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    public void addEntry(Text text) {
        this.addEntry(new Entry(text));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public static class Entry extends EntryListWidget.Entry<Entry> {
        private final Text text;

        public Entry(Text text) {
            this.text = text;
        }

        public Text getText() {
            return text;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            VertexConsumerProvider.Immediate vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            textRenderer.draw(
                    this.text,
                    x, y,
                    0xFFFFFFFF,
                    true,
                    matrix,
                    vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    0xF000F0
            );
            vertexConsumers.draw();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
    }
}
