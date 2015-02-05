package thahn.java.agui.ide.eclipse.project;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class AguiStartup implements IStartup, IWindowListener {

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
	}

	@Override
	public void earlyStartup() {
		AguiPlugin.getDefault().workbenchStarted();
	}
}
