package de.fabiansiemens.hyperion.core.features.bank;

import java.util.List;
import java.util.stream.Collectors;

import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import de.fabiansiemens.hyperion.persistence.bank.account.group.GroupAccountEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupAccount extends BankAccount {
	
	private GroupAccountEntity entity;
	
	public GroupAccount(GroupAccountEntity entity) {
		super(entity);
		this.entity = entity;
	}
	
	@Override
	public void setBalance(Double balance) {
		throw new IllegalStateException("Setting group account balance is prohibited. Use AccountService#deposit()");
	}
	
	public List<BankAccount> getMembers() {
		return ((GroupAccountEntity) entity)
					.getMembers()
					.stream()
					.map(BankAccount::new)
					.collect(Collectors.toUnmodifiableList()); 
	}
	
	public void setMembers(List<BankAccount> members) {
		List<BankaccountEntity> entities = members.stream()
				.map(member -> member.getEntity())
				.collect(Collectors.toList());
		
		((GroupAccountEntity) entity).setMembers(entities);
	}

	public boolean hasMemberWithId(long id) {
		for(BankAccount acc : getMembers()) {
			log.info("Comparing acc id {} with id {}", acc.getId(), id);
			if(acc.getId().equals(id))
				return true;
		}
		log.info("Returning false in hasMemberWithId");
		return false;
	}
	
	public boolean hasMemberWithOwnerId(long idLong) {
		for(BankAccount acc : getMembers()) {
			if(acc.getOwnerId().equals(idLong))
				return true;
		}
		return false;
	}

	public boolean isOwner(long idLong) {
		return this.getOwnerId().equals(idLong);
	}
}
