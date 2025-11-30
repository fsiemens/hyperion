package de.fabiansiemens.hyperion.core.features.audio;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackRequestData {
	private Date requestInstant;
	private Long memberId;
}
