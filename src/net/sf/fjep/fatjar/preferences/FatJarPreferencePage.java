/*******************************************************************************
 * Copyright (c) 2004 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Fat Jar Eclipse Plug-In
 *
 * The Fat Jar Eclipse Plug-In is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Fat Jar Eclipse Plug-In is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Fat Jar Eclipse Plug-In;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package net.sf.fjep.fatjar.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */


public class FatJarPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	public static final String P_CONFIGFILE = "configfilePreference";
	public static final String P_JARNAME = "jarnamePreference";
    public static final String P_MERGEMANIFEST = "mergemanifestPreference";
    public static final String P_REMOVESIGNERS = "removesignersPreference";
	public static final String P_SCMAUTOCHECKOUT = "scmautocheckoutPreference";
	public static final String P_ESCAPEUPPERCASE = "escapeuppercasePreference";

	public FatJarPreferencePage() {
		super(GRID);
		setPreferenceStore(AguiPlugin.getDefault().getPreferenceStore());
		setDescription("Default values for Fat Jar (use \"<project>\" as template for the project-name)");
		initializeDefaults();
	}
/**
 * Sets the default values of the preferences.
 */
	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(P_JARNAME, "<project>_fat.jar");
        store.setDefault(P_MERGEMANIFEST, true);
        store.setDefault(P_REMOVESIGNERS, true);
		store.setDefault(P_SCMAUTOCHECKOUT, true);
		store.setDefault(P_ESCAPEUPPERCASE, false);
		store.setDefault(P_CONFIGFILE, ".fatjar");
	}
	
/**
 * Creates the field editors. Field editors are abstractions of
 * the common GUI blocks needed to manipulate various types
 * of preferences. Each field editor knows how to save and
 * restore itself.
 */

	public void createFieldEditors() {
		addField( new StringFieldEditor(P_JARNAME, "Jar-Name:", getFieldEditorParent()));
        addField( new BooleanFieldEditor(P_MERGEMANIFEST, "merge individual-sections of all MANIFEST.MF files", getFieldEditorParent()));
        addField( new BooleanFieldEditor(P_REMOVESIGNERS, "remove signer files (META-INF/*.SF)", getFieldEditorParent()));
		addField( new BooleanFieldEditor(P_SCMAUTOCHECKOUT, "automatically checkout files from version control like SubVersion, ClearCase...", getFieldEditorParent()));
		addField( new BooleanFieldEditor(P_ESCAPEUPPERCASE, "escape upper case letters in package and class-names to avoid conflicts with filesystems like NTFS ignoring case", getFieldEditorParent()));
		addField( new StringFieldEditor(P_CONFIGFILE, "Project Config File:", getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {
	}
}