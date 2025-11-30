package de.fabiansiemens.hyperion.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("de.fabiansiemens.hyperion.persistence")
@EntityScan("de.fabiansiemens.hyperion.persistence")
public class PersistenceConfiguration {

}
