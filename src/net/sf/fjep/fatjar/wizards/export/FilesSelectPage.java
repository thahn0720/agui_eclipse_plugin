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
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import net.sf.fjep.fatjar.popup.actions.BuildFatJar;
import net.sf.fjep.fatjar.popup.actions.FJTree;
import net.sf.fjep.fatjar.preferences.FatJarPreferencePage;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.osgi.framework.Bundle;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class FilesSelectPage extends WizardPage {

    private CheckboxTreeViewer fileTree = null;
    private JProjectConfiguration jproject = null;
    private FJTree rootTree = null;
    private FJExpWizard fjew = null; 
    
    public FilesSelectPage(FJExpWizard fjew) {
        super("wizardPage");
        System.out.println("ctor FilesSelectPage");
        this.fjew = fjew;
        setTitle("Select files for Fat Jar");
        setDescription("define includes / excludes");
    }


    public class FileSelectTreeContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object element) {
            FJTree tree = (FJTree)element;
            return tree.getChildren();
        }
        public Object[] getElements(Object element) {
            FJTree tree = (FJTree)element;
            return tree.getChildren();
        }
        public boolean hasChildren(Object element) {
            FJTree tree = (FJTree)element;
            return tree.hasChildren();
        }
        public Object getParent(Object element) {
            FJTree tree = (FJTree)element;
            return tree.getParent();
        }
        public void dispose() {}
        public void inputChanged(Viewer viewer, Object old_input, Object new_input) {}
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        
        try {
        GridData gd;
        Label label;

        Composite comp = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        comp.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;

        label = new Label(comp, SWT.NULL);
        label.setText("NEW-File-List:");
        //
        Tree tree = new Tree(comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK | SWT.V_SCROLL );
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = tree.getItemHeight() * 10;
        tree.setLayoutData(gd);
        fileTree = new CheckboxTreeViewer(tree);
        FileSelectTreeContentProvider cp = new FileSelectTreeContentProvider();
        fileTree.setContentProvider(cp);
        fileTree.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                dialogSelectionChanged(event);
            }
        });
        fileTree.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                    dialogCheckChanged(event);
            }
        });
        fileTree.addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event) {}
            public void treeExpanded(TreeExpansionEvent event) {
                dialogTreeExpanded(event);
            }
        });
        //
        //
        Button addDir = new Button(comp, SWT.PUSH);
        addDir.setText("Add Dir...");
        addDir.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleAddDirButton();
            }
        });

        Button saveAsButton = new Button(comp, SWT.PUSH);
        gd = new GridData();
        gd.horizontalAlignment = GridData.CENTER;
        gd.horizontalSpan = 2;
        saveAsButton.setLayoutData(gd);
        saveAsButton.setText("Save Settings...");
        saveAsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleSaveSettings();
            }
        });
        //
        Button exportAntButton = new Button(comp, SWT.PUSH);
        gd = new GridData();
        gd.horizontalAlignment = GridData.CENTER;
        gd.horizontalSpan = 1;
        exportAntButton.setLayoutData(gd);
        exportAntButton.setText("Export ANT...");
        exportAntButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleExportANT();
            }
        });

        
        initialize();
        fileTree.setCheckedElements(rootTree.getChildren());
        dialogChanged();
        setControl(comp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleAddDirButton() {
        String includeDir = selectWorkspaceDir();
        if (includeDir != null) {
            addProjectInclude(includeDir);
        }
    }

    private void handleSaveSettings() {

        SaveAsDialog dialog = new SaveAsDialog(getShell());
        String name = BuildFatJar.absPropertiesFilename;
        if (name == null) {
            name = "settings.fatjar";
        }
        int pos = name.lastIndexOf(File.separatorChar);
        if (pos != -1) {
            name = name.substring(pos+1);
        }
        IProject project = jproject.getJproject().getProject();
        
        IFile iFile = project.getFile(name);
        
        
        dialog.setOriginalFile(iFile);
//        dialog.setOriginalName(name);
        dialog.setTitle("Save FatJar Settings"); //$NON-NLS-1$

        if (dialog.open() == SaveAsDialog.OK) {
            IPath saveFile= dialog.getResult();
            if (saveFile != null) {
                String path = saveFile.toString();
                String projectName = jproject.getJproject().getElementName();
                if (!path.startsWith("/" + projectName + "/")) {
                    MessageDialog.openInformation(getShell(), "Fat Jar Save Settings", "settings must be stored in project " + projectName);
                }
                else {
                    if (!"fatjar".equals(saveFile.getFileExtension()))
                        saveFile = saveFile.addFileExtension("fatjar");
                    BuildProperties buildProps = fjew.getBuildProperties();
                    if (buildProps != null) {
                        saveConfig(saveFile, buildProps.toProperties());
                    }
                }
            }
        }
    }

    private void saveConfig(IPath saveFile, Properties props) {

        boolean ok = true;
        try {
            String prjFilename = saveFile.toString().replace('/', File.separatorChar).replace('\\', File.separatorChar);
            String propertiesFile = BuildFatJar.absProjectPath(jproject.getJproject().getProject().getWorkspace().getRoot(), prjFilename);
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
                            ok = (status != null) && !status.isOK();
                            if (ok) {
                                Shell shell = new Shell();
                                MessageDialog.openInformation(shell, "Fat Jar Plug-In Warning", "could not save current settings: " + status.toString());
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
                jproject.setPropertiesFilename(propertiesFile);
                try {
                    jproject.getJproject().getProject().refreshLocal(IResource.DEPTH_ONE, null);
                } catch (CoreException e) { e.printStackTrace(); }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MessageDialog.openInformation(getShell(), "Fat Jar Save Settings", "path='" + saveFile.toString() + "': msg='"+ e.getMessage() + "'");
        } catch (IOException e) {
            e.printStackTrace();
            MessageDialog.openInformation(getShell(), "Fat Jar Save Settings", "path='" + saveFile.toString() + "': msg='"+ e.getMessage() + "'");
        }
    }


    private void handleExportANT() {

        SaveAsDialog dialog = new SaveAsDialog(getShell());
        String name = "build.xml";

        IProject project = jproject.getJproject().getProject();
        
        IFile iFile = project.getFile(name);
        
        dialog.setOriginalFile(iFile);
//        dialog.setOriginalName(name);
        dialog.setTitle("Export ANT build file"); //$NON-NLS-1$

        if (dialog.open() == SaveAsDialog.OK) {
            IPath saveFile= dialog.getResult();
            if (saveFile != null) {
                String path = saveFile.toString();
                String projectName = jproject.getJproject().getElementName();
                if (!"xml".equals(saveFile.getFileExtension()))
                    saveFile = saveFile.addFileExtension("xml");
                
                boolean ok = true;
                IFile buildFile = project.getWorkspace().getRoot().getFile(saveFile);
                if (buildFile != null) {
                    IStatus status = AguiPlugin.askFileWriteAccess(buildFile);
                    if ((status != null) && !status.isOK()) {
                        ok = false;
                        Shell shell = new Shell();
                        MessageDialog.openError(shell, "Fat Jar Plug-In Error", "Export ANT, write access denied: " + status.toString());
                    }
                }
                if (ok) {
                    SourceInfo[] sourceInfo = getANTBuildInfo();
                    exportANTBuild(saveFile, sourceInfo);
                }
            }
        }
    }



    private void exportANTBuild(IPath saveFile, SourceInfo[] sourceInfo) {

        try {
            String antScript = buildANTScript(sourceInfo);
            String prjFilename = saveFile.toString().replace('/', File.separatorChar).replace('\\', File.separatorChar);
            String buildFile = BuildFatJar.absProjectPath(jproject.getJproject().getProject().getWorkspace().getRoot(), prjFilename);
            File f = new File(buildFile);
            OutputStream out = new FileOutputStream(f);
            out.write(antScript.getBytes());
            out.flush();
            out.close();
            try {
                jproject.getJproject().getProject().refreshLocal(IResource.DEPTH_ONE, null);
            } catch (CoreException e) { e.printStackTrace(); }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MessageDialog.openInformation(getShell(), "Fat Jar Export ANT", "path=" + saveFile.toString() + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            MessageDialog.openInformation(getShell(), "Fat Jar Export ANT", "path=" + saveFile.toString() + e.getMessage());
        }
    }

    
    
    /**
     * build ANT script from sourceInfo
     * @param sourceInfos
     * @return
     */
    private String buildANTScript(SourceInfo[] sourceInfos) {

        StringBuffer script = new StringBuffer();
        script.append("<?xml version=\"1.0\"?>\r\n");

        String fatjarPath = "?";
        String output = fjew.configPage.getJarname().replace('\\', '/');
        String outputName = output.substring(output.lastIndexOf('/')+1);
        
        AguiPlugin pi = AguiPlugin.getDefault();
        Bundle bundle = pi.getBundle();
        if (bundle != null) {
            String location = bundle.getLocation();
            if (location != null)
                fatjarPath = location.replaceFirst("update[@][/]?", "") + "fatjar.jar";
        }

        script.append("<project name=\"FatJar "+ outputName + " (experimental)\" default=\"main\" basedir=\".\">\r\n");
        script.append("    <!-- this file was created by Fat-Jar Eclipse Plug-in -->\r\n");
        script.append("    <!-- the ANT-Export is in a very early stage, so this -->\r\n");
        script.append("    <!-- is only experimental, ANT 1.6 or above is        -->\r\n");
        script.append("    <!-- required, feedback is always welcome:            -->\r\n");
        script.append("    <!--       http://sourceforge.net/projects/fjep       -->\r\n");
        script.append("    <!-- uncomment the following lines if using ANT outside Eclipse -->\r\n");
        script.append("    <!--\r\n");
        script.append("        <property name=\"fjepPath\" value=\"" + fatjarPath + "\"/>\r\n");
        script.append("        <taskdef name=\"fatjar.build\" classname=\"net.sf.fjep.anttask.FJBuildTask\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.manifest\" classname=\"net.sf.fjep.anttask.FJManifestType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.exclude\" classname=\"net.sf.fjep.anttask.FJExcludeType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.jarsource\" classname=\"net.sf.fjep.anttask.FJJarSourceType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.filesource\" classname=\"net.sf.fjep.anttask.FJFileSourceType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.autojar\" classname=\"net.sf.fjep.anttask.FJAutoJarType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("        <typedef name=\"fatjar.class\" classname=\"net.sf.fjep.anttask.FJClassType\" classpath=\"${fjepPath}\"/>\r\n");
        script.append("    -->\r\n");
        script.append("    <!-- uncomment the above lines to use ANT outside of Eclipse -->\r\n");
        script.append("    <target name=\"main\">\r\n");

        String attribs = "";
        boolean oneJar = fjew.configPage.getOneJar();
        if (oneJar) {
            attribs += " onejar=\"true\"";
        }
		IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
		boolean escapeUCase = store.getBoolean(FatJarPreferencePage.P_ESCAPEUPPERCASE);
        if (escapeUCase) {
            attribs += " escapeUCase=\"true\"";
        }

        attribs += " output=\"" + outputName + "\"";
        script.append("        <fatjar.build" + attribs + ">\r\n");

        // create manifest data
        String manifestFile = fjew.configPage.getManifestfile();
        String manifestClasspath = fjew.configPage.getManifestclasspath();
        String manifestMainclass = fjew.configPage.getManifestmainclass();
        String manifestArguments = fjew.configPage.getManifestarguments();
        String manifestVMArguments = fjew.configPage.getManifestvmarguments();
        boolean manifestMergeall = fjew.configPage.getManifestmergeall();
        boolean manifestRemovesigners = fjew.configPage.getManifestremovesigners();
        boolean autojarEnable = fjew.autoJarPage.getAutojarEnable();
        String autojarVisitClasses = fjew.autoJarPage.getAutojarVisitClasses();
        boolean autojarSearchClassForName = fjew.autoJarPage.getAutojarClassForName();
        
        attribs = "";
        if (!manifestMergeall) {
            attribs += " mergemanifests=\"false\"";
        }
        if (!manifestRemovesigners) {
            attribs += " removesigners=\"false\"";
        }
        if ((manifestFile != null) && (!manifestFile.trim().equals("")) && (!manifestFile.trim().equals("<createnew>"))) {
            attribs += " manifestfile=\"" + manifestFile + "\""; 
        }
        else {
            if ((manifestMainclass != null) && (!manifestMainclass.trim().equals(""))) {
                attribs += " mainclass=\"" + manifestMainclass + "\""; 
            }
            if ((manifestArguments != null) && (!manifestArguments.trim().equals(""))) {
                attribs += " arguments=\"" + manifestArguments + "\""; 
            }
            if ((manifestVMArguments != null) && (!manifestVMArguments.trim().equals(""))) {
                attribs += " vmarguments=\"" + manifestVMArguments + "\""; 
            }
            if ((manifestClasspath != null) && (!manifestClasspath.trim().equals(""))) {
                attribs += " classpath=\"" + manifestClasspath + "\""; 
            }
        }
        script.append("            <fatjar.manifest" + attribs + "/>\r\n");

        if (autojarEnable) {
            attribs = "";
            if (autojarSearchClassForName) {
                attribs += " searchclassforname=\"true\"";
            }
            script.append("            <fatjar.autojar" + attribs + ">\r\n");
            String[] classnames = autojarVisitClasses.split("\\s+");
            for (int i = 0; i < classnames.length; i++) {
                if (!classnames[i].trim().equals("")) {
                    script.append("                <fatjar.class classname=\"" + classnames[i] + "\"/>\r\n");
                }
            }
            script.append("            </fatjar.autojar>\r\n");
        }
        
        for (int i = 0; i < sourceInfos.length; i++) {
            SourceInfo info = sourceInfos[i];
            if (info.isJar) {
                script.append("            <fatjar.jarsource file=\"" + info.absPath + "\" relpath=\"" + info.relPath + "\"/>\r\n");
            }
            else {
                if (info.excludes.size() == 0) {
                    script.append("            <fatjar.filesource path=\"" + info.absPath + "\" relpath=\"" + info.relPath + "\"/>\r\n");
                }
                else {
                    script.append("            <fatjar.filesource path=\"" + info.absPath + "\" relpath=\"" + info.relPath + "\">\r\n");
                    for (int j = 0; j < info.excludes.size(); j++) {
                        String exclude = (String) info.excludes.get(j);
                        script.append("                <fatjar.exclude relpath=\"" + exclude + "\"/>\r\n");
                    }
                    script.append("            </fatjar.filesource>\r\n");
                }
            }
        }

        script.append("        </fatjar.build>\r\n");
        script.append("    </target>\r\n");
        script.append("</project>\r\n");
        
        
        /*
        <?xml version="1.0"?>
                <project name="FatJar anttest.jar" default="main" basedir=".">

                    <taskdef name="fatjar.build" classname="net.sf.fjep.anttask.FJBuildTask"/>
                    <typedef name="fatjar.exclude" classname="net.sf.fjep.anttask.FJExcludeType"/>
                    <typedef name="fatjar.filesource" classname="net.sf.fjep.anttask.FJFileSourceType"/>
                    <typedef name="fatjar.jarsource" classname="net.sf.fjep.anttask.FJJarSourceType"/>
                    
                  <target name="main">
                    <fatjar.build output="anttest.jar">
                        <fatjar.filesource path="u:/opt/eclipse301/workspace/fjanttasks/classes" relpath="">
                            <fatjar.exclude relpath="net/sf/fjep/anttask/DemoTask.class"/>
                        </fatjar.filesource>    
                        <fatjar.jarsource  file="u:/opt/eclipse301/workspace/fjanttasks/anttestx.jar" relpath="fromjar">
                            <fatjar.exclude relpath="fromjar/net/sf/fjep/anttask/DTParam.class"/>
                        </fatjar.jarsource>
                    </fatjar.build>
                  </target>
                    
                </project>
                */
        
        return script.toString();
    }

    /**
     * @param projectDir
     */
    private void addProjectInclude(String projectDir) {
        int pos = projectDir.indexOf(File.separatorChar, 1);
        if (pos == -1) {
            projectDir += File.separator + ".";
            pos = projectDir.indexOf(File.separatorChar, 1);
        }
        if (pos != -1) {
            String project = projectDir.substring(1, pos);
            String relPath = projectDir.substring(pos + 1);
            String displayName = "Include from project '" + project + "': " + relPath;
            String absPath = BuildFatJar.absProjectPath(ResourcesPlugin.getWorkspace().getRoot(), projectDir);
            if (absPath != null) {
                rootTree.addChild(FJTree.NT_ADD_DIR, displayName, new File(absPath), FJTree.CS_UNCHECKED);
                fileTree.refresh(rootTree);
            }
        }
    }

    private String selectWorkspaceDir() {

        String result = null;
        ILabelProvider lp= new WorkbenchLabelProvider();
        ITreeContentProvider cp= new WorkbenchContentProvider();

        ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(
                getShell(), lp, cp);
        dialog.setValidator(null);
        dialog.setAllowMultiple(false);
        dialog.setTitle("Select base directory to add"); //$NON-NLS-1$
        dialog.setMessage("msg?"); //$NON-NLS-1$
        ViewerFilter filter = new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                boolean ok = (element instanceof Folder) || (element instanceof Project);
                return ok;
            }
        };
        dialog.addFilter(filter);
        
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());   
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

        if (dialog.open() == ElementTreeSelectionDialog.OK) {
            Object[] elements= dialog.getResult();
            if (elements.length == 1) {
                result = ((IResource)elements[0]).getFullPath().toOSString();
            }
        }
        return result;
    }

    private void initialize() {
        if (jproject == null) {
            setDescription("no project selected");
            rootTree = new FJTree(null, FJTree.NT_ROOT, "empty", null, FJTree.CS_CHECKED);
        }
        else {
            setDescription("Files for project " + jproject.getName());
            rootTree = BuildFJ.buildTree(jproject);
        }
        fileTree.setInput(rootTree);
        fileTree.setCheckedElements(rootTree.getReadChildren());
    }
    
    private void dialogSelectionChanged(SelectionChangedEvent event) {

        dialogChanged();
    }

    private void recursiveCheckChildren(FJTree parent, boolean checked) {

        FJTree[] children = parent.getReadChildren();
        for (int i=0; i<children.length; i++) {
            FJTree child = children[i];
            if (    (checked && (child.getCheckState()!=FJTree.CS_CHECKED)) ||
                    (!checked && (child.getCheckState()!=FJTree.CS_UNCHECKED)) ){
                child.setChecked(checked);
                fileTree.setChecked(child, checked);
                fileTree.setGrayed(child, false);
                recursiveCheckChildren(child, checked);
            }
        }
    }
    
    private void recursiveGrayParents(FJTree child) {

        FJTree parent = (FJTree)child.getParent();
        if (parent != null) {
            parent.setGrayChecked();
            fileTree.setGrayChecked(parent, true);
            recursiveGrayParents(parent);
        }
    }
    
    private void dialogCheckChanged(CheckStateChangedEvent event) {

        FJTree element = (FJTree)event.getElement();
        if (element != null) {
            boolean checked = event.getChecked();
            if (!checked && (element.getCheckState() == FJTree.CS_GRAYED)) {
                fileTree.setGrayed(element, false);
                fileTree.setChecked(element, true);
                checked = true;
            }
            if (    ( checked && (element.getCheckState() != FJTree.CS_CHECKED)) ||
                    (!checked && (element.getCheckState() != FJTree.CS_UNCHECKED))){
                element.setChecked(checked);
                recursiveCheckChildren(element, checked);
                recursiveGrayParents(element);
            }
        }
        dialogChanged();
    }
    
    private void dialogTreeExpanded(TreeExpansionEvent event) {

        FJTree parent = (FJTree)event.getElement();
        if (parent != null) {
            Object[] children = parent.getChildren();
            for (int i=0; i<children.length; i++) {
                FJTree child = (FJTree)children[i];
                int checkState = child.getCheckState();
                if (checkState == FJTree.CS_CHECKED) {
                    fileTree.setChecked(child, true);
                    fileTree.setGrayed(child, false);
                }
                else if (checkState == FJTree.CS_UNCHECKED)
                    fileTree.setChecked(child, false);
                else if (checkState == FJTree.CS_GRAYED)
                    fileTree.setGrayChecked(child, true);
            }
        }
        dialogChanged();
    }

    private void dialogChanged() {
        
        updateStatus(null);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public void setJProject(JProjectConfiguration jproject, BuildProperties props) {
        if (this.jproject != jproject) {
            this.jproject = jproject;
            initialize();
            setExcludes(props.getExcludeInfo());
            setIncludes(props.getIncludeInfo());
            dialogChanged();
        }
    }


    private void setExcludes(BuildProperties.AbstractFileInfo[] excludeInfo) {
        
        for (int i=0; i<excludeInfo.length; i++) {
            String[] segments = excludeInfo[i].getSegments();
            FJTree element = findBaseElement(segments[0]);
            for (int j=1; j<segments.length; j++) {
                if (element == null)
                    break;
                element = findChild(element, segments[j]);
            }
            if (element != null) {
                element.setChecked(false);
                fileTree.setChecked(element, false);
                fileTree.setGrayed(element, false);
                recursiveCheckChildren(element, false);
                recursiveGrayParents(element);
            }
        }
    }

    private void setIncludes(BuildProperties.AbstractFileInfo[] includeInfo) {
        
        for (int i=0; i<includeInfo.length; i++) {
            String[] segments = includeInfo[i].getSegments();
            FJTree element = findBaseElement(segments[0]);
            if (element == null) {
                addIncludeDir(segments[0]);
                element = findBaseElement(segments[0]);
            }
            for (int j=1; j<segments.length; j++) {
                if (element == null)
                    break;
                element = findChild(element, segments[j]);
            }
            if (element != null) {
                element.setChecked(true);
                fileTree.setChecked(element, true);
                fileTree.setGrayed(element, false);
                recursiveCheckChildren(element, true);
                recursiveGrayParents(element);
            }
        }
    }


    /**
     * @param string
     */
    private void addIncludeDir(String incDir) {
        String projectDir = incDir.replaceFirst("[<]inc[|](.*)[>]", "/$1");
        if (!projectDir.equals(incDir)) {
            addProjectInclude(projectDir.replace('/', File.separatorChar));
        }
        
    }

    private FJTree findChild(FJTree parent, String name) {
        FJTree result = null;
        if (parent != null) {
            Object[] children = parent.getChildren();
            for (int i=0; i< children.length; i++) {
                if (children[i].toString().equals(name)) {
                    result = (FJTree) children[i];
                    break;
                }
            }
        }
        return result;
    }


    private FJTree findBaseElement(String name) {
        
        String displayName = name;
        if (name.startsWith("<po|"))
            displayName = name.replaceFirst("[<]po[|](.*)[>]", "Project '$1' output");
        else if (name.startsWith("<cl|"))
            displayName = name.replaceFirst("[<]cl[|](.*)[>]", "Classes '$1'");
        else if (name.startsWith("<jar|"))
            displayName = name.replaceFirst("[<]jar[|](.*)[>]", "$1");
        else if (name.startsWith("<inc|"))
            displayName = name.replaceFirst("[<]inc[|]([^/]*)/(.*)[>]", "Include from project '$1': $2");
        return findChild(rootTree, displayName);
    }


    public String[][] getAllUnchecked() {
        Vector result = new Vector();
        FJTree[] children = rootTree.getReadChildren();
        for (int i=0; i<children.length; i++) {
            if (!children[i].isType(FJTree.NT_ADD_DIR))
                recursiveGetAllUnchecked(children[i], result);
        }
        return (String[][]) result.toArray(new String[result.size()][]);
    }


    private void recursiveGetAllUnchecked(FJTree node, Vector unchecked) {
        if (node.getCheckState() == FJTree.CS_UNCHECKED) {
            String[] display_abspath = {node.getDisplayPath(), node.getAbsPath()};
            unchecked.add(display_abspath);
        }
        else if (node.getCheckState() != FJTree.CS_CHECKED) {
            FJTree[] children = node.getReadChildren();
            for (int i=0; i<children.length; i++) 
                recursiveGetAllUnchecked(children[i], unchecked);
        }
    }

    public String[][] getAllChecked() {
        Vector result = new Vector();
        FJTree[] children = rootTree.getReadChildren();
        for (int i=0; i<children.length; i++) {
            if (children[i].isType(FJTree.NT_ADD_DIR))
                recursiveGetAllChecked(children[i], result, children[i].getAbsPath());
        }
        return (String[][]) result.toArray(new String[result.size()][]);
    }


    private void recursiveGetAllChecked(FJTree node, Vector checked, String rootAbsPath) {
        if (node.getCheckState() == FJTree.CS_CHECKED) {
            String[] display_abspath;
            int len = rootAbsPath.length()+1;
            String nodeAbsPath = node.getAbsPath();
            if (nodeAbsPath.equals(rootAbsPath)) {
                display_abspath = new String[]{node.getDisplayPath(), rootAbsPath + "|"};
            }
            else {
                display_abspath = new String[]{node.getDisplayPath(), rootAbsPath + "|" + node.getAbsPath().substring(len)};
            }
            checked.add(display_abspath);
        }
        else if (node.getCheckState() != FJTree.CS_UNCHECKED) {
            FJTree[] children = node.getReadChildren();
            for (int i=0; i<children.length; i++) 
                recursiveGetAllChecked(children[i], checked, rootAbsPath);
        }
    }
    
    public class SourceInfo {
        public boolean isJar;
        public String absPath;
        public String relPath;
        public ArrayList excludes;
        public SourceInfo(boolean isJar, String absPath, String relPath) {
            this.isJar = isJar;
            this.absPath = absPath;
            this.relPath = relPath;
            excludes = new ArrayList();
        }
    }
    
    public SourceInfo[] getANTBuildInfo() {
        Vector result = new Vector();
        FJTree[] children = rootTree.getReadChildren();
        for (int i=0; i<children.length; i++) {
            FJTree fjTree = children[i];
            SourceInfo sourceInfo = null;
            
            // handle additional dirs (includes)
            if (fjTree.isType(FJTree.NT_ADD_DIR)) {
                Vector checked = new Vector();
                recursiveGetAllChecked(fjTree, checked, "");
                for (int j = 0; j < checked.size(); j++) {
                    String[] display_abspath = (String[])checked.get(j);
                    String include = display_abspath[0];
                    include = include.replaceFirst("[<]inc[|][^>]+[>]", "");
                    include = include.replaceAll("[~]", "");
                    System.out.println("include=" + include);
                    String absPath = fjTree.getAbsPath() + "/" + include;
                    absPath = absPath.replace('/', File.separatorChar);
                    sourceInfo = new SourceInfo(false, absPath, include);
                    result.add(sourceInfo);
                }
            } 
            
            // handle classes entry
            else if (fjTree.isType(FJTree.NT_CLASSES)) {
                if (fjTree.getCheckState() != FJTree.CS_UNCHECKED) {
                    sourceInfo = new SourceInfo(false, fjTree.getAbsPath(), "");
                    Vector unchecked = new Vector();
                    recursiveGetAllUnchecked(fjTree, unchecked);
                    for (int j = 0; j < unchecked.size(); j++) {
                        String[] display_abspath = (String[])unchecked.get(j);
                        String exclude = display_abspath[0];
                        exclude = exclude.replaceFirst("[<]cl[|][^>]+[>]", "");
                        exclude = exclude.replaceAll("[~]", "");
                        System.out.println("exclude=" + exclude);
                        sourceInfo.excludes.add(exclude);
                    }
                    result.add(sourceInfo);
                }
            }

            // handle jar entry
            else if (fjTree.isType(FJTree.NT_JAR)) {
                if (fjTree.getCheckState() != FJTree.CS_UNCHECKED) {
                    sourceInfo = new SourceInfo(true, fjTree.getAbsPath(), "");
                    result.add(sourceInfo);
                }
            } 
            
            // handle project output entry
            else if (fjTree.isType(FJTree.NT_PROJECT_OUTPUT)) {
                if (fjTree.getCheckState() != FJTree.CS_UNCHECKED) {
                    sourceInfo = new SourceInfo(false, fjTree.getAbsPath(), "");
                    Vector unchecked = new Vector();
                    recursiveGetAllUnchecked(fjTree, unchecked);
                    for (int j = 0; j < unchecked.size(); j++) {
                        String[] display_abspath = (String[])unchecked.get(j);
                        String exclude = display_abspath[0];
                        exclude = exclude.replaceFirst("[<]po[|][^>]+[>]", "");
                        exclude = exclude.replaceAll("[~]", "");
                        System.out.println("exclude=" + exclude);
                        sourceInfo.excludes.add(exclude);
                    }
                    result.add(sourceInfo);
                }
            }
            
        }
        return (SourceInfo[]) result.toArray(new SourceInfo[result.size()]);
    }

    /**
     * @param buildProperties
     */
    public void updateBuildProperties(BuildProperties buildProps) {
        buildProps.setExcludeInfo(getAllUnchecked());
        buildProps.setIncludeInfo(getAllChecked());
    }
    
}