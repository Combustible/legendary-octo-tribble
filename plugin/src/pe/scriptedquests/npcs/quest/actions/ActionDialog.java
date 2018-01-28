package pe.scriptedquests.npcs.quest.actions;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pe.scriptedquests.Plugin;
import pe.scriptedquests.npcs.quest.actions.dialog.DialogBase;
import pe.scriptedquests.npcs.quest.actions.dialog.DialogClickableText;
import pe.scriptedquests.npcs.quest.actions.dialog.DialogRawText;
import pe.scriptedquests.npcs.quest.actions.dialog.DialogRandomText;
import pe.scriptedquests.npcs.quest.actions.dialog.DialogText;

public class ActionDialog implements ActionBase {
	ArrayList<DialogBase> mDialogs = new ArrayList<DialogBase>();

	public ActionDialog(String npcName, String displayName,
	                    EntityType entityType, JsonElement element) throws Exception {
		JsonObject object = element.getAsJsonObject();
		if (object == null) {
			throw new Exception("dialog value is not an object!");
		}

		Set<Entry<String, JsonElement>> entries = object.entrySet();
		for (Entry<String, JsonElement> ent : entries) {
			String key = ent.getKey();

			if (key.equals("text")) {
				mDialogs.add(new DialogText(displayName, ent.getValue()));
			} else if (key.equals("raw_text")) {
				mDialogs.add(new DialogRawText(ent.getValue()));
			} else if (key.equals("clickable_text")) {
				mDialogs.add(new DialogClickableText(npcName, displayName, entityType, ent.getValue()));
			} else if (key.equals("random_text")) {
				mDialogs.add(new DialogRandomText(displayName, ent.getValue()));
			} else {
				throw new Exception("Unknown dialog key: '" + key + "'");
			}
		}
	}

	@Override
	public void doAction(Plugin plugin, Player player) {
		for (DialogBase dialog : mDialogs) {
			dialog.sendDialog(plugin, player);
		}
	}
}
