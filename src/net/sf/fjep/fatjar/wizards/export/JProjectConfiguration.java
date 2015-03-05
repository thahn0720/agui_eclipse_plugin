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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * capsules JProject and LaunchConfiguration
 */
public class JProjectConfiguration {

    private static final String FATJAR_CONFIG_PREFIX = "fatjar_cfg_eraseme_";

    private boolean newLaunchConfig = false;

    public boolean isNewLaunchConfig() {
        return newLaunchConfig;
    }

    public void setNewLaunchConfig(boolean newLaunchConfig) {
        this.newLaunchConfig = newLaunchConfig;
    }

    private IJavaProject jproject;

    private List jProjectCollection;

    private ILaunchConfiguration launchConfig;

    private String propertiesFilename;

    public void setPropertiesFilename(String filename) {
        propertiesFilename = filename;
    }

    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    public JProjectConfiguration(IJavaProject jproject,
            ILaunchConfiguration launchConfig) {
        this.jproject = jproject;

        if (launchConfig == null) {
            String prjName = jproject.getElementName();
            launchConfig = findConfiguration(FATJAR_CONFIG_PREFIX + prjName);
            if (launchConfig == null) {
                launchConfig = createConfiguration(FATJAR_CONFIG_PREFIX
                        + prjName, prjName);
            }
        }

        this.launchConfig = launchConfig;
    }

    public JProjectConfiguration(List projectCollection) {
        this.jProjectCollection = projectCollection;
        // TODO: remove this code, temporary for regression test
        this.jproject = (IJavaProject) projectCollection.get(0);
        String prjName = jproject.getElementName();

        ILaunchConfiguration launchConfig = findConfiguration(FATJAR_CONFIG_PREFIX + prjName);
        if (launchConfig == null) {
            launchConfig = createConfiguration(FATJAR_CONFIG_PREFIX + prjName,
                    prjName);
        }
        this.launchConfig = launchConfig;
    }

    public IJavaProject getJproject() {
        return jproject;
    }

    public ILaunchConfiguration getLaunchConfig() {
        return launchConfig;
    }

    /**
     * return path to project root as absolute path
     * 
     * @return
     */
    public String getAbsProjectDir() {

        IProject project = getProject();
        String absProjectDir = absPath(project.getLocation());
        return absProjectDir;
    }

    /**
     * use folder project/TempFatjarBuildDir_erase_me for tempory build files
     */
    public String getTempBuildDir() {

        String absProjectDir = getAbsProjectDir();
        String buildDir = "TempFatjarBuildDir_erase_me";
        String result = absProjectDir + File.separator + buildDir;
        return result;
    }

