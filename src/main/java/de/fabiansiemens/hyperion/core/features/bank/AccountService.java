package de.fabiansiemens.hyperion.core.features.bank;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.bank.account.AccountPersistenceService;
import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import de.fabiansiemens.hyperion.persistence.bank.account.group.GroupAccountEntity;
import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;
import de.fabiansiemens.hyperion.persistence.bank.log.GroupAccountLogEntity;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

@Slf4j
@Service
@Primary
@Transactional
@AllArgsConstructor
public class AccountService {

	@Data
	public class UpdateAction {
		@NonNull
		private List<BankAccount> accounts;
		
		public UpdateAction() {
			this.accounts = new LinkedList<>();
		}
		
		public UpdateAction(BankAccount... accounts) {
			this.accounts = new LinkedList<>(Arrays.asList(accounts));
		}
		
		public UpdateAction(UpdateAction... actions) {
			this.accounts = new LinkedList<>();
			for(UpdateAction action : actions)
				this.accounts.addAll(action.getAccounts());
		}
		
		public void commit() {
			for(BankAccount acc : accounts)
				updateAccount(acc);
		}
	}
	
	protected final AccountPersistenceService persistenceService;
	
	@Transactional
	public UpdateAction addMember(GroupAccount group, BankAccount account) {
		if(group.hasMemberWithId(account.getId()) || group.getId().equals(account.getId()))
			return new UpdateAction();
		
		group.getEntity().getMembers().add(account.getEntity());
		return new UpdateAction(group);
	}
	
	@Transactional
	public UpdateAction removeMember(GroupAccount group, BankAccount account) {
		if(!group.hasMemberWithId(account.getId())) {
			log.info("Provided account is not a member of the group");
			return new UpdateAction();
		}
			
		
		boolean removed = group.getEntity().getMembers().removeIf(member -> member.getId().equals(account.getId()));
		log.info("Removed: {}", removed);
		return new UpdateAction(group);
	}
	
	@Transactional
	public BankAccount createAccount(User owner, String name) {
		return new BankAccount(persistenceService.createAccount(
				owner.getIdLong(), name, AccountPolicies.withDefaults(), 
				AccountLog.fromDefault().stream().map(AccountLog::getEntity).collect(Collectors.toList()))
			);
	}
	
	@Transactional
	public GroupAccount createGroup(@NonNull User user, @NonNull String name) {
		List<BankaccountEntity> members = new LinkedList<>();
		return new GroupAccount(persistenceService.createGroup(user.getIdLong(), name, AccountPolicies.withDefaults(), new LinkedList<>(), members));
	}
	
	@Transactional
	public AccountLog createAccountLog(BankAccount owner, double amount, String receiver, String sender, String usage, boolean inbound) {
		if(receiver == null || receiver.isBlank())
			receiver = "-";
		
		if(sender == null || sender.isBlank())
			sender = "-";
		
		if(usage == null || usage.isBlank())
			usage = "-";
		
		return new AccountLog(new AccountLogEntity(owner.getEntity(), amount, receiver, sender, usage, inbound));
	}
	
	@Transactional
	public GroupAccountLog createGroupLog(@NonNull GroupAccount account, @NonNull Double amount, @NonNull String receiver,
			@NonNull String sender, @NonNull String usage, boolean inbound, User authorizer) {
		if(receiver == null || receiver.isBlank())
			receiver = "-";
		
		if(sender == null || sender.isBlank())
			sender = "-";
		
		if(usage == null || usage.isBlank())
			usage = "-";
		
		return new GroupAccountLog(new GroupAccountLogEntity(account.getEntity(), amount, receiver, sender, usage, inbound, authorizer.getIdLong()));
	}
	
	@Transactional
	public Page<AccountLog> findPaginatedLogs(BankAccount account, Pageable pageable) {
		Page<AccountLogEntity> logs = persistenceService.findPaginatedLogsByAccountId(account.getEntity(), pageable);
		List<AccountLog> mapped = new LinkedList<>();
		for(AccountLogEntity log : logs.getContent()) {
			if(log instanceof GroupAccountLogEntity gale) {
				mapped.add(new GroupAccountLog(gale));
				continue;
			}
			
			mapped.add(new AccountLog(log));
		}
		
		return new PageImpl<>(
			mapped,
			logs.getPageable(),
			logs.getTotalElements()
		);
	}

