package net.sapfii.staffnotifsplus.features;

public class PunishmentData {
    public String recipient = "", punisher = "", punishmentType = "", reason = "", age = "", unpunisher = "", expiryDate = "";
    public boolean expired = false, revoked = false;

    public PunishmentData() {
    }

    public PunishmentData(String recipient, String punisher, String punishmentType, String age, String reason) {
        this.recipient = recipient;
        this.punisher = punisher;
        this.punishmentType = punishmentType;
        this.age = age;
        this.reason = reason;
    }

    public PunishmentData copy() {
        PunishmentData copiedData = new PunishmentData(
                this.recipient,
                this.punisher,
                this.punishmentType,
                this.age,
                this.reason
        );
        copiedData.expired = this.expired;
        copiedData.unpunisher = this.unpunisher;
        copiedData.expiryDate = this.expiryDate;
        return copiedData;
    }
}
