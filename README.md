Metaline | 元线模块 | Java注解 | ~~元宇宙射线~~
=========

> An annotation processor module for multi-line string and more.

# Usage | 使用 （从 JitPack 导入） : 

&ensp;
**Step 1.**  Add it in your **root build.gradle** at the end of repositories:

- ```css
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

&ensp; **Step 2.** Add the **dependency**

- ```css
	dependencies {
		compileOnly "com.gitee.knziha:Metaline:lastest"
		annotationProcessor "com.gitee.knziha:Metaline:lastest"
		testCompileOnly "com.gitee.knziha:Metaline:lastest"
		testAnnotationProcessor "com.gitee.knziha:Metaline:lastest"
	}
```

# Features | 特性

## 一、多行文本 | Easy Multiline-String.

- ```js
	/**
	Just
	  Like Good 
		Old Days.
 	*/
	@Metaline static String text;```
### &ensp; 编译 JavaScript： | Java loves Javascript.
- ```js
	// use Google Closure Compiler to compile your javascript snippets
	/** var m = document.head.querySelectorAll('meta');
		window.hasVPM = false;
		for (var i = 0; i < m.length; i++) {
			var iI = m[i];
			if (iI.name === 'viewport') {
				if (iI.content.lastIndexOf('no') > 0) {
					iI.content = 'width=device-width,minimum-scale=1,maximum-scale=5.0,user-scalable=yes';
				}
				window.hasVPM = true;
				break;
			}
		}
	 */
	@Metaline(trim=true, compile=true)
	public final static String ForceResizable = "";```
&ensp;
<br>

## 二、单行配置 | Boolean/Short Preferences stored in Bits / Flags.
### &ensp; 一行代码配置标志位方法：
- ```js
	private long myFlag;
	@Metaline(flagPos=3, shift=1) public boolean getUseCookie(){ myFlag=myFlag; throw new RuntimeException(); } 
	@Metaline(flagPos=3, shift=1) public void setUseCookie(boolean value){ myFlag=myFlag; throw new RuntimeException(); } 
```
&ensp;
<br>

## 三、删除方法 | Strip / Remove Methods.
### &ensp; 比如删除 androidx/appcompat 支持库中的 NightMode：
- ```javascript
	@StripMethods(keys="Night")
	public class AppCompatActivity
	...
	@StripMethods(keys="Night")
	public abstract class AppCompatDelegate
	...
	@StripMethods(keys="Night")
	class AppCompatDelegateImpl 
	...
	// not perfect. in this case you still need to modify 
	//  one of the method AppCompatDelegateImpl/attachBaseContext2
	//  manually.```
<br>


## 使用须知：  
<br>
&ensp; &ensp; 在代码中定义字符串资源，肯定会增加编译体积，非常大的字符串建议用asset。  
<br>
&ensp; &ensp; 关于单行配置，本注解处理器只负责生成方法体，用以替代后边写好的 stub。stub分为两句，第一句指定容器变量，可以是参数传进来的，也可以是类里面的成员变量。


## TODO： 
&ensp; &ensp; 待办文档等。
 

## &ensp;Origin | 起源

&ensp; This project is originated from [multiline](https://github.com/benelog/multiline) and from [Adrian Walker's blog post](http://www.adrianwalker.org/2011/12/java-multiline-string.html).

&ensp; 感谢benelog，感谢栈溢出。

