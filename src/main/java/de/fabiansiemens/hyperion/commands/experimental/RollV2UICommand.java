package de.fabiansiemens.hyperion.commands.experimental;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import lombok.NonNull;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "experimental/RollV2UI.json")
public class RollV2UICommand extends SlashCommandBase {

	public RollV2UICommand(@NonNull ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(RollV2UICommand.class, context);
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		List<MessageTopLevelComponent> components = Arrays.asList(
		          Container.of(
		              TextDisplay.of("Detaillierte Ansicht:\n```(d4)d6 +3```"),
		              ActionRow.of(
		                  StringSelectMenu.create("af0bad4bd89740788e27f37d6c2c5929")
		                      .addOption("Überblick", "ef0afdd9919c4daeccd71c7e8b599e3b")
		                      .addOption("d4", "6666004f62cb40f5ea9d6f2708621092", "-> 3")
		                      .addOption("3d6 ", "f8812d90500a4650ead29f9eda8f8f91", "-> 11")
		                      .setDefaultValues("ef0afdd9919c4daeccd71c7e8b599e3b")
		                      .build()
		              ),
		              Separator.createDivider(Separator.Spacing.SMALL),
		              TextDisplay.of("## 14\n`= 11 + 3`\n0x Crit, 0x Crit Fail")
		          ).withAccentColor(0x1F8B4C),
		          Container.of(
		              TextDisplay.of("Detaillierte Ansicht:\n```(d4)d6 +3```"),
		              ActionRow.of(
		                  StringSelectMenu.create("69d17c73d75f4e5f8aee2188bcf1f026")
		                      .addOption("3d6", "ac0924f839d04607993082dc27c4d44e", "-> 11")
		                      .addOption("Überblick", "290fff592e00441dba8d7b367a643bf5")
		                      .addOption("d4", "3f180ab3b35b4290e54c13f642be5a1e", "-> 3")
		                      .setDefaultValues("ac0924f839d04607993082dc27c4d44e")
		                      .build()
		              ),
		              Separator.createDivider(Separator.Spacing.SMALL),
		              Section.of(
		                  Button.of(
		                      ButtonStyle.LINK,
		                      "https://lbfs.eu",
		                      "Ergebnisse herunterladen"
		                  ),
		                  TextDisplay.of("**Würfelergebnisse:**\n`3` `5` `3`"),	//~~*`5`~~ (adv)*
		                  TextDisplay.of("Test"),
		                  TextDisplay.of("Test3")
		              ),
		              TextDisplay.of("## 11\n`= 3 + 5 + 3`")
		          ).withAccentColor(0x1F8B4C)
		      );
	
	      event.getChannel()
	          .sendMessageComponents(components)
	          .useComponentsV2()
	          .queue();
	 }
}
