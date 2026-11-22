package de.fabiansiemens.hyperion.core.features.roll;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.fabiansiemens.hyperion.core.features.roll.v2.RollCacheEntry;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Deprecated
public class RollCachingService {

	@Value("${roll.cache.maxsize}")
	private int maxSize;
	
	@Value("${roll.cache.timeout}")
	private int timeout;
	
	private Cache<Long, RollCacheEntry> rollCache;
	
	@PostConstruct
	public void init() {
		this.rollCache = CacheBuilder.newBuilder()
				.maximumSize(maxSize)
				.expireAfterWrite(timeout, TimeUnit.HOURS)
				.build();
	}
		       
	@Nullable
	public RollCacheEntry getRoll(@NonNull Long messageId){
		log.trace("Retrieving RollCacheEntries for messageId '{}'", messageId);
		return rollCache.getIfPresent(messageId);
	}
	
	public void putRoll(@NonNull Long messageId, @NonNull RollCacheEntry result) {
		log.debug("Cached RollCacheEntry for messageId '{}'", messageId);
		rollCache.put(messageId, result);
	}
}
