package de.fabiansiemens.hyperion.core.features.bank;

import de.fabiansiemens.hyperion.persistence.bank.log.GroupAccountLogEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class GroupAccountLog extends AccountLog {
	
	private GroupAccountLogEntity entity;
	
	public Long getAuthorizer() {
		return entity.getAuthorizer();
	}
	
	public void setAuthorizer(Long authorizer) {
		this.entity.setAuthorizer(authorizer);
	}
}
