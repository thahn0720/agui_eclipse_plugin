///*******************************************************************************
// * Copyright (c) 2004 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
// * 
// * This file is part of the Fat Jar Eclipse Plug-In
// *
// * The Fat Jar Eclipse Plug-In is free software;
// * you can redistribute it and/or modify it under the terms of the GNU
// * General Public License as published by the Free Software Foundation;
// * either version 2 of the License, or (at your option) any later version.
// * 
// * The Fat Jar Eclipse Plug-In is distributed
// * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
// * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
// * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with the Fat Jar Eclipse Plug-In;
// * if not, write to the Free Software Foundation, Inc.,
// * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *  
// *******************************************************************************/
//package net.sf.fjep.fatjar;
//
//import java.io.File;
//import java.util.MissingResourceException;
//import java.util.ResourceBundle;
//
//import net.sf.fjep.fatjar.popup.actions.BuildFatJar;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.plugin.AbstractUIPlugin;
//import org.osgi.framework.BundleContext;
//
///**
// * The main plugin class to be used in the desktop.
// */
//public class FatjarPlugin extends AbstractUIPlugin {
//
//    // The name of the One-JAR distribution being used.
//    public static final String ONE_JAR_BOOT = "one-jar-boot-0.95.jar";
//
//	//The shared instance.
//	private static FatjarPlugin plugin;
//	//Resource bundle.
//	private ResourceBundle resourceBundle;
//
//    // Property names in .fatjar file for each project.
//    public final static String ONEJAR = "onejar";
//
//    public final static String ONEJAR_CHECKBOX = ONEJAR + ".checkbox";
//
//    public final static String ONEJAR_EXPAND = ONEJAR + ".expand";
//
//    public final static String ONEJAR_LICENSE_REQUIRED = ONEJAR + ".license.required";
//	
//	/**
//	 * The constructor.
//	 */
//	public FatjarPlugin() {
//		super();
//		plugin = this;
//		try {
//			resourceBundle = ResourceBundle.getBundle("net.sf.fjep.fatjar.FatjarPluginResources");
//		} catch (MissingResourceException x) {
//			resourceBundle = null;
//		}
//	}
//
//	/**
//	 * This method is called upon plug-in activation
//	 */
//	public void start(BundleContext context) throws Exception {
//		super.start(context);
//	}
//
//	/**
//	 * This method is called when the plug-in is stopped
//	 */
//	public void stop(BundleContext context) throws Exception {
//		super.stop(context);
//	}
//
//	/**
//	 * Returns the shared instance.
//	 */
//	public static FatjarPlugin getDefault() {
//		return plugin;
//	}
//
//	/**
//	 * Returns the string from the plugin's resource bundle,
//	 * or 'key' if not found.
//	 */
//	public static String getResourceString(String key) {
//		ResourceBundle bundle = FatjarPlugin.getDefault().getResourceBundle();
//		try {
//			return (bundle != null) ? bundle.getString(key) : key;
//		} catch (MissingResourceException e) {
//			return key;
//		}
//	}
//
//	/**
//	 * Returns the plugin's resource bundle,
//	 */
//	public ResourceBundle getResourceBundle() {
//		return resourceBundle;
//	}
//    
//    /**
//     * 
//     * @param wr
//     * @param eFile
//     * @param showWarning
//     * @return null if no checkout request was needed (file does not exist or is writable),
//     * if checkout was asked, status represents the resul (isOK())
//     */
//    public static IStatus askFileWriteAccess(IFile eFile) {
//        
//        IStatus result = null;
//        if (eFile != null) {
//            File f = eFile.getFullPath().toFile();
//            if (!f.canWrite()) {
//            	if (BuildFatJar.getScmAutoCheckout()) {
//	                IFile[] editFiles = new IFile[1];
//	                editFiles[0] = eFile;
//	                Shell shell = new Shell();
//	                result = ResourcesPlugin.getWorkspace().validateEdit(editFiles, shell);
//	            }
//	        }
//    	}
//        return result;
//    }
//
//}
