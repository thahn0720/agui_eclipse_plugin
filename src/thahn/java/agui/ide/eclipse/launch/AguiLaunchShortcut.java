package thahn.java.agui.ide.eclipse.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import thahn.java.agui.ide.eclipse.project.AguiNature;

public class AguiLaunchShortcut implements ILaunchShortcut {

	//id - thahn.java.agui.ide.eclipse.launch.AguiLaunchShortcut
	@Override
	public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            // get the object and the project from it
            IStructuredSelection structSelect = (IStructuredSelection)selection;
            Object o = structSelect.getFirstElement();

            // get the first (and normally only) element
            if (o instanceof IAdaptable) {
                IResource r = (IResource)((IAdaptable)o).getAdapter(IResource.class);

                // get the project from the resource
                if (r != null) {
                    IProject project = r.getProject();

                    if (project != null && AguiNature.checkAguiProject(project))  {
                        launch(project, mode);
                    } else {
                    	MessageDialog.openError(Display.getCurrent().getActiveShell(),
                                "Launch Error", "This project is not the Agui Project");
                    }
                }
            }
        }
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
	}
	
    /**
     * Launch a config for the specified project.
     * @param project The project to launch
     * @param mode The launch mode ("debug", "run" or "profile")
     */
    private void launch(IProject project, String mode) {
        // get an existing or new launch configuration
    	ILaunchConfiguration config = AguiLaunchController.getLaunchConfig(project, AguiLaunchDelegate.ID);
        if (config != null) {
            // and launch!
            DebugUITools.launch(config, mode);
        }
    }
}
