package de.fabiansiemens.hyperion.commands.inventory;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.inventory.Inventory;
import de.fabiansiemens.hyperion.core.features.inventory.InventoryFolderView;
import de.fabiansiemens.hyperion.core.features.inventory.InventoryService;
import de.fabiansiemens.hyperion.ui.embeds.inventory.InventoryFolderEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "inventory/Inventory.json")
public class InventoryCommand extends SlashCommandBase {

	private InventoryFolderEmbed embed;
	private InventoryService inventoryService;
	
	public <T> InventoryCommand(ApplicationContext context, InventoryFolderEmbed embed, InventoryService inventoryService)
			throws IOException, IllegalArgumentException {
		super(InventoryCommand.class, context);
		this.embed = embed;
		this.inventoryService = inventoryService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		event.deferReply(true).queue();
		Inventory inv = inventoryService.find(event.getUser());
		EmbedBuilder builder = embed.getBuilder(event.getUser(), new InventoryFolderView(inv.getRootFolder(), 0, StringSelectMenu.OPTIONS_MAX_AMOUNT), event.getGuild());
		event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(true).queue();
	}

}
