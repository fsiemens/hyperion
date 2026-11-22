package de.fabiansiemens.hyperion.persistence.roll.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RollPresetRepository extends JpaRepository<RollPresetEntity, Long> {

	public Page<RollPresetEntity> findAllByUserId(Long userId, Pageable pageable);
}
