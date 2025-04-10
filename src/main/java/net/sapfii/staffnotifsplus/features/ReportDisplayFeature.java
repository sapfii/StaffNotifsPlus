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
        int result = (int) Math.floor(a + ((b - a) * t));
        if (result == a && result != b) {
            return a + 1;
        }
        return result;
    }

    ArrayList<Text> wrap(Text text, int maxWidth) {
        TextRenderer textRenderer = Flint.getClient().textRenderer;
        String textString = text.getString();

        ArrayList<Text> wrappedTexts = new ArrayList<>();
        StringBuilder progress = new StringBuilder();
        StringBuilder oldProgress = new StringBuilder();

        for (int i = 0; i < textString.split("\\s+").length; ++i) {

            String word = textString.split("\\s+")[i];
            oldProgress = new StringBuilder(progress.toString());
            if (progress.isEmpty()) {
                progress = new StringBuilder(word);
            } else {
                progress.append(" ").append(word);
            }
            System.out.println(progress.toString().split("\\s+").length);
            if (textString.endsWith(word)) {
                wrappedTexts.add(Text.literal(progress.toString()));
                return wrappedTexts;
            }
            if (textRenderer.getWidth(progress.toString()) > maxWidth && textRenderer.getWidth(word) < maxWidth) {
                wrappedTexts.add(Text.literal(oldProgress.toString()));
                progress = new StringBuilder(word);
            }
            if (textRenderer.getWidth(word) > maxWidth) {
                wrappedTexts.add(Text.literal(oldProgress.toString()));
                progress = new StringBuilder();
                wrappedTexts.add(Text.literal(word));
            }
        }
        return wrappedTexts;
    }


    boolean clearReports = false;

    ArrayList<ReportData> reports = new ArrayList<>();
    private static final Pattern INC_RPT_REGEX = Pattern.compile(
            "! Incoming Report \\((?<reporter>.+)\\)\\nOffender: (?<offender>.+)\\nOffense: (?<offense>.+)\\nLocation: (?<location>.+)"
    );

    @Override
    public void render(DrawContext draw, RenderTickCounter renderTickCounter) {
        float delta = renderTickCounter.getTickDelta(true);

        if (keyBinding.isPressed() && !reports.isEmpty() && !clearReports) {
            clearReports = true;
            Flint.getUser().getPlayer().playSound(SoundEvent.of(Identifier.of("staffnotifsplus","report_dismiss")));
        }
        TextRenderer textRenderer = Flint.getClient().textRenderer;

        int maxLength = 0;
        int reportOffset = 0;
        for (int i = 0; i < reports.size(); ++i) {
            ReportData report = reports.get(i);

            boolean render = true;

            for (Text reportText : report.texts) {
                int textLength = textRenderer.getWidth(reportText);
                if (textLength > maxLength) {
                    maxLength = textLength;
                }
            }
            report.x = lerp(report.x, reportOffset, 0.15F * delta);

            int bgOpacity = report.bgOpacity;
            int opacity = report.opacity;
            int bgColor = 0x66000000;
            int color = 0xFFFFFFFF;

            if (i == 2) {
                int bgAlpha = lerp(bgOpacity, 0, 0.2F * delta);
                int alpha = lerp(opacity, 0, 0.15F * delta);
                bgColor = (bgAlpha << 24);
                color = (alpha << 24) | color & 0x00FFFFFF;
                if (alpha <= 10) {
                    render = false;
                }
                report.bgOpacity = bgAlpha;
                report.opacity = alpha;
            }

            if (clearReports && i < 2) {
                int bgAlpha = lerp(bgOpacity, 0, 0.2F * delta);
                int alpha = lerp(opacity, 0, 0.15F * delta);
                bgColor = (bgAlpha << 24);
                color = (alpha << 24) | color & 0x00FFFFFF;
                if (alpha <= 10) {
                    reports.clear();
                    clearReports = false;
                    return;
                } else {
                    report.opacity = alpha;
                    report.bgOpacity = bgAlpha;
                }
            }

            if (render) {
                draw.fill(report.x, report.y-5, (10 + maxLength)+report.x, report.y+5+(textRenderer.fontHeight*report.texts.size()), bgColor);
                for (int txtI = 0; txtI < report.texts.size(); ++txtI) {
                    Text reportText = report.texts.get(txtI);
                    draw.drawTextWithShadow(
                            textRenderer,
                            reportText,
                            report.x+5, report.y+(textRenderer.fontHeight*txtI),
                            color
                    );
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
            ArrayList<Text> reportTexts = new ArrayList<>();

            reportTexts.add(Text.literal("! ").styled(style -> {
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
                        return style;})));

            reportTexts.add(Text.literal("Offender: ").withColor(0xFF545454)
                    .append(Text.literal(matcher.group("offender")).withColor(0xFFFFFFFF)));

            ArrayList<Text> offenseLines = new ArrayList<>();

            ArrayList<Text> wrappedOffenseLines = wrap(Text.literal(matcher.group("offense")), 200);
            offenseLines.add(Text.literal("Offense: ").withColor(0xFF545454).append(Text.literal(wrappedOffenseLines.getFirst().getString()).withColor(0xFFFFFFFF)));
            wrappedOffenseLines.removeFirst();
            offenseLines.addAll(wrappedOffenseLines);
            reportTexts.addAll(2, offenseLines);

            reportTexts.add(Text.literal("Location: ").withColor(0xFF545454)
                    .append(Text.literal(matcher.group("location")).withColor(0xFFFFFFFF)));

            int maxLength = 0;

            for (Text reportText : reportTexts) {
                int textLength = textRenderer.getWidth(reportText);
                if (textLength > maxLength) {
                    maxLength = textLength;
                }
            }

            ReportData data = new ReportData();
            data.x = -15 - maxLength;
            data.y = 30;
            data.opacity = 255;
            data.bgOpacity = 102;
            data.texts = reportTexts;
            reports.addFirst(data);

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