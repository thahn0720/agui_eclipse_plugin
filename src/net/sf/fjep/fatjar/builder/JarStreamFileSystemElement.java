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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class JarStreamFileSystemElement extends AbstractFileSystemElement {

	private JarEntry jarEntry;
    private byte[] content;

	/**
	 * 
	 */
	public JarStreamFileSystemElement(JarInputStream jarIn, JarEntry jarEntry, String relFolder) {
		this.jarEntry = jarEntry;
		String entryName = jarEntry.getName();
		entryName = entryName.replace('/', File.separatorChar);
		folder = relFolder;
		String dir; 
		if (jarEntry.isDirectory()) {
			// cut last separatorChar
			dir = entryName.substring(0, entryName.length()-1);
			name = null;
		}
		else {
			int pos = entryName.lastIndexOf(File.separatorChar);
			if (pos == -1) {
				dir = "";
				name = entryName;
			} else {
				dir = entryName.substring(0, pos);
				name = entryName.substring(pos + 1);
			}
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int cnt = jarIn.read(buf);
                while (cnt > 0) {
                    baos.write(buf, 0, cnt);
                    cnt = jarIn.read(buf);
                }
                content = baos.toByteArray();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		if (relFolder.equals(""))
			folder = dir;
		else if (dir.equals(""))
			folder = relFolder;
		else
			folder = relFolder + File.separatorChar + dir;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#isFolder()
	 */
	public boolean isFolder() {
		return jarEntry.isDirectory();
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getSize()
	 */
	public long getSize() {
		return jarEntry.getSize();
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getStream()
	 */
	public InputStream getStream() {
		InputStream result = new ByteArrayInputStream(content);
		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#lastModificationTime()
	 */
	public long lastModificationTime() {
		return jarEntry.getTime();
	}

}
