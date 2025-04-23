package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickableSessionFeature implements PacketListeningFeature {
    private static final Pattern INC_QUEUE_MSG = Pattern.compile("\\[SUPPORT] (?<player>.+) joined the support queue. ▶ Reason: (?<reason>.+)");

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return EventResult.PASS;
        }
        String string = msgText.getString();
        Matcher matcher = INC_QUEUE_MSG.matcher(string);
        if (matcher.find()) {
            List<Text> siblings = msgText.getSiblings();
            msgText = Text.empty();
            for (Text sibling : siblings) {
                TextColor color = TextColor.fromFormatting(Formatting.WHITE);
                if (sibling.getStyle().getColor() == color) {
                    sibling = Text.literal(sibling.getString()).setStyle(sibling.getStyle()
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/support accept "+matcher.group("player")))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("/support accept "+matcher.group("player")))));
                }
                msgText = msgText.copy().append(sibling);
            }
            Flint.getUser().getPlayer().sendMessage(msgText, false);
            return EventResult.CANCEL;
        }
        return EventResult.PASS;
    }
}
