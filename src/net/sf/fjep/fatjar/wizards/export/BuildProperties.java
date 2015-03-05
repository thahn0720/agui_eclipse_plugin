/*******************************************************************************
 * Copyright (c) 2004 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Fat Jar Eclipse Plug-In
 *
 * The Fat Jar Eclipse Plug-In is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Fat Jar Eclipse Plug-In is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Fat Jar Eclipse Plug-In;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package net.sf.fjep.fatjar.wizards.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.sf.fjep.fatjar.popup.actions.BuildFatJar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;


/**
 * this class stores information about the fat jar to build.
 */
public class BuildProperties {

    // Property names in .fatjar file for each project.
    public final static String ONEJAR = "onejar";
    public final static String ONEJAR_CHECKBOX = ONEJAR + ".checkbox";
    public final static String ONEJAR_EXPAND = ONEJAR + ".expand";
    public final static String ONEJAR_LICENSE_REQUIRED = ONEJAR + ".license.required";
    
    private String jarname;
    private boolean autojar_enable;
    private String autojar_visitclasses;
    private boolean autojar_classforname;
    private String manifest_file;
    private String manifest_mainclass;
    private String manifest_classpath;
    private String manifest_arguments;
    private String manifest_vmarguments;
    private boolean manifest_mergeall;
    private boolean manifest_removesigners; 
    private boolean useManifestFile;
    private boolean jarnameIsExtern;
    private boolean useOneJar;
    private boolean onejarLicenseRequired;
    private String onejarExpand;
    private String launchConfigName;
    
    private AbstractFileInfo[] excludeInfo; // filesSelectPage.getAllUnchecked();
    private AbstractFileInfo[] includeInfo; // filesSelectPage.getAllChecked();

    private JProjectConfiguration jproject;
    private String propertiesFilename;
    
    class AbstractFileInfo {
    	private String abstractName;
    	private String absoluteName;
    	AbstractFileInfo(String abstractName) {
    		this(abstractName, null);
    	}
    	AbstractFileInfo(String abstractName, String absoluteName) {
    		this.abstractName = abstractName;
    		this.absoluteName = absoluteName;
    	}
		public String getAbstractName() {
			return abstractName;
		}
		public String getAbsoluteName(JProjectConfiguration jproject) {
			IWorkspaceRoot root = jproject.getWorkspaceRoot();
			if (absoluteName == null) {
				String absPath = abstractName;
				String path = abstractName;
				if (abstractName.startsWith("<po|")) {
					String javaProjectName = abstractName.replaceFirst("[<]po[|](.*)[>](.*)", "$1");
					path = abstractName.replaceFirst("[<]po[|](.*)[>](.*)", "$2").replaceAll("[~]", "").replace('/', File.separatorChar);
                    try {
                        IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(javaProjectName);
                        if (javaProject != null) {
                            String projectOutput = javaProject.getOutputLocation().toOSString();
                            path = projectOutput + File.separatorChar + path;
                        }
                    } catch (JavaModelException e) {e.printStackTrace();}
					absPath = jproject.getAbsProjectPath(path);
				}
				else if (path.startsWith("<cl|")) {
					path = path.replaceFirst("[<]cl[|](.*)[>]", "$1/").replaceAll("[~]", "").replace('/', File.separatorChar);
					absPath = path;
				}
				else if (path.startsWith("<jar|")) {
					path = path.replaceFirst("[<]jar[|](.*)[>]", "$1"); 
					// absolute path is unknown here, but the check for excudes recognises jars to exclude without path
                    absPath = path;
				}
				else if (path.startsWith("<inc|")) {
					path = path.replaceFirst("[<]inc[|](.*)[>]", "/$1|").replaceAll("[~]", "").replace('/', File.separatorChar);
					absPath = jproject.getAbsProjectPath(path);
				}
				if (absPath.endsWith(File.separator)) {
					absPath = absPath.substring(0, absPath.length()-1);
				}
				try {
					File f = new File(absPath);
					absoluteName = f.getCanonicalPath();
				} 
				catch (IOException e) {
					absoluteName = absPath;
				}
			}
			return absoluteName;
		}
		
		
		public String[] getSegments() {
			String[] segments = abstractName.split("[~]");
			return segments;
		}
    }
    
    /**
     * 
     */
    public BuildProperties(JProjectConfiguration jproject) {
        this.jproject = jproject;
    }
    
    public String getJarname() {
        return jarname;
    }
    public void setJarname(String jarname) {
        this.jarname = jarname;
    }
    
