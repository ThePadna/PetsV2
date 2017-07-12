package pet;

import com.google.common.collect.Sets;
import main.Pets;
import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.PathEntity;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.*;
import org.bukkit.craftbukkit.v1_10_R1.util.UnsafeList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Cory on 23/05/2017.
 */
public class Pet {

    private String name = null;
    private final UUID owner;
    private final EntityType entityType;
    public Entity entity = null;
    private boolean baby = false;
    private boolean frozen = false;
    private final PetType petType;

    public Pet(String petName, UUID owner, EntityType e, PetType petType) {
        this.owner = owner;
        this.petType = petType;
        this.entityType = e;
        if(petName != null) name = petName;
    }

    public void kill() {
        if(entity != null) {
            entity.remove();
            entity = null;
        }
    }

    public PetType getPetType() {
        return this.petType;
    }

    public boolean isAstronaut() {
        return false;
    }

    public void setName(String name) {
        this.name = name.replace('&', '§');
        this.entity.setCustomName(Bukkit.getPlayer(this.getOwner()).getDisplayName() + "'s " + this.name);
    }

    public void spawn() {
        Player p = Bukkit.getPlayer(owner);
        entity = p.getWorld().spawnEntity(p.getLocation(), entityType);
        if(hasCustomName()) {
            entity.setCustomName(name);
        }
        entity.setCustomNameVisible(true);
        if(!(this instanceof PetAstronaut)) {
            this.NMS_eraseDefaults();
        }
        petFollowPlayer();
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public boolean hasCustomName() {
        return !(this.name == null);
    }

    public String getName() {
        if(name == null) {
            return name;
        } else {
            return this.getEntity().getName();
        }
    }

    public boolean setBaby() {
        switch(entityType) {
            case COW:
                CraftCow cow = (CraftCow) entity;
                cow.setBaby();
                baby = true;
                break;
            case ZOMBIE:
                CraftZombie zombie = (CraftZombie) entity;
                zombie.setBaby(true);
                baby = true;
                break;
            case WOLF:
                CraftWolf wolf = (CraftWolf) entity;
                wolf.setBaby();
                baby = true;
                break;
            case OCELOT:
                CraftOcelot ocelot = (CraftOcelot) entity;
                ocelot.setBaby();
                baby = true;
                break;
            case PIG:
                CraftPig pig = (CraftPig) entity;
                pig.setBaby();
                baby = true;
                break;
            case RABBIT:
                CraftRabbit rabbit = (CraftRabbit) entity;
                rabbit.setBaby();
                baby = true;
                break;
            default:
                break;
        }
        return baby;
    }

    public boolean isBaby() {
        return this.baby;
    }

    private Location lastPlayer;
    public void tick(int taskID, int xDelay, int yDelay, int zDelay) {
        if (entity != null) {
            if (entity.isDead() || !Bukkit.getPlayer(getOwner()).isOnline()) {
                Bukkit.getScheduler().cancelTask(taskID);
                entity.remove();
                return;
            }
            Location entityLocation = entity.getLocation();
            Location playerLocation = Bukkit.getPlayer(getOwner()).getLocation();
            if(!(entityLocation.getWorld() == playerLocation.getWorld())) {
                this.frozen = false;
                Pets.force_MobSpawning = true;
                entity.teleport(playerLocation);
                NMS_eraseDefaults();
                Pets.force_MobSpawning = false;
                entityLocation = entity.getLocation();
            }
            if(this.isFrozen()) return;
            if(lastPlayer != null && lastPlayer.getWorld() != playerLocation.getWorld()) lastPlayer = null;
            if(lastPlayer != null) {
                if(lastPlayer.distance(playerLocation) < 1) return;
            }
            final float newZ = (float)(playerLocation.getZ() + ( 1 * Math.sin(Math.toRadians(playerLocation.getYaw() + 90 * 0))));
            final float newX = (float)(playerLocation.getX() + ( 1 * Math.cos(Math.toRadians(playerLocation.getYaw() + 90 * 0))));
            Location improvised = playerLocation.clone();
            improvised.setX(newX);
            improvised.setZ(newZ);
            Location last;
            if (xDelay == 0) {
                xDelay = improvised.getBlockX();
                yDelay = improvised.getBlockY();
                zDelay = improvised.getBlockZ();
                last = new Location(improvised.getWorld(), xDelay, yDelay, zDelay);
            } else {
                last = new Location(improvised.getWorld(), xDelay, yDelay, zDelay);
                if (last.distance(improvised) < 5) return;
            }
            double dist = last.distance(entityLocation);
            if (dist >= 15 || (last.getBlockY() + 4) <= entityLocation.getBlockY() || (last.getBlockY() - 4) >= entityLocation.getBlockY()) {
                entity.teleport(last);
                return;
            }
            PathEntity path;
            EntityInsentient entityInsentient = (EntityInsentient) ((CraftEntity) entity).getHandle();
            path = entityInsentient.getNavigation().a(xDelay, yDelay, zDelay);
            entityInsentient.getNavigation().a(path, 2D);
            xDelay = playerLocation.getBlockX();
            yDelay = playerLocation.getBlockY();
            zDelay = playerLocation.getBlockZ();
            lastPlayer = playerLocation;
        }
    }

    public void NMS_eraseDefaults() {
        EntityCreature c = (EntityCreature) ((EntityInsentient)((CraftEntity)entity).getHandle());
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(c.goalSelector, Sets.newLinkedHashSet());
            bField.set(c.targetSelector, Sets.newLinkedHashSet());
            cField.set(c.goalSelector, Sets.newLinkedHashSet());
            cField.set(c.targetSelector, Sets.newLinkedHashSet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void petFollowPlayer() {
        FollowPlayer followPlayer = new FollowPlayer(this.owner, this);
        followPlayer.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Pets.getInstance(), followPlayer, 0L, 1L));
    }

    public Entity getEntity() {
        return entity;
    }

    public void toggleFrozen() {
        frozen = !frozen;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

class FollowPlayer implements Runnable {

    private Pet pet;
    private final UUID player;
    private int id;
    private int xDelay, yDelay, zDelay;

    public FollowPlayer(UUID p, Pet inst) {
        this.pet = inst;
        this.player = p;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int getId() {
        return id;
    }

    public void run() {
        pet.tick(this.id, xDelay, yDelay, zDelay);
    }
}
}
