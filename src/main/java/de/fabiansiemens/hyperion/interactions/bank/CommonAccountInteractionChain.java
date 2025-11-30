package de.fabiansiemens.hyperion.interactions.bank;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.AccountNotFoundException;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.AuthorizationException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.bank.AccountService;
import de.fabiansiemens.hyperion.core.features.bank.BankAccount;
import de.fabiansiemens.hyperion.core.features.bank.GroupAccount;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.interactions.InteractionChainBase;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@Getter
@Primary
@InteractionChain
public class CommonAccountInteractionChain extends InteractionChainBase {
	
	protected final AccountService bankService;
	protected final LocalizedExpressionService les;
	
	protected static final String KONTO_PANEL_PATH = "bank/KontoPanel.xml";
	protected static final String GROUP_PANEL_PATH = "bank/GroupPanel.xml";
	protected static final String KONTO_CREATE_BUTTON_PATH = "bank/AccountCreate.xml";
	protected static final String GROUP_CREATE_BUTTON_PATH = "bank/GroupCreate.xml";
	protected static final String ACCOUNT_STRING_SELECT_PATH = "bank/AccountSelect.xml";
	protected static final String ACCOUNT_CREATE_MODAL_PATH = "bank/AccountCreateModal.xml";
	
	public CommonAccountInteractionChain(final ApplicationContext context) {
		super(context);
		this.bankService = context.getBean(AccountService.class);
		this.les = context.getBean(LocalizedExpressionService.class);
	}
	
	@InteractionCallback(id = "b.konto.back")
	public void onKontoBackButton(ButtonInteractionEvent event, Arguments args) throws UiParseException, AccountNotFoundException, ArgumentTypeException {
		BankAccount account;
		try {
			account = getAccount(args.get("konto"));
		}
		catch (AccountNotFoundException | ArgumentTypeException e) {
			account = getAccount(args.get("group")); 
		}
		
		if(account instanceof GroupAccount groupAccount) {
			event.editComponents(getGroupPanel(groupAccount, args, event.getGuild(), event.getUser())).useComponentsV2().queue();
			return;
		}
		
		event.editComponents(getKontoPanel(account, args, event.getGuild(), event.getUser())).useComponentsV2().queue();
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
		onBankCommandInteraction(event, "choose", "konto");
	}
	
	public void onGroupCommandInteraction(SlashCommandInteractionEvent event) throws UiParseException {
		onBankCommandInteraction(event, "pre-select", "group");
	}
	
	/**
	 * Entry Point: KontoCommand / GroupCommand / AddMemberButton <br>
	 * Follow Ups: AccountCreateModal / AccountPanel / GroupPanel <br>
	 * 
	 * Opens an AccountCreateModal (args: type=?, konto=?) if mode == "choose" and "new" is selected, or <br>
	 * opens an AccountPanel (args ...) if mode == "choose" and an account is selected, or <br>
	 * opens an GroupPanel (args ...) if mode == "choose" and a group is selected, or
	 * opens another AccountSelect (args mode=choose, konto=?)
	 * adds a member account to a group account (args: group=?) if mode == "add-member"
	 * 
	 * <br><br>
	 * accept args: type, konto, mode, group
	 * 
	 * @param event
	 * @param args must always contain "mode" and in some circumstances "type" and "konto"
	 * @throws AccountNotFoundException 
	 * @throws NumberFormatException 
	 * @throws UiParseException 
	 */
	@InteractionCallback(id = "ss.konto")
	public void onAccountSelectInteraction(StringSelectInteractionEvent event, Arguments args) throws NumberFormatException, AccountNotFoundException, UiParseException {
		String mode = args.get("mode").asString();
		event.getMessage().delete().queue();
		
		switch(mode) {
		case "choose":
			chooseAccount(event, args.get("type").asString(), args.get("konto").asString());
			break;
		case "pre-select":
			preSelectAccount(event);
			break;
		case "join":
			addMemberToGroup(event, args.get("group").asString());
			break;
		case "leave":
			leaveGroup(event, args.get("group").asString());
			break;
		default: 
			args.put("message", "No valid AccountSelect Mode: " + String.valueOf(mode));
			event.reply(les.getLocalizedExpression("error.common.interaction", event.getGuild(), args)).setEphemeral(true).queue();
			return;
		}
	}

