package de.fabiansiemens.hyperion.persistence.file;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(columnDefinition = "BYTEA")
	private byte[] data;
	
	@NonNull
	@Nonnull
	private String extension;
	
	public FileEntity(byte[] data, String extension) {
		this.data = data;
		this.extension = extension;
	}
}
