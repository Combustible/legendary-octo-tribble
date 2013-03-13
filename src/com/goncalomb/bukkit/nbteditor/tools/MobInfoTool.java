package com.goncalomb.bukkit.nbteditor.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.material.MaterialData;

import com.goncalomb.bukkit.customitems.api.CustomItem;
import com.goncalomb.bukkit.customitems.api.PlayerDetails;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import com.goncalomb.bukkit.nbteditor.nbt.VillagerNBT;
import com.goncalomb.bukkit.nbteditor.nbt.VillagerNBTOffer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariableContainer;

public class MobInfoTool extends CustomItem {

	public MobInfoTool() {
		super("mob-info", ChatColor.AQUA + "Mob Inspector", new MaterialData(Material.STICK));
		setLore(ChatColor.YELLOW + "Right-click on entities to see their information.");
	}
	
	@Override
	public  void onInteractEntity(PlayerInteractEntityEvent event, PlayerDetails details) {
		Player player = event.getPlayer();
		
		if (!player.hasPermission("nbt.misc")) {
			player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		Entity entity = event.getRightClicked();
		if (EntityNBT.isValidType(entity.getType())) {
			EntityNBT entityNBT = EntityNBT.fromEntity(entity);
			player.sendMessage(ChatColor.YELLOW + "Information about " + entity.getType().getName() + "");
			for (NBTVariableContainer vc : entityNBT.getAllVariables()) {
				player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + vc.getName() + ":");
				for (NBTVariable var : vc) {
					String value = var.getValue();
					player.sendMessage("  " + ChatColor.AQUA + var.getName() + ": " + ChatColor.WHITE + (value != null ? value : ChatColor.ITALIC + "none"));
				}
			}
			player.sendMessage(ChatColor.YELLOW + "Extra information:");
			
			boolean extra = false;
			if (entityNBT instanceof MobNBT) {
				float[] chances = ((MobNBT) entityNBT).getDropChances();
				String[] names = { "head", "torso", "legs", "feet", "hand" };
				player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + "Drop chance:");
				for (int i = 0; i < 5; ++i) {
					player.sendMessage("  " + ChatColor.AQUA + names[i] + ": " + ChatColor.WHITE + chances[i]);
				}
				extra = true;
			}
			if (entityNBT instanceof VillagerNBT) {
				VillagerNBT villagerNBT = (VillagerNBT) entityNBT;
				player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + "Trades done:");
				int i = 1;
				for (VillagerNBTOffer offer : villagerNBT.getOffers()) {
					player.sendMessage("  " + ChatColor.AQUA + "trade " + i + ": " + ChatColor.WHITE + offer.getUses());
					++i;
				}
				extra = true;
			}
			if (!extra) {
				player.sendMessage("none");
			}
			event.setCancelled(true);
		} else {
			player.sendMessage(ChatColor.RED + "Not a valid entity!");
		}
	}
	
	@Override
	public void onDrop(PlayerDropItemEvent event) {
		event.getItemDrop().remove();
	}

}
