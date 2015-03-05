package net.sf.fjep.fatjar.wizards.export;

import net.sf.fjep.fatjar.preferences.FatJarPreferencePage;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class Prefs {

    public static String getJarName(JProjectConfiguration jproject) {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        String result = store.getString(FatJarPreferencePage.P_JARNAME);
        return result.replaceAll("[<]project[>]", jproject.getName());
    }

    public static String getManifestMergeAll() {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        boolean result = store.getBoolean(FatJarPreferencePage.P_MERGEMANIFEST);
        return (result ? "true" : "false");
    }

    public static String getManifestRemoveSigners() {
        IPreferenceStore store = AguiPlugin.getDefault().getPreferenceStore();
        boolean result = store.getBoolean(FatJarPreferencePage.P_REMOVESIGNERS);
        return (result ? "true" : "false");
    }

    public static String getRelPropertiesFilename(JProjectConfiguration jproject) {
        Preferences prefs = AguiPlugin.getDefault().getPluginPreferences();
        String result = prefs.getString(FatJarPreferencePage.P_CONFIGFILE);
        return result.replaceAll("[<]project[>]", jproject.getName());
    }
    
}
