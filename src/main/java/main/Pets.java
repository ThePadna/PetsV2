package main;

import com.massivecraft.factions.entity.MPlayer;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import gui.PetPicker;
import net.md_5.bungee.api.*;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_10_R1.*;
import net.minecraft.server.v1_10_R1.Item;
import org.bukkit.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftSkeleton;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftZombie;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pet.Pet;
import pet.PetAstronaut;
import pet.PetType;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Cory on 22/05/2017.
 */
public class Pets extends JavaPlugin implements Listener {
    static Pets instance;
    private final Random random = new Random();
    private static Permission perms = null;
    public static String prefix = ChatColor.RESET + "[" + ChatColor.GREEN + "Pets" + ChatColor.RESET + "] ";
    private final long PVP_TIMEOUT_MS = 5000, WEB_SPINNER_MS = 3000;
    private final long CD_SPIDER = 30000, CD_CREEPER = 30000, CD_PIG = 45000, CD_CHARGED_CREEPER = 45000, CD_WITCH = 60000*2+30000, CD_SKELETON = 45000, CD_ZOMBIE = 45000, CD_CAVE_SPIDER = 45000,
    CD_OCELOT = 30000, CD_WOLF = 60000, CD_RABBIT = 30000, CD_BLAZE = 15000, CD_COW = 60000, CD_ENDERMAN = 60000, CD_CHICKEN = 0, CD_SHEEP = 45000;
    private int SPIDER_BITE = 0, CAVE_SPIDER_BITE = 0;
    private final String[] ASTRONAUT_ALLOWED_WORLDS = {"Free", "Nether", "End"};
    final public HashSet<Pet> petHashSet = new HashSet<Pet>();
    private boolean pluginExplosion = false;
    private final HashMap<UUID, HashMap<PetType, Long>> cooldowns = new HashMap<UUID, HashMap<PetType, Long>>();
    private final HashMap<UUID, HashMap<UUID, Long>> target = new HashMap<UUID, HashMap<UUID, Long>>();
    private final PotionEffectType[] negativeEffects = new PotionEffectType[]{
            PotionEffectType.SLOW_DIGGING, PotionEffectType.BLINDNESS, PotionEffectType.POISON, PotionEffectType.CONFUSION,
            PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.SLOW
    };

    public Pets() {
        this.instance = this;
    }

