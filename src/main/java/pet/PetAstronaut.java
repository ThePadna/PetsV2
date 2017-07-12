package pet;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Cory on 23/05/2017.
 */
public class PetAstronaut extends Pet {
    int smokeCounter = 0;

    public PetAstronaut(String petName, UUID owner, EntityType e, PetType petType) {
        super(petName, owner, e, petType);
    }

    public static Location lookAt(Location loc, Location lookat) {
        loc = loc.clone();
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        if (dx != 0) {
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        loc.setPitch((float) -Math.atan(dy / dxz));
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
        return loc;
    }

    @Override
    public boolean isAstronaut() {
        return true;
    }

    @Override
    public void spawn() {
        Player p = Bukkit.getPlayer(super.getOwner());
        final float newZ = (float)(p.getLocation().getZ() + ( 1 * Math.sin(Math.toRadians(p.getLocation().getYaw() + 90 * 0))));
        final float newX = (float)(p.getLocation().getX() + ( 1 * Math.cos(Math.toRadians(p.getLocation().getYaw() + 90 * 0))));
        Location loc = new Location(p.getWorld(), newX, p.getLocation().getY()+2.5, newZ);
        super.entity = p.getWorld().spawnEntity(loc, super.getEntityType());
        ArmorStand entityArmorStand = (ArmorStand) super.getEntity();
        entityArmorStand.setBasePlate(false);
        entityArmorStand.setSmall(true);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.WHITE);
        boots.setItemMeta(bootsMeta);
        entityArmorStand.setBoots(boots);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) boots.getItemMeta();
        chestMeta.setColor(Color.WHITE);
        chest.setItemMeta(chestMeta);
        entityArmorStand.setChestplate(chest);
        SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);

        meta.setOwner("space_man");

        ItemStack stack = new ItemStack(Material.SKULL_ITEM,1 , (byte)3);

        stack.setItemMeta(meta);
        entityArmorStand.setHelmet(stack);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(Color.WHITE);
        leggings.setItemMeta(leggingsMeta);
        entityArmorStand.setLeggings(leggings);
        entityArmorStand.setArms(true);

        if (hasCustomName()) {
            entity.setCustomName(p.getDisplayName() + "'s " + super.getName());
        } else {
            entity.setCustomName(p.getDisplayName() + "'s " + ChatColor.ITALIC + ChatColor.YELLOW + "ASTRONAUT");
        }
        entity.setCustomNameVisible(true);
        petFollowPlayer();
    }

    private Location lastPlayer;
    @Override
    public void tick(int taskID, int xDelay, int yDelay, int zDelay) {
        smokeCounter++;
        if(entity != null) {
            if (entity.isDead() || !Bukkit.getPlayer(getOwner()).isOnline()) {
                Bukkit.getScheduler().cancelTask(taskID);
                entity.remove();
                return;
            }
            Location astronaut = super.getEntity().getLocation();
            Player player = Bukkit.getPlayer(super.getOwner());
            Location playerLoc = player.getLocation();

            if(!(astronaut.getWorld() == playerLoc.getWorld())) {
                if(super.isFrozen()) super.toggleFrozen();
                entity.teleport(playerLoc);
                astronaut = entity.getLocation();
            }
            if(super.isFrozen()) return;
            Location last;
            if (xDelay == 0) {
                xDelay = playerLoc.getBlockX();
                yDelay = playerLoc.getBlockY();
                zDelay = playerLoc.getBlockZ();
                last = new Location(player.getWorld(), xDelay, yDelay, zDelay);
            } else {
                last = new Location(player.getWorld(), xDelay, yDelay, zDelay);
            }
            float newZ = (float)(player.getLocation().getZ() + ( 1 * Math.sin(Math.toRadians(player.getLocation().getYaw() + 90 * 0))));
            float newX = (float)(player.getLocation().getX() + ( 1 * Math.cos(Math.toRadians(player.getLocation().getYaw() + 90 * 0))));
            double height = 2.5;
            switch(smokeCounter) {
                case 15:
                    height = 2.4;
                    break;
                case 16:
                    height = 2.3;
                    break;
                case 17:
                    height = 2.2;
                    break;
                case 18:
                    height = 2.1;
                    break;
                case 19:
                    height = 2;
                    break;
                case 20:
                    height = 1.9;
                    break;
                case 21:
                    height = 2;
                    break;
                case 22:
                    height = 2.1;
                    break;
                case 23:
                    height = 2.2;
                    break;
                case 24:
                    height = 2.3;
                    break;
                case 25:
                    height = 2.4;
                    break;
                case 26:
                    height = 2.5;
                    smokeCounter = -5;
                    break;
            }
            if(super.getEntity().getLocation().distance(playerLoc) > 5) {
                super.getEntity().teleport(new Location(playerLoc.getWorld(), newX, playerLoc.getY()+height, newZ));
                return;
            }
            Location target = new Location(playerLoc.getWorld(), newX, playerLoc.getY()+height, newZ);
            Location difference = target.subtract(astronaut);
            difference.getDirection().normalize().multiply(-1);
            super.getEntity().setVelocity(difference.toVector());
            Block block = player.getTargetBlock((Set<Material>)null, 5);
            Location looking = lookAt(super.getEntity().getLocation(), block.getLocation());
            ((ArmorStand)super.getEntity()).setHeadPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setBodyPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setLeftArmPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setRightArmPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setLeftLegPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setRightLegPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            super.getEntity().getWorld().playEffect(super.getEntity().getLocation().subtract(0, 0, 0), Effect.SMALL_SMOKE, 1);
            //lastPlayer = playerLoc;
        }
    }
}

