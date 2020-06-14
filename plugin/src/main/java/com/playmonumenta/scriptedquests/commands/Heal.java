package com.playmonumenta.scriptedquests.commands;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.DoubleArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Heal {
	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("entities", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.MANY_ENTITIES));
		arguments.put("amount", new DoubleArgument());

		CommandAPI.getInstance().register("heal",
			CommandPermission.fromString("scriptedquests.heal"),
			arguments,
			(sender, args) -> {
				if (args[0] instanceof Collection<?>) {
					for (Object e : (Collection<?>) args[0]) {
						if (e instanceof Damageable) {
							Damageable d = (Damageable)e;
							d.setHealth(d.getHealth() + (Double)args[1]);
							if (e instanceof Attributable) {
								Attributable a = (Attributable)e;
								AttributeInstance attr = a.getAttribute(Attribute.GENERIC_MAX_HEALTH);
								if (attr != null && d.getHealth() > attr.getValue()) {
									d.setHealth(attr.getValue());
								}
							}
						}
					}
				}
			}
		);
	}
}