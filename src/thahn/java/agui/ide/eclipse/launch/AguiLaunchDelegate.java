package thahn.java.agui.ide.eclipse.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import thahn.java.agui.Log;

public class AguiLaunchDelegate extends JavaLaunchDelegate { //implements ILaunchConfigurationDelegate {
	
	public static final String					TAG 		= "AguiLaunchDelegate";
	public static final String					ID			= "thahn.java.agui.ide.eclipse.launch.AguiLaunchDelegate";
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
		//
//		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
//		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//		IProjectDescription des = project.getDescription();
//		des.setActiveBuildConfig(configuration.getName());
		
//		Log.d(TAG, "launch : arg - " + argBuilder.toString());
//		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		//
//		ApplicationController appCon = new ApplicationController();
//		appCon.create("E:\\Workspace\\runtime-EclipseApplication\\s7", "s7.s7");
//		try {
//			Runtime.getRuntime().exec(
//					"java -classpath E:/agui-sdk-windows/platforms/agui-1/agui_lib.jar dksxogudsla.java.agui.Main");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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
