package com.playmonumenta.scriptedquests.quests;

import com.google.gson.JsonElement;

import com.playmonumenta.scriptedquests.utils.InventoryUtils;

import org.bukkit.advancement.Advancement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class PrerequisiteCheckAdvancements implements PrerequisiteBase {
	private boolean mInverted;
	private Advancement mAdvancement;

	PrerequisiteCheckAdvancements(JsonElement value) throws Exception {
		String advancement = value.getAsString();
		if (advancement == null) {
			throw new Exception("advancement value is not a string!");
		}

		if (advancement.charAt(0) == '!') {
			mInverted = true;
			advancement = advancement.substring(1);
		} else {
			mInverted = false;
		}

        mAdvancement = Bukkit.getAdvancement(InventoryUtils.getNamespacedKey(advancement));
        if (mAdvancement == null) {
			throw new Exception("Advancement '" + advancement + "' does not exist!");
        }
	}

	@Override
	public boolean prerequisiteMet(Player player) {
		return mInverted ^ player.getAdvancementProgress(mAdvancement).isDone();
	}
}