package de.fabiansiemens.hyperion.persistence.bank.account.group;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.fabiansiemens.hyperion.persistence.bank.account.BankAccountRepository;

public interface GroupAccountRepository extends BankAccountRepository {
	
	@Query("SELECT CASE WHEN COUNT(g) > 0 THEN TRUE ELSE FALSE END FROM GroupAccountEntity g JOIN g.members m WHERE m.id = :memberId")
	public boolean existsByMemberId(@Param("memberId") Long memberId);
	
	@Query("SELECT g FROM GroupAccountEntity g JOIN g.members m WHERE m.id = :memberId")
	public List<GroupAccountEntity> findByMemberId(@Param("memberId") Long memberId);
}
