package de.fabiansiemens.hyperion.core.features.bank;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.utils.TimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLog {
	private AccountLogEntity entity;
	
	public Long getId() {
		return this.entity.getId();
	}
	
	public Double getAmount() {
		return this.entity.getAmount();
	}
	
	public BankAccount getOwner() {
		return new BankAccount(this.entity.getOwner());
	}
	
	public String getSender() {
		return this.entity.getSender();
	}
	
	public String getReceiver() {
		return this.entity.getReceiver();
	}
	
	public String getUsage() {
		return this.entity.getUsage();
	}
	
	public Timestamp getTimestamp() {
		return this.entity.getTimestamp();
	}
	
	public void setAmount(Double amount) {
		this.entity.setAmount(amount);
	}
	
	public void setOwner(BankAccount account) {
		this.entity.setOwner(account.getEntity());
	}
	
	public void setSender(String sender) {
		this.entity.setSender(sender);
	}
	
	public void setReceiver(String receiver) {
		this.entity.setReceiver(receiver);
	}
	
	public void setUsage(String usage) {
		this.entity.setUsage(usage);
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.entity.setTimestamp(timestamp);
	}

	public static List<AccountLog> fromDefault() {
		return new LinkedList<AccountLog>();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("")
			.append(TimeFormat.RELATIVE.atInstant(getTimestamp().toInstant()))
			.append("`| @{.konto.input.amount}@: ")
			.append("%.2f".formatted(getAmount()))
			.append("; @{.common.from}@: ")
			.append(getSender())
			.append("; @{.common.to}@: ")
			.append(getReceiver())
			.append("; ")
			.append(getUsage())
			.append("`");
		return builder.toString();
	}

	public boolean isInbound() {
		return entity.isInbound();
	}
}
