package de.fabiansiemens.hyperion.interactions;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.ui.UiProviderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InteractionChainBase {

	protected final ApplicationContext context;
	protected final UiProviderService uiProvider;
	protected final LocalizedExpressionService les;
	
	public InteractionChainBase(ApplicationContext context) {
		this.context = context;
		this.uiProvider = context.getBean(UiProviderService.class);
		this.les = context.getBean(LocalizedExpressionService.class);
	}
}
