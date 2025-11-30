package de.fabiansiemens.hyperion.ui.embeds.inventory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Embed;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.inventory.InventoryFolder;
import de.fabiansiemens.hyperion.core.features.inventory.InventoryFolderView;
import de.fabiansiemens.hyperion.core.features.inventory.InventoryItem;
import de.fabiansiemens.hyperion.core.util.asciitable.DataType;
import de.fabiansiemens.hyperion.core.util.asciitable.FormatService;
import de.fabiansiemens.hyperion.core.util.asciitable.TableColumn;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryFolderEntity;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryItemEntity;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryDeleteElementButton;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryEditElementButton;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryFolderBackButton;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryMoveElementButton;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryPageBackButton;
import de.fabiansiemens.hyperion.ui.buttons.inventory.InventoryPageForwardButton;
import de.fabiansiemens.hyperion.ui.embeds.EmbedBase;
import io.micrometer.common.lang.NonNull;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@Embed(dataFile = "inventory/InventoryFolderEmbed.json")
public class InventoryFolderEmbed extends EmbedBase<InventoryFolderView> {

	@Value("${inv.content-width}")
	private int contentWidth;
	
	private final FormatService formatService;
//	private final InventoryElementSelect contentSelect;
	private final InventoryFolderBackButton folderBackButton;
	private final InventoryPageBackButton pageBackButton;
	private final InventoryPageForwardButton pageForwardButton;
	private final InventoryEditElementButton editElementButton;
	private final InventoryMoveElementButton moveElementButton;
	private final InventoryDeleteElementButton deleteElementButton;
//	private final InventoryAddElementSelect addElementSelect;
	
	public InventoryFolderEmbed(
			ApplicationContext context, 
				FormatService formatService,
//				InventoryElementSelect contentSelect,
				InventoryFolderBackButton folderBackButton,
				InventoryPageBackButton pageBackButton,
				InventoryPageForwardButton pageForwardButton,
				InventoryEditElementButton editElementButton,
				InventoryMoveElementButton moveElementButton,
				InventoryDeleteElementButton deleteElementButton
//				InventoryAddElementSelect addElementSelect
				)
			throws IOException, IllegalArgumentException {
		super(InventoryFolderEmbed.class, context);
		this.formatService = formatService;
//		this.contentSelect = contentSelect;
		this.folderBackButton = folderBackButton;
		this.pageBackButton = pageBackButton;
		this.pageForwardButton = pageForwardButton;
		this.editElementButton = editElementButton;
		this.moveElementButton = moveElementButton;
		this.deleteElementButton = deleteElementButton;
//		this.addElementSelect = addElementSelect;
	}

	public EmbedBuilder getBuilder(@NonNull User user, @NonNull InventoryFolderView folderView, @Nullable Guild guild) {
		InventoryFolder folder = folderView.getFolder();
		int page = folderView.getPage();
		
		EmbedBuilder builder = super.getBuilder(guild);
		builder.setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
			.addField(les.getLocalizedExpression(".inventory.folder", guild), "`" + (folder.getParentFolder() == null ? "~" : folder.getParentFolder().getName()) + "`", true)
			.addField(les.getLocalizedExpression(".inventory.description", guild), "`" + folder.getDescription() + "`", true)
			.addBlankField(false)
			.addField(les.getLocalizedExpression(".inventory.breadcrumps", guild), "`" + folder.getBreadcrumbs() + "`", false)
			.setFooter(les.getLocalizedExpression(".inventory.folder-footer", guild));
		
		addContentField(builder, user, guild, folder, page);
		return builder;
	}
	
