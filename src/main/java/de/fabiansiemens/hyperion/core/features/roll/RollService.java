package de.fabiansiemens.hyperion.core.features.roll;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.features.roll.dto.RollPresetDto;
import de.fabiansiemens.hyperion.core.features.roll.v2.ExpectedValueEvaluator;
import de.fabiansiemens.hyperion.core.features.roll.v2.RollCacheEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.RollEvaluator;
import de.fabiansiemens.hyperion.core.features.roll.v2.RollSessionMapper;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.persistence.roll.RollSessionEntity;
import de.fabiansiemens.hyperion.persistence.roll.RollSessionRepository;
import de.fabiansiemens.hyperion.persistence.roll.repo.RollPresetEntity;
import de.fabiansiemens.hyperion.persistence.roll.repo.RollPresetRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

@Slf4j
@Service
@AllArgsConstructor
public class RollService {

	private final RollSessionMapper mapper;
	private final ExpectedValueEvaluator avgEvaluator;
	private final RollSessionRepository repo;
	private final RollPresetRepository presetRepo;
	
	@Data
	@AllArgsConstructor
	public class ResultReplyBundle {
		private final ExecutionNode result;
		private final String reply;
	}
	
	public ExecutionNode evaluate(String prompt) {
		RollEvaluator eval = new RollEvaluator();
		return eval.evaluate(prompt);
	}
	
	@Transactional
	public void save(String prompt, ExecutionNode root, User user, Message message) {
		RollSessionEntity session = RollSessionEntity.of(
		        user.getIdLong(),
		        message.getGuild().getIdLong(),
		        message.getIdLong(),
		        prompt,
		        root
		    );
		
		repo.save(session);
	}
	
	public double average(String prompt) {
		return avgEvaluator.expectedValue(prompt);
	}
	
	public long countRollSessions(User user) {
		return repo.countByDiscordUserId(user.getIdLong());
	}
	
	public Optional<Double> averagDiceResult(User user, int sides) {
		return repo.avgDiceValueByUserAndSides(user.getIdLong(), sides);
	}
	
	@Transactional
	public RollCacheEntry load(long sessionId) {
	    RollSessionEntity session = repo.findById(sessionId)
	        .orElseThrow(() -> new NoSuchElementException("Session " + sessionId + " nicht gefunden."));
	    return new RollCacheEntry(mapper.toExecutionNode(session), session.getDiscordUserId());
	}
	
	@Transactional
	public Optional<RollCacheEntry> loadByMessageId(long messageId) {
	    Optional<RollSessionEntity> session = repo.findByMessageId(messageId);
	    
	    if(session.isEmpty()) 
	    	return Optional.empty();
	    
	    ExecutionNode node = mapper.toExecutionNode(session.get());
	    return Optional.of(new RollCacheEntry(node, session.get().getDiscordUserId()));
	}
	
	@Transactional
	public RollPresetDto saveRollPreset(RollPresetDto dto) {
		return mapPresetEntity(presetRepo.save(mapPresetDto(dto)));
	}
	
	public Optional<RollPresetDto> findRollPresetById(long id) {
		return presetRepo.findById(id).map(this::mapPresetEntity);
	}
	
	@Transactional
	public Page<RollPresetDto> findAllRollPresets(User user, Pageable pageable) {
		return presetRepo.findAllByUserId(user.getIdLong(), pageable)
				.map(this::mapPresetEntity);
	}
	
	@Transactional
	public void deleteRollPreset(Long id) {
		presetRepo.deleteById(id);
	}
	
	private RollPresetDto mapPresetEntity(RollPresetEntity entity) {
		return RollPresetDto.builder()
				.id(entity.getId())
				.userId(entity.getUserId())
				.name(entity.getName())
				.prompt(entity.getPrompt())
				.build();
	}
	
	private RollPresetEntity mapPresetDto(RollPresetDto dto) {
		return RollPresetEntity.builder()
				.id(dto.getId())
				.userId(dto.getUserId())
				.name(dto.getName())
				.prompt(dto.getPrompt())
				.build();
	}

	
}
