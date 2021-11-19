Metaline | 元线
=========

An annotation processor module for multi-line string and other purposes.

Usage : 

- ```js
    compileOnly project(':Metaline')
    annotationProcessor project(':Metaline')
    testCompileOnly project(':Metaline')
    testAnnotationProcessor project(':Metaline')```
&ensp;


# Features | 特性

## 一、多行文本 | Java Multiline-String

- ```js
	/**
	Just
	  Like Good 
		Old Days.
 	*/
	@Metaline static String text;```
### &ensp; 无缝编译JS： | Compile Javascript
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

## 二、单行配置 | Bit / Flag Preference Methods
### &ensp; 一行代码配置标志位方法：
- ```js
	@Metaline(flagPos=3, shift=1) public static boolean getUseCookie(long flag){ flag=flag; throw new RuntimeException(); } ```
&ensp;
<br>

## 三、删除无用方法 | Strip / Remove Methods
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
### &ensp; 删除结果：
- ```
	debug 删除NightMode后节省 760kb
	release 删除NightMode后节省 1.8MB、79个方法```
&ensp;

<br>


#### &ensp; Main Drawbacks | 缺点：  
<br>

> &ensp;“The following annotation processors are not **incremental**: Metaline.jar (project :Metaline).Make sure all annotation processors are incremental to improve your build speed.”   
> &ensp;  
> &ensp;I fixed the warning. but is incremental compilation actually enabled?
&ensp;&ensp;In addition, put strings in your code will grow your app's size.  

 

#### &ensp; Origin | 起源

&ensp; This project is originated from [multiline](https://github.com/benelog/multiline) and from [Adrian Walker's blog post](http://www.adrianwalker.org/2011/12/java-multiline-string.html).
