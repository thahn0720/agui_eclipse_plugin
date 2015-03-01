package thahn.java.agui.ide.eclipse.utils;

import org.eclipse.core.runtime.Status;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class Log {
	
	public static void i(String message) {
		AguiPlugin.getDefault().getLog().log(new Status(Status.INFO, AguiPlugin.PLUGIN_ID, message));
	}
}
