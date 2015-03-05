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

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class AutoJarPage extends WizardPage {

    private Button autojarEnableCheckbox;
    private Text autojarVisitClassesText;
    private Button autojarClassForNameCheckbox;
    BuildProperties props;

    public AutoJarPage(JProjectConfiguration jproject, BuildProperties props) {
        super("wizardPage");
        System.out.println("ctor ConfigPage");
        setTitle("Shrink using Auto-Jar");
        setDescription("select classes to include");
        this.props = props;
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
    
            //----------------------------------------------------------
            label = new Label(comp, SWT.NULL);
            label.setVisible(false);
            //
            autojarEnableCheckbox = new Button(comp, SWT.CHECK);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            autojarEnableCheckbox.setLayoutData(gd);
            autojarEnableCheckbox.setText("shrink jar file using auto-jar");
            autojarEnableCheckbox.setEnabled(true);

            autojarEnableCheckbox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    boolean autojarEnable = autojarEnableCheckbox.getSelection();
                    autojarVisitClassesText.setEnabled(autojarEnable);
                    autojarClassForNameCheckbox.setEnabled(autojarEnable);
                    if (autojarEnable) {
                        if (getAutojarVisitClasses().trim().equals("")) {
                            autojarVisitClassesText.setText(getConfigMainClass());
                        }
                    }
                    dialogChanged();
                }
            });


            //----------------------------------------------------------
            label = new Label(comp, SWT.NULL);
            label.setText("Classes-List:");
            //
            autojarVisitClassesText = new Text(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            gd = new GridData(GridData.FILL_BOTH);
            autojarVisitClassesText.setLayoutData(gd);

            autojarVisitClassesText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    dialogChanged();
                }
            });
            
            //----------------------------------------------------------
            label = new Label(comp, SWT.NULL);
            label.setVisible(false);
            //
            autojarClassForNameCheckbox = new Button(comp, SWT.CHECK);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            autojarClassForNameCheckbox .setLayoutData(gd);
            autojarClassForNameCheckbox .setText("search for calls to Class.forName(...)");
            autojarClassForNameCheckbox .setEnabled(true);

            //----------------------------------------------------------

            if (props != null) {
                initialize();
                dialogChanged();
            }
            setControl(comp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    private void initialize() {
        boolean autojarEnable = props.isAutojarEnable(); 
        String autojarVisitClasses = props.getAutojarVisitClasses();
        boolean autojarClassForName = props.isAutojarClassForName(); 
        autojarEnableCheckbox.setSelection(autojarEnable);
        autojarVisitClassesText.setText(autojarVisitClasses);
        autojarVisitClassesText.setEnabled(autojarEnable);
        autojarClassForNameCheckbox.setSelection(autojarClassForName);
        autojarClassForNameCheckbox.setEnabled(autojarEnable);
    }
    

    void dialogChanged() {

        String errorMsg = null;
        String warnMsg = null;
        boolean autojarEnable = getAutojarEnable();
        if (autojarEnable) {
            String autojarVisitClasses = getAutojarVisitClasses();
            if (autojarVisitClasses.trim().equals("")) {
                errorMsg = "at least one class must be defined";
            }
            else {
                String mainClass = getConfigMainClass();
                if (mainClass != null) {
                    if (autojarVisitClasses.indexOf(mainClass) == -1) {
                        warnMsg = "main class '" + mainClass + "' is missing";
                    }
                }
            }
        }
        updateStatus(errorMsg, warnMsg);
    }



    private String getConfigMainClass() {
        
        FJExpWizard fjew = (FJExpWizard)this.getWizard();
        String mainClass = null;
        if (fjew.configPage != null) {
            mainClass = fjew.configPage.getManifestmainclass();
        }
        return mainClass;
    }

    private void updateStatus(String errorMessage, String warnMessage) {
            
            if (warnMessage == null)
                    setMessage(null);
            else
                    setMessage(warnMessage, IMessageProvider.WARNING);
            setErrorMessage(errorMessage);
            setPageComplete(errorMessage == null);
    }

    public boolean getAutojarEnable() {
        return autojarEnableCheckbox.getSelection();
    }
    public String getAutojarVisitClasses() {
        return autojarVisitClassesText.getText();
    }
    public boolean getAutojarClassForName() {
        return autojarClassForNameCheckbox.getSelection();
    }

    public BuildProperties updateProperties() {
        
        boolean autojarEnable = getAutojarEnable();
        String autojarVisitClasses = getAutojarVisitClasses();
        boolean autojarClassForName = getAutojarClassForName();

        props.setAutojarEnable(autojarEnable);
        props.setAutojarVisitClasses(autojarVisitClasses);
        props.setAutojarClassForName(autojarClassForName);

        return props;
    }

    public void updateBuildProperties(BuildProperties buildProps) {
        
        boolean autojarEnable = getAutojarEnable();
        String autojarVisitClasses = getAutojarVisitClasses();
        boolean autojarClassForName = getAutojarClassForName();

        buildProps.setAutojarEnable(autojarEnable);
        buildProps.setAutojarVisitClasses(autojarVisitClasses);
        buildProps.setAutojarClassForName(autojarClassForName);
        
    }

    /**
     * @param selectedJavaProject
     */
    public void setJProject(JProjectConfiguration jproject, BuildProperties props) {
        if (this.props != props) {
            this.props = props;
            initialize();
            dialogChanged();
            setDescription("Shrink using Auto-Jar for project " + jproject.getName());
        }
    }

}