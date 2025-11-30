package de.fabiansiemens.hyperion.core.ui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.core.util.SpringExpressionService;
import de.fabiansiemens.hyperion.core.util.TriFunction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

@Slf4j
@Service
@AllArgsConstructor
public class XmlParseService {
	
	private final LocalizedExpressionService les;
	private final SpringExpressionService spes;
	
	public Container parseContainer(Element container, Arguments args, Guild guild) throws UiParseException {
		Collection<ContainerChildComponent> containerChilds = new LinkedList<>();
		NodeList children = container.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "separator":
				containerChilds.add(Separator.create(((Element) child).getAttribute("divider").equals("true"), Spacing.fromKey(Integer.parseInt(((Element) child).getAttribute("spacing")))));
				break;
			case "text-display":
				String forArg = ((Element) child).getAttribute("for");
				
				if(forArg == null || forArg.isBlank()) {
					containerChilds.add(parseTextDisplay((Element) child, args, guild));
					break;
				}
				
				try {
					Arguments[] argList = args.get(forArg).as(Arguments[].class);
					for(Arguments elem : argList) {
						containerChilds.add(parseTextDisplay((Element) child, elem, guild));
					}
				} catch (ArgumentTypeException e) {
					log.warn("For variable must be an array of Arguments; {}", e);
					containerChilds.add(parseTextDisplay((Element) child, args, guild));
				}
				break;
			case "section":
				forArg = ((Element) child).getAttribute("for");
				
				if(forArg == null || forArg.isBlank()) {
					containerChilds.add(parseSection((Element) child, args, guild));
					break;
				}
				
				try {
					Arguments[] argList = args.get(forArg).as(Arguments[].class);
					for(Arguments elem : argList) {
						containerChilds.add(parseSection((Element) child, elem, guild));
					}
				} catch (ArgumentTypeException e) {
					log.warn("For variable must be an array of Arguments; {}", e);
					containerChilds.add(parseSection((Element) child, args, guild));
				}
				
				break;
			case "action-row":
				containerChilds.add(parseActionRow((Element) child, args, guild));
				break;
			case "media-gallery":
				containerChilds.add(parseMediaGallery((Element) child, args, guild));
				break;
			case "file-display":
				containerChilds.add(parseFileDisplay((Element) child, args, guild));
				break;
			default: continue;
			}
		}
		
		return Container.of(containerChilds);
	}

	private TextDisplay parseTextDisplay(Element element, Arguments args, Guild guild) {
		StringBuilder sb = new StringBuilder();
		NodeList children = element.getChildNodes();
		
		boolean first = true;
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			if(!first)
				sb.append("\n");
			first = false;
			
			switch(child.getNodeName()) {
			case "text":
				sb.append(child.getTextContent());
				break;
			case "h1":
				sb.append("# ").append(child.getTextContent());
				break;
			case "h2":
				sb.append("## ").append(child.getTextContent());
				break;
			case "h3":
				sb.append("### ").append(child.getTextContent());
				break;
			case "code":
				sb.append("```").append(child.getTextContent()).append("```");
				break;
			case "subtext":
				sb.append("-# ").append(child.getTextContent());
				break;
			case "quote":
				sb.append("> ").append(child.getTextContent());
				break;
			default: continue;
			}
			
		}
		
		String text = sb.toString();
		String finalText = les.replacePlaceholders(text, guild, args);
		
		return TextDisplay.of(finalText);
	}
	
	private Section parseSection(Element element, Arguments args, Guild guild) throws UiParseException {
		SectionAccessoryComponent accessory = null;
		Collection<SectionContentComponent> contents = new LinkedList<>();
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "text-display":
				String forArg = ((Element) child).getAttribute("for");
				
				if(forArg == null || forArg.isBlank()) {
					contents.add(parseTextDisplay((Element) child, args, guild));
					break;
				}
				
				try {
					Arguments[] argList = args.get(forArg).as(Arguments[].class);
					for(Arguments elem : argList) {
						contents.add(parseTextDisplay((Element) child, elem, guild));
					}
				} catch (ArgumentTypeException e) {
					log.warn("For variable must be an array of Arguments; {}", e);
					contents.add(parseTextDisplay((Element) child, args, guild));
				}
				break;
			case "button":
				accessory = parseButton((Element) child, args, guild);
				break;
			case "thumbnail":
				accessory = parseThumbnail((Element) child, args, guild);
			default: continue;
			}
		}
	
		return Section.of(accessory, contents);
	}


	public Button parseButton(Element element, Arguments args, Guild guild) throws UiParseException {
		String id = getIdWithAppendedArgs(element, args);
		ButtonStyle style;
		try {
			style = ButtonStyle.valueOf(element.getAttribute("style").toUpperCase());
		} catch(Exception e) {
			style = ButtonStyle.valueOf( evaluateString(element.getAttribute("style"), args).toUpperCase() );
		}
		boolean disabled = false;
		String statement = element.getAttribute("disabled");
		if(statement != null && !statement.isBlank())
			disabled = spes.evaluateBoolean(statement, args.getMap());
		
		String emoji = null;
		String label = null;
		
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
		
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "text":
				label = les.replacePlaceholders(((Element) child).getTextContent(), guild, args);
				break;
			case "emoji":
				emoji = ((Element) child).getTextContent();
			default: continue;
			}
		}
		
		if(emoji == null && label == null)
			throw new UiParseException("Button must have at least one and at most both of the following elements: text, emoji");
		return Button.of(style, id, label, emoji == null ? null : Emoji.fromFormatted(emoji)).withDisabled(disabled);
	}
	
	public Thumbnail parseThumbnail(Element child, Arguments args, Guild guild) {
		return Thumbnail.fromUrl(child.getTextContent());	//TODO maybe make this more safe?
	}
	
	public ActionRow parseActionRow(Element element, Arguments args, Guild guild) throws UiParseException {
		return parseActionRow(element, args, guild, List.of()); 
	}
	
	public ActionRow parseActionRow(Element element, Arguments args, Guild guild, Collection<SelectOption> options, SelectOption... defaultOptions) throws UiParseException {
		Collection<ActionRowChildComponent> components = new LinkedList<>();
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			components.add(switch(child.getNodeName()) {
			case "button":
				yield parseButton((Element) child, args, guild);
			case "string-select":
				yield parseStringSelect(element, args, guild, options, defaultOptions);
			default:
				throw new UiParseException("Unexpected child tag in ActionRow declaration: " + child.getNodeName());
			});
		}
	
		return ActionRow.of(components);
	}
	
	public StringSelectMenu parseStringSelect(Element element, Arguments args, Guild guild, Collection<SelectOption> options, SelectOption... defaultOptions) throws UiParseException {
		String id = getIdWithAppendedArgs(element, args);
		int min = Integer.parseInt( element.getAttribute("min") );
		int max = Integer.parseInt( element.getAttribute("max") );
		boolean required = element.getAttribute("required").equalsIgnoreCase("true");
		boolean disabled = false;
		String statement = element.getAttribute("disabled");
		if(statement != null && !statement.isBlank())
			disabled = spes.evaluateBoolean(statement, args.getMap());
		String placeholder = null;
		
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "placeholder":
				placeholder = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "option":
				String forArg = ((Element) child).getAttribute("for");
				
				if(forArg == null || forArg.isBlank()) {
					options.add(parseSelectOption((Element) child, args, guild));
					break;
				}
				
				try {
					Arguments[] argList = args.get(forArg).as(Arguments[].class);
					for(Arguments elem : argList) {
						options.add(parseSelectOption((Element) child, elem, guild));
					}
				} catch (ArgumentTypeException e) {
					log.warn("For variable must be an array of Arguments; {}", e);
					options.add(parseSelectOption((Element) child, args, guild));
				}
				
				break;
			default: continue;
			}
		}
		
		return StringSelectMenu.create(id)
				.addOptions(options)
				.setDefaultOptions(defaultOptions)
				.setPlaceholder(placeholder)
				.setRequiredRange(min, max)
				.setRequired(required)
				.setDisabled(disabled)
				.build();
	}
	
	public EntitySelectMenu parseEntitySelect(Element element, Arguments args, Guild guild) throws UiParseException {
		String id = getIdWithAppendedArgs(element, args);
		int min = Integer.parseInt( element.getAttribute("min") );
		int max = Integer.parseInt( element.getAttribute("max") );
		boolean required = element.getAttribute("required").equalsIgnoreCase("true");
		boolean disabled = false;
		String statement = element.getAttribute("disabled");
		if(statement != null && !statement.isBlank())
			disabled = spes.evaluateBoolean(statement, args.getMap());
		String target = element.getAttribute("target");
		String placeholder = null;
		
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "placeholder":
				placeholder = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			default: continue;
			}
		}
		
		SelectTarget[] targets = switch (target) {
			case "user":
				yield new SelectTarget[]{SelectTarget.USER};
			case "role":
				yield new SelectTarget[]{SelectTarget.ROLE};
			case "mentionable":
				yield new SelectTarget[]{SelectTarget.USER, SelectTarget.ROLE};
			case "channel":
				yield new SelectTarget[]{SelectTarget.CHANNEL};
			default:
				throw new UiParseException("Unexpected SelectTarget in EntitySelect declaration: " + target);
		};
		
		Collection<SelectTarget> targetCollection = List.of(targets);
		return EntitySelectMenu.create(id, targetCollection)
				.setRequired(required)
				.setRequiredRange(min, max)
				.setPlaceholder(placeholder)
				.setDisabled(disabled)
				.build();
	}
	
	public Modal parseModal(Element base, Arguments args, Guild guild) throws UiParseException {
		Collection<ModalTopLevelComponent> components = new LinkedList<>();
		NodeList children = base.getChildNodes();
		
		String id = getIdWithAppendedArgs(base, args);
		String title = null;
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "title":
				title = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "label":
				components.add(parseLabel((Element) child, args, guild));
				break;
			case "textDisplay":
				String forArg = ((Element) child).getAttribute("for");
				
				if(forArg == null || forArg.isBlank()) {
					components.add(parseTextDisplay((Element) child, args, guild));
					break;
				}
				
				try {
					Arguments[] argList = args.get(forArg).as(Arguments[].class);
					for(Arguments elem : argList) {
						components.add(parseTextDisplay((Element) child, elem, guild));
					}
				} catch (ArgumentTypeException e) {
					log.warn("For variable must be an array of Arguments; {}", e);
					components.add(parseTextDisplay((Element) child, args, guild));
				}
				break;
			default: continue;
			}
		}
		
		return Modal.create(id, title)
				.addComponents(components)
				.build();
	}
	
	public Label parseLabel(Element base, Arguments args, Guild guild) throws UiParseException {
		NodeList children = base.getChildNodes();
		
		String title = null;
		String description = null;
		LabelChildComponent component = null;
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "title":
				title = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "description":
				description = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "textInput":
				component = parseTextInput((Element) child, args, guild);
				break;
			case "stringSelect":
				component = parseStringSelect((Element) child, args, guild, new LinkedList<SelectOption>());
				break;
			case "entitySelect": 
				component = parseEntitySelect((Element) child, args, guild);
			default: continue;
			}
		}
		
		return Label.of(title, description, component);
	}
	
	public TextInput parseTextInput(Element base, Arguments args, Guild guild) {
		NodeList children = base.getChildNodes();
		
		String id = getIdWithAppendedArgs(base, args);
		String placeholder = null;
		TextInputStyle style = TextInputStyle.valueOf(base.getAttribute("style").toUpperCase());
		int min = Integer.parseInt(base.getAttribute("min"));
		int max = Integer.parseInt(base.getAttribute("max"));
		boolean required = base.getAttribute("required").equalsIgnoreCase("true");
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "placeholder":
				placeholder = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			default: continue;
			}
		}
		
		return TextInput.create(id, style)
				.setRequiredRange(min, max)
				.setPlaceholder(placeholder)
				.setRequired(required)
				.build();
	}
	
