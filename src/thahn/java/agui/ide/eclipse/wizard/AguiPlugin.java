package thahn.java.agui.ide.eclipse.wizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import thahn.java.agui.ide.eclipse.preferences.AguiPreferenceConstants;
import thahn.java.agui.ide.eclipse.preferences.AguiPreferencePage;

/**
 * The activator class controls the plug-in life cycle
 */
public class AguiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "thahn.java.agui.ide.eclipse.wizard"; //$NON-NLS-1$

	// The shared instance
	private static AguiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public AguiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AguiPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static URL getBundleAbsolutePath(String path) {
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
        if (!BundleUtility.isReady(bundle)) {
			return null;
		}

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = BundleUtility.find(bundle, path);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(path);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return fullPathString;
	}
	
	private void checkSdkLocation() {
		final Display disp = getDisplay();
        disp.asyncExec(new Runnable() {
            @Override
            public void run() {
                Shell shell = disp.getActiveShell();
                if (shell == null) {
                    return;
                }

                String customLabel = null;
                customLabel = "Open Preferences";

                String btnLabels[] = new String[customLabel == null ? 1 : 2];
                btnLabels[0] = customLabel;
                btnLabels[btnLabels.length - 1] = IDialogConstants.CLOSE_LABEL;

                MessageDialog dialog = new MessageDialog(
                        shell, // parent
                        "Agui Sdk",
                        null, // dialogTitleImage
                        "Set Agui SDK Path",
                        MessageDialog.WARNING,
                        btnLabels,
                        btnLabels.length - 1);
                int index = dialog.open();

                if (customLabel != null && index == 0) {
//                    switch(solution) {
//                    case OPEN_ANDROID_PREFS:
                	openAguiPrefs();
//                        break;
//                    case OPEN_P2_UPDATE:
//                        openP2Update();
//                        break;
//
//                	case OPEN_SDK_MANAGER:
//                        openSdkManager();
//                        break;
//                    }
//                }
                }
            }
        });
    }
	
	 /**
     * Returns the current display, if any
     *
     * @return the display
     */
    public static Display getDisplay() {
        Display display = Display.getCurrent();
        if (display != null) {
            return display;
        }

        return Display.getDefault();
    }

    /**
     * Returns the shell, if any
     *
     * @return the shell, if any
     */
    public static Shell getShell() {
        Display display = AguiPlugin.getDisplay();
        Shell shell = display.getActiveShell();
        if (shell == null) {
            Shell[] shells = display.getShells();
            if (shells.length > 0) {
                shell = shells[0];
            }
        }
        return shell;
    }

	public void workbenchStarted() {
		IPreferenceStore pref = getPreferenceStore();
		String sdkLocation = pref.getString(AguiPreferenceConstants.P_SDK_LOCATION);
		if(sdkLocation == null || sdkLocation.trim().equals("")) {
			checkSdkLocation();
		}
//		RuntimeJarLoader.loadJarIndDir(getToolsLibLocation());
	}
	
	private void openAguiPrefs() {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                getDisplay().getActiveShell(),
                AguiPreferencePage.PAGE_ID, //$NON-NLS-1$ preferencePageId
                null,  // displayedIds
                null); // data
        dialog.open();
    }
	
	public static void displayError(String title, String message) {
		MessageDialog dialog = new MessageDialog(getShell(), title, null,
			    message, MessageDialog.ERROR, new String[] { "Ok", "Cancel" }, 0);
	}
}
