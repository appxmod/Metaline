package org.knziha.metaline;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.PropertyRenamingPolicy;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.VariableRenamingPolicy;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@SupportedAnnotationTypes({"org.knziha.metaline.Metaline"})
public final class JavacMetalineProcessor extends AbstractProcessor {
	
	private JavacElements elementUtils;
	private TreeMaker maker;
	private Context context;
	
	@Override
	public void init(final ProcessingEnvironment procEnv) {
		super.init(procEnv);
		final String initMsg = "> Metaline: init";
		System.out.println(initMsg);
		CMN.messager = procEnv.getMessager();
		CMN.messager.printMessage(Diagnostic.Kind.WARNING, initMsg);
		this.elementUtils = (JavacElements) procEnv.getElementUtils();
		JavacProcessingEnvironment jcEnv = null;
		if (procEnv instanceof JavacProcessingEnvironment) {
			jcEnv = (JavacProcessingEnvironment) procEnv;
		} else {
			try {
				Field f = procEnv.getClass().getDeclaredField("delegate");
				f.setAccessible(true);
				jcEnv = (JavacProcessingEnvironment) f.get(procEnv);
				 CMN.Log(jcEnv);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		if (jcEnv!=null) {
			this.maker = TreeMaker.instance(context=jcEnv.getContext());
		} else {
			CMN.messager.printMessage(Diagnostic.Kind.ERROR
				, "JavacProcessingEnvironment Is NOT Available! Try Downgrading to IDEA 2019 \n\t"
					+"or add '-Djps.track.ap.dependencies=false' to BUILD->COMPILER->SHARED VM OPTIONS!\n\t"
					+"see https://stackoverflow.com/questions/65128763");
		}
	}
	
	// https://stackoverflow.com/questions/22494596/eclipse-annotation-processor-get-project-path
	static String projectPath;
	String getProjectPath() {
		if (projectPath!=null)
			return projectPath;
		try { // slow
			JavaFileObject generationForPath = processingEnv.getFiler().createSourceFile("PathTest"/* + getClass().getSimpleName()*/);
			//Writer writer = generationForPath.openWriter();
			String path = generationForPath.toUri().getPath();
			int idx = path.lastIndexOf("/", path.indexOf("/generated/")-1);
			path = path.substring(0, idx);
			if (path.startsWith("/") && path.contains(":")) {
				path = path.substring(1);
			}
			//writer.close();
			generationForPath.delete();
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
		if(maker==null)
			return false;
		Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(Metaline.class);
		boolean haveNoIdea = CMN.haveNoIdea;
		for (Element field : fields) {
			Metaline annotation = field.getAnnotation(Metaline.class);
			if(!haveNoIdea) {
				CMN.haveNoIdea = roundEnv.getElementsAnnotatedWith(HaveNoIdea.class).size()>0;
				haveNoIdea = true;
			}
			ElementKind KIND = field.getKind();
			int log = annotation.log();
			if(KIND == ElementKind.FIELD||KIND==ElementKind.LOCAL_VARIABLE) {
				String docComment = elementUtils.getDocComment(field);
				String file = annotation.file();
				boolean fromFile = !StringUtils.isEmpty(file);
				boolean jsFile = fromFile&&file.endsWith(".js");
				String charset = "UTF-8";
				JCVariableDecl varDcl = (JCVariableDecl) elementUtils.getTree(field);
				TypeName typeName = TypeName.get(field.asType());
				String name = typeName.toString();
				if(fromFile) {
					File path = null;
					if (annotation.rootPath().length() > 0) {
						try {
							File project_path = new File(annotation.rootPath());
							if(log>0) CMN.Log("fast projectPath=" + project_path);
							path = new File(project_path.getCanonicalFile(), file);
						} catch (IOException ignored) {
						}
					}
					if(path==null && !file.contains(":")&&!file.startsWith("/")) {
						try {
							File project_path = new File(getProjectPath());
							CMN.Log("projectPath="+project_path);
							path = new File(project_path.getCanonicalFile(), file);
						} catch (IOException ignored) { }
					}
					if(path==null) {
						path = new File(file);
					}
					//String to = annotation.to();
//					if(!StringUtils.isEmpty(to)) {
//						try {
//							File toDir = new File("");
//							toDir = new File(toDir.getCanonicalPath(), to);
//							if(toDir.getParentFile().exists()&&(path.lastModified()>toDir.lastModified()||annotation.debug()==1)) {
//								FileInputStream fin = new FileInputStream(path);
//								FileOutputStream fout = new FileOutputStream(toDir);
//								byte[] buffer = new byte[1024];
//								int len;
//								while((len=fin.read(buffer))>0) {
//									fout.write(buffer,0,len);
//								}
//								fin.close();
//								fout.close();
//							}
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
					if(name.equals("int")) {
						varDcl.init = maker.Literal((int)path.length());
						if(!path.exists()) {
							throw new IllegalArgumentException("File Not Exist : "+path);
						}
						continue;
					}
					if(docComment==null) { //only for string. for bytes read them from file.
						byte[] bytes = new byte[(int) path.length()];
						try {
							FileInputStream fin = new FileInputStream(path);
							fin.read(bytes);
							fin.close();
							docComment = new String(bytes, StandardCharsets.UTF_8);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if (docComment!=null) {
					if(annotation.trim()){
						docComment=comments.matcher(docComment).replaceAll(" ");
						docComment=docComment.replaceAll("\\s+"," ");
						docComment=docComment.replaceAll(" ?([={}<>;,+\\-]) ?","$1");
					}
					if(annotation.compile()) {
						// https://stackoverflow.com/questions/14576782/closure-compiler-options
						try {
							Compiler compiler = new Compiler();
							CompilerOptions opt = new CompilerOptions();
							CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(opt);
							opt.setRenamingPolicy(VariableRenamingPolicy.ALL, PropertyRenamingPolicy.OFF);
							opt.setOutputCharset(StandardCharsets.UTF_8);
							SourceFile source = SourceFile.fromCode("input.js", docComment);
							SourceFile extern = SourceFile.fromCode("extern.js","");
							
							Result res = compiler.compile(extern, source, opt);
							if(log>0) CMN.Log("编译JS", field.getSimpleName(), res.success);
							if(res.success) {
								docComment = compiler.toSource();
								if(log>0) CMN.Log("-->成功", docComment);
							} else {
								if(log>0) CMN.Log("-->失败", res.errors.toString());
							}
						} catch (Exception e) {
							if(log>0) CMN.Log("编译JS-->失败", e);
						}
					}
					JCLiteral doclet = maker.Literal(docComment);
					if(name.equals(String.class.getName())) {
						varDcl.init = doclet;
						if(log>0) CMN.Log("string...", varDcl.init);
					} else if(name.equals("byte[]")) {
						if(false) {
							byte[] data = docComment.getBytes();
							JCExpression[] dataExp = new JCExpression[data.length];
							for (int i = 0; i < data.length; i++) {
								dataExp[i] = maker.Literal((int)data[i]);
							}
							varDcl.init = maker.NewArray(maker.TypeIdent(TypeTag.BYTE), List.nil(), List.from(dataExp));
						} else {
							Names names = Names.instance(context);
							JCFieldAccess select = maker.Select(doclet, names.fromString("getBytes"));
							JCFieldAccess charAccess = maker.Select(maker.Ident(names.fromString("java.nio.charset")), names.fromString("Charset"));
							charAccess = maker.Select(charAccess, names.fromString("forName"));
							List<JCExpression> charArgs = List.of(maker.Apply(List.nil(), charAccess, List.of(maker.Literal(charset))));
							varDcl.init = maker.Apply(List.nil(), select, charArgs);
						}
						//CMN.Log( "processed_len:",data.length, varDcl.init);
					}
				}
			}
			else if(KIND == ElementKind.METHOD){
				JCMethodDecl metDcl = (JCMethodDecl) elementUtils.getTree(field);
				if(annotation.fin()) metDcl.mods.flags |= Flags.FINAL;
				//TypeName typeName = TypeName.get();
				//String name = typeName.toString();
				//metDcl.body = maker.Block()
				if(log>0) CMN.Log(field, metDcl, metDcl.body.flags);
				List<JCStatement> statements = metDcl.body.stats;
				//CMN.Log(statements.toArray());
				JCStatement _1st = statements.get(0);
				if(log>0) CMN.Log("_1st", _1st);
				
				JCTree retType = metDcl.getReturnType();
				if(retType instanceof JCPrimitiveTypeTree) {
					JCPrimitiveTypeTree RETType = (JCPrimitiveTypeTree) retType;
					if(statements.length()==2&&statements.get(1) instanceof JCThrow) {
						if(_1st instanceof JCExpressionStatement) {
							JCExpressionStatement stat = (JCExpressionStatement) _1st;
							JCAssign assgn = (JCAssign) stat.expr;
							JCExpression flag = assgn.getVariable(); //JCIdent
							int flagPos = annotation.flagPos();
							int mask = (1<<annotation.flagSize())-1;
							long maskVal = ((long)mask)<<annotation.flagPos();
							int max = annotation.max();
							int shift = annotation.shift();
							if(max==0 || max>mask) {
								max = mask + 1;
							} else {
								max ++;
							}
							int elevation = annotation.elevation();
							List<JCVariableDecl> parms = metDcl.getParameters();
							
							boolean TogglePosFlag=false;
							try {
								TogglePosFlag=((JCIdent)((JCNewClass)((JCThrow)statements.get(1)).expr).clazz).name.toString().equals("IllegalArgumentException");
							} catch (Exception ignored) { }
							//CMN.Log("TogglePosFlag", TogglePosFlag);
							
							if(RETType.typetag==TypeTag.INT) {
								int debugVal = annotation.debug();
								//CMN.Log("mask", mask);
								JCBinary core = maker.Binary(Tag.SR, flag, maker.Literal(flagPos));
								JCBinary basic = maker.Binary(Tag.BITAND, maker.Parens(core), maker.Literal(mask));
								JCExpression finalExpr = basic;
								if(shift!=0||max<mask) {
									if(shift!=0) {
										finalExpr = maker.Binary(Tag.PLUS, maker.Parens(basic), maker.Literal(shift));
									}
									finalExpr = maker.Binary(Tag.MOD, maker.Parens(finalExpr), maker.Literal(max));
								}
								if(elevation>0) {
									finalExpr = maker.Binary(Tag.PLUS, finalExpr, maker.Literal(annotation.elevation()));
								}
								if(debugVal>=0) {
									finalExpr = maker.Literal(debugVal);
								}
								finalExpr = maker.TypeCast(maker.TypeIdent(TypeTag.INT), finalExpr);
								if(log>0) CMN.Log(121,finalExpr);
								metDcl.body = maker.Block(0, List.from(new JCStatement[]{maker.Return(finalExpr)}));
							}
							else if(RETType.typetag==TypeTag.BOOLEAN) {//get boolean
								int debugVal = annotation.debug();
								int size = parms.size();
								if(size==1) {
									JCVariableDecl val=parms.get(0);
									JCPrimitiveTypeTree type = (JCPrimitiveTypeTree) val.getType();
									if(type.typetag==TypeTag.LONG||type.typetag==TypeTag.INT) {
										flag = maker.Ident(val);
									}
								}
								if(TogglePosFlag) {//toggle boolean
									if(log>0) CMN.Log("TogglePosFlag");
									JCExpression fetVal = maker.Binary(Tag.EQ, maker.Binary(Tag.BITAND, flag, maker.Literal(maskVal)), maker.Literal(0));
									
									Names names = Names.instance(context);
									Name valName = names.fromString("b");
									
									JCVariableDecl bEmptyVal = maker.VarDef(maker.Modifiers(0), valName, maker.TypeIdent(TypeTag.BOOLEAN), fetVal);
									JCExpression bEmptyEval = maker.Ident(valName);
									
									JCExpression core = maker.Assignop(Tag.BITAND_ASG, flag, maker.Literal(~maskVal));
									
									JCExpression FlagMaskPosPutOne = maker.Binary(Tag.SL, maker.Literal((long)1), maker.Literal(flagPos));
									
									FlagMaskPosPutOne = maker.Assignop(Tag.BITOR_ASG, flag, FlagMaskPosPutOne);
									
									JCExpression finalExpr = bEmptyEval;
									if(shift!=0) {//If defaul to zero, return inverted value.
										finalExpr = maker.Unary(Tag.NOT, bEmptyEval);
									}
									
									metDcl.body = maker.Block(0, List.from(new JCStatement[]{
											bEmptyVal,
											maker.Exec(core),
											maker.If(maker.Parens(bEmptyEval), maker.Exec(FlagMaskPosPutOne), null), //如果原来为空，现在不为空
											maker.Return(finalExpr)
									}));
									if(log>0) CMN.Log("TogglePosFlag2", metDcl.body.toString());
								} else {
									JCBinary core = maker.Binary(Tag.BITAND, flag, maker.Literal(maskVal));
									JCExpression finalExpr = maker.Binary(shift==0?Tag.NE:Tag.EQ, maker.Parens(core), maker.Literal(0));
									if(debugVal>=0) {
										finalExpr = maker.Literal(debugVal==1);
									}
									metDcl.body = maker.Block(0, List.from(new JCStatement[]{maker.Return(finalExpr)}));
									//CMN.Log("getPosBooleanFlag", metDcl.body.toString());
								}
							}
							else if(RETType.typetag==TypeTag.VOID) {
								int size = parms.size();
								JCVariableDecl val=null;
								if(size==1) {
									val = parms.get(0);
								} else if(size==2) {
									val = parms.get(1);
								}
								JCExpression core = maker.Binary(Tag.BITAND, flag, maker.Literal(~maskVal));
								JCExpression basic = maker.Ident(val);
								JCPrimitiveTypeTree type = (JCPrimitiveTypeTree) val.getType();
								if(type.typetag==TypeTag.BOOLEAN) {
									core = maker.Assign(flag, core);
									JCStatement[] stats = new JCStatement[2];
									stats[0]=maker.Exec(core);
									JCAssign modify = maker.Assign(flag, maker.Binary(Tag.BITOR, flag, maker.Literal(maskVal)));
									JCExpressionStatement exec = maker.Exec(modify);
									if(shift!=0) {
										basic = maker.Unary(Tag.NOT, basic);
									}
									JCIf finalExpr = maker.If(maker.Parens(basic), exec, null);
									stats[1]=finalExpr;
									metDcl.body = maker.Block(0, List.from(stats));
								}
								else if(type.typetag==TypeTag.INT) {
									JCExpression finalExpr = null;
									JCExpression var = maker.Ident(val);
									if(shift!=0||max<mask) {
										shift=-shift-elevation+max;
										finalExpr = maker.Binary(Tag.PLUS, var, maker.Literal(shift));
										finalExpr = maker.Binary(Tag.MOD, maker.Parens(finalExpr), maker.Literal(max));
									}
									finalExpr = maker.Binary(Tag.BITAND, finalExpr==null?var:maker.Parens(finalExpr), maker.Literal((long)mask));
									finalExpr = maker.Binary(Tag.SL, maker.Parens(finalExpr), maker.Literal(flagPos));
									finalExpr = maker.Binary(Tag.BITOR, maker.Parens(core), maker.Parens(finalExpr));
									metDcl.body = maker.Block(0, List.from(new JCStatement[]{maker.Exec(maker.Assign(flag, finalExpr))}));
									if(log>0) CMN.Log("111", maker.Parens(core), metDcl.body);
								}
							}
						}
						else {
							if(_1st instanceof JCIf) {
								//CMN.Log(((JCParens)((JCIf)_1st).cond).expr);
								JCStatement thenExp = ((JCIf) _1st).thenpart;
								if(thenExp instanceof JCExpressionStatement) {
									if(log>0) CMN.Log(((JCExpressionStatement)thenExp).expr);
								}
								if(thenExp instanceof JCBlock) {
									if(log>0) CMN.Log(((JCBlock)thenExp).stats.get(0));
								}
								
							}
						}
					}
					
				}


//				JCExpressionStatement statement = (JCExpressionStatement) metDcl.body.stats.get(0);
//				CMN.Log(statement);
//				CMN.Log(statement.expr);
//				CMN.Log(statement.expr.getClass());
			
			}
		}
		
		return true;
	}
}
