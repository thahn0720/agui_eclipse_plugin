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

import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import thahn.java.agui.ide.eclipse.wizard.export.AguiExportWizard;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class FJExportWizardAutoJarPage extends WizardPage {

    private AguiExportWizard fjew;
    private Text classesListText;
	
	public FJExportWizardAutoJarPage(AguiExportWizard fjew) {
		super("wizardPage");
        this.fjew = fjew;
		setTitle("Shrink using Auto-Jar");
		setDescription("select classes to include");
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
		label.setText("Classes-List:");
        //
        classesListText = new Text(comp, SWT.BORDER | SWT.MULTI);
        gd = new GridData(GridData.FILL_BOTH);
        classesListText.setLayoutData(gd);

        initialize();
		dialogChanged();
		setControl(comp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    

	private void initialize() {
        Properties props = fjew.getProperties();
        String visitclasses = props.getProperty("visitclasses", "");

		if (visitclasses.trim().length() == 0) {
			setDescription("select classes to include - autojar off");
		}
		else {
            setDescription("select classes to include - autojar on");
		}
	}
	

	private void dialogChanged() {
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}


}