package de.fabiansiemens.hyperion.persistence.bank;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.persistence.bank.account.AccountPersistenceService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class Test_BankAccountPersistenceService {
	
	private JDAManager jdaManager;
	private AccountPersistenceService service;
	
	public Test_BankAccountPersistenceService(final JDAManager jdaManager, final AccountPersistenceService service) {
		this.jdaManager = jdaManager;
		this.service = service;
	}
	
	@Test
	public void testCreate() {
		service.createAccount(jdaManager.getJDA().getSelfUser().getIdLong(), "Test", AccountPolicies.withDefaults(), new LinkedList<>());
	}
	
	@Test
	public void testExistsAccount() {
		assertTrue(service.existsAccountWithOwnerId(jdaManager.getJDA().getSelfUser().getIdLong()));
		service.createAccount(jdaManager.getJDA().getSelfUser().getIdLong(), "Test", AccountPolicies.withDefaults(), new LinkedList<>());
		assertTrue(service.existsAccountWithOwnerId(jdaManager.getJDA().getSelfUser().getIdLong()));
	}
}
