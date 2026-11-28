package de.fabiansiemens.hyperion.persistence.draw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawRepository extends JpaRepository<DrawEntity, Long> {

}
