package de.fabiansiemens.hyperion.interactions.bank;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.AuthorizationException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class AccountTransactionInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_TRANSACTION_MODAL_PATH = "bank/AccountTransactionModal.xml";
	
	public AccountTransactionInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	@InteractionCallback(id = "b.konto.transaction")
	public void onAccountTransactionButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		prepareAccountArgs(account, args, event.getUser());
		Modal transactionModal = uiProvider.modalOf(ACCOUNT_TRANSACTION_MODAL_PATH, event.getGuild(), args);
		event.replyModal(transactionModal).queue();
	}

	@InteractionCallback(id = "m.konto.transaction")
	public void onAccountTransactionModalInteraction(ModalInteractionEvent event, Arguments args) throws UiParseException, AccountNotFoundException, ArgumentTypeException {
		String mode = event.getValue("mode").getAsStringList().getFirst();
		String amountRaw = event.getValue("amount").getAsString();
		String counterpart = event.getValue("counterpart").getAsString();
		String usage = event.getValue("note").getAsString();
		
		BankAccount account = getAccount(args.get("konto"));
		BankAccount counterpartAccount;
		try {
			counterpartAccount = getAccount(counterpart);
		}
		catch(Exception e) {
			counterpartAccount = null;
		}
		
		Double amount;
		try {
			amount = Double.parseDouble(amountRaw);
		}
		catch (NumberFormatException e) {
			event.reply(les.getLocalizedExpression("error.konto.parse-amount-error", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		if(account == null) {
			event.reply(les.getLocalizedExpression("error.konto.not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		if(counterpart.isBlank())
			counterpart = "-";
		
		if(usage.isBlank())
			usage = "-";
		
		try {
			switch(mode) {
			case "deposit":
				handleDeposit(event, account, counterpart, amount, usage);
				break;
				
			case "transfer": 
				handleTransfer(event, account, counterpartAccount, amount, usage);
				break;
				
			case "withdraw": 
				handleWithdrawal(event, account, counterpart, amount, usage);
				break;
			}
			
		}
		catch (IllegalArgumentException| AuthorizationException e) {
			event.reply(les.getLocalizedExpression(e.getLocalizedMessage(), event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//Update GroupPanel
		if(account instanceof GroupAccount groupAccount) {
			Container groupPanel = getGroupPanel(groupAccount, args, event.getGuild(), event.getUser());
			event.editComponents(groupPanel).useComponentsV2().queue();
			return;
		}
	
		//Update AccountPanel
		Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(accountPanel).useComponentsV2().queue();
	}
	
	private void handleDeposit(ModalInteractionEvent event, BankAccount account, String counterpart, double amount, String usage) throws AuthorizationException {
		authorize(account.hasPolicy(AccountPolicies.ALLOW_DEPOSIT) && (isOwnerOrManager(account, event.getUser()) || account.hasPolicy(AccountPolicies.ALLOW_MEMBER_DEPOSIT)), "error.konto.deposit-prohibited");
		bankService.deposit(account, counterpart, amount, usage, event.getUser()).commit();
	}
	
	private void handleTransfer(ModalInteractionEvent event, BankAccount account, BankAccount counterpart, double amount, String usage) throws AuthorizationException {
		if(counterpart == null)
			throw new IllegalArgumentException("error.konto.parse-receiver-error");
		
		authorize(account.hasPolicy(AccountPolicies.ALLOW_SENDING_TRANSFER) && (isOwnerOrManager(account, event.getUser()) || account.hasPolicy(AccountPolicies.ALLOW_MEMBER_TRANSFER)), "error.konto.send-transfer-prohibited");
		authorize(counterpart.hasPolicy(AccountPolicies.ALLOW_RECEIVING_TRANSFER), "error.konto.receive-transfer-prohibited");
		
		bankService.transfer(account, counterpart, amount, usage, event.getUser()).commit();
	}
	
	private void handleWithdrawal(ModalInteractionEvent event, BankAccount account, String counterpart, double amount, String usage) throws AuthorizationException {
		authorize(account.hasPolicy(AccountPolicies.ALLOW_WITHDRAWAL) && (isOwnerOrManager(account, event.getUser()) || account.hasPolicy(AccountPolicies.ALLOW_MEMBER_WITHDRAW)), "error.konto.withdraw-prohibited");
		bankService.withdraw(account, counterpart, amount, usage, event.getUser()).commit();
	}
}
