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
package net.sf.fjep.fatjar.popup.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import net.sf.fjep.fatjar.builder.IFileSystemElement;
import net.sf.fjep.fatjar.builder.IFileSystemSource;
import net.sf.fjep.fatjar.extensionpoints.IJarUtilFactory;
import net.sf.fjep.fatjar.preferences.FatJarPreferencePage;
import net.sf.fjep.fatjar.wizards.export.FJExpWizard;
import net.sf.fjep.fatjar.wizards.export.JProjectConfiguration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Strings;

import thahn.java.agui.ide.eclipse.project.AguiConstants;
import thahn.java.agui.ide.eclipse.project.BaseProjectHelper;
import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class BuildFatJar implements IObjectActionDelegate {

    private final static int PROGRESS_CLEAN = 50;

    private final static int PROGRESS_COLLECT = 1000;

    private final static int PROGRESS_PACK = 300;

    private final static char PATH_SEPARATOR = '.';

    // hack for changing current properties file...
    public static String absPropertiesFilename = null;

    protected IStructuredSelection lastSelection = null;

    /**
     * Constructor for Action1.
     */
    public BuildFatJar() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        if ((lastSelection != null) && !lastSelection.isEmpty()) {
            Object sel = lastSelection.getFirstElement();
            if (sel instanceof IJavaProject) {
                absPropertiesFilename = null;
                if (action.getId().equals("net.sf.fjep.fatjar.buildFatJar"))
                    runBuildFatJar(lastSelection.toList());
                else
                    System.err.println("unknown action-id for BuildFatJar: "
                            + action.getId());
            } else if (sel instanceof IFile) {
                for(Iterator iter =  lastSelection.iterator();iter.hasNext();) {
                    Object ifile = iter.next();
                    if(ifile instanceof IFile)
                        buildFatJarConditional(action, ifile);
                }
            }
        }
        
    }

    private void buildFatJarConditional(IAction action, Object sel) {
        IFile settings = (IFile) sel;
        String projectName = settings.getProject().getName();
        IJavaProject jproject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
        absPropertiesFilename = settings.getLocation().toOSString();
        if (action.getId().equals("net.sf.fjep.fatjar.buildFatJarFromSettings")) {
            List jplist = new ArrayList();
            jplist.add(jproject);
            runBuildFatJar(jplist);
        } else if (action.getId().equals("net.sf.fjep.fatjar.quickBuildFatJarFromSettings")) {
            // TODO: Add collections for quick build
            runQuickBuildFatJarFromSettings(jproject);
        } else {
            System.err.println("unknown action-id for BuildFatJar: "
                    + action.getId());
        }
    }

    /**
     * @param jproject
     * @param settings
     */
    private void runQuickBuildFatJarFromSettings(IJavaProject jproject) {

        try {
            doQuickBuildFatJar(jproject);
        } catch (Exception e) {
            e.printStackTrace();
            Shell shell = new Shell();
            MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR", e
                    .getMessage());
        }
    }

    protected void runBuildFatJar(List jprojects) {

        boolean ok = false;
        try {
            ok = doBuildFatJar(jprojects);
        } catch (Exception e) {
            e.printStackTrace();
            Shell shell = new Shell();
            MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR", e
                    .getMessage());
        }
    }

    public void runBuildConfiguredFatJar(IJavaProject jproject,
            Properties props, Set excludes, ArrayList includes) {

        // Create a progress bar
        Shell shell = new Shell();
        ProgressMonitorDialog progressmonitordialog = new ProgressMonitorDialog(
                shell);
        progressmonitordialog.open();
        IProgressMonitor iprogressmonitor = progressmonitordialog
                .getProgressMonitor();
        iprogressmonitor.beginTask("Build Fat jar", PROGRESS_CLEAN
                + PROGRESS_COLLECT + PROGRESS_PACK + PROGRESS_CLEAN);

        // Make One-JAR?
        boolean onejar = new Boolean(props
                .getProperty(AguiPlugin.ONEJAR_CHECKBOX)).booleanValue();
        String expand = props.getProperty(AguiPlugin.ONEJAR_EXPAND);

        String jarfile = null;
        try {
            saveConfig(jproject, props);

            doCleanFatJar(jproject, iprogressmonitor);
            doCollectFatJar(jproject, iprogressmonitor, excludes, includes,
                    onejar, expand);
            jarfile = doPackFatJar(jproject, iprogressmonitor);
            doCleanFatJar(jproject, iprogressmonitor);
            try {
                jproject.getProject().refreshLocal(IResource.DEPTH_ONE, null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR", e
                    .getMessage());
        }

        progressmonitordialog.close();
        MessageDialog.openInformation(shell, "Fat Jar Plug-In", "built "
                + jarfile);
    }

    private boolean doBuildFatJar(List jprojects) {

        boolean ok = false;
        Shell shell = new Shell();
        JProjectConfiguration jprojectConf = new JProjectConfiguration(
                jprojects);
        jprojectConf.setPropertiesFilename(absPropertiesFilename);
        FJExpWizard wizard = new FJExpWizard(jprojectConf);
        ok = wizard.execute(shell);
        if (ok) {
            try {
                for (Iterator iter = jprojects.iterator(); iter.hasNext();) {
                    IJavaProject jproject = (IJavaProject) iter.next();
                    jproject.getProject().refreshLocal(IResource.DEPTH_ONE,
                            null);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    // TODO: beta1 - change to launchconfig
    public void doQuickBuildFatJar(IJavaProject jproject) {
        Properties props = getFatJarProperties(jproject);
        Set excludes = getExcludes(jproject, props.getProperty("excludes"));
        ArrayList includes = getIncludes(jproject, props
                .getProperty("includes"));

        // Create a progress bar
        Shell shell = new Shell();
        ProgressMonitorDialog progressmonitordialog = new ProgressMonitorDialog(
                shell);
        progressmonitordialog.open();
        IProgressMonitor iprogressmonitor = progressmonitordialog
                .getProgressMonitor();
        iprogressmonitor.beginTask("Quick Build Fat jar", PROGRESS_CLEAN
                + PROGRESS_COLLECT + PROGRESS_PACK + PROGRESS_CLEAN);

        // Make One-JAR?
        boolean onejar = new Boolean(props
                .getProperty(AguiPlugin.ONEJAR_CHECKBOX)).booleanValue();
        String expand = props.getProperty(AguiPlugin.ONEJAR_EXPAND);

        String jarfile = null;
        try {
            // saveConfig(jproject, props);

            doCleanFatJar(jproject, iprogressmonitor);
            doCollectFatJar(jproject, iprogressmonitor, excludes, includes,
                    onejar, expand);
            jarfile = doPackFatJar(jproject, iprogressmonitor);
            doCleanFatJar(jproject, iprogressmonitor);
            try {
                jproject.getProject().refreshLocal(IResource.DEPTH_ONE, null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR", e
                    .getMessage());
        }

        progressmonitordialog.close();
        MessageDialog.openInformation(shell, "Fat Jar Plug-In", "built "
                + jarfile);
        // testExtensionPoints(jarfile);

    }

    /**
     * @param jarfile
     */
    private void testExtensionPoints(String jarfile) {

        try {
            System.out
                    .println("dumping contents in jar using extension net.sf.fjep.fatjar.fatjarJarUtil");
            System.out.println("--- " + jarfile + " ---");
            IExtensionRegistry reg = Platform.getExtensionRegistry();
            IExtensionPoint exp = reg
                    .getExtensionPoint("net.sf.fjep.fatjar.jarutil");
            IExtension[] extensions = exp.getExtensions();
            IConfigurationElement[] elements = extensions[0]
                    .getConfigurationElements();
            IJarUtilFactory ju = (IJarUtilFactory) elements[0]
                    .createExecutableExtension("class");
            IFileSystemSource fss = ju.createJARFileSystemSource(jarfile, "");
            while (fss.hasMoreElements()) {
                IFileSystemElement fse = fss.nextElement();
                System.out.println(fse.getFullName());
            }
            System.out.println("--- finished ---");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param jproject
     * @param property
     * @return
     */
    private ArrayList getIncludes(IJavaProject jproject, String includeString) {

        ArrayList result = new ArrayList();
        IWorkspaceRoot root = jproject.getProject().getWorkspace().getRoot();
        if ((includeString != null) && !includeString.trim().equals("")) {
            String[] includes = includeString.split("[;]");
            for (int i = 0; i < includes.length; i++) {
                String include = includes[i];
                if (include.startsWith("<inc|"))
                    include = include.replaceFirst("[<]inc[|](.*)[>]", "/$1|")
                            .replaceAll("[~]", "").replace('/',
                                    File.separatorChar);
                String absPath = absProjectPath(root, include);
                result.add(absPath);
            }
        }
        return result;
    }

    /**
     * @param jproject
     * @param property
     * @return
     */
    private Set getExcludes(IJavaProject jproject, String excludeString) {

        Set result = new HashSet();
        IWorkspaceRoot root = jproject.getProject().getWorkspace().getRoot();
        if ((excludeString != null) && !excludeString.trim().equals("")) {
            String[] excludes = excludeString.split("[;]");
            for (int i = 0; i < excludes.length; i++) {
                String exclude = excludes[i];
                String absPath = exclude;
                if (exclude.startsWith("<po|")) {
                    String javaProjectName = exclude.replaceFirst(
                            "[<]po[|](.*)[>](.*)", "$1");
                    exclude = exclude.replaceFirst("[<]po[|](.*)[>](.*)", "$2")
                            .replaceAll("[~]", "").replace('/',
                                    File.separatorChar);
                    try {
                        IJavaProject javaProject = JavaModelManager
                                .getJavaModelManager().getJavaModel()
                                .getJavaProject(javaProjectName);
                        if (javaProject != null) {
                            String projectOutput = javaProject
                                    .getOutputLocation().toOSString();
                            exclude = projectOutput + File.separatorChar
                                    + exclude;
                        }
                    } catch (JavaModelException e) {
                        e.printStackTrace();
                    }
                    absPath = absProjectPath(root, exclude);
                } else if (exclude.startsWith("<cl|")) {
                    exclude = exclude.replaceFirst("[<]cl[|](.*)[>]", "$1/")
                            .replaceAll("[~]", "").replace('/',
                                    File.separatorChar);
                    absPath = exclude;
                } else if (exclude.startsWith("<jar|")) {
                    exclude = exclude.replaceFirst("[<]jar[|](.*)[>]", "$1");
                    // absolute path is unknown here, but the check for excudes
                    // recognises jars to exclude without path
                    absPath = exclude;
                }
                if (absPath.endsWith(File.separator)) {
                    absPath = absPath.substring(0, absPath.length() - 1);
                }
                result.add(absPath);
            }
        }
        return result;
    }

    private void saveConfig(IJavaProject jproject, Properties props) {

        boolean ok = true;
        try {
            String propertiesFile = getPropertiesFilename(jproject);
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
                } catch (Exception ignore) {
                }

                // allow check out of properties if readonly
                if (ok) {
                    String projectDir = getProjectDir(jproject);
                    if (propertiesFile.startsWith(projectDir)) {
                        String relFile = propertiesFile.substring(projectDir
                                .length() + 1);
                        IFile editFile = jproject.getProject().getFile(relFile);
                        if (editFile != null) {
                            IStatus status = AguiPlugin
                                    .askFileWriteAccess(editFile);
                            ok = (status == null) || status.isOK();
                            if (!ok) {
                                Shell shell = new Shell();
                                MessageDialog.openInformation(shell,
                                        "Fat Jar Plug-In Warning",
                                        "could not save current settings: "
                                                + status.toString());
                            }
                        }
                    }
                }
            }

            if (ok) {
                OutputStream out = new FileOutputStream(f);
                props.store(out, "Fat Jar Configuration File");
                out.flush();
                out.close();
                try {
                    jproject.getProject().refreshLocal(IResource.DEPTH_ONE,
                            null);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getFatJarProperties(IJavaProject jproject) {

        Properties props = new Properties();
        try {
            String propertiesFile = getPropertiesFilename(jproject);
            File f = new File(propertiesFile);
            if (!f.exists()) {
                props.setProperty("jarname.isextern", "false");
                props.setProperty("jarname", getJarName(jproject));
                props.setProperty("manifest.file", "<createnew>");
                props.setProperty("manifest.mainclass", "");
                props.setProperty("manifest.classpath", "");
                props.setProperty("manifest.mergeall",
                        getManifestMergeAll(jproject));
                props.setProperty("includes", "notyetsupported");
                props.setProperty("excludes", "notyetsupported");
            } else {
                InputStream in = new FileInputStream(f);
                props.load(in);
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    private String doCollectFatJar(IJavaProject jproject,
            IProgressMonitor iprogressmonitor, Set excludes,
            ArrayList includes, boolean onejar, String onejarexpand)
            throws IOException {

        Shell shell = new Shell();

        String jartype = onejar ? "One-JAR" : "Fat Jar";

        iprogressmonitor.subTask("Collect " + jartype + " - reading classpath");

        Vector childProjects = new Vector();
        Vector jarFiles = new Vector();
        Vector classesDirs = new Vector();
        Vector libDirs = new Vector();
        Vector libNames = new Vector();

        getChildProjects(jproject, childProjects, false);
        for (int i = childProjects.size() - 1; i >= 0; i--) {
            IJavaProject jChildProject = (IJavaProject) childProjects.get(i);
            if (onejar) {
                // Ferenc: libNames have to be updated for every new entry in
                // libdirs.
                // dirname is not unique, so using enumeration of childproject
                // name
                String childProjectName = jChildProject.getElementName();
                int current = libDirs.size();
                getClassPathJars(jChildProject, jarFiles, libDirs, true);
                for (int j = 1; j <= libDirs.size() - current; j++) {
                    String libDir = (String) libDirs.get(j - 1);
                    int start = libDir.indexOf(childProjectName);
                    if (start >= 0) {
                        start += childProjectName.length() + 1;
                        String libName = libDir.substring(start).replace(
                                File.separatorChar, PATH_SEPARATOR);
                        libNames.add(libName);
                    } else {
                        libNames.add(childProjectName + PATH_SEPARATOR
                                + Integer.toString(i));
                    }
                }
                getClassesDir(jChildProject, libDirs);
                libNames.add(childProjectName);
            } else {
                getClassPathJars(jChildProject, jarFiles, classesDirs, true);
                getClassesDir(jChildProject, classesDirs);
            }
        }

        if (onejar) {
            int current = libDirs.size();
            String projectName = jproject.getElementName();
            getClassPathJars(jproject, jarFiles, libDirs, false);
            for (int i = 1; i <= libDirs.size() - current; i++) {
                String libDir = (String) libDirs.get(i - 1);
                int start = libDir.indexOf(projectName);
                if (start >= 0) {
                    start += projectName.length() + 1;
                    String libName = libDir.substring(start).replace(
                            File.separatorChar, PATH_SEPARATOR);
                    libNames.add(libName);
                } else {
                    libNames.add(projectName + PATH_SEPARATOR
                            + Integer.toString(i));
                }
            }
        } else {
            getClassPathJars(jproject, jarFiles, classesDirs, false);
        }
        getClassesDir(jproject, classesDirs);
        String projectDir = getProjectDir(jproject);
        String projectName = getProjectName(jproject);
        String tempdir = configGetTempBuildDir(jproject);
        boolean mergeManifests = configGetManifestMergeAll(jproject);

        mkdir(tempdir);

        ManifestData manifestData = null;
        if (mergeManifests)
            manifestData = new ManifestData();
        iprogressmonitor.worked(PROGRESS_COLLECT * 1 / 20);

        for (int i = 0; i < jarFiles.size(); i++) {
            String jarFile = (String) jarFiles.get(i);
            String jarname = File.separatorChar + jarFile;
            jarname = jarname
                    .substring(jarname.lastIndexOf(File.separatorChar) + 1);
            iprogressmonitor.subTask("Collect " + jartype + " - extracting "
                    + jarname);
            if (!checkFileInList(jarFile, excludes)
                    && !checkFileInList(jarname, excludes))
                extractJar(jarFile, tempdir, manifestData, onejar);
            iprogressmonitor.worked(PROGRESS_COLLECT * 15 / 20
                    / jarFiles.size());
        }

        for (int i = 0; i < classesDirs.size(); i++) {
            String classesDir = (String) classesDirs.get(i);
            iprogressmonitor.subTask("Collect " + jartype
                    + " - copying class files " + classesDir);
            if (!onejar) {
                copyFiles(classesDir, tempdir, excludes, jproject);
            } else {
                // there is only one element in classesDirs if onejar is true,
                // so this code is only executed once.
                String mainClass = configGetMainClass(jproject);
                if (mainClass == null || mainClass.length() == 0)
                    throw new IOException(
                            "Main-Class manifest entry cannot be missing in a One-JAR application");
                String classPath = configGetClassPath(jproject);
                copyJar(classesDir, tempdir, excludes, mainClass, classPath,
                        "main/main.jar", jproject);
                copyOneJARBootFiles(tempdir);
            }
            iprogressmonitor.worked(PROGRESS_COLLECT * 2 / 20
                    / classesDirs.size());
        }

        // One-JAR supporting libraries taken from other projects classes.
        for (int i = 0; i < libDirs.size(); i++) {
            String classesDir = (String) libDirs.get(i);
            copyJar(classesDir, tempdir, excludes, null, null, "lib/"
                    + libNames.get(i) + ".jar", jproject);
        }

        for (int i = 0; i < includes.size(); i++) {
            String include = (String) includes.get(i);
            String[] basedir_relpath = include.split("[|]");
            String baseDir = basedir_relpath[0];
            String relPath = "";
            if (basedir_relpath.length > 1)
                relPath = File.separatorChar + basedir_relpath[1];
            iprogressmonitor.subTask("Collect " + jartype
                    + " - including resource files " + relPath);
            String src = baseDir + relPath;
            String dest = tempdir + relPath;
            File fsrc = new File(src);
            File fdest = new File(dest);
            fdest.getParentFile().mkdirs();
            recursiveCopyFiles(fsrc, fdest);
        }
        iprogressmonitor.worked(PROGRESS_COLLECT * 1 / 20);

        iprogressmonitor.subTask("Collect " + jartype + " creating manifest");
        mkdir(tempdir + File.separator + "META-INF");
        if (manifestData == null)
            manifestData = new ManifestData();
        String manifestFile = configGetManifestFile(jproject);
        if (manifestFile.equals("<createnew>")) {
            manifestData.clearMainSection();
            manifestData.addMainSectionLine("Manifest-Version: 1.0");
            if (onejar) {
                manifestData
                        .addMainSectionLine("Created-By: Fat Jar/One-JAR Eclipse Plug-In");
                manifestData
                        .addMainSectionLine("Main-Class: com.simontuffs.onejar.Boot");
                if (onejarexpand.length() > 0)
                    manifestData.addMainSectionLine("One-Jar-Expand: "
                            + onejarexpand);

            } else {
                manifestData
                        .addMainSectionLine("Created-By: Fat Jar Eclipse Plug-In");
                String mainClass = configGetMainClass(jproject);
                if ((mainClass != null) && !mainClass.equals(""))
                    manifestData.addMainSectionLine("Main-Class: " + mainClass);
                String classPath = configGetClassPath(jproject);
                if ((classPath != null) && !classPath.equals(""))
                    manifestData.addMainSectionLine("Class-Path: " + classPath);
            }
        } else {
            manifestData.addFile(projectDir + File.separator + manifestFile);
        }
        String manifestText = manifestData.toString();
        writeTextToFile(new File(tempdir + File.separator + "META-INF"
                + File.separator + "MANIFEST.MF"), manifestText);
        iprogressmonitor.worked(PROGRESS_COLLECT * 1 / 20);

        return tempdir;
    }

    private boolean checkFileInList(String filename, Set files) {
        boolean result = false;
        if (files != null) {
            if (files.contains(filename))
                result = true;
            else {
                File f = new File(filename);
                try {
                    String abs = f.getCanonicalPath();
                    if (files.contains(abs))
                        result = true;
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    public FJTree buildTree(IJavaProject jproject) {
        String projectName = jproject.getElementName();
        FJTree rootNode = new FJTree(null, FJTree.NT_ROOT,
                "Fat Jar Tree for Project " + projectName, jproject,
                FJTree.CS_CHECKED);
        Vector childProjects = new Vector();
        Vector jarFiles = new Vector();
        Vector classesDirs = new Vector();

        getClassPathJars(jproject, jarFiles, classesDirs, false);
        getClassesDir(jproject, classesDirs);
        String classesDir = (String) classesDirs.lastElement();
        rootNode.addChild(FJTree.NT_PROJECT_OUTPUT, "Project '"
                + jproject.getElementName() + "' output", new File(classesDir),
                FJTree.CS_CHECKED);
        for (int i = 0; i < classesDirs.size() - 1; i++) {
            classesDir = (String) classesDirs.get(i);
            rootNode.addChild(FJTree.NT_CLASSES,
                    "Classes '" + classesDir + "'", new File(classesDir),
                    FJTree.CS_CHECKED);
        }

        getChildProjects(jproject, childProjects, false);
        for (int i = 0; i < childProjects.size(); i++) {
            IJavaProject jChildProject = (IJavaProject) childProjects.get(i);
            String childProjectName = jChildProject.getElementName();
            classesDirs.clear();
            getClassPathJars(jChildProject, jarFiles, classesDirs, true);
            getClassesDir(jChildProject, classesDirs);
            classesDir = (String) classesDirs.lastElement();
            rootNode.addChild(FJTree.NT_PROJECT_OUTPUT, "Project '"
                    + childProjectName + "' output", new File(classesDir),
                    FJTree.CS_CHECKED);
            for (int j = 0; j < classesDirs.size() - 1; j++) {
                classesDir = (String) classesDirs.get(j);
                rootNode.addChild(FJTree.NT_CLASSES, "Classes '" + classesDir
                        + "'", new File(classesDir), FJTree.CS_CHECKED);
            }
        }

        for (int i = 0; i < jarFiles.size(); i++) {
        	int checkd = FJTree.CS_CHECKED;
            String jarFile = (String) jarFiles.get(i);
            if (jarFile.endsWith(AguiConstants.AGUI_SDK_JAR)) {
            	checkd = FJTree.CS_UNCHECKED;
            }
            String jarname = File.separatorChar + jarFile;
            jarname = jarname.substring(jarname.lastIndexOf(File.separatorChar) + 1);
            rootNode.addChild(FJTree.NT_JAR, jarname, new File(jarFile), checkd);
        }
        // include AguiManifest.xml
        String aguiManifestPath = BaseProjectHelper.getManifest(jproject.getProject()).getRawLocation().toFile().getAbsolutePath();
        rootNode.addChild(FJTree.NT_FILE, AguiConstants.AGUI_MANIFEST, new File(aguiManifestPath), FJTree.CS_CHECKED);
        // res
        String aguiResPath = BaseProjectHelper.getResFolder(jproject.getProject()).getRawLocation().toFile().getAbsolutePath();
        rootNode.addChild(FJTree.NT_DIR, AguiConstants.FD_RES, new File(aguiResPath), FJTree.CS_CHECKED);
        
        return rootNode;
    }

    private void getChildProjects(IJavaProject jproject, Vector projects,
            boolean exportedOnly) {

        IClasspathEntry[] cpes = jproject.readRawClasspath();
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
                                                projects.add(jChildProject2);
                                                getChildProjects(
                                                        jChildProject2,
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
                                projects.add(jChildProject);
                                getChildProjects(jChildProject, projects, true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * do some security checking, that not the whole harddisc is scratched. if
     * tempDir is not a subdirectory of the project dir, then use
     * "TempFatjarBuildDir"
     */
    private String configGetTempBuildDir(IJavaProject jproject) {

        String projectDir = getProjectDir(jproject);
        String absProject = projectDir + File.separatorChar;
        String buildDir = "TempFatjarBuildDir_erase_me";
        String result = absProject + buildDir;
        return result;
    }

    private boolean configGetJarNameIsExtern(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("jarname.isextern", "false");
        return result.equalsIgnoreCase("true");
    }

    private String configGetJarName(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("jarname", getJarName(jproject));
        if (!configGetJarNameIsExtern(jproject))
            result = getProjectDir(jproject) + File.separator + result;
        return result;
    }

    private String configGetManifestFile(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("manifest.file", "<createnew>");
        return result;
    }

    private String configGetMainClass(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("manifest.mainclass", "");
        return result;
    }

    private String configGetClassPath(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("manifest.classpath", "");
        return result;
    }

    private boolean configGetManifestMergeAll(IJavaProject jproject) {
        Properties props = getProperties(jproject);
        String result = props.getProperty("manifest.mergeall", "true");
        return result.equalsIgnoreCase("true");
    }

    private Properties getProperties(IJavaProject jproject) {
        Properties props = new Properties();
        try {
            String propertiesFile = getPropertiesFilename(jproject);
            File f = new File(propertiesFile);
            String msg;
            if (f.exists()) {
                InputStream in = new FileInputStream(f);
                props.load(in);
                in.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    private String doPackFatJar(IJavaProject jproject,
            IProgressMonitor iprogressmonitor) {

        String tempdir = configGetTempBuildDir(jproject);
        String jarName = configGetJarName(jproject);
        boolean ok = checkWritable(jproject, jarName);
        if (!ok) {
            jarName = "(no write access)";
        } else {
            iprogressmonitor.subTask("Pack Fat Jar - " + jarName);
            packJar(jarName, tempdir);
            iprogressmonitor.worked(PROGRESS_PACK);
        }
        return jarName;
    }

    private boolean checkWritable(IJavaProject jproject, String filename) {

        boolean result = true;
        String projectDir = getProjectDir(jproject) + File.separator;
        if (filename.startsWith(projectDir)) {
            String relFilename = filename.substring(projectDir.length());
            IFile editFile = jproject.getProject().getFile(relFilename);
            IStatus status = AguiPlugin.askFileWriteAccess(editFile);
            if ((status != null) && !status.isOK()) {
                result = false;
                Shell shell = new Shell();
                MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR",
                        "no write acces for output jar '" + relFilename + "': "
                                + status.toString());
            }
        }
        return result;
    }

    /**
     * 50 progress
     */
    private String doCleanFatJar(IJavaProject jproject,
            IProgressMonitor iprogressMonitor) {

        String tempdir = configGetTempBuildDir(jproject);
        iprogressMonitor.subTask("Clean Fat Jar - remove " + tempdir);
        recursiveRm(new File(tempdir));
        iprogressMonitor.worked(50);
        return tempdir;
    }

    public static String getJarName(IJavaProject jproject) {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        String result = store.getString(FatJarPreferencePage.P_JARNAME);
        return result.replaceAll("[<]project[>]", getProjectName(jproject));
    }

    public static String getManifestMergeAll(IJavaProject jproject) {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        boolean result = store.getBoolean(FatJarPreferencePage.P_MERGEMANIFEST);
        return (result ? "true" : "false");
    }

    public static String getManifestRemoveSigners(IJavaProject jproject) {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        boolean result = store.getBoolean(FatJarPreferencePage.P_REMOVESIGNERS);
        return (result ? "true" : "false");
    }

    public static boolean getScmAutoCheckout() {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        boolean result = store
                .getBoolean(FatJarPreferencePage.P_SCMAUTOCHECKOUT);
        return result;
    }

    private static String getRelPropertiesFilename(IJavaProject jproject) {
        Preferences prefs = AguiPlugin.getDefault().getPluginPreferences();
        String result = prefs.getString(FatJarPreferencePage.P_CONFIGFILE);
        if (Strings.isNullOrEmpty(result)) {
        	return result = getProjectName(jproject) + "-agui.properties";
        }
        return result.replaceAll("[<]project[>]", getProjectName(jproject));
    }

    public static String getPropertiesFilename(IJavaProject jproject) {
        String result = absPropertiesFilename;
        if (result == null)
            result = getProjectDir(jproject) + File.separator
                    + getRelPropertiesFilename(jproject);
        return result;
    }

    private void recursiveJarFilesExclude(JarOutputStream jarro, File rootDir,
            String filename, Set excludes) {

        try {
            File fsrc;
            if (filename == null)
                fsrc = rootDir;
            else
                fsrc = new File(rootDir, filename);
            if (!checkFileInList(fsrc, excludes)) {
                if (fsrc.isDirectory()) {
                    if (filename == null)
                        filename = "";
                    else {
                        filename += File.separator;
                        JarEntry entry = new JarEntry(filename.replace('\\',
                                '/'));
                        jarro.putNextEntry(entry);
                        jarro.closeEntry();
                    }
                    String[] filenames = fsrc.list();
                    for (int i = 0; i < filenames.length; i++) {
                        recursiveJarFilesExclude(jarro, rootDir, filename
                                + filenames[i], excludes);
                    }
                } else {
                    JarEntry entry = new JarEntry(filename.replace('\\', '/'));
                    entry.setSize(fsrc.length());
                    entry.setTime(fsrc.lastModified());
                    jarro.putNextEntry(entry);
                    FileInputStream in = new FileInputStream(fsrc);
                    byte[] buffer = new byte[4096];
                    int cnt = in.read(buffer);
                    while (cnt > 0) {
                        jarro.write(buffer, 0, cnt);
                        cnt = in.read(buffer);
                    }
                    jarro.closeEntry();
                    in.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recursiveJarFiles(JarOutputStream jarro, File rootDir,
            String filename) {

        try {
            File fsrc;
            if (filename == null)
                fsrc = rootDir;
            else
                fsrc = new File(rootDir, filename);
            if (fsrc.isDirectory()) {
                if (filename == null)
                    filename = "";
                else {
                    filename += File.separator;
                    JarEntry entry = new JarEntry(filename.replace('\\', '/'));
                    jarro.putNextEntry(entry);
                    jarro.closeEntry();
                }
                String[] filenames = fsrc.list();
                for (int i = 0; i < filenames.length; i++) {
                    recursiveJarFiles(jarro, rootDir, filename + filenames[i]);
                }
            } else {
                JarEntry entry = new JarEntry(filename.replace('\\', '/'));
                entry.setSize(fsrc.length());
                entry.setTime(fsrc.lastModified());
                jarro.putNextEntry(entry);
                FileInputStream in = new FileInputStream(fsrc);
                byte[] buffer = new byte[4096];
                int cnt = in.read(buffer);
                while (cnt > 0) {
                    jarro.write(buffer, 0, cnt);
                    cnt = in.read(buffer);
                }
                jarro.closeEntry();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void packJar(String jarName, String tempdir) {

        try {
            OutputStream out = new FileOutputStream(jarName);
            JarOutputStream jarro = new JarOutputStream(out);
            recursiveJarFiles(jarro, new File(tempdir), null);
            jarro.flush();
            jarro.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getProjectName(IJavaProject jproject) {
        IProject project = jproject.getProject();
        String projectName = project.getName();
        return projectName;
    }

    private void extractJar(String jarfilename, String destdir,
            ManifestData md, boolean onejar) {

        try {
            if (!onejar) {
                JarFile jarFile = new JarFile(jarfilename);
                Enumeration enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    if (jarEntry.isDirectory()) {
                        mkdir(destdir + File.separator + entryName);
                    } else {
                        mkParentDirs(destdir + File.separator + entryName);
                        InputStream in = jarFile.getInputStream(jarEntry);
                        writeToFile(in, destdir + File.separator + entryName);
                        in.close();
                        if ((md != null)
                                && (entryName
                                        .equalsIgnoreCase("META-INF/MANIFEST.MF"))) {
                            md.addFile(destdir + File.separator + entryName);
                        }
                    }
                }
                jarFile.close();
            } else {
                // One-JAR just copies the JAR files in to a "lib" directory.
                FileInputStream input = new FileInputStream(new File(
                        jarfilename));
                String output = destdir + "/lib/"
                        + new File(jarfilename).getName();
                ;
                mkParentDirs(output);
                writeToFile(input, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mkParentDirs(String filename) {
        String normFilename = filename.replace('\\', File.separatorChar)
                .replace('/', File.separatorChar);
        int pos = normFilename.lastIndexOf(File.separatorChar);
        String dir = normFilename.substring(0, pos);
        File f = new File(dir);
        f.mkdirs();
    }

    private void writeTextToFile(File f, String text) {
        try {
            OutputStream out = new FileOutputStream(f);
            out.write(text.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeToFile(InputStream in, String filename) {
        writeToFile(in, new File(filename));
    }

    private void writeToFile(InputStream in, File fdest) {

        try {
            FileOutputStream out = new FileOutputStream(fdest);
            byte[] buffer = new byte[4096];
            int cnt = in.read(buffer);
            while (cnt > 0) {
                out.write(buffer, 0, cnt);
                cnt = in.read(buffer);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyJar(String classesDir, String tempdir, Set excludes,
            String mainclass, String classpath, String jar,
            IJavaProject jproject) throws IOException {
        // Copy classes into a "main.jar" file under "main" subdirectory.

        // stop if dir is excluded
        if (checkFileInList(classesDir, excludes))
            return;

        // exclude tempdir if it is beyond classes
        if (checkSubDir(new File(classesDir), new File(tempdir))) {
            try {
                String abs = new File(tempdir).getCanonicalPath();
                if (excludes == null)
                    excludes = new HashSet();
                excludes.add(abs);
            } catch (IOException e) {
            }
        }

        // exclude output jarfile if it is beyond srcDir to avoid doubling with
        // each build
        if (checkSubDir(new File(classesDir), new File(
                configGetJarName(jproject)))) {
            try {
                String abs = new File(configGetJarName(jproject))
                        .getCanonicalPath();
                if (excludes == null)
                    excludes = new HashSet();
                excludes.add(abs);
            } catch (IOException e) {
            }
        }

        File maindir = new File(tempdir, jar);
        maindir.getParentFile().mkdirs();
        JarOutputStream output = new JarOutputStream(new FileOutputStream(
                maindir));

        // Write the manifest which would have been present in the original
        // Fat-Jar file.
        JarEntry man = new JarEntry("META-INF/MANIFEST.MF");
        output.putNextEntry(man);
        ManifestData manifestData = new ManifestData();
        manifestData
                .addMainSectionLine("Created-By: Fat Jar/One-JAR Eclipse Plug-In");
        manifestData.addMainSectionLine("Main-Class: " + mainclass);
        if ((classpath != null) && !classpath.equals(""))
            manifestData.addMainSectionLine("Class-Path: " + classpath);
        output.write(manifestData.toString().getBytes());

        if ((excludes != null) || (excludes.size() > 0))
            recursiveJarFilesExclude(output, new File(classesDir), null,
                    excludes);
        else
            recursiveJarFiles(output, new File(classesDir), null);

        output.close();
    }

    private void copyOneJARBootFiles(String tempdir) throws IOException {
        // Put the OneJAR Boot files at the top of the tree.
        JarInputStream jis = new JarInputStream(AguiPlugin.class
                .getResourceAsStream(AguiPlugin.ONE_JAR_BOOT));
        JarEntry entry = (JarEntry) jis.getNextEntry();
        while (entry != null) {
            File dest = new File(tempdir, entry.getName());
            if (dest.getName().endsWith(".class")
                    || entry.getName().startsWith("doc")) {
                dest.getParentFile().mkdirs();
                writeToFile(jis, dest);
            }
            entry = (JarEntry) jis.getNextEntry();
        }
    }

    private void copyFiles(String srcDir, String destDir, Set excludes,
            IJavaProject jproject) {

        if (!checkFileInList(srcDir, excludes)) {
            File fsrc = new File(srcDir);
            File fdest = new File(destDir);

            // exclude destDir if it is beyond srcDir to avoid an endless
            // recursion
            if (checkSubDir(fsrc, fdest)) {
                try {
                    String abs = fdest.getCanonicalPath();
                    if (excludes == null)
                        excludes = new HashSet();
                    excludes.add(abs);
                } catch (IOException e) {
                }
            }

            // exclude output jarfile if it is beyond srcDir to avoid doubling
            // with each build
            if (checkSubDir(fsrc, new File(configGetJarName(jproject)))) {
                try {
                    String abs = new File(configGetJarName(jproject))
                            .getCanonicalPath();
                    if (excludes == null)
                        excludes = new HashSet();
                    excludes.add(abs);
                } catch (IOException e) {
                }
            }

            if ((excludes != null) || (excludes.size() > 0))
                recursiveCopyFilesExclude(fsrc, fdest, excludes);
            else
                recursiveCopyFiles(fsrc, fdest);
        }
    }

    /**
     * @return true if fchild is descendant of fparent, false on any error
     */
    private boolean checkSubDir(File fparent, File fchild) {
        boolean result = false;
        try {
            result = fchild.getCanonicalPath().startsWith(
                    fparent.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void recursiveCopyFiles(File fsrc, File fdest) {

        if (fsrc.isDirectory()) {
            fdest.mkdirs();
            String[] filenames = fsrc.list();
            for (int i = 0; i < filenames.length; i++) {
                File fchildsrc = new File(fsrc, filenames[i]);
                File fchilddest = new File(fdest, filenames[i]);
                recursiveCopyFiles(fchildsrc, fchilddest);
            }
        } else {
            copyFile(fsrc, fdest);
        }
    }

    /**
     * copy all files recursive from fsrc to fdest, skip all (src-)files in
     * exclude
     */
    private void recursiveCopyFilesExclude(File fsrc, File fdest, Set excludes) {

        if (!checkFileInList(fsrc, excludes)) {
            if (fsrc.isDirectory()) {
                fdest.mkdirs();
                String[] filenames = fsrc.list();
                for (int i = 0; i < filenames.length; i++) {
                    File fchildsrc = new File(fsrc, filenames[i]);
                    File fchilddest = new File(fdest, filenames[i]);
                    recursiveCopyFilesExclude(fchildsrc, fchilddest, excludes);
                }
            } else {
                copyFile(fsrc, fdest);
            }
        }
    }

    private boolean checkFileInList(File file, Set files) {
        boolean result = false;
        String abs = null;
        try {
            abs = file.getCanonicalPath();
            if (files.contains(abs))
                result = true;
        } catch (IOException e) {
        }
        return result;
    }

    private void copyFile(String src, String dest) {
        copyFile(new File(src), new File(dest));
    }

    private void copyFile(File fsrc, File fdest) {

        try {
            FileInputStream in = new FileInputStream(fsrc);
            writeToFile(in, fdest);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mkdir(String dir) {
        File f = new File(dir);
        f.mkdirs();
    }

    private void recursiveRm(File f) {
        if (f.isDirectory()) {
            String[] filenames = f.list();
            for (int i = 0; i < filenames.length; i++)
                recursiveRm(new File(f, filenames[i]));
            f.delete();
        } else {
            f.delete();
        }
    }

    /**
     * Add all jars and class-folders referenced by jproject to jarfiles /
     * classesDirs. If exportedOnly is true, then only jars/class-folders which
     * are marked as exported will be added.
     * 
     * JRE_LIB (.../jre/lib/rt.jar) is ignored and not added to jarfiles
     * 
     * @param jproject
     * @param jarfiles
     * @param classesDirs
     * @param exportedOnly
     */
    private void getClassPathJars(IJavaProject jproject, Vector jarfiles,
            Vector classesDirs, boolean exportedOnly) {

        IProject project = jproject.getProject();
        IWorkspace workspace = project.getWorkspace();
        IWorkspaceRoot workspaceRoot = workspace.getRoot();
        String rootDir = absPath(workspaceRoot.getLocation());
        IClasspathEntry[] cpes = jproject.readRawClasspath();
        // cpes = jproject.getResolvedClasspath(true);
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
                                    String jar2 = absOrProjectPath(
                                            workspaceRoot, dir2);
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
                        String jar = absOrProjectPath(workspaceRoot, dir);
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

    public String absOrProjectPath(IWorkspaceRoot workspaceRoot,
            String absOrProjectpath) {
        String result = absOrProjectpath;
        try {
            File f = new File(absOrProjectpath);
            if (!f.exists()) {
                result = absProjectPath(workspaceRoot, absOrProjectpath);
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * get absolute path for /project/xyz by resolving "project" location
     */
    public static String absProjectPath(IWorkspaceRoot workspaceRoot,
            String projectpath) {

        int n = projectpath.indexOf(File.separatorChar, 1);
        String jarProjectName;
        String jarProjectPath;
        if (n != -1) {
            jarProjectName = projectpath.substring(1, n);
            jarProjectPath = projectpath.substring(n);
        } else {
            jarProjectName = projectpath.substring(1);
            jarProjectPath = "";
        }
        IProject jarProject = workspaceRoot.getProject(jarProjectName);
        String jarProjectRoot = absPath(jarProject.getLocation());
        String result = null;
        if (jarProjectRoot != null)
            result = jarProjectRoot + jarProjectPath;
        return result;
    }

    private void getClassesDir(IJavaProject jproject, Vector classesDirs) {

        IProject project = jproject.getProject();
        IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
        String outputDir = null;
        try {
            outputDir = absProjectPath(workspaceRoot, relPath(jproject
                    .getOutputLocation()));
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        if (!classesDirs.contains(outputDir))
            classesDirs.add(outputDir);
    }

    public static String getProjectDir(IJavaProject jproject) {

        IProject project = jproject.getProject();
        String projectDir = absPath(project.getLocation());
        return projectDir;
    }

    /**
     * convert IPath to absolute string path
     * 
     * @param path
     * @return
     */
    private static String absPath(IPath path) {

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
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {

        lastSelection = null;
        if ((selection != null) && !selection.isEmpty()) {
            lastSelection = (IStructuredSelection) selection;
        }
    }

}
