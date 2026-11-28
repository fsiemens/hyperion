package de.fabiansiemens.hyperion.persistence.draw;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;

@Service
public class DrawPersistenceService extends CrudPersistenceService<DrawEntity, Long, DrawRepository> {

	public DrawPersistenceService(DrawRepository repos) {
		super(repos);
	}
}