	@Transactional
	public boolean hasGroup(@NonNull BankAccount account) {
		return persistenceService.existsGroupByMemberId(account.getId());
	}
	
	@Transactional
	public boolean hasAccount(@NonNull User user) {
		return persistenceService.existsAccountWithOwnerId(user.getIdLong()) || persistenceService.existsAccountWithManagerId(user.getIdLong());
	}
	
	@Transactional
	public List<BankAccount> findAccountsFromOwner(User owner) {
		return persistenceService.findByOwnerId(owner.getIdLong())
				.stream()
				.map(BankAccount::new)
				.collect(Collectors.toList());
	}
	
	@Transactional
	public List<BankAccount> findAccountsWithOwnerOrManager(User user) {
		return persistenceService.findAll()
				.stream()
				.filter(account -> account.getOwnerId().equals(user.getIdLong()) || account.getManagerIds().contains(user.getIdLong()))
				.map(BankAccount::new)
				.collect(Collectors.toList());
	}

	@Transactional
	public Optional<BankAccount> findAccountWithId(long id) {
		Optional<BankaccountEntity> entity = persistenceService.findById(id);
		if(entity.isEmpty())
			return Optional.empty();
		
		if(entity.get() instanceof GroupAccountEntity) {
			return Optional.of(new GroupAccount((GroupAccountEntity) entity.get()));
		}
		
		return Optional.of(new BankAccount(entity.get()));
	}
	
	@Transactional
	public Page<BankAccount> findPaginatedOwnedAccounts(User user, Pageable pageable) {
		Page<BankaccountEntity> accounts = persistenceService.findPaginatedAccountsWithOwner(user.getIdLong(), pageable);
		List<BankAccount> mapped = accounts.getContent()
				.stream()
				.map(BankAccount::new)
				.collect(Collectors.toList());
		
		return new PageImpl<>(
			mapped,
			accounts.getPageable(),
			accounts.getTotalElements()
		);
	}
	
	@Transactional
	public Page<BankAccount> findPaginatedManagedAccounts(User user, Pageable pageable) {
		Page<BankaccountEntity> accounts = persistenceService.findPaginatedAccountsWithManager(user.getIdLong(), pageable);
		List<BankAccount> mapped = accounts.getContent()
				.stream()
				.map(BankAccount::new)
				.collect(Collectors.toList());
		
		return new PageImpl<>(
			mapped,
			accounts.getPageable(),
			accounts.getTotalElements()
		);
	}
	
	@Transactional
	public Page<GroupAccount> findPaginatedGroupsWithUser(User user, Pageable pageable) {
		Page<GroupAccountEntity> accounts = persistenceService.findPaginatedGroupsWithMember(user.getIdLong(), pageable);
		List<GroupAccount> mapped = accounts.getContent()
				.stream()
				.map(GroupAccount::new)
				.collect(Collectors.toList());
		
		return new PageImpl<>(
			mapped,
			accounts.getPageable(),
			accounts.getTotalElements()
		);
	}

	@Transactional
	public List<GroupAccount> findGroupsWithMember(@NonNull BankAccount account) {
		return persistenceService.findByMemberId(account.getId())
				.stream()
				.map(GroupAccount::new)
				.collect(Collectors.toList());
	}
	
	@Transactional
	public Optional<AccountLog> findLogById(long id) {
		Optional<AccountLogEntity> log = persistenceService.findLogById(id);
		if(log.isEmpty())
			return Optional.empty();
		
		if(log.get() instanceof GroupAccountLogEntity gale) {
			return Optional.of(new GroupAccountLog(gale));
		}
		
		return Optional.of(new AccountLog(log.get()));
	}

	@Transactional
	public UpdateAction deposit(final @NonNull BankAccount account, final @NonNull String sender, final @NonNull Double amount, final @NonNull String usage, final @NonNull User user) throws IllegalArgumentException {
		if(!Double.isFinite(amount) || amount <= 0)
			throw new IllegalArgumentException("error.konto.transfer.amount-negative");
		
		if(account instanceof GroupAccount g && g.getMembers().isEmpty())
			throw new IllegalArgumentException("error.group.no-members");

		if(account.hasPolicy(AccountPolicies.SAVE_LOGS)) {
			AccountLog log = createAccountLog(account, amount, "@{.konto.this}@", sender, usage, true);
			account.addLogEntry(log);
		}
		
		if(!(account instanceof GroupAccount)) {
			account.setBalance(account.getBalance() + amount);
			return new UpdateAction(account);
		}
		
		
		GroupAccount group = (GroupAccount) account;
		GroupAccountLog log = createGroupLog(group, amount, "@{.konto.this}@", sender, usage, true, user);
		group.addLogEntry(log);
		Double share = amount / group.getMembers().size();
		UpdateAction action = new UpdateAction(new BankAccount[0]);
		for(BankAccount member : group.getMembers()) {
			action.accounts.addAll( deposit(member, sender + " (via " + group.getName() + ")", share, usage, user).getAccounts());
		}
		action.accounts.add(group);
		return action;
	}
	

