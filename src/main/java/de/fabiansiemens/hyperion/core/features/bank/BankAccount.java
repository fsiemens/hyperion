package de.fabiansiemens.hyperion.core.features.bank;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
	protected BankaccountEntity entity;
	
	private Set<Long> cachedManagerIds;
	
	public BankAccount(BankaccountEntity entity) {
	    this.entity = entity;
	    // Lazy collections sofort auflösen solange Session offen ist
	    this.cachedManagerIds = new HashSet<>(entity.getManagerIds());
	}
	
	public Long getId() {
		return entity.getId();
	}
	
	public Long getOwnerId() {
		return entity.getOwnerId();
	}
	
	public Double getBalance() {
		return entity.getBalance();
	}
	
	public String getName() {
		return entity.getName();
	}
	
	public Map<AccountPolicies, Boolean> getPolicies() {
		return entity.getPolicies();
	}
	
	public List<AccountLog> getLogs() {
		return this.entity.getLogs()
				.stream()
				.map(AccountLog::new)
				.collect(Collectors.toList());
	}
	
	public Set<Long> getManagerIds() {
		return this.cachedManagerIds;
	}
	
	public void setOwnerId(Long ownerId) {
		this.entity.setOwnerId(ownerId);
	}
	
	public void setBalance(Double balance) {
		this.entity.setBalance(balance);
	}
	
	public void setName(String name) {
		this.entity.setName(name);
	}
	
	public void setPolicies(Map<AccountPolicies, Boolean> policy) {
		this.entity.setPolicies(policy);
	}
	
	public void setLogs(List<AccountLog> logs) {
		this.entity.setLogs(
				logs.stream()
				.map(AccountLog::getEntity)
				.collect(Collectors.toList())
			);
	}
	
	public void setManagerIds(Set<Long> managerIds) {
		this.entity.setManagerIds(managerIds);
		this.cachedManagerIds = new HashSet<>(managerIds);
	}

	public void addLogEntry(AccountLog logEntry) {
		this.entity.getLogs().add(logEntry.getEntity());
	}

	public boolean hasPolicy(AccountPolicies policy) {

		if(!(this instanceof GroupAccount) && policy.isGroupPolicy())
			return false;
		
		return this.getPolicies().getOrDefault(policy, false);
	}

	public void togglePolicy(AccountPolicies policy) {
		this.getPolicies().put(policy, !hasPolicy(policy));
	}

	public boolean hasManager(long id) {
	    return cachedManagerIds.contains(id);
	}
}
