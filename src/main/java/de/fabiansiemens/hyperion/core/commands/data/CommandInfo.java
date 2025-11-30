package de.fabiansiemens.hyperion.core.commands.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Data
public class CommandInfo {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LegacyData{
		private boolean deleteInit;
		private String memberPermission;
		
		public static LegacyData fromDefault() {
			return new LegacyData(false, "");
		}
	}
	
	private final LegacyData legacyData;
	@NonNull
	private final SlashCommandData data;
	
	public String getName() {
		return data.getName();
	}
	
	public String getDescription() {
		return data.getDescription();
	}

}
