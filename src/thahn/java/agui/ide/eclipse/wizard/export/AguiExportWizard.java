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
package thahn.java.agui.ide.eclipse.wizard.export;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import net.sf.fjep.fatjar.popup.actions.BuildFatJar;
import net.sf.fjep.fatjar.wizard.FJExportWizardConfigPage;
import net.sf.fjep.fatjar.wizard.FJExportWizardFilesSelectPage;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import thahn.java.agui.exception.WrongFormatException;
import thahn.java.agui.ide.eclipse.project.BaseProjectHelper;
import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class AguiExportWizard extends Wizard implements IExportWizard {

	private FJExportWizardConfigPage		fjewConfig;
	private ProjectCheckPage				projectCheckPage;
	private FJExportWizardFilesSelectPage	fjewFiles;
	// FJExportWizardAutoJarPage 			fjewAutoJar;
	private IJavaProject					jproject	= null;

	public AguiExportWizard() {
		super();
	}

	public AguiExportWizard(IJavaProject jproject) {
		this.jproject = jproject;
		if (jproject != null)
			setJProject(jproject);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		projectCheckPage = new ProjectCheckPage(this, jproject, "Choose Project");
		addPage(projectCheckPage);
		fjewConfig = new FJExportWizardConfigPage(null, null);
		addPage(fjewConfig);
		fjewFiles = new FJExportWizardFilesSelectPage(this);
		addPage(fjewFiles);
		// fjewAutoJar = new FJExportWizardAutoJarPage(this);
		// addPage(fjewAutoJar);
		super.addPages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			if (jproject == null) {
				jproject = getJProject();// fjewProject.getSelectedJavaProject();
			}
			if (jproject != null) {
				Properties props = fjewConfig.updateProperties();

				String[][] excludeInfo = fjewFiles.getAllUnchecked();
				HashSet excludes = new HashSet();
				StringBuffer excludeProp = new StringBuffer();
				for (int i = 0; i < excludeInfo.length; i++) {
					excludes.add(excludeInfo[i][1]);
					if (i > 0)
						excludeProp.append(';');
					excludeProp.append(excludeInfo[i][0]);
				}
				props.setProperty("excludes", excludeProp.toString());

				String[][] includeInfo = fjewFiles.getAllChecked();
				ArrayList includes = new ArrayList();
				StringBuffer includeProp = new StringBuffer();
				for (int i = 0; i < includeInfo.length; i++) {
					includes.add(includeInfo[i][1]);
					if (i > 0)
						includeProp.append(';');
					includeProp.append(includeInfo[i][0]);
				}
				props.setProperty("includes", includeProp.toString());

				BuildFatJar bfj = new BuildFatJar();
				bfj.runBuildConfiguredFatJar(jproject, props, excludes, includes);
			}
			return (jproject != null);
		} catch (Exception x) {
			// Debugging...
			x.printStackTrace();
			// Convert to unchecked so we can throw it out of this scope.
			throw new Error(x);
		}
	}

	public Properties getProperties() {
		Properties result = null;
		try {
			if (jproject == null) {
				jproject = getJProject();// fjewProject.getSelectedJavaProject();
			}
			if (jproject != null) {
				Properties props = fjewConfig.updateProperties();

				String[][] excludeInfo = fjewFiles.getAllUnchecked();
				StringBuffer excludeProp = new StringBuffer();
				for (int i = 0; i < excludeInfo.length; i++) {
					if (i > 0)
						excludeProp.append(';');
					excludeProp.append(excludeInfo[i][0]);
				}
				props.setProperty("excludes", excludeProp.toString());

				String[][] includeInfo = fjewFiles.getAllChecked();
				StringBuffer includeProp = new StringBuffer();
				for (int i = 0; i < includeInfo.length; i++) {
					if (i > 0)
						includeProp.append(';');
					includeProp.append(includeInfo[i][0]);
				}
				props.setProperty("includes", includeProp.toString());
				result = props;
			}
		} catch (Exception x) {
			// Debugging...
			x.printStackTrace();
			// Convert to unchecked so we can throw it out of this scope.
			throw new Error(x);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@SuppressWarnings("restriction")
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object[] jprojects = selection.toArray();
		if (jprojects.length == 1) {
			String selectedJavaProjects = null;
			if (jprojects[0] instanceof JavaProject) {
				JavaProject javaProject = (JavaProject) jprojects[0];
				if (BaseProjectHelper.isAguiProject(javaProject.getProject())) {
					selectedJavaProjects = ((JavaProject) jprojects[0]).getProject().getName();
					jproject = ((JavaProject) jprojects[0]);
				} else {
					AguiPlugin.displayError("Error", "Select Agui project.");
				}
			} else if (jprojects[0] instanceof PackageFragmentRoot) {
				IJavaProject javaProject = ((PackageFragmentRoot) jprojects[0]).getJavaProject();
				if (BaseProjectHelper.isAguiProject(javaProject.getProject())) {
					selectedJavaProjects = javaProject.getProject().getName();
					jproject = ((PackageFragmentRoot) jprojects[0]).getJavaProject();
				} else {
					AguiPlugin.displayError("Error", "Select Agui project.");
				}
			}
		} else {
			AguiPlugin.displayError("Error", "Select a one project.");
		}
	}

	public boolean execute(Shell shell) {
		setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(shell, this);
		// dialog.setMinimumPageSize(640,400);
		return (dialog.open() == Window.OK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#getStartingPage()
	 */
	public IWizardPage getStartingPage() {
		if (jproject != null)
			setJProject(jproject);
		return super.getStartingPage();
	}

	public IJavaProject getJProject() {
		return this.jproject;
	}

	public void setJProject(IJavaProject jproject) {
		this.jproject = jproject;
		Properties props = BuildFatJar.getFatJarProperties(jproject);
		fjewFiles.setJProject(jproject, props);
		fjewConfig.setJProject(jproject, props);
	}

	public FJExportWizardConfigPage getFjewConfig() {
		return fjewConfig;
	}
}
