package de.fabiansiemens.hyperion.persistence.bank.log;

import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupAccountLogEntity extends AccountLogEntity {

	public GroupAccountLogEntity(@NonNull BankaccountEntity owner, @NonNull Double amount, @NonNull String receiver, @NonNull String sender, @NonNull String usage, @NonNull Boolean inbound, @NonNull Long authorizerId) {
		super(owner, amount, receiver, sender, usage, inbound);
		this.authorizer = authorizerId;
	}
	
	@NonNull
	@Nonnull
	private Long authorizer;
	
}
