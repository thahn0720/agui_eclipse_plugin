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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.sf.fjep.anttask.FJAutoJarType;
import net.sf.fjep.anttask.FJBuildTask;
import net.sf.fjep.anttask.FJClassType;
import net.sf.fjep.anttask.FJExcludeType;
import net.sf.fjep.anttask.FJFileSourceType;
import net.sf.fjep.anttask.FJJarSourceType;
import net.sf.fjep.anttask.FJManifestType;
import net.sf.fjep.fatjar.popup.actions.FJTree;
import net.sf.fjep.fatjar.preferences.FatJarPreferencePage;
import net.sf.fjep.fatjar.wizards.export.FilesSelectPage.SourceInfo;
import net.sf.fjep.utils.FileUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class BuildFJ {

	private final static int PROGRESS_CLEAN   =   50;
	private final static int PROGRESS_COLLECT = 1000;
	private final static int PROGRESS_PACK    =  300;

    public static void buildConfiguredFatJar(JProjectConfiguration jproject, BuildProperties props, SourceInfo[] sourceInfo) {
    	
		// Create a progress bar
		Shell shell = new Shell();
		ProgressMonitorDialog progressmonitordialog = new ProgressMonitorDialog(shell);
		progressmonitordialog.open();
		IProgressMonitor iprogressmonitor = progressmonitordialog.getProgressMonitor();
		iprogressmonitor.beginTask("Build Fat jar", PROGRESS_CLEAN + PROGRESS_COLLECT + PROGRESS_PACK + PROGRESS_CLEAN);

		// Make One-JAR?
		boolean onejar = props.isUseOneJar();
		String expand = props.getOnejarExpand();
		
		String jarfile = null;
		try {
	    	props.save();

	    	jarfile = buildFatJar(jproject, props, sourceInfo);

			try {
				jproject.getProject().refreshLocal(IResource.DEPTH_ONE, null);
			} catch (CoreException e) { e.printStackTrace(); }
			JProjectConfiguration.removeTempFatjarConfigs();
		} 
		catch (Exception e) { 
			e.printStackTrace();
			MessageDialog.openInformation(shell, "Fat Jar Plug-In ERROR", e.getMessage());
		}
			
		progressmonitordialog.close();
		MessageDialog.openInformation(shell, "Fat Jar Plug-In", "built " + jarfile);

    }
	
    
    /**
     * build using ANT Tasks from props and sourceInfo
     * @param props
     * @param sourceInfos
     * @return
     */
    public static String buildFatJar(JProjectConfiguration jproject, BuildProperties props, SourceInfo[] sourceInfos) {

    	FJBuildTask fjBuildTask = new FJBuildTask();
    	
        String fatjarPath = "?";
        String output = props.getJarname().replace('\\', '/');
        if (!props.isJarnameIsExtern()) {
        	output = jproject.getAbsProjectDir().replace('\\', '/') + "/" + output;
        }
        String outputName = output.substring(output.lastIndexOf('/')+1);
        
        AguiPlugin pi = AguiPlugin.getDefault();
        Bundle bundle = pi.getBundle();
        if (bundle != null) {
            String location = bundle.getLocation();
            if (location != null)
                fatjarPath = location.replaceFirst("update[@][/]?", "") + "fatjar.jar";
        }

        boolean oneJar = props.isUseOneJar();

        fjBuildTask.setOnejar(oneJar);
        fjBuildTask.setOutput(output);

		IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
		boolean escapeUCase = store.getBoolean(FatJarPreferencePage.P_ESCAPEUPPERCASE);
		fjBuildTask.setEscapeUCase(escapeUCase);
        
        // create manifest data
        String manifestFile = props.getManifest_file();

        String manifestClasspath = props.getManifest_classpath();
        String manifestMainclass = props.getManifest_mainclass();
        String manifestArguments = props.getManifest_arguments();
        String manifestVMArguments = props.getManifest_vmarguments();
        boolean manifestMergeall = props.isManifest_mergeall();
        boolean manifestRemovesigners = props.isManifest_removesigners();
        boolean autojarEnable = props.isAutojarEnable();
        String autojarVisitClasses = props.getAutojarVisitClasses();
        boolean autojarClassForName = props.isAutojarClassForName();

        FJManifestType fjManifest = new FJManifestType();
        fjManifest.setMergemanifests(manifestMergeall);
        fjManifest.setRemovesigners(manifestRemovesigners);
        if ((manifestFile != null) && (!manifestFile.trim().equals("")) && (!manifestFile.trim().equals("<createnew>"))) {
        	fjManifest.setManifestfile(manifestFile);
        }
        fjManifest.setMainclass(manifestMainclass);
        if ((manifestClasspath != null) && (!manifestClasspath.trim().equals(""))) {
        	fjManifest.setClassPath(manifestClasspath);
        }
        if ((manifestArguments != null) && (!manifestArguments.trim().equals(""))) {
            fjManifest.setArguments(manifestArguments);
        }
        if ((manifestVMArguments != null) && (!manifestVMArguments.trim().equals(""))) {
            fjManifest.setVmarguments(manifestVMArguments);
        }
        fjBuildTask.addConfigured(fjManifest);
        
        for (int i = 0; i < sourceInfos.length; i++) {
            SourceInfo info = sourceInfos[i];
            if (info.isJar) {
            	FJJarSourceType fjJarSource = new FJJarSourceType();
            	fjJarSource.setFile(info.absPath);
            	fjJarSource.setRelPath(info.relPath);
            	fjBuildTask.addConfigured(fjJarSource);
            }
            else {
            	FJFileSourceType fjFileSource = new FJFileSourceType();
        		fjFileSource.setPath(info.absPath);
        		fjFileSource.setRelPath(info.relPath);
            	if (info.excludes.size() != 0) {
                    for (int j = 0; j < info.excludes.size(); j++) {
                        String exclude = (String) info.excludes.get(j);
                    	FJExcludeType fjExclude = new FJExcludeType();
                    	fjExclude.setRelPath(exclude);
                    	fjFileSource.addConfigured(fjExclude);
                    }
                }
            	fjBuildTask.addConfigured(fjFileSource);
            }
        }
        
        if (autojarEnable) {
            FJAutoJarType fjAutoJar = new FJAutoJarType();
            String[] classes = autojarVisitClasses.split("\\s+");
            for (int i = 0; i < classes.length; i++) {
                String className = classes[i];
                FJClassType fjClass = new FJClassType();
                fjClass.setClassname(className);
                fjAutoJar.addConfigured(fjClass);
            }
            fjAutoJar.setSearchclassforname(autojarClassForName);
            fjBuildTask.addConfigured(fjAutoJar);
        }
        
        fjBuildTask.execute();
        return output;
    }
    

	/**
	 * 50 progress
	 */
	private static String doCleanFatJar(JProjectConfiguration jproject, IProgressMonitor iprogressMonitor) {

		String tempdir = jproject.getTempBuildDir();
		iprogressMonitor.subTask("Clean Fat Jar - remove " + tempdir);
		FileUtils.recursiveRm(new File(tempdir));
		iprogressMonitor.worked(50);
		return tempdir;
	}

	
    public static FJTree buildTree(JProjectConfiguration jprojectsIn) {
        List lst = jprojectsIn.getJProjectCollection();
        FJTree rootNode = new FJTree(null, FJTree.NT_ROOT, "Fat Jar Tree for ",
                null, FJTree.CS_CHECKED);
        if (lst == null) {
            build(rootNode,jprojectsIn);
            return rootNode;
        }
        for (Iterator iter = lst.iterator(); iter.hasNext();) {
            IJavaProject jproject = (IJavaProject) iter.next();
            JProjectConfiguration jprojectConf = new JProjectConfiguration(
                    jproject, null);
            build(rootNode, jprojectConf);
        }
        return rootNode;
    }

    private static void build(FJTree rootNode, JProjectConfiguration jprojectConf) {
        String projectName = jprojectConf.getName();
        // rootNode = new FJTree(null, FJTree.NT_ROOT,
        // "Fat Jar Tree for " + projectName, jprojectConf,
        // FJTree.CS_CHECKED);
        Vector projects = new Vector();
        Vector jarFiles = new Vector();
        Vector classesDirs = new Vector();

        jprojectConf.addClassPathEntries(jarFiles, classesDirs, projects,
                false);

        for (int i = 0; i < projects.size(); i++) {
            JProjectConfiguration jpro = (JProjectConfiguration) projects
                    .get(i);
            rootNode.addChild(FJTree.NT_PROJECT_OUTPUT, "Project '"
                    + jpro.getProjectName() + "' output", new File(jpro
                    .getProjectOutputDir()), FJTree.CS_CHECKED);
        }

        for (int j = 0; j < classesDirs.size(); j++) {
            String classesDir = (String) classesDirs.get(j);
            rootNode.addChild(FJTree.NT_CLASSES, "Classes '" + classesDir
                    + "'", new File(classesDir), FJTree.CS_CHECKED);
        }

        /*
         * jproject.getClassPathJars(jarFiles, classesDirs, false); //
         * jproject.getClassesDir(classesDirs); // String classesDir =
         * (String) classesDirs.lastElement(); //
         * rootNode.addChild(FJTree.NT_PROJECT_OUTPUT, "Project '" +
         * jproject.getProjectName() + "' output", new File(classesDir),
         * FJTree.CS_CHECKED); // for (int i=0; i<classesDirs.size()-1;
         * i++) { // classesDir = (String) classesDirs.get(i); //
         * rootNode.addChild(FJTree.NT_CLASSES, "Classes '" + classesDir +
         * "'", new File(classesDir), FJTree.CS_CHECKED); // }
         * 
         * jproject.getChildProjects(childProjects, false); for (int i=0; i<childProjects.size();
         * i++) { JProjectConfiguration jChildProject =
         * (JProjectConfiguration) childProjects.get(i); String
         * childProjectName = jChildProject.getProjectName();
         * classesDirs.clear(); jChildProject .getClassPathJars(jarFiles,
         * classesDirs, true); jChildProject.getClassesDir(classesDirs);
         * String classesDir = (String) classesDirs.lastElement();
         * rootNode.addChild(FJTree.NT_PROJECT_OUTPUT, "Project '" +
         * childProjectName + "' output", new File(classesDir),
         * FJTree.CS_CHECKED); for (int j=0; j<classesDirs.size()-1; j++) {
         * classesDir = (String) classesDirs.get(j);
         * rootNode.addChild(FJTree.NT_CLASSES, "Classes '" + classesDir +
         * "'", new File(classesDir), FJTree.CS_CHECKED); } }
         */

        for (int i = 0; i < jarFiles.size(); i++) {
            String jarFile = (String) jarFiles.get(i);
            String jarname = File.separatorChar + jarFile;
            jarname = jarname.substring(jarname
                    .lastIndexOf(File.separatorChar) + 1);
            rootNode.addChild(FJTree.NT_JAR, jarname, new File(jarFile),
                    FJTree.CS_CHECKED);
        }
    }
	
}
