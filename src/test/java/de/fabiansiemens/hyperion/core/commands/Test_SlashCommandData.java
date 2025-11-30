package de.fabiansiemens.hyperion.core.commands;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class Test_SlashCommandData {
	@Test
	public void testSlashCommandDataConversion() {
		SlashCommandData data = Commands.slash("command", "description");
		log.info("JSON: {}", data.toData().toPrettyString());
	}
}
