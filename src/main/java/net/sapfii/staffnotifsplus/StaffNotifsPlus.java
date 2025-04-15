package net.sapfii.staffnotifsplus;

import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.sapfii.staffnotifsplus.features.ReportDisplayFeature;
import net.sapfii.staffnotifsplus.features.ServerMuteFeature;
import net.sapfii.staffnotifsplus.features.LogFeature;
import net.sapfii.staffnotifsplus.features.VanishDisplayFeature;

public class StaffNotifsPlus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FlintAPI.registerFeatures(
                new VanishDisplayFeature(),
                new ReportDisplayFeature(),
                new ServerMuteFeature(),
                new LogFeature()
        );

        // report dismiss sound
        Registry.register(Registries.SOUND_EVENT, Identifier.of("staffnotifsplus", "report_dismiss"),
                SoundEvent.of(Identifier.of("staffnotifsplus", "report_dismiss")));
    }
}
