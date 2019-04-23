package com.playmonumenta.scriptedquests.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.quests.QuestNpc;
import com.playmonumenta.scriptedquests.utils.FileUtils;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;
import com.playmonumenta.scriptedquests.utils.MetadataUtils;

public class QuestNpcManager {
	private final Plugin mPlugin;
	private final Map<EntityType, Map<String, QuestNpc>> mNpcs = new HashMap<EntityType, Map<String, QuestNpc>>();
	private final EnumSet<EntityType> mEntityTypes = EnumSet.noneOf(EntityType.class);

	/*
	 * If sender is non-null, it will be sent debugging information
	 */
	public void reload(Plugin plugin, CommandSender sender) {
		String npcsLocation = plugin.getDataFolder() + File.separator +  "npcs";
		mNpcs.clear();
		mEntityTypes.clear();
		ArrayList<File> listOfFiles;
		ArrayList<String> listOfNpcs = new ArrayList<String>();
		int numComponents = 0;
		int numFiles = 0;

		// Attempt to load all JSON files in subdirectories of "npcs"
		try {
			File directory = new File(npcsLocation);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			listOfFiles = FileUtils.getFilesInDirectory(npcsLocation, ".json");
		} catch (IOException e) {
			plugin.getLogger().severe("Caught exception trying to reload NPCs: " + e);
			if (sender != null) {
				sender.sendMessage(ChatColor.RED + "Caught exception trying to reload NPCs: " + e);
			}
			return;
		}

		Collections.sort(listOfFiles);
		for (File file : listOfFiles) {
			try {
				// Load this file into a QuestNpc object
				QuestNpc npc = new QuestNpc(file.getPath());

				// Keep track of statistics for pretty printing later
				int newComponents = npc.getComponents().size();
				numComponents += newComponents;
				numFiles++;
				listOfNpcs.add(npc.getNpcName() + ":" + Integer.toString(newComponents));

				// Track this type of entity from now on when entities are interacted with
				EntityType type = npc.getEntityType();
				mEntityTypes.add(type);

				// Check if an existing NPC already exists with quest components
				Map<String, QuestNpc> entityNpcMap = mNpcs.get(type);
				if (entityNpcMap == null) {
					// This is the first NPC of this type - create the map for it
					entityNpcMap = new HashMap<String, QuestNpc>();
					mNpcs.put(type, entityNpcMap);
				}

				QuestNpc existingNpc = entityNpcMap.get(npc.getNpcName());
				if (existingNpc != null) {
					// Existing NPC - add the new quest components to it
					existingNpc.addFromQuest(plugin, npc);
				} else {
					entityNpcMap.put(npc.getNpcName(), npc);
				}
			} catch (Exception e) {
				plugin.getLogger().severe("Caught exception: " + e);
				e.printStackTrace();

				if (sender != null) {
					sender.sendMessage(ChatColor.RED + "Failed to load quest file '" + file.getPath() + "'");
					MessagingUtils.sendStackTrace(sender, e);
				}
			}
		}

		if (sender != null) {
			sender.sendMessage(ChatColor.GOLD + "Loaded " +
							   Integer.toString(numComponents) +
							   " quest components from " + Integer.toString(numFiles) + " files");

			if (numFiles <= 20) {
				Collections.sort(listOfNpcs);
				String outMsg = "";
				for (String npc : listOfNpcs) {
					if (outMsg.isEmpty()) {
						outMsg = npc;
					} else {
						outMsg = outMsg + ", " + npc;
					}

					if (outMsg.length() > 1000) {
						sender.sendMessage(ChatColor.GOLD + outMsg);
						outMsg = "";
					}
				}

				if (!outMsg.isEmpty()) {
					sender.sendMessage(ChatColor.GOLD + outMsg);
				}
			}
		}
	}

	public QuestNpcManager(Plugin plugin) {
		mPlugin = plugin;
		reload(plugin, null);
	}

	public QuestNpc getInteractNPC(String npcName, EntityType entityType) {
		// Only search for the entity's name if we have a quest with that entity type
		if (!mEntityTypes.contains(entityType)) {
			return null;
		}

		// Only entities with custom names
		if (npcName == null || npcName.isEmpty()) {
			return null;
		}

		// Return the NPC if we have an NPC with that name
		Map<String, QuestNpc> entityNpcMap = mNpcs.get(entityType);
		if (entityNpcMap == null) {
			mPlugin.getLogger().severe("BUG! EntityTypes contains type '" +
			                          entityType.toString() + "' but there is no map for it!");
			return null;
		} else {
			QuestNpc npc = entityNpcMap.get(QuestNpc.squashNpcName(npcName));
			if (npc != null) {
				return npc;
			}
		}

		return null;
	}

	/*
	 * Note: npcEntity might be null
	 */
	public boolean interactEvent(Plugin plugin, Player player, String npcName, EntityType entityType, Entity npcEntity, boolean force) {
		QuestNpc npc = getInteractNPC(npcName, entityType);
		if (npc != null) {
			return interactEvent(plugin, player, npcName, entityType, npcEntity, npc, force);
		}
		return false;
	}

	/*
	 * Note: npcEntity might be null
	 */
	public boolean interactEvent(Plugin plugin, Player player, String npcName, EntityType entityType, Entity npcEntity, QuestNpc npc, boolean force) {
		// Only one interaction per player per tick
		if (!force && !MetadataUtils.checkOnceThisTick(plugin, player, "ScriptedQuestsNPCInteractNonce")) {
			return false;
		}

		// Players who are racing can not interact with NPCs
		if (plugin.mRaceManager.isRacing(player)) {
			return false;
		}

		if (npc != null) {
			return npc.interactEvent(plugin, player, QuestNpc.squashNpcName(npcName), entityType, npcEntity);
		}
		return false;
	}
}