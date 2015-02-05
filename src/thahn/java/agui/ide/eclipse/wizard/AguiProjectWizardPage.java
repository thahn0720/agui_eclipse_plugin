package thahn.java.agui.ide.eclipse.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */
public class AguiProjectWizardPage extends WizardPage {
	private Text mApplicationNameText;
	private Text mProjectNmameText;
	private Text mPackageNameText;
	private Text mActivityNameText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public AguiProjectWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("AGUI Project");
		setDescription("This wizard creates a new AGUI project.");
		this.selection = selection;
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		//
		if(AguiPlugin.getDefault().getSdkLocation() == null || AguiPlugin.getDefault().getSdkLocation().trim().equals("")) {
			MessageBoxUtils.showMessageBox("Agui", "Agui SDK Location should set");
			AguiPlugin.getDefault().workbenchStarted();
		} else {
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 2;
			layout.verticalSpacing = 9;
			//
			Label label = new Label(container, SWT.NULL);
			label.setText("&Project Name:");
			
			mProjectNmameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			mProjectNmameText.setLayoutData(gd);
			mProjectNmameText.addModifyListener(textListener);
			//
			label = new Label(container, SWT.NULL);
			label.setText("&Application Name:");
			
			mApplicationNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			mApplicationNameText.setLayoutData(gd);
			mApplicationNameText.addModifyListener(textListener);
			//
			label = new Label(container, SWT.NULL);
			label.setText("&Package Name:");
	
			mPackageNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			mPackageNameText.setLayoutData(gd);
			mPackageNameText.addModifyListener(textListener);
			//
			label = new Label(container, SWT.NULL);
			label.setText("&Main Activity Name:");
	
			mActivityNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			mActivityNameText.setLayoutData(gd);
			mActivityNameText.addModifyListener(textListener);
			//
			initialize();
			dialogChanged();
			setControl(container);
		}
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				mApplicationNameText.setText(container.getFullPath().toString());
			}
		}
	}

//	/**
//	 * Uses the standard container selection dialog to choose the new value for
//	 * the container field.
//	 */
//	private void handleBrowse() {
//		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
//				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
//				"Select new file container");
//		if (dialog.open() == ContainerSelectionDialog.OK) {
//			Object[] result = dialog.getResult();
//			if (result.length == 1) {
//				applicationNameText.setText(((Path) result[0]).toString());
//			}
//		}
//	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
//		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
		String applicationName = mApplicationNameText.getText();
		String projectName = mProjectNmameText.getText();
		String packageName = mPackageNameText.getText();
		String activityName = mActivityNameText.getText();
		
		if(TextUtils.isEmpty(applicationName)) {
			updateStatus("Application Name must be specified");
			return;
		}
		if(TextUtils.isEmpty(projectName)) {
			updateStatus("Project Name must be specified");
			return;
		}
		if(TextUtils.isEmpty(packageName)) {
			updateStatus("Package Name must be specified");
			return;
		}
		if(TextUtils.isEmpty(activityName)) {
			updateStatus("Activity Name must be specified");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getApplicationName() {
		return mApplicationNameText.getText();
	}

	public String getPackageName() {
		return mPackageNameText.getText();
	}
	
	public String getProjectName() {
		return mProjectNmameText.getText();
	}
	
	public String getActivityName() {
		return mActivityNameText.getText();
	}
	
	ModifyListener textListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			dialogChanged();
		}
	};
	
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}
}