package de.fabiansiemens.hyperion.ui.embeds;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Embed;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.ui.InteractionBase;
import io.micrometer.common.lang.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Data
@Deprecated
@EqualsAndHashCode(callSuper = true)
public abstract class EmbedBase<D> extends InteractionBase {
	
	private final EmbedBuilder builder;
	
	public <T> EmbedBase(Class<T> clazz, ApplicationContext context) throws IOException, IllegalArgumentException {
		super(clazz, Embed.class, context);
		builder = super.getImportService().parseEmbedJson(getDataPath());
	}
	
	//TODO implement id
	@Override
	public String getId() {
		return "";
	}

	public EmbedBuilder getBuilder(@Nullable Guild guild) {
		EmbedBuilder newBuilder = new EmbedBuilder();
		newBuilder.copyFrom(builder);
		MessageEmbed embed = builder.build();
		if(embed.getAuthor() != null)
			newBuilder.setAuthor(les.replacePlaceholders(embed.getAuthor().getName(), guild), embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl());
		
		if(embed.getDescription() != null)
			newBuilder.setDescription(les.replacePlaceholders(embed.getDescription(), guild));
		
		if(embed.getFooter() != null)
			newBuilder.setFooter(les.replacePlaceholders(embed.getFooter().getText(), guild), embed.getFooter().getIconUrl());
		
		if(embed.getTitle() != null)
			newBuilder.setTitle(les.replacePlaceholders(embed.getTitle(), guild), embed.getUrl());
		return newBuilder;
	}
	
	public MessageEmbed get(@Nullable Guild guild) {
		return getBuilder(guild).build();
	}
	
	public List<MessageTopLevelComponent> getActionComponents(D data, @Nullable Guild guild) throws UiParseException {
		return new LinkedList<>();
	}
}
