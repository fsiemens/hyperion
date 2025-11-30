package de.fabiansiemens.hyperion.core.cli;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service(value = "launchArgumentService")
public class LaunchArgumentService {
	
	public final Environment env;
	
	public LaunchArgumentService(final Environment env) {
		this.env = env;
	}
	
	public boolean isDebugEnabled() {
		boolean debugFlag = env.getProperty("debug") != null || env.getProperty("d") != null || env.getProperty("spring.debug") != null;
		return debugFlag;
	}
}
