package com.sensationcraft.sccore.ranks;

import com.sensationcraft.sccore.SCCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * Created by Anml on 1/12/16.
 */

public class PermissionsManager {

    private SCCore instance;
    private RankManager rankManager;
    private Map<UUID, PermissionAttachment> attachments;

    public PermissionsManager(SCCore instance) {
        this.instance = instance;
        this.rankManager = instance.getRankManager();
        this.attachments = new HashMap<>();
    }

    public Map<UUID, PermissionAttachment> getAttachments() {
        return this.attachments;
    }

    public void setAttachment(Player player) {
        PermissionAttachment attachment = player.addAttachment(this.instance);

        for (String permission : this.getRollingPermissions(this.rankManager.getRank(player.getUniqueId()))) {
            if (permission.substring(0, 1).equalsIgnoreCase("-")) {
                if (attachment.getPermissions().containsKey(permission.substring(1, permission.length())))
                    attachment.unsetPermission(permission.substring(1, permission.length()));
                attachment.setPermission(permission.substring(1, permission.length()), false);
            } else {
                if (attachment.getPermissions().keySet().contains(permission))
                    attachment.unsetPermission(permission);
                attachment.setPermission(permission, true);
            }
        }

        for (String permission : this.getPermissions(player.getUniqueId())) {
            if (permission.substring(0, 1).equalsIgnoreCase("-")) {
                if (attachment.getPermissions().containsKey(permission.substring(1, permission.length())))
                    attachment.unsetPermission(permission.substring(1, permission.length()));
                attachment.setPermission(permission.substring(1, permission.length()), false);
            } else {
                if (attachment.getPermissions().keySet().contains(permission))
                    attachment.unsetPermission(permission);
                attachment.setPermission(permission, true);
            }
        }

        this.attachments.put(player.getUniqueId(), attachment);
    }

    public void removeAttachment(UUID uuid) {
        if (this.attachments.containsKey(uuid)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.removeAttachment(this.attachments.get(uuid));
            }
            this.attachments.remove(uuid);
        }
    }

    public void updateAttachment(UUID uuid) {

        if (!Bukkit.getOfflinePlayer(uuid).isOnline())
            return;

        this.removeAttachment(uuid);
        this.setAttachment(Bukkit.getPlayer(uuid));
    }

    public void updateAttachments(Rank rank) {
        for (UUID uuid : this.attachments.keySet()) {
            if (this.rankManager.getRank(uuid).equals(rank))
                this.updateAttachment(uuid);
        }
    }

    public List<String> getPermissions(UUID uuid) {
        List<String> permissions = new ArrayList<>();

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = this.instance.getConfig();
        if (config.contains(path)) {
            for (String permission : config.getStringList(path)) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }

    public List<String> getPermissions(Rank rank) {
        List<String> permissions = new ArrayList<>();

        String path = "Permissions.Rank." + rank.name();
        FileConfiguration config = this.instance.getConfig();
        if (config.contains(path)) {
            for (String permission : config.getStringList(path)) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }

    public List<String> getRollingPermissions(Rank rank) {
        List<String> permissions = new ArrayList<>();

        for (int i = 0; i <= rank.getId(); i++) {
            for (String permission : this.getPermissions(this.rankManager.getRankById(i))) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }


    public boolean addPermission(Rank rank, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(this.getPermissions(rank));
        if (permissions.contains(node))
            return false;

        String path = "Permissions.Rank." + rank.name();
        FileConfiguration config = this.instance.getConfig();
        permissions.add(node);
        config.set(path, permissions);
        this.instance.saveConfig();

        this.updateAttachments(rank);

        return true;
    }

    public boolean removePermission(Rank rank, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(this.getPermissions(rank));
        if (!permissions.contains(node))
            return false;

        String path = "Permissions.Rank." + rank.name();
        FileConfiguration config = this.instance.getConfig();
        permissions.remove(node);
        config.set(path, permissions);
        this.instance.saveConfig();

        this.updateAttachments(rank);

        return true;
    }

    public boolean addPermission(UUID uuid, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(this.getPermissions(uuid));
        if (permissions.contains(node))
            return false;

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = this.instance.getConfig();
        permissions.add(node);
        config.set(path, permissions);
        this.instance.saveConfig();

        this.updateAttachment(uuid);

        return true;
    }

    public boolean removePermission(UUID uuid, String node) {
        node = node.toLowerCase();
        if (!this.getPermissions(uuid).contains(node))
            return false;

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = this.instance.getConfig();
        List<String> permissions = new ArrayList<>(this.getPermissions(uuid));
        permissions.remove(node);
        config.set(path, permissions);
        this.instance.saveConfig();

        this.updateAttachment(uuid);

        return true;
    }


}
