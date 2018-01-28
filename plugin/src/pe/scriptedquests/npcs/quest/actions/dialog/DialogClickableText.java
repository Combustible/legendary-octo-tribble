package pe.scriptedquests.npcs.quest.actions.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.gson.JsonElement;

import pe.scriptedquests.Constants;
import pe.scriptedquests.Plugin;
import pe.scriptedquests.point.AreaBounds;
import pe.scriptedquests.point.Point;

public class DialogClickableText implements DialogBase {
	private ArrayList<DialogClickableTextEntry> mEntries = new ArrayList<DialogClickableTextEntry>();

	public DialogClickableText(String npcName, String displayName,
	                           EntityType entityType, JsonElement element) throws Exception {
		/*
		 * Integer used to determine which of the available clickable entries was
		 * clicked when a player clicks a chat message
		 * Choosing a random 32-bit starting number is good enough to prevent collisions
		 * Doesn't actually matter from a security perspective if collisions do happen,
		 *  it'd just mean you could click dialog from a different NPC way up in chat
		 *  and it'd potentially trigger a current conversation
		 */
		int entryIdx = (new Random()).nextInt();

		if (element.isJsonObject()) {
			mEntries.add(new DialogClickableTextEntry(npcName, displayName, entityType, element, entryIdx));
		} else if (element.isJsonArray()) {
			Iterator<JsonElement> iter = element.getAsJsonArray().iterator();
			while (iter.hasNext()) {
				mEntries.add(new DialogClickableTextEntry(npcName, displayName, entityType, iter.next(), entryIdx));

				entryIdx++;
			}
		} else {
			throw new Exception("clickable_text value is neither an object nor an array!");
		}
	}

	@Override
	public void sendDialog(Plugin plugin, Player player) {
		for (DialogClickableTextEntry entry : mEntries) {
			entry.sendDialog(plugin, player);
		}

		/*
		 * Attach the available clickable text options to the player so they can
		 * be decoded when the player clicks a message
		 */
		player.setMetadata(Constants.PLAYER_CLICKABLE_DIALOG_METAKEY,
		                   new FixedMetadataValue(plugin, mEntries));

		/*
		 * Attach an area bound around where the player is now. They can only
		 * reply to messages if they are still within this bound when they click
		 * the chat option
		 */
		player.setMetadata(Constants.PLAYER_CLICKABLE_DIALOG_LOCATION_METAKEY,
		                   new FixedMetadataValue(plugin,
		                                          new AreaBounds("", new Point(player.getLocation().subtract(4.0, 4.0, 4.0)),
		                                                         new Point(player.getLocation().add(4.0, 4.0, 4.0)))));
	}
}

