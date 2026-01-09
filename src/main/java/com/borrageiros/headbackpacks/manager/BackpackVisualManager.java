package com.borrageiros.headbackpacks.manager;

import com.borrageiros.headbackpacks.HeadBackpacks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackVisualManager {
    private final Map<UUID, ArmorStand> stands = new HashMap<>();
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Plugin plugin;
    private final Map<UUID, Float> bodyYaw = new HashMap<>();
    private final Map<UUID, Location> lastPos = new HashMap<>();
    private final Map<UUID, double[]> velocity = new HashMap<>();

    public BackpackVisualManager(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled() {
        HeadBackpacks instance = HeadBackpacks.getInstance();
        return instance != null && instance.isVisualBackpacksEnabled();
    }

    public void spawnBackpackFor(Player player, ItemStack backpackItem) {
        if (!isEnabled()) return;
        removeBackpack(player);
        if (backpackItem == null) return;

        Location loc = player.getLocation();
        double headYawRad = Math.toRadians(loc.getYaw());
        // place behind the player: backward vector = (sin(yaw), -cos(yaw))
        double dx = Math.sin(headYawRad) * 0.3;
        double dz = -Math.cos(headYawRad) * 0.3;
        double y = player.getLocation().getY() + 0.2;
        // rotate 180 degrees so the backpack faces opposite direction
        Location spawnLoc = new Location(loc.getWorld(), loc.getX() + dx, y, loc.getZ() + dz, loc.getYaw() + 180f, 0);

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        // make the stand smaller so the helmet (backpack) appears smaller on the player
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        // marker true removes collision and can improve positioning for cosmetic items
        stand.setMarker(true);
        stand.setCustomNameVisible(false);
        stand.getEquipment().setHelmet(backpackItem.clone());

        stands.put(player.getUniqueId(), stand);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (player.isOnline() && !stand.isDead()) {
                Location pLoc = player.getLocation();
                Location prev = lastPos.get(player.getUniqueId());
                
                float yaw = bodyYaw.getOrDefault(player.getUniqueId(), pLoc.getYaw());
                double[] vel = velocity.get(player.getUniqueId());
                double velX = 0.0, velZ = 0.0;
                
                if (prev != null) {
                    double dxPos = pLoc.getX() - prev.getX();
                    double dzPos = pLoc.getZ() - prev.getZ();
                    double distSq = dxPos * dxPos + dzPos * dzPos;
                    
                    if (distSq > 0.0001) {
                        double movementYaw = Math.toDegrees(Math.atan2(-dxPos, dzPos));
                        if (movementYaw < 0) movementYaw += 360;
                        yaw = (float) movementYaw;
                        bodyYaw.put(player.getUniqueId(), yaw);
                        
                        velX = dxPos;
                        velZ = dzPos;
                        velocity.put(player.getUniqueId(), new double[]{velX, velZ});
                    } else {
                        if (vel != null) {
                            velX = vel[0] * 0.9;
                            velZ = vel[1] * 0.9;
                            velocity.put(player.getUniqueId(), new double[]{velX, velZ});
                        }
                        
                        float currentYaw = pLoc.getYaw();
                        float storedYaw = bodyYaw.getOrDefault(player.getUniqueId(), currentYaw);
                        float yawDiff = Math.abs(((currentYaw - storedYaw) + 180 + 360) % 360 - 180);
                        
                        if (yawDiff > 15.0 && player.isOnGround()) {
                            yaw = currentYaw;
                            bodyYaw.put(player.getUniqueId(), yaw);
                        }
                    }
                    
                    lastPos.put(player.getUniqueId(), pLoc.clone());
                } else {
                    yaw = pLoc.getYaw();
                    bodyYaw.put(player.getUniqueId(), yaw);
                    lastPos.put(player.getUniqueId(), pLoc.clone());
                    velocity.put(player.getUniqueId(), new double[]{0.0, 0.0});
                }

                double pyaw = Math.toRadians(yaw);
                double pdx = Math.sin(pyaw) * 0.3;
                double pdz = -Math.cos(pyaw) * 0.3;
                
                double speed = Math.sqrt(velX * velX + velZ * velZ);
                double predictionFactor = 0.8 + (speed * 6.0);
                predictionFactor = Math.min(predictionFactor, 5.0);
                
                double predictedX = pLoc.getX() + velX * predictionFactor;
                double predictedZ = pLoc.getZ() + velZ * predictionFactor;
                double py = pLoc.getY() + 0.2;
                
                Location desired = new Location(pLoc.getWorld(), predictedX + pdx, py, predictedZ + pdz, yaw + 180f, 0);

                Location current = stand.getLocation();
                double diffX = current.getX() - desired.getX();
                double diffZ = current.getZ() - desired.getZ();
                double dy = Math.abs(current.getY() - desired.getY());
                float currentYaw = current.getYaw();
                double yawDiff = Math.abs(((currentYaw - desired.getYaw()) + 180 + 360) % 360 - 180);

                if (diffX * diffX + diffZ * diffZ > 0.0001 || dy > 0.01 || yawDiff > 2.0) {
                    stand.teleport(desired);
                }
            }
        }, 0L, 1L);

        tasks.put(player.getUniqueId(), task);
    }

    public void removeBackpack(Player player) {
        UUID id = player.getUniqueId();
        if (tasks.containsKey(id)) {
            tasks.get(id).cancel();
            tasks.remove(id);
        }
        if (stands.containsKey(id)) {
            ArmorStand stand = stands.remove(id);
            if (stand != null && !stand.isDead()) stand.remove();
        }
        bodyYaw.remove(id);
        lastPos.remove(id);
        velocity.remove(id);
    }

    public void cleanup() {
        for (BukkitTask task : tasks.values()) task.cancel();
        tasks.clear();
        for (ArmorStand stand : stands.values()) if (!stand.isDead()) stand.remove();
        stands.clear();
        bodyYaw.clear();
        lastPos.clear();
        velocity.clear();
    }
}
