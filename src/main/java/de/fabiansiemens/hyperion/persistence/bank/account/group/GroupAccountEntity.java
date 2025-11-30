package de.fabiansiemens.hyperion.persistence.bank.account.group;

import java.util.List;
import java.util.Map;

import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupAccountEntity extends BankaccountEntity {
	
	@NonNull
	@Nonnull
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<BankaccountEntity> members;
	
	public GroupAccountEntity(
			long owner, 
			double balance, 
			@NonNull String name,
			@NonNull Map<AccountPolicies, Boolean> policy, 
			@NonNull List<AccountLogEntity> logs,
			@NonNull List<BankaccountEntity> members,
			@NonNull List<Long> managerIds) {
		
		super(owner, balance, name, policy, logs, managerIds);
		this.members = members;
	}
	
	@Override
	public Double getBalance() {
		Double sum = 0.0;
		
		for(BankaccountEntity member : this.members) {
			sum += member.getBalance();
		}
		
		return sum;
	}
}
