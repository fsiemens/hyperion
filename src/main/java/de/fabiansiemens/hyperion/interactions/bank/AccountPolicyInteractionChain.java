package de.fabiansiemens.hyperion.interactions.bank;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Slf4j
@InteractionChain
public class AccountPolicyInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_POLICY_PANEL_PATH = "bank/AccountPolicyPanel.xml";
	
	@Value("${konto.policies.max-entries}")
	private int maxEntries;
	
	public AccountPolicyInteractionChain(ApplicationContext context) {
		super(context);
	}

	//TODO fix PolicyPanel too many components
	@InteractionCallback(id = "b.konto.policies")
	public void onPolicyButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		displayPolicyPage(event, account, 0);
	}
	
	@InteractionCallback(id = "b.konto.policies.toggle")
	public void onPolicyToggleButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		if(!account.hasPolicy(AccountPolicies.ALLOW_POLICY_CHANGE)) {
			event.reply(les.getLocalizedExpression("error.konto.policy-change-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		int page = 0;
		AccountPolicies policy;
		try {
			page = args.get("page").asInteger();
			policy = AccountPolicies.values()[args.get("ordinal").asInteger()];
		} catch (ArgumentTypeException e) {
			event.reply(les.getLocalizedExpression("error.konto.policy-change", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		bankService.setPolicyState(account, policy, !account.hasPolicy(policy)).commit();
		displayPolicyPage(event, account, page);
	}
	
	@InteractionCallback(id = "b.konto.policies.page-back")
	public void onPolicyPageBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		int pageNr = args.get("page").asInteger();
		displayPolicyPage(event, account, pageNr -1);
	}
	
	@InteractionCallback(id = "b.konto.policies.page-forward")
	public void onPolicyPageForwardButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		int pageNr = args.get("page").asInteger();
		displayPolicyPage(event, account, pageNr +1);
	}
	
	private void displayPolicyPage(ButtonInteractionEvent event, BankAccount account, int page) throws UiParseException {
		Arguments args = preparePolicyArgs(account, Arguments.empty(), event.getJDA(), event.getUser(), page).put("account", account);
		Container policyPanel = uiProvider.panelOf(ACCOUNT_POLICY_PANEL_PATH, event.getGuild(), args);
		event.editComponents(policyPanel).useComponentsV2().queue();
	}
	
	private Arguments preparePolicyArgs(BankAccount account, Arguments args, JDA jda, User user, int pageNr) {
		if(pageNr < 0)
			pageNr = 0;
		
		List<Arguments> argList = new LinkedList<>();
		prepareAccountArgs(account, args, user);
		args.put("page", pageNr)
			.put("currentPage", pageNr +1);
			
		List<Entry<AccountPolicies, Boolean>> policyList = account.getPolicies()
				.entrySet()
				.stream()
				.filter((entry) -> !entry.getKey().isGroupPolicy() || (entry.getKey().isGroupPolicy() && account instanceof GroupAccount))
				.collect(Collectors.toList());
		int totalPages = Math.ceilDiv(policyList.size(), maxEntries);
		args.put("totalPages", totalPages);
		
		if(pageNr >= totalPages)
			pageNr = totalPages -1;
		
		int start = pageNr * maxEntries;
		int end = Math.min(policyList.size(), (pageNr +1) * maxEntries);
		List<Entry<AccountPolicies, Boolean>> subList = policyList.subList(start, end);
		
		for(Entry<AccountPolicies, Boolean> policy : subList) {
			Arguments entry = 	prepareAccountArgs(account, Arguments.empty(), user).put("ordinal", policy.getKey().ordinal())
								.put("title", policy.getKey().getTitle())
								.put("description", policy.getKey().getDescription())
								.put("allowed", policy.getValue())
								.put("page", pageNr);
			
			argList.add(entry);
		}
		return args.put("policies", argList.toArray(new Arguments[0]));
	}
}
