package de.fabiansiemens.hyperion.persistence.bank.account;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fabiansiemens.hyperion.persistence.bank.log.AccountLogEntity;

@Primary
@Repository
public interface BankAccountRepository extends JpaRepository<BankaccountEntity, Long> {

	public boolean existsByOwnerId(long id);

	public List<BankaccountEntity> findByOwnerId(long idLong);

	@Query("""
	       SELECT log 
	       FROM BankaccountEntity acc 
	       JOIN acc.logs log 
	       WHERE acc.id = :accountId
	       ORDER BY log.id DESC
	       """)
	Page<AccountLogEntity> findLogsByAccountId(
	        @Param("accountId") Long accountId,
	        Pageable pageable);
	
	@Query("SELECT log FROM BankaccountEntity acc JOIN acc.logs log WHERE log.id = :id")
	Optional<AccountLogEntity> findLogById(@Param("id") Long id);
	
}
