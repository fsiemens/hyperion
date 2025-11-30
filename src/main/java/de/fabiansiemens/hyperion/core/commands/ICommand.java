package de.fabiansiemens.hyperion.core.commands;

import net.dv8tion.jda.api.interactions.DiscordLocale;

public interface ICommand {
	public String getName();
	public String getDescription(DiscordLocale locale);
}
