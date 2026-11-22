package de.fabiansiemens.hyperion.core.features.bank;

import de.fabiansiemens.hyperion.persistence.bank.log.GroupAccountLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GroupAccountLog extends AccountLog {
	
	private GroupAccountLogEntity entity;
	
	public GroupAccountLog(GroupAccountLogEntity entity) {
		super(entity);
		this.entity = entity;
	}
	
	public Long getAuthorizer() {
		return entity.getAuthorizer();
	}
	
	public void setAuthorizer(Long authorizer) {
		this.entity.setAuthorizer(authorizer);
	}
}