    public boolean isAutojarEnable() {
        return autojar_enable;
    }
    public void setAutojarEnable(boolean autojar_enable) {
        this.autojar_enable = autojar_enable; 
    }
    public String getAutojarVisitClasses() {
        return autojar_visitclasses;
    }
    public void setAutojarVisitClasses(String autojar_visitclasses) {
        this.autojar_visitclasses = autojar_visitclasses;
    }
    public boolean isAutojarClassForName() {
        return autojar_classforname;
    }
    public void setAutojarClassForName(boolean autojar_classforname) {
        this.autojar_classforname = autojar_classforname; 
    }
    public boolean isJarnameIsExtern() {
        return jarnameIsExtern;
    }
    public void setJarnameIsExtern(boolean jarnameIsExtern) {
        this.jarnameIsExtern = jarnameIsExtern;
    }
    public String getManifest_classpath() {
        return manifest_classpath;
    }
    public void setManifest_classpath(String manifest_classpath) {
        this.manifest_classpath = manifest_classpath;
    }
    public String getManifest_arguments() {
        return manifest_arguments;
    }
    public void setManifest_arguments(String manifest_arguments) {
        this.manifest_arguments = manifest_arguments;
    }
    public String getManifest_vmarguments() {
        return manifest_vmarguments;
    }
    public void setManifest_vmarguments(String manifest_vmarguments) {
        this.manifest_vmarguments = manifest_vmarguments;
    }
    public String getManifest_file() {
        return manifest_file;
    }
    public void setManifest_file(String manifest_file) {
        this.manifest_file = manifest_file;
    }
    public String getManifest_mainclass() {
        return manifest_mainclass;
    }
    public void setManifest_mainclass(String manifest_mainclass) {
        this.manifest_mainclass = manifest_mainclass;
    }
    public boolean isManifest_mergeall() {
        return manifest_mergeall;
    }
    public void setManifest_mergeall(boolean manifest_mergeall) {
        this.manifest_mergeall = manifest_mergeall;
    }
    public boolean isManifest_removesigners() {
        return manifest_removesigners;
    }
    public void setManifest_removesigners(boolean manifest_removesigners) {
        this.manifest_removesigners = manifest_removesigners;
    }
    public String getOnejarExpand() {
        return onejarExpand;
    }
    public void setOnejarExpand(String onejarExpand) {
        this.onejarExpand = onejarExpand;
    }
    public boolean isOnejarLicenseRequired() {
        return onejarLicenseRequired;
    }
    public void setOnejarLicenseRequired(boolean onejarLicenseRequired) {
        this.onejarLicenseRequired = onejarLicenseRequired;
    }
    public boolean isUseManifestFile() {
        return useManifestFile;
    }
    public void setUseManifestFile(boolean useManifestFile) {
        this.useManifestFile = useManifestFile;
    }
    public boolean isUseOneJar() {
        return useOneJar;
    }
    public void setUseOneJar(boolean useOneJar) {
        this.useOneJar = useOneJar;
    }
    public String getLaunchConfigName() {
        return launchConfigName;
    }
    public void setLaunchConfigName(String launchConfigName) {
        this.launchConfigName = launchConfigName;
    }
    
    
    public AbstractFileInfo[] getExcludeInfo() {
        return excludeInfo;
    }
    public void setExcludeInfo(AbstractFileInfo[] excludeInfo) {
        this.excludeInfo = excludeInfo;
    }
    public void setExcludeInfo(String[][] excludeStrings) {
    	excludeInfo = new AbstractFileInfo[excludeStrings.length];
    	for (int i = 0; i < excludeStrings.length; i++) {
			String[] abstract_absolute = excludeStrings[i];
			excludeInfo[i] = new AbstractFileInfo(abstract_absolute[0], abstract_absolute[1]);
		}
    }

    public AbstractFileInfo[] getIncludeInfo() {
        return includeInfo;
    }
    public void setIncludeInfo(AbstractFileInfo[] includeInfo) {
        this.includeInfo = includeInfo;
    }
    public void setIncludeInfo(String[][] includeStrings) {
    	includeInfo = new AbstractFileInfo[includeStrings.length];
    	for (int i = 0; i < includeStrings.length; i++) {
			String[] abstract_absolute = includeStrings[i];
			includeInfo[i] = new AbstractFileInfo(abstract_absolute[0], abstract_absolute[1]);
		}
    }

