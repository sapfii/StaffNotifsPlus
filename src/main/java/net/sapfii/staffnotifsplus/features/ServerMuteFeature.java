package net.sapfii.staffnotifsplus.features;

import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

public class ServerMuteFeature implements PacketListeningFeature {
    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return EventResult.PASS;
        }
        String string = msgText.getString();


        if (string.startsWith("[ViaVersion] There is a newer plugin version available:") ||
                string.startsWith("(FAWE) An update for FastAsyncWorldEdit is available.")) {
            return EventResult.CANCEL;
        }

        return switch (string) {
            case "[Server: Automatic saving is now enabled]",
                 "[Server: Automatic saving is now disabled]",
                 "[Server: Saved the game]" -> EventResult.CANCEL;
            default -> EventResult.PASS;
        };
    }

    @Override
    public boolean alwaysOn() {
        return true;
    }
}
