package org.knziha.metaline;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

//@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.knziha.metaline.StripMethods"})
public final class StripMethodsProcessor extends AbstractProcessor {
	private JavacElements elementUtils;
	private TreeMaker maker;
	private Context context;
	private Trees trees;
	
	@Override
	public void init(final ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.elementUtils = (JavacElements) procEnv.getElementUtils();
		// org.gradle.api.internal.tasks.compile.processing.IncrementalProcessingEnvironment
		// com.sun.tools.javac.processing.JavacProcessingEnvironment
		JavacProcessingEnvironment jcEnv = null;
		if (procEnv instanceof JavacProcessingEnvironment) {
			jcEnv = (JavacProcessingEnvironment) procEnv;
		} else {
			try {
				Field f = procEnv.getClass().getDeclaredField("delegate");
				f.setAccessible(true);
				jcEnv = (JavacProcessingEnvironment) f.get(procEnv);
				// CMN.Log(jcEnv);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		if (jcEnv!=null) {
			this.maker = TreeMaker.instance(context=jcEnv.getContext());
			this.trees = Trees.instance(jcEnv);
		}
	}
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(StripMethods.class);
		for (Element field : fields) {
			StripMethods annotation = field.getAnnotation(StripMethods.class);
			if (!annotation.strip()) {
				continue;
			}
			String key = annotation.key();
			ElementKind KIND = field.getKind();
			CMN.Log("StripMethods::", annotation, KIND);
			if(KIND == ElementKind.CLASS) {
				ArrayList<JCTree> defs;
				JCTree.JCClassDecl laDcl = (JCTree.JCClassDecl) elementUtils.getTree(field);
				if (trees !=null) {
					TreePath treePath = trees.getPath(field);
					JCTree.JCCompilationUnit compileUnit = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
					int imports = compileUnit.getImports().size();
					defs = new ArrayList<>(compileUnit.defs);
					//CMN.Log("imports::", compileUnit.getImports());
					for (int i = 0, len=defs.size(); i <= len && imports>0; i++) {
						JCTree obj = defs.get(i);
						if (obj instanceof JCTree.JCImport) {
							if (obj.toString().contains(key)) {
								defs.remove(i); i--; len--;
							}
							imports--;
						}
					}
					compileUnit.defs = List.from(defs);
				}
				//CMN.Log(laDcl.typarams, laDcl.mods, laDcl.implementing, laDcl.sym);
				//CMN.Log("laDcl::", field, laDcl, laDcl.getMembers());
				//CMN.Log("defs::", laDcl.defs);
				defs = new ArrayList<>(laDcl.defs);
				for (int i = defs.size()-1; i >= 0; i--) {
					JCTree member = defs.get(i);
					//CMN.Log("member::", member);
					boolean b1 = member instanceof JCTree.JCMethodDecl;
					if (b1) {
						if (((JCTree.JCMethodDecl) member).getName().toString().contains(key))
						{
							defs.remove(i);
							continue;
						}
					}
					if (member instanceof JCTree.JCVariableDecl) {
						//if (((JCTree.JCVariableDecl) member).getName().toString().contains(key))
						if (member.toString().contains(key))
						{
							defs.remove(i);
							continue;
						}
					}
					if (member instanceof JCTree.JCClassDecl) {
						if (((JCTree.JCClassDecl) member).getSimpleName().toString().contains(key))
						{
							defs.remove(i);
							continue;
						}
					}
					if (b1) {
						JCTree.JCMethodDecl metDcl = (JCTree.JCMethodDecl) member;
						if (metDcl.body!=null) {
							//CMN.Log("metDcl.body::", metDcl.body);
							ArrayList<JCTree.JCStatement> statements = new ArrayList<>(metDcl.body.stats);
							for (int j = statements.size()-1; j >= 0; j--) {
								if (statements.get(j).toString().contains(key))
								{
									statements.remove(j);
								}
							}
							metDcl.body.stats = List.from(statements);
						}
					}
				}
				laDcl.defs = List.from(defs);
			}
		}
		
		return true;
	}
}
