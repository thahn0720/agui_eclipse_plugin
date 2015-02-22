package thahn.java.agui.ide.eclipse.preferences;

import java.nio.file.Paths;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.base.Strings;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

/**
 * Class used to initialize default preference values.
 */
public class AguiPrefs extends AbstractPreferenceInitializer {

	private IPreferenceStore prefs;
	private static AguiPrefs instance = new AguiPrefs();
	
	public static AguiPrefs getInstance() {
		return instance;
	}
	
	public AguiPrefs() {
		prefs = AguiPlugin.getDefault().getPreferenceStore();
	}

	public static IPreferenceStore prefs() {
		return instance.prefs;
	}
	
	public void initializeDefaultPreferences() {
		if (prefs == null) {
			prefs = AguiPlugin.getDefault().getPreferenceStore();
		}
//		store.setDefault(AguiPreferenceConstants.P_BOOLEAN, true);
//		store.setDefault(AguiPreferenceConstants.P_CHOICE, "choice2");
//		store.setDefault(AguiPreferenceConstants.P_STRING, "Default value");
	}
	
	public void setSdkLocation(String dir) {
		prefs().setValue(AguiPreferenceConstants.P_SDK_LOCATION, dir);
	}
	
	public String getSdkLocation() {
		return prefs().getString(AguiPreferenceConstants.P_SDK_LOCATION);
	}
	
	public void setSdkJarLocation(String dir) {
		prefs().setValue(AguiPreferenceConstants.P_SDK_LIB_LOCATION, dir);
	}
	
	public String getSdkJarLocation() {
		String ret = prefs().getString(AguiPreferenceConstants.P_SDK_LIB_LOCATION);
		if(ret != null || !Strings.isNullOrEmpty(ret)) {
			ret = Paths.get(ret, "agui_sdk.jar").toFile().getAbsolutePath();
		}
		return ret;
	}
	
	public void setSdkVersionSelection(String versionSelection) {
		prefs().setValue(AguiPreferenceConstants.P_SDK_VERSION_SELECTION, versionSelection);
	}
	
	public String getSdkVersionSelection() {
		String ret = prefs().getString(AguiPreferenceConstants.P_SDK_VERSION_SELECTION);
		return ret;
	}
	
	public String getToolsLibLocation() {
		String ret = prefs().getString(AguiPreferenceConstants.P_SDK_LOCATION);
		if(ret != null || !ret.trim().equals("")) {
			ret += "/tools/lib/";
		}
		return ret;
	}
	
	public String getLocationInSdk(String path) {
		String ret = prefs().getString(AguiPreferenceConstants.P_SDK_LOCATION);
		if(ret != null || !ret.trim().equals("")) {
			ret += path;
		}
		return ret;
	}
}
