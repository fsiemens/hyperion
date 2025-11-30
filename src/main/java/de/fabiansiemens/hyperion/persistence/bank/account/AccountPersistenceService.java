package de.fabiansiemens.hyperion.persistence.bank.account;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;
import de.fabiansiemens.hyperion.persistence.bank.account.group.GroupAccountEntity;
import de.fabiansiemens.hyperion.persistence.bank.account.group.GroupAccountRepository;
import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;
import jakarta.annotation.Nonnull;
import lombok.NonNull;

@Service
@Primary
public class AccountPersistenceService extends CrudPersistenceService<BankaccountEntity, Long, GroupAccountRepository> {
	
	public AccountPersistenceService(GroupAccountRepository repos) {
		super(repos);
	}

	public BankaccountEntity createAccount(Long userId, @NonNull String name, @NonNull Map<AccountPolicies, Boolean> policy, List<AccountLogEntity> logs) {
		BankaccountEntity account = new BankaccountEntity(userId, 0.0, name, policy, logs, new LinkedList<>());
		return repository.save(account);
	}
	
	public GroupAccountEntity createGroup(
			@NonNull Long ownerId, 
			@NonNull String name, 
			@NonNull Map<AccountPolicies, Boolean> policy, 
			@NonNull List<AccountLogEntity> logs, 
			@NonNull List<BankaccountEntity> members ) {
		GroupAccountEntity group = new GroupAccountEntity(ownerId, 0, name, policy, logs, members, new LinkedList<>());
		return repository.save(group);
	}
	
	@Nonnull
	public boolean existsGroupByMemberId(@NonNull Long id) {
		return repository.existsByMemberId(id);
	}
	
	@Nonnull
	public boolean existsAccountWithManagerId(long idLong) {
		return findAll()
				.stream()
				.anyMatch(account -> account.getManagerIds().contains(idLong));
	}
	
	@Nonnull
	public List<GroupAccountEntity> findByMemberId(Long id) {
		return repository.findByMemberId(id);
	}
	
	public Page<AccountLogEntity> findPaginatedLogsByAccountId(BankaccountEntity account, @NonNull Pageable pageable){
		return repository.findLogsByAccountId(account.getId(), pageable);
	}

	public Optional<AccountLogEntity> findLogById(long id) {
		return repository.findLogById(id);
	}
	
	public boolean existsAccountWithOwnerId(long id) {
		return repository.existsByOwnerId(id);
	}

	public List<BankaccountEntity> findByOwnerId(long idLong) {
		return repository.findByOwnerId(idLong);
	}
}
