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
package net.sf.fjep.fatjar.wizard;

import java.util.Vector;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import thahn.java.agui.ide.eclipse.wizard.export.AguiExportWizard;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class FJExportWizardProjectSeletPage extends WizardPage {

	private String[] currentSelection; 

	CheckboxTreeViewer projectTree;
	/**
	 * Constructor for SampleNewWizardPage.
	 * @param pageName
	 */
	public FJExportWizardProjectSeletPage(String[] currentSelection) {
		super("wizardPage");
		setTitle("Select one Java Project");
		setDescription("This project will be deployed as Fat Jar");
		this.currentSelection = currentSelection;
	}


	public class JavaProjectsTreeContentProvider implements ITreeContentProvider {
		private Object[] root;
		public Object[] getChildren(Object element) {
			return (element == root) ? root : new Object[0];
		}
		public Object[] getElements(Object element) {
			return (element == root) ? root : new Object[0];
		}
		public boolean hasChildren(Object element) {
			return (element == root);
		}
		public Object getParent(Object element) {
			return (element == root) ? null : root;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
			root = (Object[]) new_input;
		}
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
		label.setText("Java Project:");
        //
		Tree tree = new Tree(comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK | SWT.V_SCROLL );
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = tree.getItemHeight() * 10;
		tree.setLayoutData(gd);
		projectTree = new CheckboxTreeViewer(tree);
		JavaProjectsTreeContentProvider cp = new JavaProjectsTreeContentProvider();
		projectTree.setContentProvider(cp);
		projectTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				dialogChanged();
			}
		});
        //
		label = new Label(comp, SWT.NULL);
		label.setVisible(false);

		initialize();
		projectTree.setCheckedElements(currentSelection);
		dialogChanged();
		setControl(comp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String[] getJProjectList() {
		Vector result = new Vector();
		IJavaProject[] jprojects = null;
		try {
			jprojects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
		} catch (JavaModelException e) { e.printStackTrace(); }
		for (int i=0; i<jprojects.length; i++) {
				result.add(jprojects[i].getProject().getName());
		}
		String[] resultArray = (String[]) result.toArray(new String[result.size()]);
		return resultArray;
	}

	private void initialize() {
		String[] projectList = getJProjectList();
		projectTree.setInput(projectList);

	}
	

	
	/**
	 * Ensures that exactly one project is selected
	 */

	private void dialogChanged() {
		
		int cnt = projectTree.getCheckedElements().length;
		if (cnt == 0) {
			updateStatus("No Java project has been selected");
		}
		else if (cnt == 1)
			updateStatus(null);
		else
			updateStatus("Only one Java project can be selected");
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		boolean ok = false;
		if (isPageComplete()) {
			AguiExportWizard fjew = (AguiExportWizard)getWizard();
			fjew.setJProject(getSelectedJavaProject());
			ok = true;
		}
		return (ok && super.canFlipToNextPage());
	}

	public IJavaProject getSelectedJavaProject() {
		IJavaProject result = null;
		Object[] checked = projectTree.getCheckedElements();
		if (checked.length == 1) {
			String projectName = (String)checked[0];
			result = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		}
		return result;
	}

}