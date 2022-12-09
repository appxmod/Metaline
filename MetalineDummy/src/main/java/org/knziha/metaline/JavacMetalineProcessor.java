package org.knziha.metaline;

import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@SupportedAnnotationTypes({"org.knziha.metaline.Metaline"})
public final class JavacMetalineProcessor extends AbstractProcessor {
	
//	private JavacElements elementUtils;
//	private TreeMaker maker;
//	private Context context;
	
	@Override
	public void init(final ProcessingEnvironment procEnv) {
		super.init(procEnv);
	}
	
	// https://stackoverflow.com/questions/22494596/eclipse-annotation-processor-get-project-path
	static String projectPath;
	String getProjectPath() {
		if (projectPath!=null)
			return projectPath;
		try {
			JavaFileObject generationForPath = processingEnv.getFiler().createSourceFile("PathTest"/* + getClass().getSimpleName()*/);
			//Writer writer = generationForPath.openWriter();
			String path = generationForPath.toUri().getPath();
			int idx = path.lastIndexOf("/", path.indexOf("/generated/")-1);
			path = path.substring(0, idx);
			if (path.startsWith("/") && path.contains(":")) {
				path = path.substring(1);
			}
			//writer.close();
			//generationForPath.delete();
			return projectPath = path;
		} catch (Exception e) {
			CMN.messager.printMessage(Diagnostic.Kind.WARNING, "Unable to determine source file path!");
			CMN.Log(e);
		}
		return "";
	}
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	public static Pattern comments=Pattern.compile("((?<![:/])//.*)?(\r)?\n");
	
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		return true;
	}
}
