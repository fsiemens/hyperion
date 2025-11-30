package de.fabiansiemens.hyperion.commands.calculate;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SuperCommandBase;

@Command(dataFile = "calculate/Calculate.json")
public class CalculateCommand extends SuperCommandBase {

	public <T> CalculateCommand(ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(CalculateCommand.class, context);
	}
}
