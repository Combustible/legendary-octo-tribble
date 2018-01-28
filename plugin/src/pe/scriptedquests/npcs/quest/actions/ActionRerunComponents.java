package pe.scriptedquests.npcs.quest.actions;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import pe.scriptedquests.Plugin;

public class ActionRerunComponents implements ActionBase {
	String mNpcName;
	EntityType mEntityType;
	boolean mLocked = false;

	public ActionRerunComponents(String npcName, EntityType entityType) {
		mNpcName = npcName;
		mEntityType = entityType;
	}

	@Override
	public void doAction(Plugin plugin, Player player) {
		/*
		 * Prevent infinite loops by preventing this specific action
		 * from running itself again
		 */
		if (!mLocked) {
			mLocked = true;
			plugin.mNpcManager.interactEvent(plugin, player, mNpcName, mEntityType);
			mLocked = false;
		} else {
			plugin.getLogger().severe("Stopped infinite loop for NPC '" + mNpcName + "'");
		}
	}
}
