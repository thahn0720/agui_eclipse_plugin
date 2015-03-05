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

import net.sf.fjep.fatjar.wizards.export.FilesSelectPage.SourceInfo;

import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class FJExpWizard extends Wizard implements IExportWizard {

    String[] selectedJavaProjects;
    ProjectSeletPage projectSelectPage;
    ConfigPage configPage;
    FilesSelectPage filesSelectPage;
    AutoJarPage autoJarPage;
    JProjectConfiguration jproject;
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        if (jproject == null) {
            projectSelectPage = new ProjectSeletPage(selectedJavaProjects);
            addPage(projectSelectPage);
        }
        configPage = new ConfigPage(null, null);
        addPage(configPage);
        autoJarPage = new AutoJarPage(null, null);
        addPage(autoJarPage);
        filesSelectPage = new FilesSelectPage(this);
        addPage(filesSelectPage);
        super.addPages();
    }

    
    public FJExpWizard() {
        super();
        System.out.println("ctor FJExpWizard");
    }

    public FJExpWizard(JProjectConfiguration jproject) {
        this.jproject = jproject;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        try {
            
            if (jproject == null)
                jproject = projectSelectPage.getSelectedJavaConfig();
            if (jproject != null) {
                BuildProperties props = configPage.updateProperties();
                filesSelectPage.updateBuildProperties(props);
                autoJarPage.updateBuildProperties(props);
                
                String[][] excludeStrings = filesSelectPage.getAllUnchecked();
                props.setExcludeInfo(excludeStrings);
                // HashSet excludes = new HashSet();
                // for (int i=0; i<excludeInfo.length; i++) {
                //    excludes.add(excludeInfo[i][1]);
    
                String[][] includeInfo = filesSelectPage.getAllChecked();
                props.setIncludeInfo(includeInfo);
                // ArrayList includes = new ArrayList();
                // for (int i=0; i<includeInfo.length; i++) {
                //    includes.add(includeInfo[i][1]);
    
                SourceInfo[] sourceInfo = filesSelectPage.getANTBuildInfo();

                BuildFJ.buildConfiguredFatJar(jproject, props, sourceInfo);
            }
            return (jproject != null);
        } catch (Exception x) {
            // Debugging...
            x.printStackTrace();
            // Convert to unchecked so we can throw it out of this scope.
            throw new Error(x);
        }
    }

    public BuildProperties getBuildProperties() {
        BuildProperties result = null;
        try {
            if (jproject == null)
                jproject = projectSelectPage.getSelectedJavaConfig();
            if (jproject != null) {
                BuildProperties buildProps = new BuildProperties(jproject);
                configPage.updateBuildProperties(buildProps);
                filesSelectPage.updateBuildProperties(buildProps);
                autoJarPage.updateBuildProperties(buildProps);
                result = buildProps;
            }
        } catch (Exception x) {
            // Debugging...
            x.printStackTrace();
            // Convert to unchecked so we can throw it out of this scope.
            throw new Error(x);
        }
        return result;
    }


    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object[] jprojects = selection.toArray();
        selectedJavaProjects = new String[jprojects.length];
        for (int i=0; i<jprojects.length; i++) {
            selectedJavaProjects[i] = ((JavaProject)jprojects[i]).getProject().getName();
        }
    }

    public boolean execute(Shell shell) {
        setNeedsProgressMonitor(true);
        WizardDialog dialog = new WizardDialog(shell, this);
        //dialog.setMinimumPageSize(640,400);
        return (dialog.open() == Window.OK);
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getStartingPage()
     */
    public IWizardPage getStartingPage() {
        if (jproject != null)
            setJProject(jproject);
        return super.getStartingPage();
    }
    public void setJProject(JProjectConfiguration jproject) {
        this.jproject = jproject;
        BuildProperties props = new BuildProperties(jproject);
        props.read();
        configPage.setJProject(jproject, props);
        filesSelectPage.setJProject(jproject, props);
        autoJarPage.setJProject(jproject, props);
    }

}
