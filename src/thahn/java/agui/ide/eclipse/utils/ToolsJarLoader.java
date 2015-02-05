package thahn.java.agui.ide.eclipse.utils;

import java.net.URL;
import java.net.URLClassLoader;
 
 
public class ToolsJarLoader extends URLClassLoader {
	
	private static ToolsJarLoader mInstance;
	public static ToolsJarLoader getInstance() {
		if(mInstance == null) {
			mInstance = new ToolsJarLoader(((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs());
		}
		return mInstance;
	}
	
    public ToolsJarLoader(URL[] urls) {
        super(urls);
    }
    
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
    
    public boolean isLoaded(URL url) {
    	URL[] urls = getURLs();
    	for(URL temp : urls) {
    		if(temp.equals(url)) {
    			return true;
    		}
    	}
    	return false;
    }
}

// Loading ¹æ¹ý
//public static void main(String[] args) {
//	URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();    
//	MyClassLoader l = new MyClassLoader(loader.getURLs());
//
//	File dir = new File("./ dynamic_lib");
//	File[] files = dir.listFiles();
//
//	if (files != null) {
//		// To add libraries to classpath dynamically, loading jar files in the ./ dynamic_lib directory.
//		for (File file : files) {
//System.out.println("file URI: " + file.toURI().toURL());
//			l.addURL(file.toURI().toURL());
//		}       			
//	}
//}
