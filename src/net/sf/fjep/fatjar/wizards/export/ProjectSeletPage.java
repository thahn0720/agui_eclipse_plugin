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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;
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

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class ProjectSeletPage extends WizardPage {

    private String[] preSelection; 

    CheckboxTreeViewer projectTree;
    /**
     * Constructor for SampleNewWizardPage.
     * @param pageName
     */
    public ProjectSeletPage(String[] preSelection) {
        super("wizardPage");
        System.out.println("ctor ProjectSeletPage");
        setTitle("Select one java project or launch configuration");
        setDescription("This project or launch configuration will be deployed as Fat Jar");
        this.preSelection = preSelection;
    }



    public class JavaProjectsTreeContentProvider implements ITreeContentProvider {
        private StringTree root;
        public Object[] getChildren(Object element) {
            StringTree tree = (StringTree) element;
            return tree.getChildren();
        }
        public Object[] getElements(Object element) {
            StringTree tree = (StringTree) element;
            return tree.getChildren();
        }
        public boolean hasChildren(Object element) {
            StringTree tree = (StringTree) element;
            return tree.hasChildren();
        }
        public Object getParent(Object element) {
            StringTree tree = (StringTree) element;
            return tree.getParent();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
            root = (StringTree) new_input;
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
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        label = new Label(comp, SWT.NULL);
        label.setText("Java Projects:");
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

        initialize();
        StringTree[] sel = new StringTree[preSelection.length];
        for (int i = 0; i < preSelection.length; i++) {
            final String preSel = preSelection[i];
            StringTree root = (StringTree)projectTree.getInput();
            if (root != null) {
                sel[i] = root.findFirst(new StringTree.IStringTreeCondition() {
                    public boolean check(StringTree stringTree) {
                        return stringTree.getLabel().equals(preSel);
                    }
                });
            }
        }
        projectTree.setCheckedElements(sel);
        dialogChanged();
        setControl(comp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private StringTree getJProjectList() {
        
        StringTree rootTree = new StringTree("", null);
        IJavaProject[] jprojects = null;
        try {
            jprojects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
        } catch (JavaModelException e) { e.printStackTrace(); }

        LaunchManager lm = new LaunchManager();
        ILaunchConfiguration[] lcs = lm.getLaunchConfigurations();
        
        for (int i=0; i<jprojects.length; i++) {
                IJavaProject jproject = jprojects[i];
                String projectName = jproject.getProject().getName();
                StringTree projectTree = new StringTree(projectName, jproject);
                rootTree.addChild(projectTree);
                
                // enumerate launch configs for this project
                try {
                        for (int j = 0; j < lcs.length; j++) {
                            ILaunchConfiguration lc = lcs[j];
                            String projectAttr = lc.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", (String)null);
                            if ((projectAttr != null) && projectAttr.equals(projectName)) {
                                String name = lc.getName();
                                projectTree.addChild(new StringTree(name, lc));
                            }
                        }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return rootTree;
    }

    private void initialize() {
        StringTree projectList = getJProjectList();
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
            FJExpWizard fjew = (FJExpWizard)getWizard();
            fjew.setJProject(getSelectedJavaConfig());
            ok = true;
        }
        return (ok && super.canFlipToNextPage());
    }

    public JProjectConfiguration getSelectedJavaConfig() {
        JProjectConfiguration result = null;
        Object[] checked = projectTree.getCheckedElements();
        if (checked.length == 1) {
            IJavaProject jproject = null;
            ILaunchConfiguration lc = null;
            StringTree selProject = (StringTree)checked[0];
            if (!selProject.getParent().isRoot()) {
                lc = (ILaunchConfiguration) selProject.getData();
                selProject = selProject.getParent();
            }
            jproject = (IJavaProject) selProject.getData();
            result = new JProjectConfiguration(jproject, lc);
//            result = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
        }
        return result;
    }

}