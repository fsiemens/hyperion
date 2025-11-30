package de.fabiansiemens.hyperion.core.ui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.data.DataObject;

@Slf4j
@Service
public class UiImportService {
	
	@Value("${ui.buttons.root}")
	private String xmlButtonsRoot;
	
	@Value("${ui.selects.root}")
	private String xmlSelectRoot;
	
	@Value("${ui.modals.root}")
	private String xmlModalRoot;
	
	@Value("${ui.embeds.root}")
	private String xmlEmbedRoot;
	
	@Value("${ui.panels.schema}")
	private String panelSchemaFile;
	
	@Value("${ui.buttons.schema}")
	private String buttonsSchemaFile;
	
	@Value("${ui.modals.schema}")
	private String modalSchemaFile;
	
	@Value("${ui.selects.schema}")
	private String selectSchemaFile;
	
	private final XmlParseService parseService;
	
	public UiImportService(XmlParseService parseService) {
		this.parseService = parseService;
	}
	
	public String previewButtonId(String xmlPath) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, buttonsSchemaFile);
		return base.getAttribute("id");
	}
	
	public String previewPanelId(String xmlPath) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, panelSchemaFile);
		return base.getAttribute("id");
	}
	
	public String previewModalId(String xmlPath) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, modalSchemaFile);
		return base.getAttribute("id");
	}
	
	public String previewSelectId(String xmlPath) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, selectSchemaFile);
		return base.getAttribute("id");
	}
	
	public Button importButton(String xmlPath, Arguments args, Guild guild) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, buttonsSchemaFile);
		return parseService.parseButton(base, args, guild);
	}
	
	public Container importPanel(String xmlPath, Arguments args, Guild guild) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, panelSchemaFile);
		return parseService.parseContainer(base, args, guild);
	}

	public StringSelectMenu importStringSelect(String xmlPath, Arguments args, Guild guild, Collection<SelectOption> options, SelectOption[] defaultOptions) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, selectSchemaFile);
		return parseService.parseStringSelect(base, args, guild, options, defaultOptions);
	}
	
	//TODO figure out, how selectOptions for different selectMenus inside modal are handeled
	public Modal importModal(String xmlPath, Arguments args, Guild guild) throws UiParseException {
		Element base = loadXmlBaseElement(xmlPath, modalSchemaFile);
		return parseService.parseModal(base, args, guild);
	}

	public EmbedBuilder parseEmbedJson(String dataPath) throws IOException {
		File file = new File(xmlEmbedRoot + dataPath);
		
		if(!file.exists())
			throw new IOException("Embed Data File '" + xmlEmbedRoot + dataPath + "' not found.");

		FileReader reader = new FileReader(file);
		
		try {
			DataObject data = DataObject.fromJson(reader);
			return EmbedBuilder.fromData(data);
		}
		catch (ParsingException | IllegalArgumentException e) {
			IOException ex = new IOException("Could not read EmbedBuilder from JSON Data for path '" + dataPath + "'.");
			ex.initCause(e);
			throw ex;
		}
		
	}
	
	private Element loadXmlBaseElement(String xmlFile, String xsdFile) throws UiParseException {
		Document dom;
		try {
			dom = loadAndValidate(xmlFile, xsdFile);
		} catch (Exception e) {
			throw new UiParseException("Failed to parse XML-File (" + xmlFile + ")", e);
		}
		
		return dom.getDocumentElement();
	}
	
	private Document loadAndValidate(String xmlFile, String xsdFile) throws Exception {

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(new File(xsdFile));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setSchema(schema);
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(xmlFile));
		doc.getDocumentElement().normalize();
		
		return doc;
	}
}
