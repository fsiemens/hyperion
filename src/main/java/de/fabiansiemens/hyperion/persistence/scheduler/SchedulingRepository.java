package de.fabiansiemens.hyperion.persistence.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulingRepository extends JpaRepository<TaskEntity, Long>{

	
}
