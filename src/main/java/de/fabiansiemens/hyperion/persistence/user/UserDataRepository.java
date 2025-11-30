package de.fabiansiemens.hyperion.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataRepository extends JpaRepository<UserDataEntity, Long> {

}
