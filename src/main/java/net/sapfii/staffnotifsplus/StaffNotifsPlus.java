package net.sapfii.staffnotifsplus;

import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.sapfii.staffnotifsplus.features.ReportDisplayFeature;
import net.sapfii.staffnotifsplus.features.ServerMuteFeature;
import net.sapfii.staffnotifsplus.features.VanishDisplayFeature;

public class StaffNotifsPlus implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FlintAPI.registerFeatures(
                new VanishDisplayFeature(),
                new ReportDisplayFeature(),
                new ServerMuteFeature()
        );
    }
}
