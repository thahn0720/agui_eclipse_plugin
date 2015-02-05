package thahn.java.agui.ide.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

/**
 * Class used to initialize default preference values.
 */
public class AguiPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
//		store.setDefault(AguiPreferenceConstants.P_BOOLEAN, true);
//		store.setDefault(AguiPreferenceConstants.P_CHOICE, "choice2");
//		store.setDefault(AguiPreferenceConstants.P_STRING, "Default value");
	}

}
