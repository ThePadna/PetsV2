package gui;

import main.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import pet.PetType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Cory on 23/05/2017.
 */
public class PetPicker {

    public PetPicker() {
    }

    public static void openPetMenu(Player p) {
        Inventory i = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Pets Menu");
        for(int x = 0; x <= PetType.values().length-1; x++) {
            PetType petType = PetType.values()[x];
            if(Data.hasPet(p, petType)) {
                i.setItem(x, getPetTypeICON(petType));
            } else {
                ItemStack i1 = new ItemStack(Material.IRON_FENCE, 1);
                ItemMeta itemMeta = i1.getItemMeta();
                itemMeta.setDisplayName(ChatColor.ITALIC +  "" + ChatColor.BOLD + petType.name());
                i1.setItemMeta(itemMeta);
                i.setItem(x, i1);
            }
        }
        ItemStack cancel = new ItemStack(Material.BARRIER, 1);
        ItemMeta itemMeta = cancel.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "Remove Pet");
        cancel.setItemMeta(itemMeta);
        i.setItem(31, cancel);
        p.openInventory(i);
    }

    private static ItemStack getPetTypeICON(PetType petType) {
        ItemStack i = new ItemStack(Material.IRON_FENCE);
        List<String> lore = new ArrayList<String>();
        if(petType == PetType.ASTRONAUT) {
            i.setType(Material.GLASS);
            lore.add(ChatColor.WHITE + "Passive: Jump Boost 2");
            lore.add(ChatColor.WHITE + "Action: Fly in Free/End/Nether");
        } else if(petType == PetType.BLAZE) {
            i.setType(Material.BLAZE_POWDER);
            lore.add(ChatColor.RED + "Passive: Fire Resistance");
            lore.add(ChatColor.RED + "Action: Molten Burst AoE (Radius: 7)");
        } else if(petType == PetType.CAVE_SPIDER) {
            i.setType(Material.STRING);
            lore.add(ChatColor.DARK_BLUE + "Passive: Poison Bite (6s)");
            lore.add(ChatColor.DARK_BLUE + "Action: Poison & Blind Enemies (5s)");
        } else if(petType == PetType.SPIDER) {
            i.setType(Material.SPIDER_EYE);
            lore.add(ChatColor.RED + "Passive: Bite (3s)");
            lore.add(ChatColor.RED + "Action: Web Spinner");
        } else if(petType == PetType.CREEPER) {
            i.setType(Material.SULPHUR);
            lore.add(ChatColor.GREEN + "Passive: Explosive Mine");
            lore.add(ChatColor.GREEN + "Action: Blast Mine (Radius: 5)");
        } else if(petType == PetType.CHICKEN) {
            i.setType(Material.FEATHER);
            lore.add(ChatColor.DARK_GREEN + "Passive: Glide, No fall damage");
            lore.add(ChatColor.DARK_GREEN + "Action: No Action");
        } else if(petType == PetType.PIG) {
            i.setType(Material.PORK);
            lore.add(ChatColor.LIGHT_PURPLE + "Passive: Feeding Time");
            lore.add(ChatColor.LIGHT_PURPLE + "Action: Health (3.5)");
        } else if(petType == PetType.ENDERMAN) {
            i.setType(Material.ENDER_PEARL);
            lore.add(ChatColor.DARK_PURPLE + "Passive: Arrow Reflect, Scared of the Light");
            lore.add(ChatColor.DARK_PURPLE + "Action: Teleport Behind PVP target (+3sec blind)");
        } else if(petType == PetType.WITHER_SKELETON) {
            i = new ItemStack(Material.SKULL_ITEM, 1, (byte) 1);
            lore.add(ChatColor.DARK_PURPLE + "Passive: -");
            lore.add(ChatColor.DARK_PURPLE + "Action: -");
        } else if(petType == PetType.CHARGED_CREEPER) {
            i.setType(Material.SULPHUR);
            i.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
            lore.add(ChatColor.GREEN + "Passive: PVP Lightning Strike (5% chance), Mob (25% chance)");
            lore.add(ChatColor.GREEN + "Action: Explode Enemies");
        } else if(petType == PetType.SKELETON) {
            i.setType(Material.BOW);
            lore.add(ChatColor.DARK_AQUA + "Passive: Halves damage dealt by arrows");
            lore.add(ChatColor.DARK_AQUA + "Action: Arrow Rain");
        } else if(petType == PetType.ZOMBIE) {
            i.setType(Material.ROTTEN_FLESH);
            lore.add(ChatColor.GREEN + "Passive: Regeneration II at night");
            lore.add(ChatColor.GREEN + "Action: Hunger AoE (Radius: 10)");
        } else if(petType == PetType.COW) {
            i.setType(Material.RAW_BEEF);
            lore.add(ChatColor.WHITE + "Passive: Regeneration I");
            lore.add(ChatColor.WHITE + "Action: Reverse Potion Effects (Negative->Positive)");
        } else if(petType == PetType.SHEEP) {
            i.setType(Material.WOOL);
            lore.add(ChatColor.LIGHT_PURPLE + "Passive: Speed II");
            lore.add(ChatColor.LIGHT_PURPLE + "Action: Switch places with an enemy.");
        } else if(petType == PetType.OCELOT) {
            i.setType(Material.RAW_FISH);
            lore.add(ChatColor.GOLD + "Passive: Speed I");
            lore.add(ChatColor.GOLD + "Action: Repel Near");
        } else if(petType == PetType.WITCH) {
            i.setType(Material.POTION);
            lore.add(ChatColor.DARK_PURPLE + "Passive: Negative Effect Prevention for all Allies in 10 block Radius");
            lore.add(ChatColor.DARK_PURPLE + "Action: Potion Barrage AoE (Radius: 5), friendlies get effects prevented");
        } else if(petType == PetType.RABBIT) {
            i.setType(Material.RABBIT_FOOT);
            lore.add(ChatColor.YELLOW + "Passive: Jump I");
            lore.add(ChatColor.YELLOW + "Action: Jumper AoE (Radius: 7)");
        } else if(petType == PetType.WOLF) {
            i.setType(Material.BONE);
            lore.add(ChatColor.WHITE + "Passive: Strength I");
            lore.add(ChatColor.WHITE + "Action: Release the Hounds");
        }
        ItemMeta itemMeta = i.getItemMeta();
        itemMeta.setDisplayName(ChatColor.ITALIC + "" + ChatColor.BOLD + petType.name());
        itemMeta.setLore(lore);
        i.setItemMeta(itemMeta);
        return i;
    }
}
