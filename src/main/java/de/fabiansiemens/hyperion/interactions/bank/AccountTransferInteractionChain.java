package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class AccountTransferInteractionChain extends CommonAccountInteractionChain {

	private static final String ACCOUNT_TRANSFER_MODAL_PATH = "bank/AccountTransferModal.xml";
	private static final String ACCOUNT_TRANSFER_PROCEED_BUTTON_PATH = "bank/AccountTransferProceed.xml";
	
	public AccountTransferInteractionChain(ApplicationContext context) {
		super(context);
	}

	@InteractionCallback(id = "b.konto.transfer")
	public void onTransferButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		Modal transferModal = uiProvider.modalOf(ACCOUNT_TRANSFER_MODAL_PATH, event.getGuild(), args);
		event.replyModal(transferModal).queue();
	}
	
	//TODO implement User Select instead of input field
	@InteractionCallback(id = "m.konto.transfer")
	public void onTransferModalInteraction(ModalInteractionEvent event, Arguments args) throws UiParseException {
		String userName = event.getValue("user").getAsString();
		List<Member> members = event.getGuild().getMembersByName(userName, true);
		
		User user; ;
		
		if(members.isEmpty() || (user = members.getFirst().getUser()) == null) {
			event.reply(les.getLocalizedExpression("error.konto.transfer.user-not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		args.put("user", user.getId());
		args.put("mention", user.getAsMention());
		
		Button proceedButton = uiProvider.buttonOf(ACCOUNT_TRANSFER_PROCEED_BUTTON_PATH, event.getGuild(), args);
		event.reply(les.getLocalizedExpression(".konto.transfer.safety-check", event.getGuild(), args))
			.addComponents(ActionRow.of(proceedButton))
			.setEphemeral(true)
			.queue();
	}
	
	@InteractionCallback(id = "b.konto.transfer.proceed")
	public void onTransferSafetyCheckInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException {
		BankAccount account = getAccount(args.get("konto"));
		User user = event.getJDA().getUserById(args.get("user").asString());
		
		args.put("mention", user.getAsMention());
		bankService.changeOwner(account, user.getIdLong()).commit();
		event.reply(les.getLocalizedExpression("success.konto.transfer", event.getGuild(), args)).setEphemeral(true).queue();
	}
}
