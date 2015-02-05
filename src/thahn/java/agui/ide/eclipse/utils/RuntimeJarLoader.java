package thahn.java.agui.ide.eclipse.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class RuntimeJarLoader {
	
	public static void loadJarIndDir(String dir) {
		try {
			final URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);

			new File(dir).listFiles(new FileFilter() {
				public boolean accept(File jar) {
					if (jar.toString().toLowerCase().contains(".jar")) {
						try {
							method.invoke(loader, new Object[] { jar.toURI()
									.toURL() });
							System.out.println(jar.getName() + " is loaded.");
						} catch (Exception e) {
							System.out.println(jar.getName() + " can't load.");
						}
					}
					return false;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
/* way2

// MyClassLoader which extends URLClassLoader class.
import java.net.URL;
import java.net.URLClassLoader;
 
 
public class MyClassLoader extends URLClassLoader {
    public MyClassLoader(URL[] urls) {
        super(urls);
    }
    
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}

// Loading ¹æ¹ý
public static void main(String[] args) {
	URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();    
	MyClassLoader l = new MyClassLoader(loader.getURLs());

	File dir = new File("./ dynamic_lib");
	File[] files = dir.listFiles();

	if (files != null) {
		// To add libraries to classpath dynamically, loading jar files in the ./ dynamic_lib directory.
		for (File file : files) {
System.out.println("file URI: " + file.toURI().toURL());
			l.addURL(file.toURI().toURL());
		}       			
	}
}

*/