package com.sensationcraft.sccore.punishments;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.mysql.MySQL;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.Utils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Anml on 1/8/16.
 */
public class Punishment {

    SCCore instance;
    Utils utils;
    PunishmentManager punishmentManager;
    SCPlayerManager scPlayerManager;
    MySQL mySQL;

    PunishmentType type;
    UUID target;
    UUID punisher;
    long created;
    long expires;
    String reason;
    boolean executed = false;

    public Punishment(PunishmentType type, UUID target, UUID punisher, long expires, String reason) {
        this.type = type;
        this.target = target;
        this.punisher = punisher;
        this.created = System.currentTimeMillis();
        this.expires = expires;
        this.reason = reason;

        this.instance = SCCore.getInstance();
        this.utils = this.instance.getUtils();
        this.punishmentManager = this.instance.getPunishmentManager();
        this.scPlayerManager = this.instance.getSCPlayerManager();
        this.mySQL = this.instance.getMySQL();
    }

    public PunishmentType getType() {
        return this.type;
    }

    public UUID getTarget() {
        return this.target;
    }

    public UUID getPunisher() {
        return this.punisher;
    }

    public long getCreated() {
        return this.created;
    }

    public void setCreated(long value) {
        this.created = value;
    }

    public long getExpires() {
        return this.expires;
    }

    public void setExpires(long value) {
        this.expires = value;
    }

    public List<String> getHoverText() {
        String tag = this.punisher != null ? this.scPlayerManager.getSCPlayer(this.punisher).getTag() : "§6Console";
        List<String> info = Arrays.asList(
                "§b" + this.type.name() + " Information:",
                "   §aCreator: §f" + tag,
                "   §aCreated: §f" + this.utils.getTimeStamp(this.created));

        if ((this.type.equals(PunishmentType.TEMPBAN) || this.type.equals(PunishmentType.TEMPMUTE)) && !this.hasExpired()) {
            info.add("   §aLength: §f" + this.utils.getDifference(this.created, this.created + this.expires));
            if (this.hasExpired())
                info.add("   §aRemaining: §f" + this.utils.getDifference(System.currentTimeMillis(), this.created + this.expires));
        }

        return info;
    }

    public boolean hasExpired() {
        return this.expires == 0L || (this.expires != -1L && (this.created + this.expires) <= System.currentTimeMillis());
    }

    public String getReason() {
        return this.reason;
    }

    public String getMessage() {
        String message = "";
        String tag = this.punisher != null ? this.scPlayerManager.getSCPlayer(this.punisher).getTag() : "§6Console";
        switch (this.type.getId()) {
            case 5:
                message = "§7You are permanently banned from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + this.reason + " §8- " + tag;
                break;
            case 4:
                message = "§7You are temporarily banned from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + this.reason + " §8- " + tag + "\n" +
                        "§7Remaining: §f" + this.utils.getDifference(System.currentTimeMillis(), this.created + this.expires) + "\n\n";
                break;
            case 3:
                message = "§7You have been muted by " + tag + " §7for: §a" + this.reason + "§7.";
                break;
            case 2:
                message = "§7You have been temporarily muted for §c" + this.utils.getDifference(System.currentTimeMillis(), this.created + this.expires) + " §7by " + tag + " §7for: §a" + this.reason + "§7.";
                break;
            case 1:
                message = "§7You have been warned by " + tag + " §7with reason: §a" + this.reason + "§7.";
                break;
            case 0:
                message = "§7You have been kicked from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + this.reason + " §8- " + tag;
                break;
            default:
                message = "§cError in Ban Type! Contact an administrator of SensationCraft.";
                break;
        }

        return message;
    }

    public boolean isExecuted() {
        return this.executed;
    }

    public void setExecuted(boolean value) {
        this.executed = value;
    }

    public void execute() {

        if (this.executed) {
            try {
                this.mySQL.executeUpdate("UPDATE SCPunishments SET Expires='" + this.expires + "' WHERE Target='" + this.target + "' AND Created='" + this.created + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            String punisherUUID = this.punisher == null ? "Console" : this.punisher.toString();
            this.mySQL.executeUpdate("INSERT INTO `SCPunishments` (`Type`, `Target`, `Punisher`, `Created`, `Expires`, `Reason`) " +
                    "VALUES ('" + this.type.name() + "','" + this.target + "', '" + punisherUUID + "', '" + this.created + "', '" + this.expires + "', '" + this.reason + "')");
            this.executed = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
