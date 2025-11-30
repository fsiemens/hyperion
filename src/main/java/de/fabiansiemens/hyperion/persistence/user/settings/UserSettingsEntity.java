package de.fabiansiemens.hyperion.persistence.user.settings;

import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import de.fabiansiemens.hyperion.persistence.safeguard.SafeguardEntity;
import de.fabiansiemens.hyperion.persistence.user.UserDataEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsEntity {
	
	@Id
	@Column(name = "SETTINGS_ID")
	private Long id;
	
	@OneToOne
	@PrimaryKeyJoinColumn(name = "SETTINGS_ID", referencedColumnName = "USER_ID")
	private UserDataEntity userData;
	
	private boolean useCritSettings;
	
	@Nullable
	private String critSuccessMessage;

	@Nullable
	private String critFailMessage;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private FileEntity critSuccessFile;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private FileEntity critFailFile;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private SafeguardEntity safeguard;
}