    public void read() {

        String propertiesFile = getPropertiesFilename(); 
        Properties props = new Properties();
        try {
            File f = new File(propertiesFile);
            if (!f.exists()) {
                props.setProperty("jarname.isextern", "false");
                props.setProperty("jarname", BuildFatJar.getJarName(jproject.getJproject()));
                props.setProperty("autojar.enable", "false");
                props.setProperty("autojar.visitclasses", getDefaultAutojarVisitClasses());
                props.setProperty("manifest.file", "<createnew>");
                props.setProperty("manifest.mainclass", getDefaultMainClass());
                props.setProperty("manifest.classpath", "");
                props.setProperty("manifest.arguments", getDefaultArguments());
                props.setProperty("manifest.vmarguments", getDefaultVMArguments());
                props.setProperty("manifest.mergeall", BuildFatJar.getManifestMergeAll(jproject.getJproject()));
                props.setProperty("manifest.removesigners", BuildFatJar.getManifestRemoveSigners(jproject.getJproject()));
                props.setProperty("launchconfigname", getDefaultLaunchConfigName());
                props.setProperty("includes", "");
                props.setProperty("excludes", "");
                
                props.setProperty(ONEJAR_CHECKBOX, "false");
                props.setProperty(ONEJAR_LICENSE_REQUIRED, "true");
                props.setProperty(ONEJAR_EXPAND, "");

            
            }
            else {
                InputStream in = new FileInputStream(f);
                props.load(in);
                in.close();
            }
            setFromProperties(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return never null
     */
    private String getDefaultMainClass() {
        String result = "";
        if (jproject!=null) {
            result = jproject.getMainClass();
            if (result == null) {
                result = "";
            }
        }
        return result;
    }

    /**
     * @return never null
     */
    private String getDefaultAutojarVisitClasses() {
        String result = "";
        if (jproject!=null) {
            result = jproject.getMainClass();
            if (result == null) {
                result = "";
            }
        }
        return result;
    }

    /**
     * @return never null
     */
    private String getDefaultArguments() {
        String result = "";
        if (jproject!=null) {
            result = jproject.getArguments();
            if (result == null) {
                result = "";
            }
        }
        return result;
    }

    /**
     * @return never null
     */
    private String getDefaultVMArguments() {
        String result = "";
        if (jproject!=null) {
            result = jproject.getVMArguments();
            if (result == null) {
                result = "";
            }
        }
        return result;
    }

    /**
     * @return never null
     */
    private String getDefaultLaunchConfigName() {
        String result = "";
        if (jproject!=null) {
            result = jproject.getLaunchConfigName();
            if (result == null) {
                result = "";
            }
        }
        return result;
    }

    public void save() {
        String propertiesFile = getPropertiesFilename(); 
        boolean ok = true;
        try {
        	Properties props = toProperties();
            File f = new File(propertiesFile);
            if (f.exists() && f.canRead()) {
                // check for changes
                try {
                    InputStream in = new FileInputStream(f);
                    Properties oldProps = new Properties();
                    oldProps.load(in);
                    in.close();
                    if (oldProps.equals(props)) {
                        ok = false;
                    }
                }
                catch (Exception ignore) {}

                // allow check out of properties if readonly
                if (ok) {
                    String projectDir = BuildFatJar.getProjectDir(jproject.getJproject());
                    if (propertiesFile.startsWith(projectDir)) {
                        String relFile = propertiesFile.substring(projectDir.length()+1);
                        IFile editFile = jproject.getJproject().getProject().getFile(relFile);
                        if (editFile != null) {
                            IStatus status = AguiPlugin.askFileWriteAccess(editFile);
                            if ((status != null) && !status.isOK()) {
                                Shell shell = new Shell();
                                MessageDialog.openInformation(shell, "Fat Jar Plug-In Warning", "could not save current settings: " + status.toString());
                            }
                            ok = status.isOK();
                        }
                    }
                }
            }
            
            if (ok) {
                OutputStream out = new FileOutputStream(f);
                props.store(out, "Fat Jar Configuration File");
                out.flush();
                out.close();
                jproject.setPropertiesFilename(propertiesFile);
                try {
                    jproject.getJproject().getProject().refreshLocal(IResource.DEPTH_ONE, null);
                } catch (CoreException e) { e.printStackTrace(); }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void setFromProperties(Properties props) {
        jarname = props.getProperty("jarname", BuildFatJar.getJarName(jproject.getJproject()));
        jarnameIsExtern = Boolean.valueOf(props.getProperty("jarname.isextern")).booleanValue();
        
        autojar_enable = Boolean.valueOf(props.getProperty("autojar.enable")).booleanValue();
        autojar_visitclasses = props.getProperty("autojar.visitclasses", getDefaultAutojarVisitClasses());
        autojar_classforname = Boolean.valueOf(props.getProperty("autojar.classforname")).booleanValue();

        manifest_file = props.getProperty("manifest.file", "<createnew>");
        useManifestFile = !manifest_file.equals("<createnew>");

        manifest_mainclass = props.getProperty("manifest.mainclass", getDefaultMainClass());
        manifest_classpath = props.getProperty("manifest.classpath", "");
        manifest_arguments = props.getProperty("manifest.arguments", getDefaultArguments());
        manifest_vmarguments = props.getProperty("manifest.vmarguments", getDefaultVMArguments());
        manifest_mergeall = Boolean.valueOf(props.getProperty("manifest.mergeall")).booleanValue();
        manifest_removesigners = Boolean.valueOf(props.getProperty("manifest.removesigners")).booleanValue();

        useOneJar = Boolean.valueOf(props.getProperty(ONEJAR_CHECKBOX)).booleanValue();
        onejarLicenseRequired = Boolean.valueOf(props.getProperty(ONEJAR_LICENSE_REQUIRED)).booleanValue();
        onejarExpand = props.getProperty(ONEJAR_EXPAND, "");

        launchConfigName = props.getProperty("launchconfigname", getDefaultLaunchConfigName());
        
        String excludes = props.getProperty("excludes", "");
        if (excludes.trim().equals("")) {
        	excludeInfo = new AbstractFileInfo[0];
        }
        else {
	        String[] excludeList = excludes.split("[;]");
	        excludeInfo = new AbstractFileInfo[excludeList.length];
	        for (int i = 0; i < excludeList.length; i++) {
				String exclude = excludeList[i];
				excludeInfo[i] = new AbstractFileInfo(exclude);
			}
        }
        
        String includes = props.getProperty("includes", "");
        if (includes.trim().equals("")) {
        	includeInfo = new AbstractFileInfo[0];
        }
        else {
	        String[] includeList = includes.split("[;]");
	        includeInfo = new AbstractFileInfo[includeList.length];
	        for (int i = 0; i < includeList.length; i++) {
				String include = includeList[i];
				includeInfo[i] = new AbstractFileInfo(include);
			}
        }
    }

    public Properties toProperties() {

        Properties props = new Properties();
        props.setProperty("jarname.isextern", Boolean.toString(jarnameIsExtern));
        props.setProperty("jarname", jarname);
        
        props.setProperty("autojar.enable", Boolean.toString(autojar_enable));
        props.setProperty("autojar.visitclasses", autojar_visitclasses);
        props.setProperty("autojar.classforname", Boolean.toString(autojar_classforname));
        
        if (useManifestFile)
            props.setProperty("manifest.file", manifest_file);
        else
            props.setProperty("manifest.file", "<createnew>");
        props.setProperty("manifest.mainclass", manifest_mainclass);
        props.setProperty("manifest.classpath", manifest_classpath);
        props.setProperty("manifest.arguments", manifest_arguments);
        props.setProperty("manifest.vmarguments", manifest_vmarguments);
        props.setProperty("manifest.mergeall", Boolean.toString(manifest_mergeall));
        props.setProperty("manifest.removesigners", Boolean.toString(manifest_removesigners));
        props.setProperty(ONEJAR_CHECKBOX, Boolean.toString(useOneJar));
        props.setProperty(ONEJAR_LICENSE_REQUIRED, Boolean.toString(onejarLicenseRequired));
        props.setProperty(ONEJAR_EXPAND, onejarExpand);
        props.setProperty("launchconfigname", launchConfigName);
        
        StringBuffer excludeProp = new StringBuffer();
        for (int i=0; i<excludeInfo.length; i++) {
            if (i>0)
                excludeProp.append(';');
            excludeProp.append(excludeInfo[i].getAbstractName());
        }
        props.setProperty("excludes", excludeProp.toString());

        StringBuffer includeProp = new StringBuffer();
        for (int i=0; i<includeInfo.length; i++) {
            if (i>0)
                includeProp.append(';');
            includeProp.append(includeInfo[i].getAbstractName());
        }
        props.setProperty("includes", includeProp.toString());
        return props;
    }

    public String getPropertiesFilename() {
        String result = propertiesFilename;
        if (result == null) {
            result = jproject.getPropertiesFilename();
        }
        if (result == null) {
            result = jproject.getAbsProjectDir() + File.separator + Prefs.getRelPropertiesFilename(jproject);
        }
        return result;
    }

}
