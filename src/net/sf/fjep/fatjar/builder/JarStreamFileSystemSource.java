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
package net.sf.fjep.fatjar.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * enumerate over a file/folder structure
 */
public class JarStreamFileSystemSource extends AbstractFileSystemSource {

	private String jarName;
	private JarInputStream jarIn;
	private String relFolder;
	
	/**
	 * @throws IOException
	 */
	public JarStreamFileSystemSource(InputStream inStream, String jarName, String relFolder) throws IOException  {

		this.jarName = jarName;
		this.relFolder = relFolder;
		description = "jar-stream " + jarName  + " " + relFolder;
		jarIn = new JarInputStream(inStream);
	}

	public IFileSystemElement nextUnqueuedElement() {

		IFileSystemElement result = null;
        try {
            JarEntry jarEntry = jarIn.getNextJarEntry();
            if (jarEntry != null) {
    			result = new JarStreamFileSystemElement(jarIn, jarEntry, relFolder);
    		}
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;
	}

    public String getJarName() {
        return jarName;
    }
}
