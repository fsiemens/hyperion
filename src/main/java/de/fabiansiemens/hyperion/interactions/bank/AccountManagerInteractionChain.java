package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@InteractionChain
public class AccountManagerInteractionChain extends CommonAccountInteractionChain {

	private static final String MANAGER_PANEL_PATH = "bank/AccountManagerPanel.xml";
	private static final String MANAGER_ADD_MODAL_PATH = "bank/AccountAddManagerModal.xml";
	
	public AccountManagerInteractionChain(ApplicationContext context) {
		super(context);
	}

	//TODO change authorization to authorize Function which throws exception for centralized error handling
	
	@InteractionCallback(id = "b.konto.manager")
	public void onManagerButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		if(!isOwnerOrManager(account, event.getUser())) {
			event.reply(les.getLocalizedExpression("error.konto.unauthorized", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Container panel = getManagerPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.konto.manager.add")
	public void onAddManagerButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		if(!isOwner(account, event.getUser())) {
			event.reply(les.getLocalizedExpression("error.konto.unauthorized", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Modal modal = uiProvider.modalOf(MANAGER_ADD_MODAL_PATH, event.getGuild(), args);
		event.replyModal(modal).queue();
	}
	
	@InteractionCallback(id = "m.konto.add-manager")
	public void onAddManagerModalInteraction(ModalInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		if(!isOwner(account, event.getUser())) {
			event.reply(les.getLocalizedExpression("error.konto.unauthorized", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		List<Long> ids = event.getValue("user").getAsLongList();
		
		if(!ids.isEmpty()) {
			User user = event.getGuild().getMemberById(ids.getFirst()).getUser();
			bankService.addManager(account, user).commit();
		}
		
		Container panel = getManagerPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.konto.manager.remove")
	public void onRemoveManagerButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {	
		BankAccount account = getAccount(args.get("konto"));
		Long id = args.get("id").asLong();
		
		if(!isOwner(account, event.getUser())) {
			event.reply(les.getLocalizedExpression("error.konto.unauthorized", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		bankService.removeManager(account, id).commit();
		Container panel = getManagerPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	protected Arguments prepareManagerArgs(BankAccount account, Arguments args, User user) {
		prepareAccountArgs(account, args, user);
		Arguments[] argList = account.getManagerIds()
				.stream()
				.map(entry -> {
					User manager = user.getJDA().getUserById(entry);
					return prepareAccountArgs(account, Arguments.empty(), user)
							.put("id", entry)
							.put("mention", manager == null ? entry : manager.getAsMention());
				})
				.collect(Collectors.toList())
				.toArray(new Arguments[0]);
		
		return args.put("managers", argList);
	}
	
	protected Container getManagerPanel(BankAccount account, Arguments args, Guild guild, User user) throws UiParseException {
		prepareManagerArgs(account, args, user);
		return uiProvider.panelOf(MANAGER_PANEL_PATH, guild, args);
	}
}