	public void addContentField(EmbedBuilder builder, User user, Guild guild, InventoryFolder folder, int page){
		List<InventoryFolder> folders = folder.getSubFolders();
		List<InventoryItem> items = folder.getItems();
		
		// TODO TEST ONLY
		folders.add(new InventoryFolder(new InventoryFolderEntity("Secret Loot", "Kann man nicht öffnen (noch)", folder.getEntity())));
		items.add(new InventoryItem(new InventoryItemEntity("Testobjekt", "Eine tolle Beschreibung", folder.getEntity(), 3, 42.69, true, false, null)));
		items.add(new InventoryItem(new InventoryItemEntity("Krasses Schwert mit viel zu langem Namen", "Eine tolle Beschreibung", folder.getEntity(), 1, 500_000_000, true, true, null)));
		// --
		
		int length = folders.size() + items.size();
		
		TableColumn indexCol = new TableColumn(les.getLocalizedExpression(".inventory.column.index", guild), DataType.NUMERIC, 2, false);
		TableColumn nameCol = new TableColumn(les.getLocalizedExpression(".inventory.column.name", guild), DataType.STRING, 40, true);
		TableColumn quantCol = new TableColumn(les.getLocalizedExpression(".inventory.column.quantity", guild), DataType.NUMERIC, 8, false);
		TableColumn priceCol = new TableColumn(les.getLocalizedExpression(".inventory.column.price", guild), DataType.NUMERIC, 8, false);
		TableColumn attunementCol = new TableColumn(les.getLocalizedExpression(".inventory.column.attunement", guild), DataType.STRING, 3, false);
		TableColumn[] headers = {indexCol, nameCol, quantCol, priceCol, attunementCol};
		
		String[][] rows = new String[length][5];
		int c = 0;
		for(int i = 0; i < folders.size() && c < length; i++) {
			InventoryFolder subFolder = folders.get(i);
			rows[c][0] = String.valueOf(c + 1);
			rows[c][1] = "📁 " + subFolder.getName();
			rows[c][2] = String.valueOf( subFolder.getChildCount() );
			rows[c][3] = String.valueOf( subFolder.getValue() );
			rows[c][4] = "";
			c++;
		}
		
		for(int i = 0; i < items.size() && c < length; i++) {
			InventoryItem item = items.get(i);
			rows[c][0] = String.valueOf(c + 1);
			rows[c][1] = "📦 " + item.getName();
			rows[c][2] = String.valueOf( item.getQuantity() );
			rows[c][3] = String.valueOf( item.getPrice() );
			rows[c][4] = (!item.isRequiresAttunement() ? "" : (item.isAttuned() ? "🔗" : "oo"));
			c++;
		}
		
		String content = "```" + formatService.formatTable(headers, rows, contentWidth) + "```";
		builder.addField(les.getLocalizedExpression(".inventory.content", guild), content, false);
	}
	
	@Override
	public List<MessageTopLevelComponent> getActionComponents(InventoryFolderView folderView, Guild guild) throws UiParseException {
		List<MessageTopLevelComponent> components = super.getActionComponents(folderView, guild);
		InventoryFolder folder = folderView.getFolder();
		
		//Dropdown Select --> Content Selector (max 25)
		List<SelectOption> contentOptions = new LinkedList<>();
		folder.getSubFolders().stream().forEach(t -> {
			SelectOption option = SelectOption.of(formatService.shorten(t.getName(), SelectOption.LABEL_MAX_LENGTH), String.valueOf( t.getId() ));
			contentOptions.add(
				option.withEmoji(Emoji.fromFormatted("📁"))
					.withDescription(formatService.shorten(t.getDescription(), SelectOption.DESCRIPTION_MAX_LENGTH))
			);
		});
		
		folder.getItems().stream().forEach(t -> {
			SelectOption option = SelectOption.of(formatService.shorten(t.getName(), SelectOption.LABEL_MAX_LENGTH), String.valueOf( t.getId() ));
			contentOptions.add(
				option.withEmoji(Emoji.fromFormatted("📦"))
					.withDescription(formatService.shorten(t.getDescription(), SelectOption.DESCRIPTION_MAX_LENGTH))
			);
		});
		
		List<SelectOption> options = new LinkedList<>();
		if(folderView.getLength() * folderView.getPage() < contentOptions.size())
			options = contentOptions.subList(folderView.getLength() * folderView.getPage(), Math.min(folderView.getLength() * (folderView.getPage() +1), contentOptions.size()));
		
//		StringSelectMenu contentSelectMenu = this.contentSelect.get(guild, options);
//		components.add(ActionRow.of(contentSelectMenu));
		
		
		//Button Row Navigation --> Folder Back, Page Back, Page Fwd
		components.add(ActionRow.of(
			folderBackButton.get(guild, folder.getParentFolder() != null),
			pageBackButton.get(guild, folderView.getPage() <= 0),
			pageForwardButton.get(guild, (folderView.getPage() +1) * folderView.getLength() < contentOptions.size())
		));
		
		//Button Row Modification --> Edit, Move, Delete
		components.add(ActionRow.of(
			editElementButton.get(guild),
			moveElementButton.get(guild),
			deleteElementButton.get(guild)
		));
		
		//Dropdown Select --> Add Content Selector (Option 1: Folder, Option 2: Item) 
//		components.add(ActionRow.of(addElementSelect.get(guild, addElementSelect.getOptions(guild))));
		
		return components;
	}
}