    /**
     * convert IPath to absolute (canonical) string path
     * 
     * @param path
     * @return null if path is null or an invalid path
     */
    public String absPath(IPath path) {

        String result = null;
        if (path != null) {
            File f = path.toFile();
            try {
                result = f.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @return associated IProject
     */
    public IProject getProject() {
        IProject result = null;
        if (jproject != null) {
            result = jproject.getProject();
        }
        return result;
    }

    /**
     * @return associated IResource
     */
    public IResource[] getResource() {
        IResource []results = new IResource[this.jProjectCollection.size()];
        for (int i = 0; i<this.jProjectCollection.size();i++) {
            IJavaProject project = (IJavaProject) this.jProjectCollection.get(i);
            try {
                results[i] = project.getCorrespondingResource();
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    /**
     * @param name
     * @return
     */
    public IFile getIFile(String name) {

        IFile result = null;
        IProject project = getProject();
        if (project != null) {
            result = project.getFile(name);
        }
        return result;
    }

    /**
     * @return "&lt;project&gt;.&lt;lauchconfig&gt;"
     */
    public String getName() {
        String prjName = getProjectName();
        String lcName = getLaunchConfigName();
        return prjName + "." + lcName;
    }

    /**
     * @return name of the project
     * @see getName()
     */
    public String getProjectName() {
        String result = null;
        IProject project = getProject();
        if (project != null) {
            result = project.getName();
        }
        return result;
    }

    /**
     * @return name of the LaunchConfiguration
     * @see getName()
     */
    public String getLaunchConfigName() {
        String result = null;
        ILaunchConfiguration lc = getLaunchConfig();
        if (lc != null) {
            result = lc.getName();
        }
        if ((result == null) || result.startsWith(FATJAR_CONFIG_PREFIX)) {
            result = "<default>";
        }
        return result;
    }

    public IWorkspaceRoot getWorkspaceRoot() {
        IWorkspaceRoot result = null;
        if (jproject != null) {
            result = jproject.getProject().getWorkspace().getRoot();
        }
        return result;
    }

    /**
     * get absolute path for /project/xyz by resolving "project" location
     * 
     * @param path
     * @return
     */
    public String getAbsProjectPath(String path) {
        int n = path.indexOf(File.separatorChar, 1);
        String jarProjectName;
        String jarProjectPath;
        if (n != -1) {
            jarProjectName = path.substring(1, n);
            jarProjectPath = path.substring(n);
        } else {
            jarProjectName = path.substring(1);
            jarProjectPath = "";
        }
        IProject jarProject = getWorkspaceRoot().getProject(jarProjectName);
        String jarProjectRoot = absPath(jarProject.getLocation());
        String result = null;
        if (jarProjectRoot != null)
            result = jarProjectRoot + jarProjectPath;
        return result;
    }

    /**
     * add classe-dir of the project to classesDirs if it exists
     * 
     * @param classesDirs
     */
    public void getClassesDir(Vector classesDirs) {

        if (jproject != null) {
            IProject project = jproject.getProject();
            IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
            String outputDir = null;
            try {
                outputDir = getAbsProjectPath(relPath(jproject
                        .getOutputLocation()));
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
            if (!classesDirs.contains(outputDir))
                classesDirs.add(outputDir);
        }
    }

    /**
     * @return output class-dir of the project or null if none exists
     */
    public String getProjectOutputDir() {

        String outputDir = null;
        if (jproject != null) {
            IProject project = jproject.getProject();
            IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
            try {
                outputDir = getAbsProjectPath(relPath(jproject
                        .getOutputLocation()));
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
        }
        return outputDir;
    }

    /**
     * Add all jars and class-folders referenced by jproject to jarfiles /
     * classesDirs. If exportedOnly is true, then only jars/class-folders which
     * are marked as exported will be added.
     * 
     * JRE_LIB (.../jre/lib/rt.jar) is ignored and not added to jarfiles
     * 
     * @param jarfiles
     * @param classesDirs
     * @param exportedOnly
     */
    public void addClassPathEntries(Vector jarfiles, Vector classesDirs,
            Vector projects, boolean exportedOnly) {

        IClasspathEntry[] cpes = getRawClasspathEntries();
        if (cpes != null) {
            for (int i = 0; i < cpes.length; i++) {
                IClasspathEntry cpe = JavaCore
                        .getResolvedClasspathEntry(cpes[i]);
                if ((cpe != null) && (!exportedOnly || cpe.isExported())) {
                    int kind = cpe.getEntryKind();
                    String dir = relPath(cpe.getPath());
                    if (kind == IClasspathEntry.CPE_CONTAINER) {
                        try {
                            IClasspathContainer container = JavaCore
                                    .getClasspathContainer(cpe.getPath(),
                                            jproject);
                            if ((container.getKind() == IClasspathContainer.K_APPLICATION)
                                    || (container.getKind() == IClasspathContainer.K_SYSTEM)) {
                                IClasspathEntry[] cpes2 = container
                                        .getClasspathEntries();
                                for (int j = 0; j < cpes2.length; j++) {
                                    IClasspathEntry cpe2 = cpes2[j];
                                    int kind2 = cpe2.getEntryKind();
                                    String dir2 = relPath(cpe2.getPath());
                                    String jar2 = getAbsOrProjectPath(dir2);
                                    if (jar2 == null) {
                                        System.err
                                                .println("invalid classpath entry: "
                                                        + cpe2.toString());
                                    } else {
                                        File f2 = new File(jar2);
                                        if (f2.isDirectory()) {
                                            if (!classesDirs.contains(jar2)) {
                                                classesDirs.add(jar2);
                                            }
                                        } else { // assume jar file
                                            if (!jarfiles.contains(jar2)) {
                                                jarfiles.add(jar2);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JavaModelException e) {
                        }
                    } else if (kind == IClasspathEntry.CPE_LIBRARY) {
                        String jar = getAbsOrProjectPath(dir);
                        if (jar == null) {
                            System.err.println("invalid classpath entry: "
                                    + cpe.toString());
                        } else {

                            // ignore JRE_LIB
                            if (!jar.replace(File.separatorChar, '/')
                                    .toLowerCase().endsWith("/jre/lib/rt.jar")) {
                                File f = new File(jar);
                                if (f.isDirectory()) {
                                    if (!classesDirs.contains(jar)) {
                                        classesDirs.add(jar);
                                    }
                                } else { // assume jar file
                                    if (!jarfiles.contains(jar)) {
                                        jarfiles.add(jar);
                                    }
                                }
                            }
                        }
                    } else if (kind == IClasspathEntry.CPE_PROJECT) {
                        if (!exportedOnly || cpe.isExported()) {
                            IJavaProject jPro = jproject.getJavaModel()
                                    .getJavaProject(dir);
                            JProjectConfiguration jProCon = new JProjectConfiguration(
                                    jPro, null);
                            if (!projects.contains(jProCon)) {
                                projects.add(jProCon);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add all jars and class-folders referenced by jproject to jarfiles /
     * classesDirs. If exportedOnly is true, then only jars/class-folders which
     * are marked as exported will be added.
     * 
     * JRE_LIB (.../jre/lib/rt.jar) is ignored and not added to jarfiles
     * 
     * @param jarfiles
     * @param classesDirs
     * @param exportedOnly
     */
    public void getClassPathJars(Vector jarfiles, Vector classesDirs,
            boolean exportedOnly) {

        IClasspathEntry[] cpes = getRawClasspathEntries();
        if (cpes != null) {
            for (int i = 0; i < cpes.length; i++) {
                IClasspathEntry cpe = JavaCore
                        .getResolvedClasspathEntry(cpes[i]);
                if ((cpe != null) && (!exportedOnly || cpe.isExported())) {
                    int kind = cpe.getEntryKind();
                    String dir = relPath(cpe.getPath());
                    if (kind == IClasspathEntry.CPE_CONTAINER) {
                        try {
                            IClasspathContainer container = JavaCore
                                    .getClasspathContainer(cpe.getPath(),
                                            jproject);
                            if ((container.getKind() == IClasspathContainer.K_APPLICATION)
                                    || (container.getKind() == IClasspathContainer.K_SYSTEM)) {
                                IClasspathEntry[] cpes2 = container
                                        .getClasspathEntries();
                                for (int j = 0; j < cpes2.length; j++) {
                                    IClasspathEntry cpe2 = cpes2[j];
                                    int kind2 = cpe2.getEntryKind();
                                    String dir2 = relPath(cpe2.getPath());
                                    String jar2 = getAbsOrProjectPath(dir2);
                                    if (jar2 == null) {
                                        System.err
                                                .println("invalid classpath entry: "
                                                        + cpe2.toString());
                                    } else {
                                        File f2 = new File(jar2);
                                        if (f2.isDirectory()) {
                                            if (!classesDirs.contains(jar2)) {
                                                classesDirs.add(jar2);
                                            }
                                        } else { // assume jar file
                                            if (!jarfiles.contains(jar2)) {
                                                jarfiles.add(jar2);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JavaModelException e) {
                        }
                    } else if (kind == IClasspathEntry.CPE_LIBRARY) {
                        String jar = getAbsOrProjectPath(dir);
                        if (jar == null) {
                            System.err.println("invalid classpath entry: "
                                    + cpe.toString());
                        } else {

                            // ignore JRE_LIB
                            if (!jar.replace(File.separatorChar, '/')
                                    .toLowerCase().endsWith("/jre/lib/rt.jar")) {
                                File f = new File(jar);
                                if (f.isDirectory()) {
                                    if (!classesDirs.contains(jar)) {
                                        classesDirs.add(jar);
                                    }
                                } else { // assume jar file
                                    if (!jarfiles.contains(jar)) {
                                        jarfiles.add(jar);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void getChildProjects(Vector projects, boolean exportedOnly) {

        IClasspathEntry[] cpes = this.getRawClasspathEntries();
        if (cpes != null) {
            for (int i = 0; i < cpes.length; i++) {
                IClasspathEntry cpe = JavaCore
                        .getResolvedClasspathEntry(cpes[i]);
                if (cpe == null) {
                    System.err.println("Error: cpes[" + i + "]=" + cpes[i]
                            + " does not resolve");
                    continue;
                }
                int kind = cpe.getEntryKind();
                String name = relPath(cpe.getPath());
                if (kind == IClasspathEntry.CPE_CONTAINER) {
                    try {
                        IClasspathContainer container = JavaCore
                                .getClasspathContainer(cpe.getPath(), jproject);
                        if ((container.getKind() == IClasspathContainer.K_APPLICATION)
                                || (container.getKind() == IClasspathContainer.K_SYSTEM)) {
                            IClasspathEntry[] cpes2 = container
                                    .getClasspathEntries();
                            for (int j = 0; j < cpes2.length; j++) {
                                IClasspathEntry cpe2 = cpes2[j];
                                int kind2 = cpe2.getEntryKind();
                                String name2 = relPath(cpe2.getPath());
                                if (name2 == null) {
                                    System.err
                                            .println("invalid classpath entry: "
                                                    + cpe2.toString());
                                } else {
                                    if (kind2 == IClasspathEntry.CPE_PROJECT) {
                                        if (!exportedOnly || cpe2.isExported()) {
                                            if (!projects.contains(name2)) {
                                                IJavaProject jChildProject2 = jproject
                                                        .getJavaModel()
                                                        .getJavaProject(name2);
                                                JProjectConfiguration jpcChild2 = new JProjectConfiguration(
                                                        jChildProject2, null);
                                                projects.add(jpcChild2);
                                                jpcChild2.getChildProjects(
                                                        projects, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JavaModelException e) {
                    }
                } else if (kind == IClasspathEntry.CPE_PROJECT) {
                    if (name == null) {
                        System.err.println("invalid classpath entry: "
                                + cpe.toString());
                    } else {
                        if (!exportedOnly || cpe.isExported()) {
                            if (!projects.contains(name)) {
                                IJavaProject jChildProject = jproject
                                        .getJavaModel().getJavaProject(name);
                                JProjectConfiguration jpcChild = new JProjectConfiguration(
                                        jChildProject, null);
                                projects.add(jpcChild);
                                jpcChild.getChildProjects(projects, true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * get mainclass set in launch configuration
     * 
     * @return "" if not set, never null
     */
    public String getMainClass() {
        String result = null;
        if (launchConfig != null) {
            try {
                result = launchConfig.getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                        (String) null);
            } catch (CoreException ignore) {
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * get program arguments set in launch configuration
     * 
     * @return "" if not set, never null
     */
    public String getArguments() {
        String result = null;
        if (launchConfig != null) {
            try {
                result = launchConfig
                        .getAttribute(
                                IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                                (String) null);
            } catch (CoreException ignore) {
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * get vm arguments set in launch configuration
     * 
     * @return "" if not set, never null
     */
    public String getVMArguments() {
        String result = null;
        if (launchConfig != null) {
            try {
                result = launchConfig.getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                        (String) null);
            } catch (CoreException ignore) {
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    protected ILaunchConfiguration findConfiguration(String configname) {
        String projectName = getProjectName();
        ILaunchConfiguration result = null;
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault()
                    .getLaunchManager().getLaunchConfigurations();
            for (int i = 0; i < configs.length; i++) {
                ILaunchConfiguration configuration = configs[i];
                if (configuration.getName().equals(configname)) {
                    String confProjectName = configuration
                            .getAttribute(
                                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                                    "");
                    if (projectName.equals(confProjectName)) {
                        result = configuration;
                    }
                }
            }
        } catch (CoreException ignore) {
        }
        return result;
    }

    /**
     * 
     * @return list of names of all existing launch configuration for this
     *         project
     */
    public String[] getProjectConfigurations() {
        String projectName = getProjectName();
        ArrayList result = new ArrayList();
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault()
                    .getLaunchManager().getLaunchConfigurations();
            for (int i = 0; i < configs.length; i++) {
                ILaunchConfiguration configuration = configs[i];
                String attrProjectName = configuration
                        .getAttribute(
                                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                                "");
                if (attrProjectName.equals(projectName)) {
                    String lcName = configuration.getName();
                    if (lcName.startsWith(FATJAR_CONFIG_PREFIX)) {
                        lcName = "<default>";
                    }
                    result.add(lcName);
                }
            }
        } catch (CoreException ignore) {
        }
        String[] resultArray = (String[]) result.toArray(new String[result
                .size()]);
        return resultArray;
    }

    // from
    // org.eclipse.jdt.internal.debug.ui.launcher.JavaApplicationLaunchShortcut
    protected ILaunchConfiguration createConfiguration(String configname,
            String projectName) {
        ILaunchConfiguration config = null;
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            ILaunchConfigurationType configType = getJavaLaunchConfigType();
            wc = configType.newInstance(null, getLaunchManager()
                    .generateUniqueLaunchConfigurationNameFrom(configname));
        } catch (CoreException exception) {
            exception.printStackTrace();
            return null;
        }
        // wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
        // fqcnMain);
        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                projectName);
        try {
            config = wc.doSave();
        } catch (CoreException exception) {
            exception.printStackTrace();
        }
        return config;
    }

    protected ILaunchConfigurationType getJavaLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(
                IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    }

    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    private IClasspathEntry[] getRawClasspathEntries() {

        IClasspathEntry[] result = null;
        ILaunchConfiguration lc = getLaunchConfig();
        if (lc != null) {
            try {
                Vector cpeList = new Vector();
                // JavaRuntime.computeUnresolvedRuntimeClasspath(lc);
                IRuntimeClasspathProvider rcp = JavaRuntime
                        .getClasspathProvider(lc);
                if (rcp != null) {
                    IRuntimeClasspathEntry[] rawRcpes = rcp
                            .computeUnresolvedClasspath(lc);
                    IRuntimeClasspathEntry[] entries = JavaRuntime
                            .resolveRuntimeClasspath(rawRcpes, lc);
                    List userEntries = new ArrayList(entries.length);
                    for (int i = 0; i < entries.length; i++) {
                        if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
                            String location = entries[i].getLocation();
                            if (location != null) {
                                System.out.println("cpe[" + i + "]='"
                                        + entries[i].getLocation() + "'");
                                IClasspathEntry cpe = entries[i]
                                        .getClasspathEntry();
                                if (cpe != null) {
                                    cpeList.add(cpe);
                                }
                            }
                        }
                    }
                }
                result = (IClasspathEntry[]) cpeList
                        .toArray(new IClasspathEntry[cpeList.size()]);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @return
     */
    private IClasspathEntry[] oldGetRawClasspathEntries() {
        IClasspathEntry[] result = null;

        ILaunchConfiguration lc = getLaunchConfig();
        if (lc != null) {
            try {
                Vector cpeList = new Vector();
                // JavaRuntime.computeUnresolvedRuntimeClasspath(lc);
                IRuntimeClasspathProvider rcp = JavaRuntime
                        .getClasspathProvider(lc);
                if (rcp != null) {
                    IRuntimeClasspathEntry[] rawRcpes = rcp
                            .computeUnresolvedClasspath(lc);
                    // IRuntimeClasspathEntry[] rcpes =
                    // rcp.resolveClasspath(rawRcpes, lc);
                    if (rawRcpes != null) {
                        for (int i = 0; i < rawRcpes.length; i++) {
                            IRuntimeClasspathEntry rcpe = rawRcpes[i];
                            System.out.println("cpe[" + i + "]='"
                                    + rcpe.getLocation() + "'");
                            IClasspathEntry cpe = rcpe.getClasspathEntry();
                            if (cpe != null) {
                                cpeList.add(cpe);
                            }
                        }
                    }
                }
                result = (IClasspathEntry[]) cpeList
                        .toArray(new IClasspathEntry[cpeList.size()]);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = jproject.readRawClasspath();
        }
        return result;
    }

    /**
     * convert IPath to relative string path
     * 
     * @param path
     * @return
     */
    private String relPath(IPath path) {

        String result = null;
        File f = path.toFile();
        result = f.getPath();
        return result;
    }

    /**
     * check if path is absolute by checking file existence, resolve as project
     * path "/<project>/.." otherwise
     * 
     * @param path
     *            an absolute path or an project path
     * @return
     */
    public String getAbsOrProjectPath(String path) {
        String result = path;
        try {
            File f = new File(path);
            if (!f.exists()) {
                result = getAbsProjectPath(path);
            }
        } catch (Exception e) {
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        boolean result = false;
        if (arg0 instanceof JProjectConfiguration) {
            JProjectConfiguration otherJP = (JProjectConfiguration) arg0;
            if (getName().equals(otherJP.getName())) {
                result = true;
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        String hashSeed = getName();
        return hashSeed.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName();
    }

    public static void removeTempFatjarConfigs() {
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault()
                    .getLaunchManager().getLaunchConfigurations();
            for (int i = 0; i < configs.length; i++) {
                ILaunchConfiguration configuration = configs[i];
                if (configuration.getName().startsWith(FATJAR_CONFIG_PREFIX)) {
                    configuration.delete();
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public List getJProjectCollection() {
        return jProjectCollection;
    }

    public void setJProjectCollection(List projectCollection) {
        jProjectCollection = projectCollection;
    }

}
