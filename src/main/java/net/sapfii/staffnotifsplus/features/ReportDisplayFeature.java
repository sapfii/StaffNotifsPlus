package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.feature.trait.RenderedFeature;
import dev.dfonline.flint.util.result.EventResult;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sapfii.staffnotifsplus.GUIKeyBinding;
import net.sapfii.staffnotifsplus.StaffNotifsPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportDisplayFeature implements RenderedFeature, PacketListeningFeature {
    private final GUIKeyBinding keyBinding = new GUIKeyBinding(
            "key.staffnotifsplus.dismissReports",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_J,
            "key.staffnotifsplus.category"
    );


    public ReportDisplayFeature() {
        KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    int lerp(int a, int b, float t) {
        int result = (int) Math.floor(a + (b - a) * t);
        if (result == a && result != b) {
            return a + 1;
        }
        return (int) result;
    }

    // bg color = 0x88000000

    boolean clearReports = false;

    HashMap<Text[], HashMap<String, Integer>> reportData = new HashMap<>(3);
    ArrayList<Text[]> reports = new ArrayList<>(3);
    private static final Pattern INC_RPT_REGEX = Pattern.compile(
            "! Incoming Report \\((?<reporter>.+)\\)\\nOffender: (?<offender>.+)\\nOffense: (?<offense>.+)\\nLocation: (?<location>.+)"
    );


    @Override
    public void render(DrawContext draw, RenderTickCounter renderTickCounter) {
        if (keyBinding.isPressed() && !reports.isEmpty() && !clearReports) {
            clearReports = true;
            Flint.getUser().getPlayer().playSound(SoundEvent.of(Identifier.of("staffnotifsplus","report_dismiss")));
        }
        TextRenderer textRenderer = Flint.getClient().textRenderer;
        float delta = renderTickCounter.getTickDelta(true);

        int maxLength = 0;

        int reportOffset = 0;
        for (Text[] reportTexts : reports) {
            boolean render = true;

            for (Text reportText : reportTexts) {
                int textLength = textRenderer.getWidth(reportText);
                if (textLength > maxLength) {
                    maxLength = textLength;
                }
            }

            int x = reportData.get(reportTexts).get("x");
            int y = reportData.get(reportTexts).get("y");
            int expectedX = reportOffset;
            reportData.get(reportTexts).put("x", lerp(x, expectedX, 0.15F * delta));
            x = reportData.get(reportTexts).get("x");

            int bgOpacity = reportData.get(reportTexts).get("bgOpacity");
            int opacity = reportData.get(reportTexts).get("opacity");
            int bgColor = 0x66000000;
            int color = 0xFFFFFFFF;


            if (reports.size() == 3) {
                if (reports.get(2) == reportTexts) {
                    int bgAlpha = lerp(bgOpacity, 0, 0.2F * delta);
                    int alpha = lerp(opacity, 0, 0.15F * delta);
                    bgColor = (bgAlpha << 24);
                    color = (alpha << 24) | color & 0x00FFFFFF;
                    if (alpha <= 10) {
                        render = false;
                    }
                    reportData.get(reportTexts).put("bgOpacity", bgAlpha);
                    reportData.get(reportTexts).put("opacity", alpha);
                }
            }

            if (clearReports) {
                if (reports.size() == 3 && (reports.get(2) == reportTexts)) {
                    break;
                } else {
                    int bgAlpha = lerp(bgOpacity, 0, 0.2F * delta);
                    int alpha = lerp(opacity, 0, 0.15F * delta);
                    bgColor = (bgAlpha << 24);
                    color = (alpha << 24) | color & 0x00FFFFFF;
                    if (alpha <= 10) {
                        reports.clear();
                        clearReports = false;
                        return;
                    } else {
                        reportData.get(reportTexts).put("bgOpacity", bgAlpha);
                        reportData.get(reportTexts).put("opacity", alpha);
                    }
                }
            }

            if (render && !reports.isEmpty()) {
                draw.fill(x, y-5, (10 + maxLength)+x, y+5+(textRenderer.fontHeight*4), bgColor);
                int txtI = 0;
                for (Text reportText : reportTexts) {
                    draw.drawTextWithShadow(
                            textRenderer,
                            reportText,
                            x+5, y+(textRenderer.fontHeight*txtI),
                            color
                    );
                    txtI += 1;
                }
                reportOffset += 15 + maxLength;
            }
        }
    }

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return EventResult.PASS;
        }
        TextRenderer textRenderer = Flint.getClient().textRenderer;

        String string = msgText.getString();

        Matcher matcher = INC_RPT_REGEX.matcher(string);
        if (matcher.find()) {
            Text[] reportTexts = new Text[4];

            reportTexts[0] = Text.literal("! ").styled(style -> {
                    style = style.withColor(0xFB5454);
                    style = style.withBold(true);
                    return style;})
                .append(Text.literal("Incoming Report ").styled(style -> {
                    style = style.withColor(0xa7a7a7);
                    style = style.withBold(false);
                    return style;}))
                .append(Text.literal("(" + matcher.group("reporter") + ")").styled(style -> {
                    style = style.withColor(0x545454);
                    style = style.withBold(false);
                    return style;}));

            reportTexts[1] = Text.literal("Offender: ").withColor(0xFF545454)
                    .append(Text.literal(matcher.group("offender")).withColor(0xFFFFFFFF));

            reportTexts[2] = Text.literal("Offense: ").withColor(0xFF545454)
                    .append(Text.literal(matcher.group("offense")).withColor(0xFFFFFFFF));

            reportTexts[3] = Text.literal("Location: ").withColor(0xFF545454)
                    .append(Text.literal(matcher.group("location")).withColor(0xFFFFFFFF));

            reports.addFirst(reportTexts);
            reportData.put(reportTexts, new HashMap<>());

            int maxLength = 0;

            for (Text reportText : reportTexts) {
                int textLength = textRenderer.getWidth(reportText);
                if (textLength > maxLength) {
                    maxLength = textLength;
                }
            }

            int x = -15 - maxLength;
            int y = 30;
            reportData.get(reportTexts).put("x", x);
            reportData.get(reportTexts).put("y", y);
            reportData.get(reportTexts).put("opacity", 255);
            reportData.get(reportTexts).put("bgOpacity", 102);
            if (reports.size() > 3) {
                reports.remove(3);
            }
            return EventResult.CANCEL;
        }

        return EventResult.PASS;
    }

    @Override
    public boolean alwaysOn() {
        return true;
    }
}
