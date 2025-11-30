package de.fabiansiemens.hyperion.interactions.bank;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
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
public class AccountCreateInteractionChain extends CommonAccountInteractionChain {
	
	public AccountCreateInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	/**
	 * Entry Point: KontoCommand <br>
	 * Follow Ups: AccountCreateModal <br>
	 * 
	 * Opens an AccountCreateModal (args: type=konto)
	 * 
	 * <br><br>
	 * accept args: none
	 * 
	 * @param event
	 * @param args - certainly empty
	 * @throws UiParseException 
	 */
	@InteractionCallback(id = "b.konto.create")
	public void onAccountCreateButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		event.getMessage().delete().queue();
		args.put("type", "konto");
		args.put("show", true);
		
		Modal accountCreateModal = uiProvider.modalOf(ACCOUNT_CREATE_MODAL_PATH, event.getGuild(), args);
		event.replyModal(accountCreateModal).queue();
	}
	
	/**
	 * Entry Point: GroupCommand <br>
	 * Follow Ups: AccountCreateModal <br>
	 * 
	 * Opens an AccountCreateModal (args: type=group, konto=?)
	 * 
	 * <br><br>
	 * accept args: konto
	 * 
	 * @param event
	 * @param args must contain the entry "konto"
	 * @throws UiParseException 
	 */
	@InteractionCallback(id = "b.group.create")
	public void onGroupCreateButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		event.getMessage().delete().queue();
		args.put("type", "group");
		
		Modal accountCreateModal = uiProvider.modalOf(ACCOUNT_CREATE_MODAL_PATH, event.getGuild(), args);
		event.replyModal(accountCreateModal).queue();
	}
	
	/**
	 * Entry Points: AccountCreateButton / GroupCreateButton
	 * Follow Ups: AccountPanel / GroupPanel
	 * 
	 * Creates an account and opens the accounts Panel (args ...) if type == "konto", or <br>
	 * creates a group and opens the groups Panel (args ...) if type == "group" and konto exists
	 * 
	 * <br><br>
	 * accept args: konto, type
	 *  
	 * @param event
	 * @param args must contain the entries "konto" and "type"
	 * @throws AccountNotFoundException 
	 * @throws NumberFormatException 
	 */
	@InteractionCallback(id = "m.konto.create")
	public void onAccountCreateModalInteraction(ModalInteractionEvent event, Arguments args) throws NumberFormatException, AccountNotFoundException {
		String name = event.getValue("name").getAsString();
		String konto = args.get("konto").asString();
		String type = args.get("type").asString();
		
		BankAccount owner;
		try {
			owner = getAccount(konto);
		}
		catch (AccountNotFoundException | NumberFormatException e) {
			owner = null;
		}
		
		BankAccount account;
		try {
			switch(type) {
			case "konto":
				account = bankService.createAccount(event.getUser(), name);
				Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
				event.replyComponents(accountPanel).useComponentsV2().setEphemeral(true).queue();
				break;
			case "group":
				if(owner == null)
					throw new Exception("Could not find user account");
				GroupAccount group = bankService.createGroup(owner, name);
				Container groupPanel = getGroupPanel(group, args, event.getGuild(), event.getUser());
				event.replyComponents(groupPanel).useComponentsV2().setEphemeral(true).queue();
				break;
			default: throw new Exception("Invalid Konto Type '" + type + "'");
			}
			
			
		} catch(AccountNotFoundException | NumberFormatException e) {
			throw e;
		}
		catch(Exception e) {	//TODO outsource exception handling to UiListener?
			log.warn("Could not create a new Bank Account. Cause: {}", e.getLocalizedMessage());
			log.warn("Stack Trace: ", e);
			event.reply(les.getLocalizedExpression("error.konto.create", event.getGuild())).setEphemeral(true).queue();
		}
	}

}
