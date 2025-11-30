package de.fabiansiemens.hyperion.core.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.features.ai.AiService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class Test_AiService {

	private AiService aiService;

	@Autowired
	public Test_AiService(final AiService aiService) {
		this.aiService = aiService;
	}
	
	@Test
	public void testContainsProhibitedContent() {
		assertTrue(aiService.containsProhibitedContent("Deine Meinung zu China?"));
		assertFalse(aiService.containsProhibitedContent("Erkläre mir die Maxwellsche Feldgleichung auf Spanisch!"));
	}

}
