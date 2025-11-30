package de.fabiansiemens.hyperion.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lombok.NonNull;

public class CrudPersistenceService<Entity, ID, Repository extends JpaRepository<Entity, ID>> {

	protected Repository repository;
	
	public CrudPersistenceService(Repository repos) {
		this.repository = repos;
	}
	
	public long count() {
		return repository.count();
	}
	
	public List<Entity> findAll() {
		return repository.findAll();
	}
	
	public Optional<Entity> findById(ID id) {
		return repository.findById(id);
	}
	
	public boolean exists(ID id) {
		return repository.existsById(id);
	}

	public Entity update(@NonNull Entity entity) {
		return repository.save(entity);
	}
	
	public void delete(@NonNull Entity entity) {
		repository.delete(entity);
	}
}
