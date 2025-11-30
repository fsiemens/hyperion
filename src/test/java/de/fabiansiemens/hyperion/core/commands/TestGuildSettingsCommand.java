package de.fabiansiemens.hyperion.core.commands;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command.SubcommandGroup;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestGuildSettingsCommand {

	@Test
	public void testGuildSettingsCommand() {
		SubcommandData data = new SubcommandData("name", "desc");
		data.setDescriptionLocalization(DiscordLocale.GERMAN, "german")
		.setNameLocalization(DiscordLocale.GERMAN, "germanname")
		.addOptions(new OptionData(OptionType.STRING, "option", "test option", true, false));
		
		log.info("JSON: {}", data.toData().toPrettyString());
	}
	
	@Test
	public void testSubcommandGroups() {
		SubcommandGroupData data = new SubcommandGroupData("name", "desc");
		data.setDescriptionLocalization(DiscordLocale.GERMAN, "german")
		.setNameLocalization(DiscordLocale.GERMAN, "germanname")
		.addSubcommands(new SubcommandData("name", "desc"));
		
		log.info("JSON: {}", data.toData().toPrettyString());
	}

}
