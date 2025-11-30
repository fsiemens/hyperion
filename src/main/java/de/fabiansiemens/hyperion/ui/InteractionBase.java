package de.fabiansiemens.hyperion.ui;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.ui.UiImportService;
import lombok.Data;
import lombok.Getter;

@Data
@Deprecated
public abstract class InteractionBase {
	private final String dataPath;
	
	protected final String id;
	
	protected final ApplicationContext context;
	
	@Getter
	protected final LocalizedExpressionService les;
	
	@Getter
	protected final UiImportService importService;
	
	public abstract String getId();
	
	public <T> InteractionBase(Class<T> clazz, Class<? extends Annotation> annotationClass, ApplicationContext context) throws IllegalArgumentException {
		this.context = context;
		this.les = context.getBean(LocalizedExpressionService.class);
		this.importService = context.getBean(UiImportService.class);
		
		try {
			Annotation annotation = clazz.getAnnotation(annotationClass);
			if(annotation == null)
				throw new IllegalArgumentException("Add a " + annotationClass.getName() + "-Annotation and specify a valid data file.");
			
			Method dataPathMethod = annotationClass.getMethod("dataFile");
			Method idMethod = annotationClass.getMethod("id");
			
			this.dataPath = (String) dataPathMethod.invoke(annotation);
			this.id = (String) idMethod.invoke(annotation);
			
			if(dataPath.isBlank() && id.isBlank())
				throw new IllegalArgumentException("DataPath and ID must not both be blank.");
		}
		catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			IllegalArgumentException ex = new IllegalArgumentException("Can't construct " + clazz.getName());
			ex.initCause(e);
			throw ex;
		}
	}
}