    public static Pets getInstance() {
        return instance;
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if(!this.setupPermissions()) {
            System.out.println("[PETSV2] Can't enable without vault.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for(Pet pet : petHashSet) {
                    Player p = Bukkit.getPlayer(pet.getOwner());
                    if(pet.getPetType() == PetType.PIG) {
                        if(p != null) {
                            p.setFoodLevel(20);
                        }
                    } else if(pet.getPetType() == PetType.WITCH) {
                        if(p != null) {
                            for(PotionEffectType potionEffectType : negativeEffects) {
                                p.removePotionEffect(potionEffectType);
                            }
                            for(Player p1 : Bukkit.getOnlinePlayers()) {
                                if(p1.getWorld() == p.getWorld()) {
                                    if(isFriendly(p, p1)) {
                                        if(p1.getLocation().distance(p.getLocation()) <= 10) {
                                            for(PotionEffectType potionEffectType : negativeEffects) {
                                                p1.removePotionEffect(potionEffectType);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if(pet.getPetType() == PetType.WOLF) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.OCELOT) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.SPEED, 40, 0);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.RABBIT) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.JUMP, 40, 0);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.BLAZE) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.COW) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.REGENERATION, 1000, 0);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.SHEEP) {
                        Sheep sheep = (Sheep) pet.getEntity();
                        int i = random.nextInt(DyeColor.values().length);
                        sheep.setColor(DyeColor.values()[i]);
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.SPEED, 40, 1);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.ZOMBIE) {
                        if(!day(p)) {
                            PotionEffect potionEffect = new PotionEffect(PotionEffectType.REGENERATION, 40, 1);
                            p.removePotionEffect(potionEffect.getType());
                            p.addPotionEffect(potionEffect);
                        }
                    } else if(pet.getPetType() == PetType.ASTRONAUT) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.JUMP, 40, 2);
                        p.removePotionEffect(potionEffect.getType());
                        p.addPotionEffect(potionEffect);
                    } else if(pet.getPetType() == PetType.CAVE_SPIDER) {
                        if(CAVE_SPIDER_BITE >= 8) {
                            List<UUID> targets = getTargets(p);
                            if (targets == null) return;
                            else {
                                for (UUID uuid : targets) {
                                    Player p1 = Bukkit.getPlayer(uuid);
                                    if (p1 != null) {
                                        if(!isInPVP(p1)) return;
                                        p1.damage(1D);
                                        p1.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                                        Location loc = p1.getLocation().add(0, 3, 0);
                                        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP};
                                        for (BlockFace blockFace : faces) {
                                            final org.bukkit.entity.Item e = loc.getWorld().dropItem(loc.getBlock().getRelative(blockFace).getLocation(), new ItemStack(Material.DIAMOND_SWORD, 1));
                                            e.setPickupDelay(1000000000);
                                            e.setCustomName(ChatColor.RED + "Poison Bite");
                                            e.setCustomNameVisible(true);
                                            new BukkitRunnable() {
                                                public void run() {
                                                    e.remove();
                                                }
                                            }.runTaskLater(getInstance(), 40L);
                                        }
                                    }
                                }
                            }
                        }
                    } else if(pet.getPetType() == PetType.SPIDER) {
                        if(SPIDER_BITE >= 5) {
                            List<UUID> targets = getTargets(p);
                            if(targets == null) return;
                            else {
                                for(UUID uuid : targets) {
                                    Player p1 = Bukkit.getPlayer(uuid);
                                    if (p1 != null) {
                                        if(!isInPVP(p1)) return;
                                        p1.damage(2D);
                                        Location loc = p1.getLocation().add(0, 3, 0);
                                        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP};
                                        for (BlockFace blockFace : faces) {
                                            final org.bukkit.entity.Item e = loc.getWorld().dropItem(loc.getBlock().getRelative(blockFace).getLocation(), new ItemStack(Material.IRON_SWORD, 1));
                                            e.setPickupDelay(1000000000);
                                            e.setCustomName(ChatColor.RED + "Bite");
                                            e.setCustomNameVisible(true);
                                            new BukkitRunnable() {
                                                public void run() {
                                                    e.remove();
                                                }
                                            }.runTaskLater(getInstance(), 40L);
                                        }
                                    }
                                }
                            }
                            SPIDER_BITE = 0;
                        }
                    }
                }
                SPIDER_BITE++;
                CAVE_SPIDER_BITE++;
                Iterator iterator = instance.target.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<UUID, HashMap<UUID, Long>> entry = (Map.Entry<UUID, HashMap<UUID, Long>>)iterator.next();
                    Iterator iterator1 = ((HashMap<UUID, Long>)entry.getValue()).entrySet().iterator();
                    while(iterator1.hasNext()) {
                        Map.Entry<UUID, Long> entry1 = (Map.Entry<UUID, Long>)iterator1.next();
                        long millis = entry1.getValue();
                        long now = System.currentTimeMillis();
                        if(now >= millis) iterator1.remove();
                    }
                    if(instance.target.get(entry.getKey()).isEmpty()) {
                        iterator.remove();
                    }
                }
            }
        }, 60L, 20L);
    }

    public boolean day(Player p) {
        long time = p.getWorld().getTime();
        return time < 12300 || time > 23850;
    }

    @Override
    public void onDisable() {
        Iterator iterator = this.petHashSet.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet) iterator.next();
            pet.kill();
            iterator.remove();
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(commandLabel.equalsIgnoreCase("pets")) {
            if (args.length == 0) {
                if (perms.has(sender, "pets.selector")) {
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage(ChatColor.RED + "Error: Can't open pets menu in console.");
                        return false;
                    }
                    PetPicker.openPetMenu((Player) sender);
                }
            } else if(args.length > 0 && sender.hasPermission("pets.admin")) {
                if(args[0].equalsIgnoreCase("grant")) {
                    if(args.length == 3) {
                        String player = args[1];
                        String pet = args[2];

                        Player p = Bukkit.getPlayer(player);
                        if(p == null) {
                            sender.sendMessage(ChatColor.RED + "Error: Player: " + ChatColor.DARK_RED + player + ChatColor.RED + " doesn't exist or isn't online.");
                            return false;
                        }
                        if(PetType.valueOf(pet) == null) {
                            sender.sendMessage(ChatColor.RED + "Error: Pet Type: " + ChatColor.DARK_RED + pet + ChatColor.RED + " doesn't exist.");
                            return false;
                        }
                        PetType petType = PetType.valueOf(pet);

                        Data.setPet(p, petType, true);
                        sender.sendMessage(ChatColor.RED + "Player " + ChatColor.DARK_RED + p.getDisplayName() + ChatColor.RED + " now has " + ChatColor.DARK_RED + petType.name());
                    } else {
                        sender.sendMessage(ChatColor.RED + "Error: syntax is /pets grant <Player> <PetType>");
                        return false;
                    }
                } else if(args[0].equalsIgnoreCase("remove")) {
                    if(args.length == 3) {
                        String player = args[1];
                        String pet = args[2];

                        Player p = Bukkit.getPlayer(player);
                        if(p == null) {
                            sender.sendMessage(ChatColor.RED + "Error: Player: " + ChatColor.DARK_RED + player + ChatColor.RED + " doesn't exist or isn't online.");
                            return false;
                        }
                        if(PetType.valueOf(pet) == null) {
                            sender.sendMessage(ChatColor.RED + "Error: Pet Type: " + ChatColor.DARK_RED + pet + ChatColor.RED + " doesn't exist.");
                            return false;
                        }
                        PetType petType = PetType.valueOf(pet);

                        Data.setPet(p, petType, false);
                        sender.sendMessage(ChatColor.RED + "Removed " + ChatColor.DARK_RED + p.getDisplayName() + ChatColor.RED + "'s pet " + ChatColor.DARK_RED + petType.name());
                    } else {
                        sender.sendMessage(ChatColor.RED + "Error: syntax is /pets grant <Player> <PetType>");
                        return false;
                    }
                } else if(args[0].equalsIgnoreCase("admin")) {
                    sender.sendMessage(ChatColor.GREEN + "/pets grant/remove <player> <pet>");
                }
            }
        }
        return false;
    }

    private long getCoolDown(Player p, PetType petType) {
        HashMap<PetType, Long> cooldowns = this.cooldowns.get(p.getUniqueId());
        if(cooldowns == null || cooldowns.get(petType) == null) {
            return 0L;
        } else {
            long now = System.currentTimeMillis();
            long cooldown = cooldowns.get(petType);
            if(now >= cooldown) {
                return 0L;
            } else {
                return (cooldown - now);
            }
        }
    }

    private void setCooldown(Player p, final PetType petType, long cooldown) {
        HashMap<PetType, Long> cooldowns = this.cooldowns.get(p.getUniqueId());
        cooldown += System.currentTimeMillis();
        if(cooldowns == null) {
            HashMap<PetType, Long> map  = new HashMap<PetType, Long>();
            map.put(petType, cooldown);
            this.cooldowns.put(p.getUniqueId(), map);
        } else {
            cooldowns.put(petType, cooldown);
        }
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(e.getCurrentItem() != null &&
                e.getCurrentItem().getItemMeta() != null &&
                e.getCurrentItem().getItemMeta().getDisplayName() != null &&
                e.getCurrentItem().getItemMeta().getDisplayName().contains("Remove")) {
            this.eraseCurrentPet(p);
            e.setCancelled(true);
            return;
        }
        for(PetType petType : PetType.values()) {
            if(e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.ITALIC +  "" + ChatColor.BOLD + petType.name())) {
                if(Data.hasPet(p, petType)) {
                    Pet pet;
                    p.setAllowFlight(false);
                    p.setFlying(false);
                    if(petType == petType.ASTRONAUT) {
                        pet = new PetAstronaut(null, p.getUniqueId(), EntityType.ARMOR_STAND, petType);
                        for(String s : this.ASTRONAUT_ALLOWED_WORLDS) {
                            if(p.getWorld().getName().equals(s)) {
                                p.setAllowFlight(true);
                                p.setFlying(true);
                            }
                        }
                    } else if(petType == PetType.WITHER_SKELETON) {
                        pet = new Pet(p.getDisplayName() + "'s " + ChatColor.GOLD + "✪ " + ChatColor.ITALIC + ChatColor.YELLOW + petType.name().replace("_", " "), p.getUniqueId(), EntityType.SKELETON, petType);
                    } else if(petType == PetType.CHARGED_CREEPER) {
                        pet = new Pet(p.getDisplayName() + "'s " + ChatColor.GOLD + "✪ " + ChatColor.ITALIC + ChatColor.YELLOW +  petType.name().replace("_", " "), p.getUniqueId(), EntityType.CREEPER, petType);
                    } else {
                        pet = new Pet(p.getDisplayName() + "'s " + ChatColor.GOLD + "✪ " + ChatColor.ITALIC + ChatColor.YELLOW +  petType.name(), p.getUniqueId(), EntityType.valueOf(petType.name()), petType);
                    }
                    this.eraseCurrentPet(p);
                    this.petHashSet.add(pet);
                    force_MobSpawning = true;
                    pet.spawn();
                    force_MobSpawning = false;
                    if(pet.getPetType() == PetType.CHARGED_CREEPER) {
                        Creeper creeper = (Creeper) pet.getEntity();
                        creeper.setPowered(true);
                    } else if(pet.getPetType() == PetType.SKELETON) {
                        Skeleton skeleton = (Skeleton) pet.getEntity();
                        CraftSkeleton craftSkeleton = (CraftSkeleton) skeleton;
                        EntitySkeleton skeletonNMS = craftSkeleton.getHandle();
                        skeletonNMS.setEquipment(EnumItemSlot.HEAD, new net.minecraft.server.v1_10_R1.ItemStack(net.minecraft.server.v1_10_R1.Item.getById(306)));
                    } else if(pet.getPetType() == PetType.ZOMBIE) {
                        Zombie zombie = (Zombie) pet.getEntity();
                        CraftZombie craftZombie = (CraftZombie) zombie;
                        EntityZombie zombieNMS = craftZombie.getHandle();
                        zombieNMS.setEquipment(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_10_R1.ItemStack(net.minecraft.server.v1_10_R1.Item.getById(283)));
                        zombieNMS.setEquipment(EnumItemSlot.HEAD, new net.minecraft.server.v1_10_R1.ItemStack(net.minecraft.server.v1_10_R1.Item.getById(314)));
                    } else if(pet.getPetType() == PetType.WITHER_SKELETON) {
                        Skeleton skeleton = (Skeleton) pet.getEntity();
                        skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
                    }
                    pet.petFollowPlayer();
                    p.sendMessage(ChatColor.GRAY + "Your pet " + ChatColor.YELLOW + petType.name() + ChatColor.GRAY + " now follows close behind.");
                    e.setCancelled(true);
                    return;
                } else {
                    p.sendMessage(ChatColor.GRAY + "Sorry, you haven't unlocked pet: " + ChatColor.YELLOW + petType.name());
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if(e.getInventory().getTitle().contains("Pets")) e.setCancelled(true);
    }

    public void eraseCurrentPet(Player p) {
        Iterator iterator = this.petHashSet.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet)iterator.next();
            if(pet.getPetType() == PetType.COW) {
                p.removePotionEffect(PotionEffectType.REGENERATION);
            }
            if(pet.getOwner().equals(p.getUniqueId())) {
                iterator.remove();
                pet.kill();
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Pet pet = this.getCurrentPet(e.getPlayer());
        if(pet != null && pet.getPetType() == PetType.ASTRONAUT) {
            for(String s : this.ASTRONAUT_ALLOWED_WORLDS) {
                if(e.getFrom().getName().equals(s)) {
                    e.getPlayer().setAllowFlight(false);
                    e.getPlayer().setFlying(false);
                }
                if(e.getPlayer().getWorld().getName().equals(s)) {
                    e.getPlayer().setAllowFlight(true);
                    e.getPlayer().setFlying(true);
                }
            }
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private Pet getCurrentPet(Player p) {
        for(Pet pet : this.petHashSet) {
            if(pet.getOwner().equals(p.getUniqueId())) {
                return pet;
            }
        }
        return null;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Pet pet = this.getCurrentPet(p);
        if(pet != null && pet.getPetType() == PetType.ASTRONAUT) {
            e.getPlayer().setAllowFlight(false);
        }
        this.eraseCurrentPet(p);
    }

    public static boolean force_MobSpawning = false;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(CreatureSpawnEvent e){
        if (force_MobSpawning){
            e.setCancelled(false);
        }
    }

    @EventHandler(priority =  EventPriority.MONITOR)
    public void explode(BlockExplodeEvent e) {
        if(pluginExplosion) {
            e.setYield(2);
            Iterator iterator = e.blockList().iterator();
            while(iterator.hasNext()) {
                Block b = (Block) iterator.next();
                boolean build = getWorldGuard().getRegionManager(b.getWorld()).getApplicableRegions(b.getLocation()).testState(null, DefaultFlag.BLOCK_BREAK);
                if(b.getType() == Material.SAPLING || b.getType() == Material.LOG || b.getType() == Material.LOG_2) build = false;
                if(!build) {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        Pet pet = null;
        Iterator iterator = this.petHashSet.iterator();
        while (iterator.hasNext()) {
            Pet pet1 = (Pet) iterator.next();
            if (pet1.getEntity() == e.getEntity()) {
                pet = pet1;
            }
        }
        if (pet != null) {
            e.setCancelled(true);
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                if (pet.getOwner().equals(p.getUniqueId())) {
                    //PET EFFECTS
                    long cooldown = this.getCoolDown(p, pet.getPetType());
                    if (cooldown != 0) {
                        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "You have a cooldown on this effect " + ChatColor.RED + "(" + ChatColor.RED + this.formatTime(cooldown) + ")");
                        e.setCancelled(true);
                        return;
                    }
                    if (pet.getPetType() == PetType.SPIDER) {
                        if (!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + ChatColor.GREEN + "You must be in PVP to activate this effect!");
                            e.setCancelled(true);
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Web Spinner activated!");
                            for (UUID uuid : targets) {
                                Player p1 = Bukkit.getPlayer(uuid);
                                if (p1 != null) {
                                    if(!this.isInPVP(p1)) continue;
                                    final Location l = p1.getLocation().add(0, 1, 0);
                                    final Location l2 = p1.getLocation().add(1, 0, 0);
                                    final Location l3 = p1.getLocation().add(0, 0, 1);
                                    final Material type = l.getBlock().getType();
                                    final Material l2type = l2.getBlock().getType();
                                    final Material l3type = l3.getBlock().getType();
                                    l.getBlock().setType(Material.WEB);
                                    new BukkitRunnable() {
                                        public void run() {
                                            l.getBlock().setType(type);
                                            l2.getBlock().setType(l2type);
                                            l3.getBlock().setType(l3type);
                                        }
                                    }.runTaskLater(this, 40L);
                                }
                            }
                            this.setCooldown(p, pet.getPetType(), this.CD_SPIDER);
                        }
                    } else if (pet.getPetType() == PetType.CREEPER) {
                        this.pluginExplosion = true;
                        pet.getEntity().getWorld().createExplosion(pet.getEntity().getLocation().getX(), pet.getEntity().getLocation().getY(), pet.getEntity().getLocation().getZ(), 5, false, true);
                        this.setCooldown(p, pet.getPetType(), this.CD_CREEPER);
                        this.pluginExplosion = false;
                    } else if (pet.getPetType() == PetType.ZOMBIE) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        int size = 3;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.SPELL, 1);
                        }
                        size = 5;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.SPELL, 2);
                        }
                        size = 10;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.SPELL, 3);
                        }

                        for (Player p1 : Bukkit.getOnlinePlayers()) {
                            if (p1.getWorld() != pet.getEntity().getWorld()) continue;
                            if(this.isFriendly(p, p1)) continue;
                            if(!this.isInPVP(p1)) continue;
                            if (pet.getEntity().getLocation().distance(p1.getLocation()) <= 10) {
                                if (p1.getUniqueId().equals(pet.getOwner())) continue;
                                p1.setFoodLevel(3);
                            }
                        }
                        this.setCooldown(p, pet.getPetType(), this.CD_ZOMBIE);
                    } else if (pet.getPetType() == PetType.OCELOT) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        int size = 3;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.WATERDRIP, 1);
                        }
                        size = 5;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.WATERDRIP, 2);
                        }
                        size = 10;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            loc.getWorld().playEffect(loc, Effect.WATERDRIP, 3);
                        }
                        for (final Player p1 : Bukkit.getOnlinePlayers()) {
                            if (p1.getWorld() != pet.getEntity().getWorld()) continue;
                            if(this.isFriendly(p, p1)) continue;
                            if (p1.getUniqueId().equals(pet.getOwner())) continue;
                            if(!this.isInPVP(p1)) continue;
                            if (pet.getEntity().getLocation().distance(p1.getLocation()) <= 7) {
                                p1.setVelocity(p1.getLocation().getDirection().setY(0).multiply(-7));
                            }
                        }
                        this.setCooldown(p, pet.getPetType(), this.CD_OCELOT);
                    } else if (pet.getPetType() == PetType.PIG) {
                        if (p.getHealth() >= 13) {
                            p.setHealth(20);
                        } else {
                            p.setHealth(p.getHealth() + 7.5);
                        }
                        Location l1 = p.getLocation().add(0, 2, 0), l2 = pet.getEntity().getLocation().add(0, 1, 0);
                        l1.getWorld().playEffect(l1, Effect.HEART, 5);
                        l2.getWorld().playEffect(l2, Effect.HEART, 5);
                        this.setCooldown(p, PetType.PIG, this.CD_PIG);
                    } else if (pet.getPetType() == PetType.WITCH) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        int size = 2;
                        this.createHelix(pet.getEntity(), EnumParticle.SPELL_WITCH);
                        Random rdm = new Random();
                        PotionType[] potionTypes = new PotionType[]{PotionType.POISON, PotionType.WEAKNESS, PotionType.SLOWNESS};
                        for (int i = 0; i < 360; i = i + 60) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            ThrownPotion thrownPotion = (ThrownPotion) pet.getEntity().getWorld().spawnEntity(loc.add(0, 2, 0), EntityType.SPLASH_POTION);
                            thrownPotion.setShooter(p);
                            int o = rdm.nextInt(3);
                            thrownPotion.setItem(genCustomPotion(potionTypes[o], 1, 1, true, false));
                        }
                        size = 5;
                        for (int i = 0; i < 360; i = i + 90) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 1, z);
                            ThrownPotion thrownPotion = (ThrownPotion) pet.getEntity().getWorld().spawnEntity(loc.add(0, 2, 0), EntityType.SPLASH_POTION);
                            thrownPotion.setShooter(p);
                            int o = rdm.nextInt(3);
                            thrownPotion.setItem(genCustomPotion(potionTypes[o], 1, 1, true, false));
                        }
                        this.setCooldown(p, pet.getPetType(), this.CD_WITCH);
                    } else if (pet.getPetType() == PetType.RABBIT) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        for (Player p1 : Bukkit.getOnlinePlayers()) {
                            if (p1.getWorld() != pet.getEntity().getWorld()) continue;
                            if(this.isFriendly(p, p1)) continue;
                            if(!this.isInPVP(p1)) continue;
                            if(p1.getUniqueId().equals(pet.getOwner())) continue;
                            if(pet.getEntity().getLocation().distance(p1.getLocation()) <= 7) {
                                p1.setVelocity(p1.getVelocity().setY(1));
                                this.createHelix(p1, EnumParticle.ENCHANTMENT_TABLE);
                            }
                        }
                        this.setCooldown(p, pet.getPetType(), this.CD_RABBIT);
                    } else if (pet.getPetType() == PetType.WOLF) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Release the Hounds activated!");
                            for (UUID uuid : targets) {
                                Player p1 = Bukkit.getPlayer(uuid);
                                if (p1 != null) {
                                    if(!this.isInPVP(p1)) continue;
                                    if (pet.getEntity().getLocation().distance(p1.getLocation()) <= 10) {
                                        if (p1.getUniqueId().equals(pet.getOwner())) continue;
                                        this.force_MobSpawning = true;
                                        Wolf wolf = (Wolf) pet.getEntity().getWorld().spawnEntity(pet.getEntity().getLocation(), EntityType.WOLF);
                                        wolf.playEffect(EntityEffect.FIREWORK_EXPLODE);
                                        wolf.playEffect(EntityEffect.WOLF_SHAKE);
                                        wolf.playEffect(EntityEffect.WOLF_SMOKE);
                                        wolf.setAngry(true);
                                        wolf.setCollarColor(DyeColor.RED);
                                        wolf.setTarget((LivingEntity) p1);
                                        wolf.damage(0, p1);
                                        Wolf wolf2 = (Wolf) pet.getEntity().getWorld().spawnEntity(pet.getEntity().getLocation(), EntityType.WOLF);
                                        wolf2.playEffect(EntityEffect.FIREWORK_EXPLODE);
                                        wolf2.playEffect(EntityEffect.WOLF_SHAKE);
                                        wolf2.playEffect(EntityEffect.WOLF_SMOKE);
                                        wolf2.setAngry(true);
                                        wolf2.setCollarColor(DyeColor.BLACK);
                                        wolf2.setTarget((LivingEntity) p1);
                                        wolf2.damage(0, p1);
                                        this.force_MobSpawning = false;
                                    }
                                }
                            }
                            this.setCooldown(p, pet.getPetType(), CD_WOLF);
                        }
                    } else if (pet.getPetType() == PetType.COW) {
                        for (PotionEffect potionEffect : p.getActivePotionEffects()) {
                            if (potionEffect.getType().getName().equals("POISON")) {
                                p.removePotionEffect(PotionEffectType.POISON);
                            } else if (potionEffect.getType().getName().equals("WEAKNESS")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, potionEffect.getDuration(), 0));
                                p.removePotionEffect(PotionEffectType.WEAKNESS);
                            } else if (potionEffect.getType().getName().equals("BLINDNESS")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, potionEffect.getDuration(), 0));
                                p.removePotionEffect(PotionEffectType.BLINDNESS);
                            } else if (potionEffect.getType().getName().equals("SLOW")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionEffect.getDuration(), 0));
                                p.removePotionEffect(PotionEffectType.SLOW);
                            }
                        }
                        this.setCooldown(p, pet.getPetType(), CD_COW);
                    } else if (pet.getPetType() == PetType.BLAZE) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        int size = 10;
                        for (int i = 0; i < 360; i++) {
                            double angle = (i * Math.PI / 180);
                            double x = size * Math.cos(angle);
                            double z = size * Math.sin(angle);
                            Location loc = pet.getEntity().getLocation().add(x, 0, z);
                            loc.getWorld().playEffect(loc, Effect.FLAME, 3);
                        }
                        for (final Player p1 : Bukkit.getOnlinePlayers()) {
                            if(p1.getWorld() != pet.getEntity().getWorld()) continue;
                            if(this.isFriendly(p, p1)) continue;
                            if(p1.getUniqueId().equals(pet.getOwner())) continue;
                            if(!this.isInPVP(p1)) continue;
                            if (pet.getEntity().getLocation().distance(p1.getLocation()) <= 10) {
                                p1.setFireTicks(80);
                                p1.playEffect(p1.getLocation(), Effect.LAVA_POP, 5);
                                p1.playEffect(p1.getLocation().add(0, 1, 0), Effect.LAVA_POP, 5);
                            }
                        }
                        this.setCooldown(p, pet.getPetType(), CD_BLAZE);
                    } else if (pet.getPetType() == PetType.ASTRONAUT) {
                    } else if (pet.getPetType() == PetType.ENDERMAN) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Teleport Near activated!");
                            for (UUID uuid : targets) {
                                Player p1 = Bukkit.getPlayer(uuid);
                                if (p1 != null) {
                                    for (BlockFace blockFace : BlockFace.values()) {
                                        switch(blockFace) {
                                            case SOUTH:
                                            case EAST:
                                            case NORTH:
                                            case WEST:
                                                p.getWorld().playEffect(p.getLocation().add(0, 1, 0).getBlock().getRelative(blockFace).getLocation(), Effect.ENDER_SIGNAL, 1);
                                        }
                                    }
                                    Vector inverseDirectionVec = p1.getLocation().getDirection().normalize().multiply(-1);
                                    Location to = p1.getLocation().clone().add(inverseDirectionVec);
                                    p.teleport(to);
                                    for (BlockFace blockFace : BlockFace.values()) {
                                        switch(blockFace) {
                                            case SOUTH:
                                            case EAST:
                                            case NORTH:
                                            case WEST:
                                                p.getWorld().playEffect(p.getLocation().add(0, 1, 0).getBlock().getRelative(blockFace).getLocation(), Effect.ENDER_SIGNAL, 1);
                                        }
                                    }
                                    break;
                                }
                                p1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true));
                            }
                            this.setCooldown(p, pet.getPetType(), CD_ENDERMAN);
                        }
                    } else if (pet.getPetType() == PetType.SKELETON) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Arrow Rain activated!");
                            for (UUID uuid : targets) {
                                final Player p1 = Bukkit.getPlayer(uuid);
                                if (p1 != null) {
                                    if(!this.isInPVP(p1)) continue;
                                    Location l1 = p1.getLocation().add(0, 6, 0);
                                    Arrow arrow = l1.getWorld().spawnArrow(l1, new Vector(0, 5, 0), 1f, 1f);
                                    arrow.setKnockbackStrength(2);
                                    arrow.setCritical(true);
                                    for (BlockFace face : BlockFace.values()) {
                                        Location l = p1.getLocation().add(0, 5, 0).getBlock().getRelative(face).getLocation();
                                        final Arrow arrow1 = l.getWorld().spawnArrow(l, new Vector(0, -5, 0), 1f, 1f);
                                        arrow1.setKnockbackStrength(2);
                                        arrow1.setCritical(true);
                                        new BukkitRunnable() {
                                            public void run() {
                                                arrow1.remove();
                                            }
                                        }.runTaskLater(this, 20L);
                                    }
                                }
                            }
                            this.setCooldown(p, pet.getPetType(), CD_SKELETON);
                        }
                    } else if (pet.getPetType() == PetType.CHARGED_CREEPER) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Explode Enemies activated!");
                            for (UUID uuid : targets) {
                                final Player p1 = Bukkit.getPlayer(uuid);
                                if(!this.isInPVP(p1)) continue;
                                if (p1 != null) {
                                    this.pluginExplosion = true;
                                    for (int i = 0; i < 4; i++) {
                                        p1.getWorld().createExplosion(p1.getLocation().add(0, i, 0), 3L, false);
                                    }
                                    p1.damage(8.5D);
                                    this.pluginExplosion = false;
                                }
                            }
                            this.setCooldown(p, pet.getPetType(), CD_CHARGED_CREEPER);
                        }
                    } else if (pet.getPetType() == PetType.CAVE_SPIDER) {
                        if(!this.isInPVP(p)) {
                            p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                            return;
                        }
                        List<UUID> targets = this.getTargets(p);
                        if (targets == null) {
                            p.sendMessage(this.prefix + "You do not have any targets.");
                        } else {
                            p.sendMessage(this.prefix + "Poison & Blind Enemies activated!");
                            for (UUID uuid : targets) {
                                    final Player p1 = Bukkit.getPlayer(uuid);
                                    if(!this.isInPVP(p1)) continue;
                                    if (p1 != null) {
                                        p1.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, true));
                                        p1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true));
                                    }
                                }
                            this.setCooldown(p, pet.getPetType(), CD_CAVE_SPIDER);
                            }
                            } else if (pet.getPetType() == PetType.WITHER_SKELETON) {
                            } else if (pet.getPetType() == PetType.SHEEP) {
                                if(!this.isInPVP(p)) {
                                    p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                                    return;
                                }
                                List<UUID> targets = this.getTargets(p);
                                if (targets == null) {
                                    p.sendMessage(this.prefix + "You do not have any targets.");
                                } else {
                                    p.sendMessage(this.prefix + "Switch activated!");
                                    for (UUID uuid : targets) {
                                        final Player p1 = Bukkit.getPlayer(uuid);
                                        if (p1 != null) {
                                            if(!this.isInPVP(p1)) continue;
                                            final Location p_l = p.getLocation(), p1_l = p1.getLocation();
                                            Vector start = p_l.clone().add(0, 1, 0).toVector();
                                            Vector target = p1_l.toVector();
                                            Set<Vector> lineBlocks = calcLine(start, target);
                                            for (Vector v : lineBlocks) {
                                                Location l = new Location(p_l.getWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ());
                                                l.getWorld().playEffect(l, Effect.COLOURED_DUST, 1);
                                            }
                                            p.teleport(p1_l);
                                            p1.teleport(p_l);
                                            break;
                                        }
                                    }
                                    this.setCooldown(p, pet.getPetType(), CD_SHEEP);
                                }
                            } else if (pet.getPetType() == PetType.CHICKEN) {
                                if(!this.isInPVP(p)) {
                                    p.sendMessage(this.prefix + "You must be in a pvp enabled area to use this!");
                                    return;
                                }
                                List<UUID> targets = this.getTargets(p);
                                if (targets == null) {
                                    p.sendMessage(this.prefix + "You do not have any targets.");
                                } else {
                                    p.sendMessage(this.prefix + "Egg Attack activated.");
                                    for (UUID uuid : targets) {
                                        final Player p1 = Bukkit.getPlayer(uuid);
                                        if (p1 != null) {
                                            final Pet pet1 = pet;
                                            for (int i = 0; i < 20; i++) {
                                                new BukkitRunnable() {
                                                    public void run() {
                                                        if (p1 != null) {
                                                            if (pet1 != null) {
                                                                Egg egg = (Egg) pet1.getEntity().getLocation().getWorld().spawnEntity(pet1.getEntity().getLocation().add(0, 1, 0), EntityType.ARROW);
                                                                Vector direction = p1.getLocation().toVector().subtract(egg.getLocation().toVector()).normalize();
                                                                egg.setVelocity(new Vector(0, 5, 0));
                                                            }
                                                        }
                                                    }
                                                }.runTaskLater(this, i);
                                            }
                                        }
                                    }
                                    this.setCooldown(p, pet.getPetType(), CD_CHICKEN);
                                }
                            }
                        }
                    }
                }
            }

    public static ItemStack genCustomPotion(PotionType potionType, int lvl, int amt, boolean splash, boolean extended) {
        Potion potion = new Potion(potionType);
        potion.setSplash(splash);
        potion.setLevel(lvl);
        if(extended) {
            potion.setHasExtendedDuration(extended);
        }
        return potion.toItemStack(amt);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(p.isFlying()) return;
        Pet pet = this.getCurrentPet(p);
        if(pet != null && pet.getPetType() == PetType.CHICKEN) {
            if(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && !hovering(p.getLocation())) {
                Vector dir = p.getLocation().getDirection()
                        .multiply(0.25);
                p.setVelocity(new Vector(dir.getX(), p.getVelocity()
                        .getY() * 0.5, dir.getZ()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLightningStrike(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            Pet pet = this.getCurrentPet(p);
            if(pet != null && pet.getPetType() == PetType.CHARGED_CREEPER) {
                if(this.isPet(e.getEntity())) return;
                int r = random.nextInt(100) + 1;
                if((e.getEntity() instanceof Player)) {
                    if(r <= 5) {
                        e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());
                        ((Player) e.getEntity()).damage(3);
                        e.getEntity().setFireTicks(80);
                    }
                } else if(r <= 25) {
                    e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());
                    e.setDamage(e.getDamage()*2);
                    e.getEntity().setFireTicks(80);
                }
            }
        }
    }

    private HashSet<Vector> calcLine(Vector vec1, Vector vec2) {
        HashSet<Vector> vectors = new HashSet<Vector>();
        double length = vec1.distance(vec2) - 1;
        int points = (int) (length / 1.0D) + 1;
        double gap = length / (points - 1);
        Vector gapVector = vec2.clone().subtract(vec1).normalize().multiply(gap);
        for (int i = 1; i < points; i++) {
            vectors.add(vec1.clone().add(gapVector.clone().multiply(i)));
        }
        return vectors;
    }

    private boolean isPet(Entity e) {
        Iterator iterator = petHashSet.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet)iterator.next();
            if(pet.getEntity() == e) return true;
        }
        return false;
    }

    private boolean isFriendly(Player p, Player p1) {
        MPlayer mPlayer = MPlayer.get(p);
        MPlayer mPlayer1 = MPlayer.get(p1);

        if(!mPlayer.hasFaction() && !mPlayer1.hasFaction()) return false;

        return mPlayer.getRelationTo(mPlayer1).isFriend();
    }

    @EventHandler
    public void playerHitByArrow(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Pet pet = this.getCurrentPet(p);
            if(pet != null && pet.getPetType() == PetType.SKELETON && e.getDamager() instanceof Arrow) {
                e.setDamage(e.getDamage()/2);
            } else if(pet != null && pet.getPetType() == PetType.ENDERMAN && e.getDamager() instanceof Arrow) {
                e.setCancelled(true);
                p.launchProjectile(Arrow.class);
            }
        }
    }
    @EventHandler
    public void fallDamage(EntityDamageEvent e) {
        if(e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity() instanceof Player) {
            Player p = (Player)e.getEntity();
            Pet pet = this.getCurrentPet(p);
            if(pet != null && pet.getPetType() == PetType.CHICKEN) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void petDamage(EntityDamageEvent e) {
        Iterator iterator = this.petHashSet.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet)iterator.next();
            if(pet.getEntity() == e.getEntity()) {
                e.setCancelled(true);
            }
        }
    }

    private boolean hovering(Location loc) {
        Block lower = loc.getBlock().getRelative(BlockFace.DOWN);
        if(lower.getRelative(BlockFace.NORTH).getType() != Material.AIR ||
                lower.getRelative(BlockFace.SOUTH).getType() != Material.AIR ||
                lower.getRelative(BlockFace.EAST).getType() != Material.AIR ||
                lower.getRelative(BlockFace.WEST).getType() != Material.AIR ||
                lower.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            return true;
        }
        return false;
    }
    @EventHandler
    private boolean isInPVP(Player p) {
        RegionManager regionManager = getWorldGuard().getRegionManager(p.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(p.getLocation());
        boolean pvp = applicableRegionSet.testState(null, DefaultFlag.PVP);

        return pvp;
    }

    @EventHandler
    public void onPotionThrow(EntitySpawnEvent e) {
        if(pluginExplosion && e.getEntity().getType() == EntityType.SPLASH_POTION) {
            Entity splash = e.getEntity();
        }
    }

    public void createHelix(Entity entity, EnumParticle type) {
        Location loc = entity.getLocation();
        int radius = 1;
        for (double y = 0; y <= 50; y += 0.05) {
            double x = radius * Math.cos(y);
            double z = radius * Math.sin(y);
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(type, true, (float) (loc.getX() + x), (float) (loc.getY() + y), (float) (loc.getZ() + z), 0F, 0F, 0F, 0F, 1);
            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    int ct = 0;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSit(PlayerInteractAtEntityEvent e) {
        if(ct == 0) {
            ct++;
            return;
        } else {
            ct = 0;
        }
        Player p = e.getPlayer();
        Pet pet = this.getCurrentPet(e.getPlayer());
        if(pet != null) {
            if(pet.getEntity() == e.getRightClicked()) {
                if(pet.getPetType() == PetType.ASTRONAUT) {
                    e.setCancelled(true);
                } else {
                    String phrase = pet.isFrozen() ? ChatColor.YELLOW + "" + ChatColor.ITALIC + "*WHISTLE* Follow me." : ChatColor.YELLOW + "" + ChatColor.ITALIC + "*WHISTLE* Sit.";
                    pet.toggleFrozen();
                    if (pet.getPetType() == PetType.WOLF) {
                        Wolf wolf = (Wolf) pet.getEntity();
                        wolf.setSitting(pet.isFrozen());
                    }
                    p.sendMessage(phrase);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerTarget(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        Entity damager = e.getDamager();
        Entity entity = e.getEntity();
        if(damager instanceof Player && entity instanceof Player) {
            Player damagerPlayer = (Player) damager;
            Player entityPlayer = (Player) entity;
            HashMap<UUID, Long> map = new HashMap<UUID, Long>();
            map.put(entityPlayer.getUniqueId(), System.currentTimeMillis() + this.PVP_TIMEOUT_MS);
            HashMap<UUID, Long> map2 = new HashMap<UUID, Long>();
            map2.put(damagerPlayer.getUniqueId(), System.currentTimeMillis() + this.PVP_TIMEOUT_MS);
            this.target.put(damagerPlayer.getUniqueId(), map);
            this.target.put(entityPlayer.getUniqueId(), map2);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerTargetBow(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if(e.getDamager() instanceof Arrow && e.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) e.getDamager();
            Player entityPlayer = (Player) e.getEntity();
            if(arrow.getShooter() instanceof Player) {
                Player damagerPlayer = (Player) arrow.getShooter();
                HashMap<UUID, Long> map = new HashMap<UUID, Long>();
                map.put(entityPlayer.getUniqueId(), System.currentTimeMillis() + this.PVP_TIMEOUT_MS);
                this.target.put(damagerPlayer.getUniqueId(), map);
                HashMap<UUID, Long> map2 = new HashMap<UUID, Long>();
                map.put(damagerPlayer.getUniqueId(), System.currentTimeMillis() + this.PVP_TIMEOUT_MS);
                this.target.put(entityPlayer.getUniqueId(), map2);
            }
        }
    }

    @EventHandler
    public void playerHarmEvent(EntityDamageEvent event) {
        org.bukkit.entity.Entity e = event.getEntity();
        if (pluginExplosion) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Random random = new Random();
        Pet pet = this.getCurrentPet(e.getPlayer());
        if(pet != null) {
            if(pet.getPetType() == PetType.CREEPER) {
                int i = random.nextInt(100) + 1;
                if(i == 1) {
                    this.pluginExplosion = true;
                    e.getPlayer().getWorld().createExplosion(e.getPlayer().getLocation().getX(), e.getPlayer().getLocation().getY(), e.getPlayer().getLocation().getZ(), 3, false, true);
                    this.pluginExplosion = false;
                }
            } else if(pet.getPetType() == PetType.SPIDER) {

            }
        }
    }

    public void dropBlocks(List<Location> locations) {
        Iterator iterator = locations.iterator();
        while(iterator.hasNext()) {
            Location l = (Location) iterator.next();
            Block b = l.getBlock();
            l.getWorld().dropItem(l, new ItemStack(b.getType(), 2));
            b.setType(Material.AIR);
        }
    }

    public static List<Location> generateSphere(Location centerBlock, int radius, boolean hollow) {

        List<Location> circleBlocks = new ArrayList<Location>();

        int bx = centerBlock.getBlockX();
        int by = centerBlock.getBlockY();
        int bz = centerBlock.getBlockZ();

        for(int x = bx - radius; x <= bx + radius; x++) {
            for(int y = by - radius; y <= by + radius; y++) {
                for(int z = bz - radius; z <= bz + radius; z++) {

                    double distance = ((bx-x) * (bx-x) + ((bz-z) * (bz-z)) + ((by-y) * (by-y)));

                    if(distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {

                        Location l = new Location(centerBlock.getWorld(), x, y, z);

                        circleBlocks.add(l);

                    }

                }
            }
        }

        return circleBlocks;
    }

    public String formatTime(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;

        second += 1;

        String time = String.format("%02dm%02ds", minute, second);
        return time;
    }

    private ArrayList<UUID> getTargets(Player p) {
        HashMap<UUID, Long> map = this.target.get(p.getUniqueId());
        if(map == null) {
            return null;
        } else {
            ArrayList<UUID> uuids = new ArrayList<UUID>();
            Iterator iterator = map.keySet().iterator();
            while(iterator.hasNext()) {
                UUID uuid = (UUID) iterator.next();
                uuids.add(uuid);
            }
            return uuids;
        }
    }
}