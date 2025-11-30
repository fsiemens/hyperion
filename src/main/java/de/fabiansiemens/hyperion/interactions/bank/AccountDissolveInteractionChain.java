package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class AccountDissolveInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_DISSOLVE_MODAL_PATH = "bank/AccountDissolveModal.xml";
	
	public AccountDissolveInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	@InteractionCallback(id = "b.konto.dissolve")
	public void onDissolveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		boolean authorized = isOwner(account, event.getUser());
		
		if(authorized) {
			Modal dissolveModal = uiProvider.modalOf(ACCOUNT_DISSOLVE_MODAL_PATH, event.getGuild(), args);
			event.replyModal(dissolveModal).queue();
			return;
		}

		event.reply(les.getLocalizedExpression("error.konto.dissolve-prohibited", event.getGuild())).setEphemeral(true).queue();
	}
	
	@InteractionCallback(id = "m.konto.dissolve")
	public void onDissolveModalInteraction(ModalInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException {
		BankAccount account = getAccount(args.get("konto"));
		List<GroupAccount> groups = bankService.findGroupsWithMember(account);
		for(GroupAccount group : groups)
			bankService.removeMember(group, account).commit();
		
		bankService.deleteAccount(account);
		
		event.deferEdit().queue();
		event.getHook().deleteOriginal().complete();
		event.getHook().sendMessage(les.getLocalizedExpression("success.konto.dissolve-confirmed", event.getGuild())).setEphemeral(true).queue();
	}

}
