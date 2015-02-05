package thahn.java.agui.ide.eclipse.launch;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import thahn.java.agui.Log;
import thahn.java.agui.ide.eclipse.project.AguiConstants;
import thahn.java.agui.ide.eclipse.project.AguiProjectInfo;
import thahn.java.agui.ide.eclipse.project.BaseProjectHelper;
import thahn.java.agui.ide.eclipse.wizard.ProjectHelper;

public class AguiLaunchController {
	
	 /**
     * Returns an {@link ILaunchConfiguration} for the specified {@link IProject}.
     * @param project the project
     * @param launchTypeId launch delegate type id
     * @return a new or already existing <code>ILaunchConfiguration</code> or null if there was
     * an error when creating a new one.
     */
    public static ILaunchConfiguration getLaunchConfig(IProject project, String launchTypeId) {
        // get the launch manager
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

        // now get the config type for our particular android type.
        ILaunchConfigurationType configType = manager.getLaunchConfigurationType(launchTypeId);

        String name = project.getName();

        // search for an existing launch configuration
        ILaunchConfiguration config = findConfig(manager, configType, name);

        // test if we found one or not
        if (config == null) {
            // Didn't find a matching config, so we make one.
            // It'll be made in the "working copy" object first.
            ILaunchConfigurationWorkingCopy wc = null;

            try {
                // make the working copy object
                wc = configType.newInstance(null,
                        manager.generateLaunchConfigurationName(name));

                // set the value
                wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
                //
                wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, AguiConstants.SDK_MAIN_CLASS_NAME);
                //
                StringBuilder argBuilder = new StringBuilder();//"E:\\Workspace\\runtime-EclipseApplication\\s7").append(" ").append("s7.s7");
                IProject tempProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if(BaseProjectHelper.isAguiProject(tempProject)) {
					AguiProjectInfo info = BaseProjectHelper.getAguiProjectInfo(tempProject);
					argBuilder.append(info.projectPath).append(" ").append(info.packageName).append(" ").append(info.mainActivityName);
				}
    			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argBuilder.toString()); 
                //
//    			IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
//				IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK
    			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:" + project.getFullPath().makeRelative().toOSString() + "}");

                // map the config and the project
                wc.setMappedResources(getResourcesToMap(project));

                // save the working copy to get the launch config object which we return.
                return wc.doSave();

            } catch (CoreException e) {
                String msg = String.format(
                        "Failed to create a Launch config for project '%1$s': %2$s",
                        project.getName(), e.getMessage());
                Log.e(msg);
//                AdtPlugin.printErrorToConsole(project, msg);
                // no launch!
                return null;
            }
        }

        return config;
    }

    /**
     * Returns the list of resources to map to a Launch Configuration.
     * @param project the project associated to the launch configuration.
     */
    public static IResource[] getResourcesToMap(IProject project) {
        ArrayList<IResource> array = new ArrayList<IResource>(2);
        array.add(project);

        IFile manifest = ProjectHelper.getManifest(project);
        if (manifest != null) {
            array.add(manifest);
        }

        return array.toArray(new IResource[array.size()]);
    }
	
	/**
     * Looks for and returns an existing {@link ILaunchConfiguration} object for a
     * specified project.
     * @param manager The {@link ILaunchManager}.
     * @param type The {@link ILaunchConfigurationType}.
     * @param projectName The name of the project
     * @return an existing <code>ILaunchConfiguration</code> object matching the project, or
     *      <code>null</code>.
     */
    public static ILaunchConfiguration findConfig(ILaunchManager manager,
            ILaunchConfigurationType type, String projectName) {
        try {
            ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);

            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                        "").equals(projectName)) {  //$NON-NLS-1$
                    return config;
                }
            }
        } catch (CoreException e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    "Launch Error", e.getStatus().getMessage());
        }

        // didn't find anything that matches. Return null
        return null;
    }
    
    protected static String getAttributeValueFrom(String value) {
		String content = value;
		if (content.length() > 0) {
			return content;
		}
		return null;
	}
}
