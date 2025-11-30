package de.fabiansiemens.hyperion.persistence.bank.account;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;

import de.fabiansiemens.hyperion.core.features.bank.AccountPolicies;
import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class BankaccountEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Nonnull
	private Long ownerId;
	
	@Nonnull
	@ColumnDefault(value = "0")
	private Double balance;
	
	@NonNull
	@Nonnull
	private String name;
	
	@NonNull
	@Nonnull
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "account_policies", joinColumns = @JoinColumn(name = "id"))
	@MapKeyEnumerated(EnumType.STRING)
	@MapKeyColumn(name = "policy")
	@Column(name = "allowed")
	private Map<AccountPolicies, Boolean> policies;
//	@OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//	private AccountPolicyEntity policy;
	
	@NonNull
	@Nonnull
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<AccountLogEntity> logs;
	
	@NonNull
	@Nonnull
	private List<Long> managerIds;
	
	public Map<AccountPolicies, Boolean> getPolicies() {
		if(policies == null || policies.isEmpty())
			return AccountPolicies.withDefaults();
		
		return policies;
	}
	
	public List<Long> getManagerIds() {
		if(managerIds == null)
			return new LinkedList<Long>();
		
		return managerIds;
	}
}
