package de.fabiansiemens.hyperion.interactions.bank;

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
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class AccountRenameInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_RENAME_MODAL_PATH = "bank/AccountRenameModal.xml";
	
	public AccountRenameInteractionChain(ApplicationContext context) {
		super(context);
	}

	@InteractionCallback(id = "b.konto.rename")
	public void onRenameButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		boolean authorized = account.hasPolicy(AccountPolicies.ALLOW_MEMBER_NAME_CHANGE) 
				|| (isOwnerOrManager(account, event.getUser()) && account.hasPolicy(AccountPolicies.ALLOW_NAME_CHANGE));
		
		if(!authorized) {
			event.reply(les.getLocalizedExpression("error.konto.name-change-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Modal accountRenameModal = uiProvider.modalOf(ACCOUNT_RENAME_MODAL_PATH, event.getGuild(), args);
		event.replyModal(accountRenameModal).queue();
	}
	
	@InteractionCallback(id = "m.konto.rename")
	public void onRenameModalInteraction(ModalInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		String name = event.getValue("name").getAsString();
		BankAccount account = getAccount(args.get("konto"));
		
		account.setName(name);
		account = bankService.updateAccount(account);
		
		if(account instanceof GroupAccount groupAccount) {
			Container groupPanel = getGroupPanel(groupAccount, args, event.getGuild(), event.getUser());
			event.editComponents(groupPanel).useComponentsV2().queue();
			return;
		}
	
		Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(accountPanel).useComponentsV2().queue();
	}
}
