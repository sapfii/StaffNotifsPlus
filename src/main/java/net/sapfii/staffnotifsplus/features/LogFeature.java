package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.feature.trait.RenderedFeature;
import dev.dfonline.flint.feature.trait.UserCommandListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import dev.dfonline.flint.util.result.ReplacementEventResult;
import jdk.jfr.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.sapfii.staffnotifsplus.features.screens.LogScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFeature implements PacketListeningFeature, RenderedFeature, UserCommandListeningFeature {
    private static final Pattern LOG_INITIATOR = Pattern.compile("--------------\\[ (?<logType>.+) \\| (?<node>.+) ]--------------");
    private static final Pattern CMD_INITIATOR = Pattern.compile("(?<logType>.+) log (?<params>.+)");
    private static final Pattern PLOT_FILTER = Pattern.compile("(?<time>.+) \\[(?<action>.+)]\\[(?<plot>.+)] (?<player>.+): (?<msg>.+)");
    LogScreen logScreen = new LogScreen(
            Text.empty(), "Log"
    );

    boolean didLogCmd = false;
    boolean expectingLogMsgs = false;
    ArrayList<Text> logMsgs = new ArrayList<>();

    boolean openScreen = false;
    String logType = "Log";

    public static ArrayList<Text> wrap(Text text, int maxLength) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String string = text.getString();

        List<Text> siblings = text.getSiblings();
        List<Style> styleList = new ArrayList<>();

        for (Text sibling : siblings) {
            String s = sibling.getString();
            for (int i = 0; i < s.length(); ++i) {
                styleList.add(sibling.getStyle());
            }
        }

        ArrayList<Text> wrappedTexts = new ArrayList<>();
        Text line = Text.empty();
        StringBuilder wordProgress = new StringBuilder();
        int wordLength;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.toCharArray()[i];
            wordProgress.append(c);
            wordLength = textRenderer.getWidth(wordProgress.toString());

            if (c == ' ') {
                Text word = Text.empty();
                for (int wordI = 0; wordI < wordProgress.length(); ++wordI) {
                    char wordC = wordProgress.toString().charAt(wordI);
                    int styleIndex = i-wordProgress.length()+wordI+1;
                    Text newChar = Text.literal(Character.toString(wordC));
                    if (styleList.size() > styleIndex) {
                        newChar = Text.literal(Character.toString(wordC)).setStyle(styleList.get(i-wordProgress.length()+wordI+1));
                    }
                    word = word.copy().append(newChar);
                    if (textRenderer.getWidth(line.copy().append(word)) > maxLength && wordLength > maxLength/1.5) {
                        line = line.copy().append(word);
                        wrappedTexts.add(line);
                        line = Text.empty();
                        word = Text.empty();
                    }
                }
                wordProgress = new StringBuilder();
                if (textRenderer.getWidth(line) > maxLength) {
                    wrappedTexts.add(line);
                    line = Text.empty();
                }
                line = line.copy().append(word);
            } else if (i == string.length()-1) {
                Text word = Text.empty();
                for (int wordI = 0; wordI < wordProgress.length(); ++wordI) {
                    char wordC = wordProgress.toString().charAt(wordI);
                    if (styleList.size() > i-wordProgress.length()+wordI+1) {
                        word = word.copy().append(Text.literal(Character.toString(wordC)).setStyle(styleList.get(i-wordProgress.length()+wordI+1)));
                    } else {
                        word = word.copy().append(Text.literal(Character.toString(wordC)));
                    }
                    if (textRenderer.getWidth(line.copy().append(word)) > maxLength && wordLength > maxLength/1.5) {
                        line = line.copy().append(word);
                        wrappedTexts.add(line);
                        line = Text.empty();
                        word = Text.empty();
                    }
                }
                line = line.copy().append(word);
                wrappedTexts.add(line);
            }
        }
        return wrappedTexts;
    }


    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return EventResult.PASS;
        }

        String string = msgText.getString();

        Matcher matcher = LOG_INITIATOR.matcher(string);
        if (matcher.find()) {
            if (didLogCmd || expectingLogMsgs) {
                didLogCmd = false;
                expectingLogMsgs = !expectingLogMsgs;
                if (!expectingLogMsgs) {
                    openScreen = true;
                    logType = matcher.group("logType") + " | " + matcher.group("node");
                }
                return EventResult.CANCEL;
            }
        }
        if (expectingLogMsgs) {
            if ((string.startsWith("[ADMIN]") || string.startsWith("[MOD]")) && !logMsgs.isEmpty()) {
                List<Text> siblings = msgText.getSiblings();
                msgText = Text.empty();
                for (Text sibling : siblings) {
                    TextColor color = TextColor.fromFormatting(Formatting.AQUA);
                    if (sibling.getStyle().getColor() == color) {
                        sibling = Text.literal(Integer.valueOf(Math.clamp(logMsgs.size()-1, 0, 150)).toString()).setStyle(sibling.getStyle());
                    }
                    msgText = msgText.copy().append(sibling);
                }
                logMsgs.add(msgText);
                return EventResult.CANCEL;
            }
            if (MinecraftClient.getInstance().currentScreen instanceof LogScreen) {
                Matcher findPlot = PLOT_FILTER.matcher(string);
                String plotFilter = ((LogScreen) MinecraftClient.getInstance().currentScreen).plotFilter.getText();
                if (!plotFilter.isEmpty()) {
                    if ((findPlot.find() && plotFilter.matches(findPlot.group("plot").substring(1)) || string.startsWith("[MOD]") || string.startsWith("[ADMIN]"))) {
                        logMsgs.add(msgText);
                    }
                    return EventResult.CANCEL;
                }
                logMsgs.add(msgText);
                return EventResult.CANCEL;
            }
            logMsgs.add(msgText);
            return EventResult.CANCEL;
        }
        return EventResult.PASS;
    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (openScreen) {
            openScreen = false;
            logScreen = new LogScreen(Text.empty(), logType);
            if (MinecraftClient.getInstance().currentScreen instanceof LogScreen) {
                logScreen = new LogScreen(
                        Text.empty(),
                        logType,
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).filter.getText(),
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).plotFilter.getText(),
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).duration.getText(),
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).durationUnits.getValue(),
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).secondDuration.getText(),
                        ((LogScreen) MinecraftClient.getInstance().currentScreen).secondDurationUnits.getValue());
            }

            MinecraftClient.getInstance().setScreen(logScreen);
            for (Text msg : logMsgs.reversed()) {
                for (Text wrappedMsg : wrap(msg, 400)) {
                    logScreen.addMsg(wrappedMsg);
                }
            }
            logMsgs = new ArrayList<>();
        }
    }

    @Override
    public ReplacementEventResult<String> sendCommand(String s) {
        Matcher matcher = CMD_INITIATOR.matcher(s);
        if (matcher.find() || (s.equals("mod log") || s.equals("admin log") || s.equals("mod log ") || s.equals("admin log "))) {
            didLogCmd = true;
        }
        return ReplacementEventResult.pass();
    }
}
