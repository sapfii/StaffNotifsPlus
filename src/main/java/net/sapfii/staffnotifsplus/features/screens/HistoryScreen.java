package net.sapfii.staffnotifsplus.features.screens;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.sapfii.staffnotifsplus.features.LogFeature;
import net.sapfii.staffnotifsplus.features.PunishmentData;
import net.sapfii.staffnotifsplus.features.screens.widgets.HistoryScroll;

import java.util.ArrayList;
import java.util.List;

public class HistoryScreen extends Screen {
    HistoryScroll historyScroll;
    public List<PunishmentData> punishments = new ArrayList<>();

    public HistoryScreen(Text title) {
        super(title);
    }

    public void logPunishment(PunishmentData punishment) {
        punishments.add(punishment);
        addPunishment(punishment);
    }

    public void addPunishment(PunishmentData punishment) {
        int colour = switch (punishment.punishmentType) {
            case "warned" -> 0xfba900;
            case "muted", "kicked" -> 0xfbfe54;
            case "banned" -> 0xfb5454;
            default -> 0xfbfbfb;
        };
        Text punishmentText = Text.literal(punishment.recipient.substring(1)).styled(style -> {
                    style = style.withColor(0x54fb54);
                    return style;})
                .append(Text.literal(" was ").styled(style -> {
                    style = style.withColor(0xfbfbfb);
                    return style;}))
                .append(Text.literal(punishment.punishmentType).styled(style -> {
                    style = style.withColor(colour);
                    return style;}))
                .append(Text.literal(" by ").styled(style -> {
                    style = style.withColor(0xfbfbfb);
                    return style;}))
                .append(Text.literal(punishment.punisher).styled(style -> {
                    style = style.withColor(0x54fb54);
                    return style;}));
        if (!punishment.punishmentType.matches("kicked")) {
            if (punishment.expired && !punishment.revoked) {
                punishmentText = punishmentText.copy()
                        .append(Text.literal(" [").styled(style -> {
                            style = style.withColor(0x555555);
                            return style;}))
                        .append(Text.literal("Expired").styled(style -> {
                            style = style.withColor(0xAAAAAA);
                            return style;}))
                        .append(Text.literal("]").styled(style -> {
                            style = style.withColor(0x555555);
                            return style;}));
            } else if (punishment.revoked) {
                punishmentText = punishmentText.copy()
                        .append(Text.literal(" [").styled(style -> {
                            style = style.withColor(0x00AAAA);
                            return style;}))
                        .append(Text.literal("Revoked").styled(style -> {
                            style = style.withColor(0x55FFFF);
                            return style;}))
                        .append(Text.literal("]").styled(style -> {
                            style = style.withColor(0x00AAAA);
                            return style;}));
            } else {
                punishmentText = punishmentText.copy()
                        .append(Text.literal(" [").styled(style -> {
                            style = style.withColor(0xfb5454);
                            return style;}))
                        .append(Text.literal("Active").styled(style -> {
                            style = style.withColor(0xfba7a7);
                            return style;}))
                        .append(Text.literal("]").styled(style -> {
                            style = style.withColor(0xfb5454);
                            return style;}));
            }
        }
        Text reasonText = Text.empty().append(Text.literal("◘ ").styled(style -> {
                    style = style.withColor(0xfba7a7);
                    return style;}))
                .append(Text.literal("“").styled(style -> {
                    style = style.withColor(0xa7a8a7);
                    return style;}))
                .append(Text.literal(punishment.reason).styled(style -> {
                    style = style.withColor(0xdadada);
                    return style;}))
                .append(Text.literal("”").styled(style -> {
                    style = style.withColor(0xa7a8a7);
                    return style;}));
        ArrayList<Text> wrappedReasonTexts = LogFeature.wrap(reasonText, 180);
        if (punishments.size() == 1) {
            historyScroll.addLine(Text.literal("                                                       ").withColor(0x555555).formatted(Formatting.STRIKETHROUGH));
        }
        historyScroll.addLine(Text.empty().append(Text.literal("⌛ ").styled(style -> {
                    style = style.withColor(0xfa7e4d);
                    return style;}))
                .append(Text.literal(punishment.age).styled(style -> {
                    style = style.withColor(0xfcb686);
                    return style;})));
        historyScroll.addLine(punishmentText);
        if (punishment.reason.matches("No reason given\\.")) {
            historyScroll.addLine(Text.empty().append(Text.literal("◘ ").styled(style -> {
                        style = style.withColor(0xfba7a7);
                        return style;}))
                    .append(Text.literal(punishment.reason).styled(style -> {
                        style = style.withColor(0xFFFFFF);
                        return style;})));
        } else {
            for (Text reasonLine : wrappedReasonTexts) {
                historyScroll.addLine(reasonLine);
            }
        }
        if (!punishment.expiryDate.isEmpty()) {
            historyScroll.addLine(Text.literal("Expires in " + punishment.expiryDate).styled(style -> {
                style = style.withColor(0xfb5454);
                return style;}));
        }
        if (!punishment.unpunisher.isEmpty() && punishment.revoked) {
            historyScroll.addLine(Text.literal("Revoked by " + punishment.unpunisher).styled(style -> {
                style = style.withColor(0x55FFFF);
                return style;}));
        }
        historyScroll.addLine(Text.literal("                                                       ").withColor(0x555555).formatted(Formatting.STRIKETHROUGH));
        addSelectableChild(historyScroll);
    }

    private void refresh() {
        historyScroll = new HistoryScroll(
                client,
                width, height-90,
                50,
                10
        );
        addSelectableChild(historyScroll);
        for (PunishmentData punishment : punishments) {
            addPunishment(punishment);
        }
    }

    @Override
    protected void init() {
        historyScroll = new HistoryScroll(
                client,
                width, height-90,
                50,
                10
        );
        addSelectableChild(historyScroll);
        super.init();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        refresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        Window window = Flint.getClient().getWindow();

        historyScroll.render(context, mouseX, mouseY, delta);

        int titleX = window.getScaledWidth()/2-this.textRenderer.getWidth("History")/2;
        context.drawText(this.textRenderer, "History", titleX, 30, 0xFFFFFFFF, true);
    }
}
