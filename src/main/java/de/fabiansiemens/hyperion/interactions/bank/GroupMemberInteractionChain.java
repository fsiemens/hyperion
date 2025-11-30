package de.fabiansiemens.hyperion.interactions.bank;

import java.util.List;
import java.util.stream.Collectors;

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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class GroupMemberInteractionChain extends CommonAccountInteractionChain {

	private static final String GROUP_ADD_MEMBER_MODAL = "bank/GroupAddMemberModal.xml";
	private static final String GROUP_LEAVE_MODAL_PATH = "bank/GroupLeaveModal.xml";
	private static final String GROUP_MEMBER_MORE_PANEL_PATH = "bank/AccountMemberMorePanel.xml";
	private static final String GROUP_MEMBER_PANEL_PATH = "bank/AccountMemberPanel.xml";
	
	public GroupMemberInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	@InteractionCallback(id = "b.group.member.add")
	public void onGroupMemberAddButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException, AccountNotFoundException, ArgumentTypeException {
		GroupAccount group = (GroupAccount) getAccount(args.get("konto"));
		prepareGroupArgs(group, args, event.getUser());
		
		boolean authorized = group.hasPolicy(AccountPolicies.ALLOW_MEMBERS_ADD_MEMBERS) || (group.isOwner(event.getUser().getIdLong()) && group.hasPolicy(AccountPolicies.ALLOW_REMOVE_MEMBERS));
		
		if(!authorized) {
			event.reply(les.getLocalizedExpression("error.group.add-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Modal groupAddMemberModal = uiProvider.modalOf(GROUP_ADD_MEMBER_MODAL, event.getGuild(), args);
		event.replyModal(groupAddMemberModal).queue();
	}
	
	@InteractionCallback(id = "m.group.add-member")
	public void onGroupMemberAddModalInteraction(ModalInteractionEvent event, Arguments args) throws NumberFormatException, AccountNotFoundException, ArgumentTypeException, UiParseException {
		String idRaw = event.getValue("user").getAsString();
		BankAccount account = getAccount(idRaw);
		GroupAccount group = (GroupAccount) getAccount(args.get("konto"));
		
		bankService.addMember(group, account).commit();
		Container groupPanel = getGroupPanel(group, args, event.getGuild(), event.getUser());
		event.editComponents(groupPanel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.group.leave")
	public void onGroupMemberLeaveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		GroupAccount group = (GroupAccount) getAccount(args.get("konto"));
		
		//Authorize
		if(!group.hasPolicy(AccountPolicies.ALLOW_LEAVE)) {
			event.reply(les.getLocalizedExpression("error.group.leave-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//List accounts owned by user
		List<BankAccount> members = group.getMembers()
										.stream()				//Only Accounts owned by the current user
										.filter(member -> isOwnerOrManager(group, event.getUser()))
										.collect(Collectors.toList());
		
		//In case list is empty, send error
		if(members.isEmpty()) {
			event.reply(les.getLocalizedExpression("error.group.not-a-member", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//In case the group has just 1 member, send error message
		if(group.getMembers().size() == 1) {
			event.reply(les.getLocalizedExpression("error.group.leave-last", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//In case the user has just 1 account as member, remove it OR if it is the owner send an error message
		if(members.size() == 1) {
			if(group.isOwner(event.getIdLong())) {
				event.reply(les.getLocalizedExpression("error.group.transfer-ownership", event.getGuild()));
				return;
			}
			
			bankService.removeMember(group, members.getFirst()).commit();
			event.reply(les.getLocalizedExpression("success.group.leave", event.getGuild())).setEphemeral(true).queue();
		}
		
		//In case the user has multiple accounts in group, send select menu
		Arguments[] options = members.stream()
				.map(
					(entry) -> Arguments.of("name", entry.getName())
						.put("id", entry.getId())
						.put("balance", entry.getBalance()))
				.toArray(Arguments[]::new);
		args.put("group", group.getId())
			.put("options", options);

		Modal leaveModal = uiProvider.modalOf(GROUP_LEAVE_MODAL_PATH, event.getGuild(), args);
		event.replyModal(leaveModal).queue();
	}

	@InteractionCallback(id = "m.group.leave")
	public void onGroupMemberLeaveModalInteraction(ModalInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		GroupAccount group = (GroupAccount) getAccount(args.get("group"));
		BankAccount account = getAccount(event.getValue("konto.modal.leave.account-select").getAsStringList().getFirst());
		
		bankService.removeMember(group, account).commit();
		Container panel = getGroupPanel(group, args, event.getGuild(), event.getUser());
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	//TODO implement auto paging 
	@InteractionCallback(id = "b.group.members")
	public void onGroupMembersButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		if(!(account instanceof GroupAccount))
			throw new UiParseException("Can only open Members Panel on GroupAccounts");
		
		prepareMembersArgs((GroupAccount) account, args, event.getUser());
		Container panel = uiProvider.panelOf(GROUP_MEMBER_PANEL_PATH, event.getGuild(), args);
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.group.member.more")
	public void onGroupMemberMoreButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		prepareAccountArgs(account, args, event.getUser());
		Container panel = uiProvider.panelOf(GROUP_MEMBER_MORE_PANEL_PATH, event.getGuild(), args);
		event.editComponents(panel).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "b.group.member.more.back")
	public void onGroupMemberMoreBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		Arguments arguments = Arguments.of("konto", args.get("group").asString());
		onGroupMembersButtonInteraction(event, arguments);
	}
	
	@InteractionCallback(id = "b.group.member.remove")
	public void onGroupMemberRemoveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AccountNotFoundException, ArgumentTypeException, UiParseException {
		BankAccount account = getAccount(args.get("konto"));
		GroupAccount group = (GroupAccount) getAccount(args.get("group"));
		
		boolean authorized = group.hasPolicy(AccountPolicies.ALLOW_MEMBERS_REMOVE_MEMBERS) || (group.isOwner(event.getUser().getIdLong()) && group.hasPolicy(AccountPolicies.ALLOW_REMOVE_MEMBERS));
		
		if(!authorized) {
			event.reply(les.getLocalizedExpression("error.group.remove-prohibited", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		List<BankAccount> members = group.getMembers();
		List<BankAccount> membersOwnedByUser = members.stream()				//Only Accounts owned by the current user
				.filter(member -> isOwnerOrManager(group, event.getUser()))
				.collect(Collectors.toList());
		
		if(members.isEmpty()) {
			event.reply(les.getLocalizedExpression("error.group.account-not-a-member", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//In case the group has just 1 member, send error message
		if(members.size() == 1) {
			event.reply(les.getLocalizedExpression("error.group.leave-last", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		//In case the selected account is the last account of the owner, send error message
		if(membersOwnedByUser.size() <= 1 && isOwnerOrManager(account, event.getUser())) {
			event.reply(les.getLocalizedExpression("error.group.transfer-ownership", event.getGuild())).setEphemeral(true).queue();
			return;
		}
				
		args.put("name", account.getName());
		bankService.removeMember(group, account).commit();
		Container accountPanel = getKontoPanel(account, args, event.getGuild(), event.getUser());
		event.editComponents(accountPanel).useComponentsV2().queue();
	}
	
	private Arguments prepareMembersArgs(GroupAccount account, Arguments args, User user) {
		prepareGroupArgs(account, args, user);
		Arguments[] argList = account.getMembers()
				.stream()
				.map(member -> prepareAccountArgs(member, Arguments.empty(), user).put("group", account.getId()))
				.collect(Collectors.toList())
				.toArray(new Arguments[0]);	
		
		return args.put("members", argList);
	}
}
