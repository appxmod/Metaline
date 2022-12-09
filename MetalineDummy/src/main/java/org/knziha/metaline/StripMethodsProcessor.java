package org.knziha.metaline;


import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

//@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.knziha.metaline.StripMethods"})
public final class StripMethodsProcessor extends AbstractProcessor {
//	private JavacElements elementUtils;
//	private TreeMaker maker;
//	private Context context;
//	private Trees trees;
	
	@Override
	public void init(final ProcessingEnvironment procEnv) {
		super.init(procEnv);
//		this.elementUtils = (JavacElements) procEnv.getElementUtils();
//		JavacProcessingEnvironment jcEnv = null;
//		if (procEnv instanceof JavacProcessingEnvironment) {
//			jcEnv = (JavacProcessingEnvironment) procEnv;
//		} else {
//			try {
//				Field f = procEnv.getClass().getDeclaredField("delegate");
//				f.setAccessible(true);
//				jcEnv = (JavacProcessingEnvironment) f.get(procEnv);
//				// CMN.Log(jcEnv);
//			} catch (Exception e) {
//				CMN.Log(e);
//			}
//		}
//		if (jcEnv!=null) {
//			this.maker = TreeMaker.instance(context=jcEnv.getContext());
//			this.trees = Trees.instance(jcEnv);
//		}
	}
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		return true;
	}
}
