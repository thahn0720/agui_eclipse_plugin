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
package net.sf.fjep.fatjar.popup.actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class ManifestData {

	private Vector  manifestMainSection; 
	private Vector  manifestIndividualSection; 

	class ManifestLine {
		private String key;
		private String value;
		String getKey() { return key; }
		String getValue() { return value; }
		ManifestLine(String key, String value) { this.key=key; this.value=value; }
		ManifestLine(String line) {
			// FIX Problem with c-string like ending
			if (line.endsWith("\0"))
				line = line.substring(0, line.length()-1);
			int posColon = line.indexOf(':');
			if (posColon != -1) {
				key = line.substring(0, posColon).trim();
				value = line.substring(posColon+1).trim();
			}
			else {
				key = "#";
				value = line;
			}
		}
		public boolean isEmptyLine() { return ( key.equals("#") && value.trim().equals("") ); }
		public String toString() {
			String result;
			if (key.equals("#"))
					result = value + "\r\n";
			else
					result = key + ": " + value + "\r\n";
			return result;
		}
	}
	
	public ManifestData() {
		manifestMainSection = new Vector();
		manifestIndividualSection = new Vector();
	}

	/**
	 * replace manifestMainSection with main-section from file and 
	 * add individual section from file to manifestIndividualSection
	 * @param filename
	 */
	public void addFile(String filename) {
		try {
			clearMainSection();
			InputStream is = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			boolean isMainSection = true;
			String line = br.readLine();
			while (line != null) {
				ManifestLine ml = new ManifestLine(line);
				if (ml.isEmptyLine())
					isMainSection = false;
				if (isMainSection)
					manifestMainSection.add(ml);
				else
					manifestIndividualSection.add(ml);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i=0; i<manifestMainSection.size(); i++) {
			ManifestLine ml = (ManifestLine) manifestMainSection.get(i);
			result.append(ml.toString());
		}
		// the first line in indiv section is always empty, so no .append("\r\n") here
		for (int i=0; i<manifestIndividualSection.size(); i++) {
			ManifestLine ml = (ManifestLine) manifestIndividualSection.get(i);
			result.append(ml.toString());
		}
		result.append("\r\n");
		return result.toString();
	}

	public void clearMainSection() {
		manifestMainSection.clear();
	}
	public void addMainSectionLine(String line) {
		manifestMainSection.add(new ManifestLine(line));
	}
	public void addIndividualSectionLine(String line) {
		manifestIndividualSection.add(new ManifestLine(line));
	}

}
