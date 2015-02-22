package thahn.java.agui.ide.eclipse.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import thahn.java.agui.ide.eclipse.project.AguiProjectInfo;
import thahn.java.agui.ide.eclipse.project.BaseProjectHelper;

public class AguiLaunchDelegate extends JavaLaunchDelegate { //implements ILaunchConfigurationDelegate {
	
	public static final String					TAG 		= "AguiLaunchDelegate";
	public static final String					ID			= "thahn.java.agui.ide.eclipse.launch.AguiLaunchDelegate";
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		StringBuilder argBuilder = new StringBuilder();//"E:\\Workspace\\runtime-EclipseApplication\\s7").append(" ").append("s7.s7");
        IProject tempProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (BaseProjectHelper.isAguiProject(tempProject)) {
			AguiProjectInfo info = BaseProjectHelper.getAguiProjectInfo(tempProject);
			argBuilder.append(info.projectPath).append(" ").append(info.packageName).append(" ").append(info.mainActivityName);
			configuration.getWorkingCopy().setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argBuilder.toString()); 
		}
		
		super.launch(configuration, mode, launch, monitor);
	}
	
	protected String getAttributeValueFrom(String text) {
		String content = text.trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

	@Override
	public IJavaProject verifyJavaProject(ILaunchConfiguration configuration)
			throws CoreException {
		return super.verifyJavaProject(configuration);
	}
}
