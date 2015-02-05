package thahn.java.agui.ide.eclipse.menu;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.ObjectPluginAction;

import thahn.java.agui.ide.eclipse.project.AguiConstants;
import thahn.java.agui.ide.eclipse.utils.ToolsJarLoader;
import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;
import thahn.java.agui.ide.eclipse.wizard.MessageBoxUtils;

public class ConvertToAndroidAction extends ActionDelegate implements IViewActionDelegate {

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		StructuredSelection selection = ((StructuredSelection)((ObjectPluginAction)action).getSelection());
		if(!selection.isEmpty()) {
			if(AguiPlugin.getDefault().getSdkLocation() == null || AguiPlugin.getDefault().getSdkLocation().trim().equals("")) {
				MessageBoxUtils.showMessageBox("Agui", "Agui SDK Location should set");
				AguiPlugin.getDefault().workbenchStarted();
			} else {
				final IProject project = (IProject) selection.getFirstElement();
				try {
					IRunnableWithProgress op = new IRunnableWithProgress() {
						
						@Override
						public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								String toolsJarPath = AguiPlugin.getDefault().getLocationInSdk(AguiConstants.TOOLS_PROJECT_CONVERTER_PATH);
								String importInfoPath = AguiPlugin.getDefault().getLocationInSdk(AguiConstants.TOOLS_IMPORT_INFO_PATH);
								URL jarUrl = new File(toolsJarPath).toURI().toURL();
								if(!ToolsJarLoader.getInstance().isLoaded(jarUrl)) {
									ToolsJarLoader.getInstance().addURL(jarUrl);
								} 
								String sdkJarPath = AguiPlugin.getDefault().getSdkJarLocation();
								Class toAguiCls = ToolsJarLoader.getInstance().loadClass("thahn.java.agui.converter.ConverterToAndroid");
								Object obj = toAguiCls.getConstructor(String.class, String.class, String.class, String.class).newInstance(project.getName(), project.getLocationURI().getPath(), sdkJarPath, importInfoPath);
								Method method = obj.getClass().getMethod("convert");
								method.invoke(obj);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
//							IConverter converter = new ConverterToAndroid(project.getName(), project.getLocationURI().getPath(), jarPath);
//							converter.setOnConverterListener(new ConverterListener() {
//								@Override
//								public void onProgress(String fileName, int progress) {
//									monitor.setTaskName(fileName);
//									monitor.worked(progress);
//									if(progress == 100) {
//										monitor.done();
//									}
//								}
//							});
//							converter.convert();
							try {
								project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					};
					new ProgressMonitorDialog(AguiPlugin.getShell()).run(true, false, op);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			MessageBoxUtils.showMessageBox("Agui", "Select project to convert");
		}
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		
	}
}
