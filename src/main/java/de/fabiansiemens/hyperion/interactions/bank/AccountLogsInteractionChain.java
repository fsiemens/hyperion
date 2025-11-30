package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.AccountLog;
import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccountLog;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Slf4j
@InteractionChain
public class AccountLogsInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_LOGS_PANEL_PATH = "bank/AccountLogsPanel.xml";
	private static final String ACCOUNT_LOGS_DETAILS_PANEL_PATH = "bank/AccountLogsDetailsPanel.xml";
	
	@Value("${konto.logs.max-entries}")
	private int accountLogPanelMaxEntries;
	
	public AccountLogsInteractionChain(ApplicationContext context) {
		super(context);
	}

	@InteractionCallback(id = "b.konto.logs")
	public void onAccountLogsButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		if(!account.hasPolicy(AccountPolicies.ALLOW_VIEW_LOGS)) {
			event.reply(les.getLocalizedExpression("error.konto.view-logs-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		int pageNr = 0;
		displayLogPage(event, account, pageNr);
	}
	
	@InteractionCallback(id = "b.konto.logs.page-back")
	public void onAccountLogsPageBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		int pageNr = args.get("page").asInteger();
		displayLogPage(event, account, pageNr -1);
	}
	
	@InteractionCallback(id = "b.konto.logs.page-forward")
	public void onAccountLogsPageForwardButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		int pageNr = args.get("page").asInteger();
		displayLogPage(event, account, pageNr +1);
	}
	
	@InteractionCallback(id = "b.konto.logs.more")
	public void onAccountLogsDetailsButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		AccountLog logEntry;
		int page = 0;
		try {
			logEntry = bankService.findLogById(args.get("id").asLong()).get();
			page = args.get("page").asInteger();
		} catch (ArgumentTypeException | NoSuchElementException e) {
			log.warn("Exception while displaying log details: ", e);
			event.reply(les.getLocalizedExpression("error.konto.log-not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		args = createLogArguments(account, logEntry, event.getJDA(), event.getUser(), page);
		Container panel = uiProvider.panelOf(ACCOUNT_LOGS_DETAILS_PANEL_PATH, event.getGuild(), args);
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.konto.logs.back")
	public void onAccountLogsDetailsBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		int page = args.get("page").asInteger();
		displayLogPage(event, account, page);
	}
	
	private void displayLogPage(ButtonInteractionEvent event, BankAccount account, int pageNr) throws UiParseException {
		Arguments args = Arguments.empty();
		
		if(pageNr < 0)
			pageNr = 0;
		
		Page<AccountLog> logs = bankService.findPaginatedLogs(account, PageRequest.of(pageNr, accountLogPanelMaxEntries));
		int totalPages = logs.getTotalPages();
		
		if(pageNr >= totalPages) {
			pageNr = totalPages -1;
			logs = bankService.findPaginatedLogs(account, logs.previousPageable());
		}
		
		prepareLogsArgs(account, args, event.getJDA(), event.getUser(), logs);
		args.put("page", pageNr)
			.put("currentPage", pageNr +1)
			.put("totalPages", totalPages);
		
		Container panel = uiProvider.panelOf(ACCOUNT_LOGS_PANEL_PATH, event.getGuild(), args);
		event.editComponents(panel).useComponentsV2().queue();
	}

	private Arguments prepareLogsArgs(BankAccount account, Arguments args, JDA jda, User user, Page<AccountLog> logs) {
		prepareAccountArgs(account, args, user);
		
		List<Arguments> argList = logs.stream()
				.map(entry -> createLogArguments(account, entry, jda, user, logs.getNumber()))
				.collect(Collectors.toList());
		
		return args.put("logs", argList.toArray(new Arguments[0]));
	}
	
	private Arguments createLogArguments(BankAccount account, AccountLog log, JDA jda, User user, int page) {
		Arguments arg = prepareAccountArgs(account, Arguments.empty(), user)
				.put("id", log.getId())
				.put("timestamp", log.getTimestamp().toInstant().getEpochSecond())
				.put("amount", String.format("%.2f", log.getAmount()))
				.put("sender", log.getSender())
				.put("receiver", log.getReceiver())
				.put("note", log.getUsage())
				.put("inbound", log.isInbound())
				.put("page", page)
				.put("isGroup", log instanceof GroupAccountLog);
		
		if(log instanceof GroupAccountLog gal) {
			arg.put("authorizerId", gal.getAuthorizer())
				.put("authorizerName", jda.getUserById(gal.getAuthorizer()).getAsMention());
		}
				
		return arg;
	}
}
