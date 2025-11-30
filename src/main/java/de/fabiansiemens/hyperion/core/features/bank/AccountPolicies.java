package de.fabiansiemens.hyperion.core.features.bank;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public enum AccountPolicies {
	//COMMON POLICIES
	ALLOW_DEBT(					"@{.konto.policies.debt.title}@", 				"@{.konto.policies.debt.desc}@", 			true),
	ALLOW_DEPOSIT(				"@{.konto.policies.deposit.title}@", 			"@{.konto.policies.deposit.desc}@", 		true),
	ALLOW_RECEIVING_TRANSFER(	"@{.konto.policies.receive-transfer.title}@",	"@{.konto.policies.receive-transfer.desc}@",true),
	ALLOW_SENDING_TRANSFER(		"@{.konto.policies.send-transfer.title}@", 		"@{.konto.policies.send-transfer.desc}@", 	true),
	ALLOW_WITHDRAWAL(			"@{.konto.policies.withdraw.title}@", 			"@{.konto.policies.withdraw.desc}@", 		true),
	ALLOW_NAME_CHANGE(			"@{.konto.policies.name-change.title}@", 		"@{.konto.policies.name-change.desc}@", 	true),
	ALLOW_POLICY_CHANGE(		"@{.konto.policies.policy-change.title}@", 		"@{.konto.policies.policy-change.desc}@", 	true),
	ALLOW_VIEW_LOGS(			"@{.konto.policies.view-logs.title}@", 			"@{.konto.policies.view-logs.desc}@", 		true),
	SAVE_LOGS(					"@{.konto.policies.save-logs.title}@", 			"@{.konto.policies.save-logs.desc}@", 		true),
	
	//GROUP POLICIES
	ALLOW_JOIN(						"@{.group.policies.join.title}@", 					"@{.group.policies.join.desc}@", 					false, 	true),
	ALLOW_LEAVE(					"@{.group.policies.leave.title}@", 					"@{.group.policies.leave.desc}@", 					true, 	true),
	ALLOW_ADD_MEMBERS(				"@{.group.policies.member.add.title}@", 			"@{.group.policies.member.add.desc}@", 				true, 	true),
	ALLOW_REMOVE_MEMBERS(			"@{.group.policies.member.remove.title}@", 			"@{.group.policies.member.remove.desc}@", 			true, 	true),
	ALLOW_MEMBER_DEPOSIT(			"@{.group.policies.member.deposit.title}@", 		"@{.group.policies.member.deposit.desc}@", 			true, 	true),
	ALLOW_MEMBER_TRANSFER(			"@{.group.policies.member.transfer.title}@", 		"@{.group.policies.member.transfer.desc}@", 		true, 	true),
	ALLOW_MEMBER_WITHDRAW(			"@{.group.policies.member.withdraw.title}@", 		"@{.group.policies.member.withdraw.desc}@", 		true, 	true),
	ALLOW_MEMBER_NAME_CHANGE(		"@{.group.policies.member-name-change.title}@", 	"@{.group.policies.member-name-change.desc}@", 		false, 	true),
	ALLOW_MEMBER_POLICY_CHANGE(		"@{.group.policies.member-policy-change.title}@", 	"@{.group.policies.member-policy-change.desc}@", 	false, 	true),
	ALLOW_MEMBERS_ADD_MEMBERS(		"@{.group.policies.member.add.title}@", 			"@{.group.policies.member.add.desc}@", 				false, 	true),
	ALLOW_MEMBERS_REMOVE_MEMBERS(	"@{.group.policies.member.members-remove.title}@", 	"@{.group.policies.member.members-remove.desc}@", 	false, 	true),
	SHOW_MEMBER_BALANCE(			"@{.group.policies.show-balance.title}@", 			"@{.group.policies.show-balance.title}@", 			true, 	true)
	;
	
	public static Map<AccountPolicies, Boolean> withDefaults() {
		Map<AccountPolicies, Boolean> policies = new HashMap<>();
		for(AccountPolicies policy : values()) {
			policies.put(policy, policy.getDefault());
		}
		return policies;
	}
	
	private final boolean defaultValue;
	private final boolean isGroup;
	@Getter
	private final String title;
	@Getter
	private final String description;
	
	private AccountPolicies(String title, String description, boolean defaultValue) {
		this(title, description, defaultValue, false);
	}
	
	private AccountPolicies(String title, String description, boolean defaultValue, boolean isGroup) {
		this.defaultValue = defaultValue;
		this.isGroup = isGroup;
		this.title = title;
		this.description = description;
	}
	
	public boolean getDefault() {
		return this.defaultValue;
	}
	
	public boolean isGroupPolicy() {
		return this.isGroup;
	}
}
