package de.fabiansiemens.hyperion.persistence.bank.log;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import de.fabiansiemens.hyperion.persistence.bank.account.BankaccountEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class AccountLogEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NonNull
	@ManyToOne
	@JoinColumn(name = "owner_id")
	private BankaccountEntity owner;
	
	@CreationTimestamp
	private Timestamp timestamp;
	
	@NonNull
	private Double amount;
	
	@NonNull
	private String receiver;
	
	@NonNull
	private String sender;
	
	@NonNull
	private String usage;
	
	@NonNull
	private Boolean inbound;
	
	public boolean isInbound() {
		if(inbound == null)
			return receiver.strip().equalsIgnoreCase("@{.konto.this}@");
		
		return this.inbound;
	}
}