	@Transactional
	public UpdateAction transfer(final @NonNull BankAccount sender, final @NonNull BankAccount receiver, final @NonNull Double amount, final @NonNull String usage, final @NonNull User user) throws IllegalArgumentException {
		if(!Double.isFinite(amount) || amount <= 0)
			throw new IllegalArgumentException("error.konto.transfer.amount-negative");
		
		UpdateAction a = withdraw(sender, receiver.getName() + " (" + receiver.getId() + ")", amount, usage, user);
		UpdateAction b = deposit(receiver, sender.getName() + " (" + sender.getId() + ")", amount, usage, user);
		
		return new UpdateAction(a, b);
	}
	
	@Transactional
	public UpdateAction withdraw(final @NonNull BankAccount account, final @NonNull String receiver, final @NonNull Double amount, final @NonNull String usage, final @NonNull User user) throws IllegalArgumentException {
		if(!Double.isFinite(amount) || amount <= 0)
			throw new IllegalArgumentException("error.konto.transfer.amount-negative");
		
		if(account instanceof GroupAccount g && g.getMembers().isEmpty())
			throw new IllegalArgumentException("error.group.no-members");

		if(account.hasPolicy(AccountPolicies.SAVE_LOGS)) {
			AccountLog log = createAccountLog(account, amount, receiver, "@{.konto.this}@", usage, false);
			account.addLogEntry(log);
		}
		
		if(!(account instanceof GroupAccount)) {
			account.setBalance(account.getBalance() - amount);
			if(account.getBalance() < 0 && !account.hasPolicy(AccountPolicies.ALLOW_DEBT))
				throw new IllegalArgumentException("error.konto.transfer.overrun");
			return new UpdateAction(account);
		}
		
		GroupAccount group = (GroupAccount) account;
		GroupAccountLog log = createGroupLog(group, amount, receiver, "@{.konto.this}@", usage, false, user);
		group.addLogEntry(log);
		Double share = amount / group.getMembers().size();
		UpdateAction action = new UpdateAction(new BankAccount[0]);
		for(BankAccount member : group.getMembers()) {
			action.accounts.addAll( withdraw(member, receiver + " (via " + group.getName() + ")", share, usage, user).getAccounts() );
		}
		action.accounts.add(group);
		return action;
	}
	
	@Transactional
	public UpdateAction setPolicyState(@NonNull BankAccount account, @NonNull AccountPolicies policies, boolean state) {
		account.getPolicies().put(policies, state);
		return new UpdateAction(account);
	}

	@Transactional
	public BankAccount updateAccount(@NonNull BankAccount account) {
		return new BankAccount(this.persistenceService.update(account.getEntity()));
	}

	@Transactional
	public void deleteAccount(@NonNull BankAccount account) {
		this.persistenceService.delete(account.getEntity());
	}

	@Transactional
	public UpdateAction changeOwner(@NonNull BankAccount account, @NonNull Long ownerId) {
		account.setOwnerId(ownerId);
		return new UpdateAction(account);
	}
	
	@Transactional
	public UpdateAction addManager(@NonNull BankAccount account, @NonNull User manager) {
		if(account.hasManager(manager.getIdLong()))
			return new UpdateAction();
		log.info("Adding Manager {} to account {}", manager.getIdLong(), account.getId());
		Set<Long> accounts = account.getManagerIds();
		accounts.add(manager.getIdLong());
		account.setManagerIds(accounts);
		return new UpdateAction(account);
	}
	
	@Transactional
	public UpdateAction removeManager(@NonNull BankAccount account, @NonNull Long managerId) {
		if(!account.hasManager(managerId))
			return new UpdateAction();
		
		Set<Long> accounts = account.getManagerIds();
		accounts.remove(managerId);
		account.setManagerIds(accounts);
		return new UpdateAction(account);
	}
}
