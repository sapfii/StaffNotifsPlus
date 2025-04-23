package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.feature.trait.RenderedFeature;
import dev.dfonline.flint.feature.trait.UserCommandListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import dev.dfonline.flint.util.result.ReplacementEventResult;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.sapfii.staffnotifsplus.features.screens.HistoryScreen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryFeature implements PacketListeningFeature, UserCommandListeningFeature, RenderedFeature {

    private static final Pattern HISTORY_INITIATOR = Pattern.compile("History for (?<user>.+) \\(Limit: (?<limit>.+)\\):");
    private static final Pattern PUNISHMENT = Pattern.compile("(?<recipient>.+) was (?<punishmentType>.+) by (?<punisher>.+): '(?<reason>.+)?'(?<expired>.+)?");
    private static final Pattern REVOKED_PUNISHMENT = Pattern.compile("(?<recipient>.+) was un(?<punishmentType>.+) by (?<unpunisher>.+)\\.");
    private static final Pattern PUNISHMENT_STARTER = Pattern.compile("-- \\[(?<date>.+)] --");
    private static final Pattern EXPIRES_IN = Pattern.compile("Expires in (?<date>.+)");

    HistoryScreen screen = new HistoryScreen(Text.empty());
    boolean expectingPunishments = false;
    boolean openScreen = false;
    PunishmentData currentPunishment = new PunishmentData();

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay)) || !expectingPunishments) {
            return EventResult.PASS;
        }
        String string = msgText.getString();
        if (string.matches("/history <player> \\[entries=10]") || string.matches("No history found")) {
            expectingPunishments = false;
            return EventResult.PASS;
        }
        Matcher historyMatcher = HISTORY_INITIATOR.matcher(string);
        Matcher punishmentMatcher = PUNISHMENT.matcher(string);
        Matcher revokedPunishmentMatcher = REVOKED_PUNISHMENT.matcher(string);
        Matcher punishmentStarterMatcher = PUNISHMENT_STARTER.matcher(string);
        Matcher expiresInMatcher = EXPIRES_IN.matcher(string);
        System.out.println(string);
        if (historyMatcher.find()) {
            currentPunishment = new PunishmentData();
        } else if (punishmentStarterMatcher.find()) {
            if (!currentPunishment.expired && currentPunishment.expiryDate.matches("") && !currentPunishment.revoked && (currentPunishment.punishmentType.matches("banned") || currentPunishment.punishmentType.matches("muted"))) {
                screen.logPunishment(currentPunishment);
            }
            currentPunishment = new PunishmentData();
            currentPunishment.age = punishmentStarterMatcher.group("date");
        } else if (punishmentMatcher.find()) {
            String reason = punishmentMatcher.group("reason");
            if (punishmentMatcher.group("reason") == null) {
                reason = "No reason given.";
            }
            currentPunishment = new PunishmentData(
                    punishmentMatcher.group("recipient"),
                    punishmentMatcher.group("punisher"),
                    punishmentMatcher.group("punishmentType"),
                    currentPunishment.age,
                    reason
            );
            if (punishmentMatcher.group("punishmentType").matches("kicked")) {
                screen.logPunishment(currentPunishment);
            }
            if (punishmentMatcher.group("expired") != null) {
                if (punishmentMatcher.group("expired").matches(" \\[Expired]")) {
                    currentPunishment.expired = true;
                    screen.logPunishment(currentPunishment);
                } else if (punishmentMatcher.group("expired").matches(" \\[Active]")) {
                    currentPunishment.expired = false;
                }
            } else {
                currentPunishment.revoked = true;
            }

        } else if (revokedPunishmentMatcher.find()) {
            currentPunishment.revoked = true;
            currentPunishment.unpunisher = revokedPunishmentMatcher.group("unpunisher");
            screen.logPunishment(currentPunishment);
        } else if (expiresInMatcher.find()){
            currentPunishment.expiryDate = expiresInMatcher.group("date");
            screen.logPunishment(currentPunishment);
            return EventResult.CANCEL;
        } else if (string.matches("")) {
            return EventResult.CANCEL;
        } else {
            expectingPunishments = false;
            return EventResult.PASS;
        }
        return EventResult.CANCEL;
    }

    @Override
    public ReplacementEventResult<String> sendCommand(String s) {
        if (s.matches("history (?<user>.+)")) {
            expectingPunishments = true;
            openScreen = true;
        }
        return ReplacementEventResult.pass();
    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (openScreen) {
            screen = new HistoryScreen(Text.empty());
            Flint.getClient().setScreen(screen);
            openScreen = false;
        }
    }
}
