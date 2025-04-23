package net.sapfii.staffnotifsplus;

import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.sapfii.staffnotifsplus.features.*;

public class StaffNotifsPlus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FlintAPI.registerFeatures(
                new VanishDisplayFeature(),
                new ReportDisplayFeature(),
                new ServerMuteFeature(),
                new LogFeature(),
                new ClickableSessionFeature(),
                new HistoryFeature()
        );

        StaffNotifsSound reportDismiss = new StaffNotifsSound("staffnotifsplus", "report_dismiss");

        registerSounds(
                reportDismiss
        );
    }

    public void registerSounds(StaffNotifsSound... sounds) {
        for (StaffNotifsSound sound : sounds) {
            Registry.register(Registries.SOUND_EVENT, Identifier.of(sound.namespace, sound.path),
                    SoundEvent.of(Identifier.of(sound.namespace, sound.path)));
        }
    }
}