//	private <T extends ContainerChildComponent> List<ContainerChildComponent> parseForWithPaging(Element element, Arguments args, Guild guild, int page, int maxItemsPerPage, TriFunction<Element, Arguments, Guild, T> fct) {
//		List<ContainerChildComponent> children = new LinkedList<>();
//		String forArg = element.getAttribute("for");
//		
//		if(forArg == null || forArg.isBlank()) {
//			children.add(fct.apply(element, args, guild));
//			return children;
//		}
//		
//		children.addAll(parseFor(element, args, guild, fct));
//		List<ContainerChildComponent> paged = children.subList(page*maxItemsPerPage, (page*maxItemsPerPage) + maxItemsPerPage);
//		paged.add(Button.primary("", ""))
//		
//	}
//	
//	private <T> List<T> parseFor(Element element, Arguments args, Guild guild, TriFunction<Element, Arguments, Guild, T> fct) {
//		List<T> children = new LinkedList<>();
//		String forArg = element.getAttribute("for");
//		
//		if(forArg == null || forArg.isBlank()) {
//			children.add(fct.apply(element, args, guild));
//			return children;
//		}
//		
//		try {
//			Arguments[] argList = args.get(forArg).as(Arguments[].class);
//			for(Arguments elem : argList) {
//				children.add(fct.apply(element, elem, guild));
//			}
//		} catch (ArgumentTypeException e) {
//			log.warn("For variable must be an array of Arguments; {}", e);
//			children.add(fct.apply(element, args, guild));
//		}
//		
//		return children;
//	}
	
	private SelectOption parseSelectOption(Element element, Arguments args, Guild guild) throws UiParseException {
		NodeList children = element.getChildNodes();
		
		boolean defaultSelection = element.getAttribute("default").equalsIgnoreCase("true");
		String label = null;
		String value = null;
		String description = null;
		Emoji emoji = null;
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !evaluateIf((Element) child, args))
				continue;
			
			switch(child.getNodeName()) {
			case "label":
				label = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "value":
				value = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "description":
				description = les.replacePlaceholders(child.getTextContent(), guild, args);
				break;
			case "emoji":
				emoji = Emoji.fromFormatted(child.getTextContent());
				break;
			default: continue;
			}
		}
		
		return SelectOption.of(label, value).withDescription(description).withEmoji(emoji).withDefault(defaultSelection);
	}
	
	private FileDisplay parseFileDisplay(Element element, Arguments args, Guild guild) {
		FileDisplay display = FileDisplay.fromFileName(les.replacePlaceholders(element.getTextContent(), guild, args));
		return display;
	}

	private MediaGallery parseMediaGallery(Element element, Arguments args, Guild guild) {
		NodeList children = element.getChildNodes();
		List<MediaGalleryItem> galleryItems = new LinkedList<>();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals("item"))
				continue;
			
			String forArg = ((Element) child).getAttribute("for");
			
			if(forArg == null || forArg.isBlank()) {
				MediaGalleryItem item = parseGalleryItem((Element) child, args, guild);
				if(item == null)
					continue;
				galleryItems.add(item);
				continue;
			}
			
			try {
				Arguments[] argList = args.get(forArg).as(Arguments[].class);
				for(Arguments elem : argList) {
					MediaGalleryItem item = parseGalleryItem((Element) child, args, guild);
					if(item == null)
						continue;
					galleryItems.add(item);
				}
			} catch (ArgumentTypeException e) {
				log.warn("For variable must be an array of Arguments; {}", e);
				MediaGalleryItem item = parseGalleryItem((Element) child, args, guild);
				if(item == null)
					continue;
				galleryItems.add(item);
			}
		}
		
		return MediaGallery.of(galleryItems);
	}
	
	private MediaGalleryItem parseGalleryItem(Element element, Arguments args, Guild guild) {
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
		
			return switch(child.getNodeName()) {
			case "url":
				String url = les.replacePlaceholders(((Element)child).getTextContent(), guild, args);
				yield MediaGalleryItem.fromUrl(url);
			case "file":
				FileUpload upload = parseFileUpload((Element) child,  args, guild);
				if(upload == null)
					yield null;
				yield MediaGalleryItem.fromFile(upload);
			default:
				yield null;
			};
		}
		return null;
	}

	private FileUpload parseFileUpload(Element child, Arguments args, Guild guild) {
		// TODO Implement File Upload XML
		return null;
	}
	
	private boolean evaluateIf(Element element, Arguments args) {
		String statement = element.getAttribute("if");
		
		if(statement == null || statement.isBlank())
			return true;
		
		try {
			boolean eval = spes.evaluateBoolean(statement, args.getMap());
			return eval;
		} catch (Exception e) {
			log.warn("Exception while evaluating statement: {}", e);
			return false;
		}
		
	}
	
	private String evaluateString(String statement, Arguments args) {
	
		if(statement == null || statement.isBlank())
			return statement;
		
		try {
			String eval = spes.evaluateString(statement, args.getMap());
			return eval;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	private String getIdWithAppendedArgs(Element element, Arguments args) {
		String id = element.getAttribute("id");
		String[] params = new String[0];
		String paramString = element.getAttribute("params"); 
		if (paramString != null && !paramString.isBlank()) {
		    params = paramString.trim().split(",");
		    id += "?";
		}
		
		boolean first = true;
		for(String param : params) {
			param = param.strip();
			
			if(!first)
				id += "&";
			
			String[] paramSplit = param.split("=", 2);
			if(paramSplit.length > 1) {
				id += param;
			}
			else {
				id += param + "=" + args.getOrDefault(param, "").asString();
			}
			
			first = false;
		}
		
		log.debug("Appended args to id '{}'", id);
		return id;
	}
}
