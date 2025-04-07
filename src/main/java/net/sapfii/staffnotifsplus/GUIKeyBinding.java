package net.sapfii.staffnotifsplus;

import dev.dfonline.flint.Flint;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.sapfii.staffnotifsplus.mixin.MKeyBindingAccessor;

public class GUIKeyBinding extends KeyBinding {
    private boolean enabledInChat = false;

    public GUIKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, code, category);
    }

    public GUIKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    public void setEnabledInChat(boolean enabledInChat) {
        this.enabledInChat = enabledInChat;
    }

    @Override
    public boolean isPressed() {
        if ((Flint.getClient().currentScreen instanceof ChatScreen || Flint.getClient().currentScreen instanceof InventoryScreen) && !enabledInChat) {
            return false;
        }

        return (InputUtil.isKeyPressed(
                Flint.getClient().getWindow().getHandle(),
                ((MKeyBindingAccessor) this).getBoundKey().getCode()
        ));
    }
}
