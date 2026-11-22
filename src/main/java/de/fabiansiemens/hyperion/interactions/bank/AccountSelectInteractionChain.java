package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;
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
import de.fabiansiemens.hyperion.core.features.bank.AccountSelectionTab;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Slf4j
@Getter
@InteractionChain
public class AccountSelectInteractionChain extends CommonAccountInteractionChain {

	protected static final String ACCOUNT_SELECT_MODAL = "bank/AccountSelectPanel.xml";
	
	@Value("${konto.select.max-entries:8}")
	private int accountSelectPanelMaxEntries;
	
	public AccountSelectInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	/**
	 * Entry Point: Command <br>
	 * Follow Ups: AccountCreateButton / AccountSelect <br>
	 * 
	 * Sends an accountCreateButton (args: none) if user has no associated account, or <br>
	 * sends an accountSelectMenu (args: mode=choose) if user has one or more accounts
	 * <br><br>
	 * accept args: none
	 * 
	 * @param event
	 */
	public void onKontoCommandInteraction(SlashCommandInteractionEvent event) throws UiParseException {
		Arguments args = prepareArgsForAccountSelectPanel(event.getUser(), Arguments.empty(), AccountSelectionTab.OWNED, 0);
		Container panel = uiProvider.panelOf(ACCOUNT_SELECT_MODAL, event.getGuild(), args);
		event.replyComponents(panel).useComponentsV2().setEphemeral(true).queue();
	}

	@InteractionCallback(id = "b.konto.select.tab.owned")
	public void onOwnedTabButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		onTabButtonInteraction(event, args, AccountSelectionTab.OWNED);
	}
	
	@InteractionCallback(id = "b.konto.select.tab.member")
	public void onMemberedTabButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		onTabButtonInteraction(event, args, AccountSelectionTab.MEMBER);
	}

	@InteractionCallback(id = "b.konto.select.tab.managed")
	public void onManagedTabButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		onTabButtonInteraction(event, args, AccountSelectionTab.MANAGED);
	}
	
	@InteractionCallback(id = "b.konto.select.page-back")
	public void onPreviousPage(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		int pageNr = args.get("page").asInteger();
		AccountSelectionTab tab = AccountSelectionTab.valueOf(args.get("tab").asString().toUpperCase());
		prepareArgsForAccountSelectPanel(event.getUser(), args, tab, --pageNr);
		updatePanel(event, args);
	}
	
	@InteractionCallback(id = "b.konto.select.page-forward")
	public void onNextPage(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		int pageNr = args.get("page").asInteger();
		AccountSelectionTab tab = AccountSelectionTab.valueOf(args.get("tab").asString().toUpperCase());
		prepareArgsForAccountSelectPanel(event.getUser(), args, tab, ++pageNr);
		updatePanel(event, args);
	}
	
	@InteractionCallback(id = "b.konto.select.choose")
	public void onKontoChoose(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, AccountNotFoundException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		if(!account.getOwnerId().equals(event.getUser().getIdLong())
				&& !account.hasManager(event.getUser().getIdLong())) {
			log.warn("Unauthorized Access in Account Selection");
			event.deferEdit().queue();
			return; //TODO show error
		}
		
		Container panel;
		if(account instanceof GroupAccount groupAccount) {
			panel = getGroupPanel(groupAccount, args, event.getGuild(), event.getUser());
		}
		else {
			panel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		}
		
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	private void onTabButtonInteraction(ButtonInteractionEvent event, Arguments args, AccountSelectionTab tab) throws UiParseException {
		prepareArgsForAccountSelectPanel(event.getUser(), args, tab, 0);
		updatePanel(event, args);
	}
	
	private void updatePanel(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		Container panel = uiProvider.panelOf(ACCOUNT_SELECT_MODAL, event.getGuild(), args);
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	private Arguments prepareArgsForAccountSelectPanel(User user, Arguments args, AccountSelectionTab tab, int pageNr) {
		
		if(pageNr < 0)
			pageNr = 0;
		
		int totalPages = 0;
		
		if(tab == AccountSelectionTab.MEMBER) {
			Page<GroupAccount> accounts = bankService.findPaginatedGroupsWithUser(user, PageRequest.of(pageNr, accountSelectPanelMaxEntries));
			totalPages = accounts.getTotalPages();
			
			if(pageNr >= totalPages) {
				pageNr = totalPages -1;
				accounts = bankService.findPaginatedGroupsWithUser(user, accounts.previousPageable());
			}
			
			List<Arguments> argList = accounts.stream()
					.map(entry -> prepareGroupArgs(entry, Arguments.empty(), user))
					.collect(Collectors.toList());
			
			args.put("accounts", argList.toArray(new Arguments[0]));
		}
		else {
			Page<BankAccount> accounts;
			if(tab == AccountSelectionTab.OWNED)
				accounts = bankService.findPaginatedOwnedAccounts(user, PageRequest.of(pageNr, accountSelectPanelMaxEntries));
			else
				accounts = bankService.findPaginatedManagedAccounts(user, PageRequest.of(pageNr, accountSelectPanelMaxEntries));
			
			totalPages = accounts.getTotalPages();
			
			if(pageNr >= totalPages) {
				pageNr = totalPages -1;
				if(tab == AccountSelectionTab.OWNED)
					accounts = bankService.findPaginatedOwnedAccounts(user, accounts.previousPageable());
				else
					accounts = bankService.findPaginatedManagedAccounts(user, accounts.previousPageable());
			}
			
			List<Arguments> argList = accounts.stream()
					.map(entry -> prepareAccountArgs(entry, Arguments.empty(), user))
					.collect(Collectors.toList());
			
			args.put("accounts", argList.toArray(new Arguments[0]));
		}
		
		args.put("page", pageNr)
			.put("currentPage", pageNr +1)
			.put("totalPages", totalPages)
			.put("tab", tab.toString().toLowerCase());
		
		return args;
	}
}
