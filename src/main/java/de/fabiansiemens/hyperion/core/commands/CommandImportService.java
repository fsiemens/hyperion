package de.fabiansiemens.hyperion.core.commands;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fabiansiemens.hyperion.core.commands.data.CommandInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

@Slf4j
@Service
public class CommandImportService {

	@Value("${commands.root}")
	private String commandRootFolder;
	
	public CommandInfo importCommand(final String path) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(commandRootFolder + path);
		
		if(!file.exists())
			throw new IOException("Command Data File '" + commandRootFolder + path + "' not found.");
		
		JsonNode root = mapper.readTree(file);
		JsonNode legacyNode = root.get("legacyData");
		JsonNode slashNode = root.get("slashData");

		CommandInfo.LegacyData legacyData = CommandInfo.LegacyData.fromDefault();
		if(legacyNode != null)
			legacyData = mapper.readValue(legacyNode.toString(), CommandInfo.LegacyData.class);
		
		if(slashNode == null)
			throw new IOException("Slash Data must be provided.");
		
		
		SlashCommandData slashData = SlashCommandData.fromData(DataObject.fromJson(slashNode.toString()));
		
		return new CommandInfo(legacyData, slashData);
	}
	
	public String valueOf(final String xmlPath, final String key) throws IOException {
		final String path = commandRootFolder + xmlPath;
		
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(path);
			
			final Element container = (Element) document.getElementsByTagName(key).item(0);
			return container.getTextContent();
		}
		catch(IOException | IllegalArgumentException | ParserConfigurationException | SAXException e) {
			IOException ex = new IOException("Unable to parse XML Command Data File '" + xmlPath + "'.");
			ex.initCause(e);
			throw ex;
		}
	}
	
}