	@InteractionCallback(id = "b.konto.refresh")
	public void onRefreshButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		
		if(account instanceof GroupAccount groupAccount) {
			Container groupPanel = getGroupPanel(groupAccount, args, event.getGuild(), event.getUser());
			event.editComponents(groupPanel).useComponentsV2().queue();
			return;
		}
		
		Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(accountPanel).useComponentsV2().queue();
	}
	
	protected BankAccount getAccount(String idRaw) throws AccountNotFoundException, NumberFormatException {
		try {
			return bankService.findAccountWithId(Long.parseLong(idRaw))
					.orElseThrow(AccountNotFoundException::new);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Could not parse Account ID");
		}
	}
	
	@Nullable
	protected BankAccount getAccount(Arguments.Entry idRaw) throws AccountNotFoundException, ArgumentTypeException {
		return bankService.findAccountWithId(idRaw.asLong())
				.orElseThrow(AccountNotFoundException::new);
	}
	
	protected Container getKontoPanel(BankAccount account, Arguments args, Guild guild, User user) throws UiParseException {
		prepareAccountArgs(account, args, user);
		return uiProvider.panelOf(KONTO_PANEL_PATH, guild, args);
	}
	
	protected Container getGroupPanel(@NonNull GroupAccount groupAccount, @NonNull Arguments args, @Nullable Guild guild, @NonNull User user) throws UiParseException {
		prepareGroupArgs(groupAccount, args, user);
		return uiProvider.panelOf(GROUP_PANEL_PATH, guild, args);
	}
	
	protected Arguments prepareAccountArgs(@NonNull BankAccount account, Arguments args, @NonNull User user) {
		args.put("name", account.getName())
			.put("konto", String.valueOf(account.getId()))
			.put("owner", user.getJDA().getUserById(account.getOwnerId()).getAsMention())
			.put("balance", String.format("%.2f", account.getBalance()))
			.put("timestamp", String.valueOf(Instant.now().getEpochSecond()))
			.put("account", account)
			.put("isOwner", isOwner(account, user))
			.put("isAuthorized", isOwnerOrManager(account, user));
		return args;
	}
	
	protected Arguments prepareGroupArgs(GroupAccount group, Arguments args, User user) {
		prepareAccountArgs(group, args, user)
			.put("account", group)
			.put("memberCount", group.getMembers().size());
		return args;
	}
	
	protected void authorize(boolean expression, String message) throws AuthorizationException {
		if(expression)
			return;
		throw new AuthorizationException(message);
	}
	
	protected boolean isOwner(BankAccount account, User user) {
		return user.getIdLong() == account.getOwnerId();
	}
	
	protected boolean isOwnerOrManager(BankAccount account, User user) {
		return isOwner(account, user) || account.hasManager(user.getIdLong());
	}
	
	private void onBankCommandInteraction(SlashCommandInteractionEvent event, String mode, String type) throws UiParseException {
		Button accountCreateButton = uiProvider.buttonOf(KONTO_CREATE_BUTTON_PATH, event.getGuild());
		
		if(!bankService.hasAccount(event.getUser())) {
			event.reply(les.getLocalizedExpression("warn.konto.no-account", event.getGuild())).setEphemeral(true)
				.addComponents(ActionRow.of(accountCreateButton)).queue();
			return;
		}
		
		Arguments args = Arguments.of("mode", mode)
				.put("type", type);
		
		List<BankAccount> accounts = bankService.findAccountsWithOwnerOrManager(event.getUser());
		List<SelectOption> options = new LinkedList<>();
		accounts.stream().forEach(acc -> options.add(
				SelectOption.of(
						acc.getName() + " (" + acc.getId() + (!isOwner(acc, event.getUser()) && isOwnerOrManager(acc, event.getUser()) ? "" : ", managed")  + ")", 
						String.valueOf(acc.getId()))));
		options.add(SelectOption.of("Create new...", "new"));
		
		StringSelectMenu accountSelect = uiProvider.stringSelectOf(ACCOUNT_STRING_SELECT_PATH, event.getGuild(), args, options);
		event.reply(les.getLocalizedExpression(".konto.select-account", event.getGuild())).setEphemeral(true)
			.addComponents(ActionRow.of(accountSelect)).queue();
	}
	
	private void chooseAccount(StringSelectInteractionEvent event, String type, String kontoRaw) throws UiParseException, NumberFormatException, AccountNotFoundException {
		Arguments args = Arguments.of("type" , type)
				.put("konto", kontoRaw);
		
		//Create new 
		if(event.getSelectedOptions().getFirst().getValue().equalsIgnoreCase("new")) {
			Modal accountCreateModal = uiProvider.modalOf(ACCOUNT_CREATE_MODAL_PATH, event.getGuild(), args);
			event.replyModal(accountCreateModal).queue();
			return;
		}
		
		//Choose existing
		String idRaw = event.getSelectedOptions().getFirst().getValue();
		BankAccount account = getAccount(idRaw);
		
		if(account == null) {
			log.warn("Invalid Selector State. Account with the id '{}' does not exist.", idRaw);
			event.reply(les.getLocalizedExpression("error.konto.select.unknown-id", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		if(account instanceof GroupAccount groupAccount) {
			//TODO open group account (+ authorize is member, req. konto args)
			Container groupPanel = getGroupPanel(groupAccount, args, event.getGuild(), event.getUser());
			event.replyComponents(groupPanel).useComponentsV2().setEphemeral(true).queue();
			return;
		}
	 
		//Open User Account (authorize)
		if(!isOwnerOrManager(account, event.getUser())) {
			log.warn("Data Leak / Unauthorized Access. User '{}' tried to select an account from user '{}'.", event.getUser().getIdLong(), account.getOwnerId());
			event.reply(les.getLocalizedExpression("error.konto.unauthorized", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		args.put("show", true);
		Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		event.replyComponents(accountPanel).useComponentsV2().setEphemeral(true).queue();
	}
	
	private void preSelectAccount(StringSelectInteractionEvent event) throws UiParseException, NumberFormatException, AccountNotFoundException {
		String idRaw = event.getSelectedOptions().getFirst().getValue();
		BankAccount account = getAccount(idRaw);
		
		if(account == null) {
			log.warn("Invalid Selector State. Account with the id '{}' does not exist.", idRaw);
			event.reply(les.getLocalizedExpression("error.konto.select.unknown-id", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Arguments args = Arguments
			.of("konto", String.valueOf(account.getId()))
			.put("owner", String.valueOf(account.getId()))
			.put("mode", "choose");
		
		boolean hasKonto = bankService.hasGroup(account);
		
		if(!hasKonto) {
			Button groupCreateButton = uiProvider.buttonOf(GROUP_CREATE_BUTTON_PATH, event.getGuild(), args);
			event.reply(les.getLocalizedExpression("warn.group.no-account", event.getGuild())).setEphemeral(true)
				.addComponents(ActionRow.of(groupCreateButton)).queue();
			return;
		}
		
		List<GroupAccount> groups = bankService.findGroupsWithMember(account);
		List<SelectOption> options = new LinkedList<>();
		groups.stream().forEach(acc -> options.add(SelectOption.of(acc.getName(), String.valueOf(acc.getId()))));
		options.add(SelectOption.of("Create new...", "new"));
		
		StringSelectMenu groupSelect = uiProvider.stringSelectOf(ACCOUNT_STRING_SELECT_PATH, event.getGuild(), options);
		event.reply(les.getLocalizedExpression(".group.select-account", event.getGuild())).setEphemeral(true)
			.addComponents(ActionRow.of(groupSelect)).queue();
	}
	
	private void addMemberToGroup(StringSelectInteractionEvent event, String groupIdRaw) throws NumberFormatException, AccountNotFoundException {
		String kontoIdRaw = event.getSelectedOptions().getFirst().getValue();
		BankAccount account = getAccount(kontoIdRaw);
		GroupAccount group = (GroupAccount) getAccount(groupIdRaw);
		
		if(account == null || group == null) {
			event.reply(les.getLocalizedExpression("error.group.konto-not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		bankService.addMember(group, account).commit();
		event.reply(les.getLocalizedExpression("success.group.add", event.getGuild())).setEphemeral(true).queue();
	}
	
	private void leaveGroup(StringSelectInteractionEvent event, String groupIdRaw) throws NumberFormatException, AccountNotFoundException {
		String kontoIdRaw = event.getSelectedOptions().getFirst().getValue();
		BankAccount account = getAccount(kontoIdRaw);
		GroupAccount group = (GroupAccount) getAccount(groupIdRaw);
		
		if(account == null || group == null) {
			event.reply(les.getLocalizedExpression("error.group.konto-not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		bankService.removeMember(group, account).commit();
		event.reply(les.getLocalizedExpression("success.group.leave", event.getGuild())).setEphemeral(true).queue();
	}
}
