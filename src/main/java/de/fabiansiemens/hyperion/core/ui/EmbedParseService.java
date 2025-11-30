package de.fabiansiemens.hyperion.core.ui;

import org.springframework.stereotype.Service;

import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class EmbedParseService {
	
	@Data
	@AllArgsConstructor
	public static class EmbedParser {
		private EmbedBuilder builder;
		
		@Nullable
		public String getFieldValue(int fieldIndex) {
			if(builder.getFields().size() <= fieldIndex)
				return null;
			
			return builder.getFields().get(fieldIndex).getValue().replace("`", "");
		}
	}
	
	public EmbedParser createParser(MessageEmbed embed) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.copyFrom(embed);
		return createParser(builder);
	}
	
	public EmbedParser createParser(EmbedBuilder builder) {
		return new EmbedParser(builder);
	}
}
