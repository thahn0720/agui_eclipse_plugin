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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

public class FatJarPreferenceInitializer extends AbstractPreferenceInitializer {

	public FatJarPreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = AguiPlugin.getDefault().getPluginPreferences();
		prefs.setDefault(FatJarPreferencePage.P_CONFIGFILE, ".fatjar");
		prefs.setDefault(FatJarPreferencePage.P_JARNAME, "<project>_fat.jar");
        prefs.setDefault(FatJarPreferencePage.P_MERGEMANIFEST, true);
        prefs.setDefault(FatJarPreferencePage.P_REMOVESIGNERS, true);
		prefs.setDefault(FatJarPreferencePage.P_SCMAUTOCHECKOUT, true);
		prefs.setDefault(FatJarPreferencePage.P_ESCAPEUPPERCASE, false);
	}

}
