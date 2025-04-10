package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.feature.trait.RenderedFeature;
import dev.dfonline.flint.feature.trait.UserCommandListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import dev.dfonline.flint.util.result.ReplacementEventResult;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class VanishDisplayFeature implements RenderedFeature, PacketListeningFeature, UserCommandListeningFeature {

    private long commandCooldown = System.currentTimeMillis();

    private boolean inModVanish = false;
    private boolean inAdminVanish = false;
    private boolean expectingVanishCmd = false;


    private int offsetX = 0;
    private int offsetY = 0;

    private int boxLength = 0;

    Text vanishText = Text.literal("");
    int vanishTextBgColor = 0x7FFFFFFF;

    int lerp(int a, int b, float t) {
        int result = (int) Math.floor(a + (b - a) * t);
        if (result == a && result != b) {
            return a + 1;
        }
        return result;
    }

    @Override
    public void render(DrawContext draw, RenderTickCounter renderTickCounter) {
        float delta = renderTickCounter.getTickDelta(true);
        TextRenderer textRenderer = Flint.getClient().textRenderer;

        if (inModVanish && inAdminVanish) {
            vanishText = Text.literal("Mod & Admin").withColor(0xfff630).append(Text.literal(" Vanish").withColor(0xFFFFFF));
            vanishTextBgColor = 0x7Fb89120;
        } else if (inModVanish) {
            vanishText = Text.literal("Mod").withColor(0x54fb54).append(Text.literal(" Vanish").withColor(0xFFFFFF));
            vanishTextBgColor = 0x7F2a8f48;
        } else if (inAdminVanish) {
            vanishText = Text.literal("Admin").withColor(0xfb2901).append(Text.literal(" Vanish").withColor(0xFFFFFF));
            vanishTextBgColor = 0x7Fc82b51;
        }

        int x = 3;
        int y = 3;


        int expectedOffsetY = 0;
        int expectedOffsetX = 0;
        if (!(inModVanish || inAdminVanish)) {
            expectedOffsetY = -textRenderer.fontHeight - 12;
        }

        offsetX = lerp(offsetX, expectedOffsetX, 0.15F * delta);
        offsetY = lerp(offsetY, expectedOffsetY, 0.15F * delta);

        int x1 = 6+offsetX;
        int expectedBoxLength = 4 + x1 + textRenderer.getWidth(vanishText);

        boxLength = lerp(boxLength, expectedBoxLength, 0.2F * delta);

        int boxHeight = 10+offsetY+textRenderer.fontHeight;

        draw.fill(x1, offsetY, boxLength, boxHeight, vanishTextBgColor);
        draw.drawTextWithShadow(
                textRenderer,
                vanishText,
                x + 5 + offsetX, y + 5 + offsetY,
                0xFFFFFF
        );

    }

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {

        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return EventResult.PASS;
        }

        String string = msgText.getString();

        if (string.startsWith("» Joined game: ")) {
            if (inModVanish) {
                inModVanish = false;
                Text message = Text.literal("[MOD] ").withColor(0xFF00a700).append(Text.literal("Mod Vanish disabled.").withColor(0xFFFFFFFF));
                Flint.getUser().getPlayer().sendMessage(message, false);
            }
        }

        switch (string) {
            case "» Vanish disabled. You will now be visible to other players.":
                return EventResult.CANCEL;
            case "» Vanish enabled. You will not be visible to other players.":
                if (!expectingVanishCmd) {
                    inModVanish = true;
                }
                expectingVanishCmd = false;
                return EventResult.CANCEL;
            case "[ADMIN] You are currently vanished!":
                inAdminVanish = true;
                break;
        }

        return EventResult.PASS;
    }

    @Override
    public ReplacementEventResult<String> sendCommand(String s) {
        Text message;
        if (System.currentTimeMillis() - commandCooldown < 100) {
            return ReplacementEventResult.pass();
        }
        switch (s) {
            case "s",
                 "spawn":
                commandCooldown = System.currentTimeMillis();
                if (inModVanish) {
                    inModVanish = false;
                    message = Text.literal("[MOD] ").withColor(0xFF00a700).append(Text.literal("Mod Vanish disabled.").withColor(0xFFFFFFFF));
                    Flint.getUser().getPlayer().sendMessage(message, false);
                }
                break;
            case "mod vanish",
                 "mod v":
                commandCooldown = System.currentTimeMillis();
                if (!inModVanish) {
                    expectingVanishCmd = true;
                }
                inModVanish = !inModVanish;
                if (inModVanish) {
                    message = Text.literal("[MOD] ").withColor(0xFF00a700).append(Text.literal("Mod Vanish enabled.").withColor(0xFFFFFFFF));
                } else {
                    message = Text.literal("[MOD] ").withColor(0xFF00a700).append(Text.literal("Mod Vanish disabled.").withColor(0xFFFFFFFF));
                }
                Flint.getUser().getPlayer().sendMessage(message, false);
                break;
            case "adminv on":
                commandCooldown = System.currentTimeMillis();
                expectingVanishCmd = true;
                if (!inAdminVanish) {
                    message = Text.literal("[ADMIN] ").withColor(0xFFfb2900).append(Text.literal("Admin Vanish enabled.").withColor(0xFFFFFFFF));

                } else {
                    message = Text.literal("Error: ").withColor(0xFFfb5454).append(Text.literal("Admin vanish is already enabled.").withColor(0xFFa8a8a8));
                    Flint.getUser().getPlayer().playSound(SoundEvents.ENTITY_SHULKER_HURT_CLOSED);
                }
                Flint.getUser().getPlayer().sendMessage(message, false);
                inAdminVanish = true;
                break;
            case "adminv off":
                commandCooldown = System.currentTimeMillis();
                if (inAdminVanish) {
                    message = Text.literal("[ADMIN] ").withColor(0xFFfb2900).append(Text.literal("Admin Vanish disabled.").withColor(0xFFFFFFFF));
                } else {
                    message = Text.literal("Error: ").withColor(0xFFfb5454).append(Text.literal("Admin vanish is already disabled.").withColor(0xFFa8a8a8));
                    Flint.getUser().getPlayer().playSound(SoundEvents.ENTITY_SHULKER_HURT_CLOSED);
                }
                Flint.getUser().getPlayer().sendMessage(message, false);
                inAdminVanish = false;
                break;
        }
        return ReplacementEventResult.pass();
    }

    @Override
    public boolean alwaysOn() {
        return true;
    }
}
