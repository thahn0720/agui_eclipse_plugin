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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import net.sf.fjep.utils.FileUtils;

public class JarBuilder implements IJarBuilder {

	private JarOutputStream jarro = null;
	byte[] syncBuffer = null;
	
	public JarBuilder(String jarFileName) {

		try {
			syncBuffer = new byte[8192];
			FileUtils.mkParentDirs(jarFileName);
			FileOutputStream out = new FileOutputStream(jarFileName);
			jarro = new JarOutputStream(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * synchronized for using syncBuffer
	 * @param f file to add
	 * @param relName relative name inside jar 
	 */
	public synchronized void add(File f, String relName) {

		try {
			if (f.isDirectory()) {
				String dirName = relName.replace(File.separatorChar, '/');
				if (!dirName.endsWith("/"))
					dirName += "/";
				JarEntry entry = new JarEntry(dirName);
				jarro.putNextEntry(entry);
				jarro.closeEntry();
			}
			else {
				JarEntry entry = new JarEntry(relName.replace(File.separatorChar, '/'));
				entry.setSize(f.length());
				entry.setTime(f.lastModified());
				jarro.putNextEntry(entry);
				FileInputStream in = new FileInputStream(f);
				int cnt = in.read(syncBuffer);  
				while (cnt > 0) {
					jarro.write(syncBuffer, 0, cnt);
					cnt = in.read(syncBuffer);  
				}
				jarro.closeEntry();
				in.close();
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * synchronized for using syncBuffer
     * @param f file to add
     * @param relName relative name inside jar 
     */
    public synchronized void add(byte[] bytes, String relName) {

        try {
            JarEntry entry = new JarEntry(relName.replace(File.separatorChar, '/'));
            entry.setSize(bytes.length);
            jarro.putNextEntry(entry);
            jarro.write(bytes);
            jarro.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * synchronized for using syncBuffer
	 */
	public synchronized void add(IFileSystemElement src) {

		try {
			if (src.isFolder()) {
				String dirName = src.getFolder().replace(File.separatorChar, '/') + '/';
				JarEntry entry = new JarEntry(dirName);
				jarro.putNextEntry(entry);
				jarro.closeEntry();
			}
			else {
				String relName;
				if (src.getFolder().equals(""))
					relName = src.getName();
				else
					relName = src.getFolder() + File.separatorChar + src.getName();
				JarEntry entry = new JarEntry(relName.replace(File.separatorChar, '/'));
				entry.setSize(src.getSize());
				entry.setTime(src.lastModificationTime());
				jarro.putNextEntry(entry);
				InputStream in = src.getStream();
				int cnt = in.read(syncBuffer);  
				while (cnt > 0) {
					jarro.write(syncBuffer, 0, cnt);
					cnt = in.read(syncBuffer);  
				}
				jarro.closeEntry();
				in.close();
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * synchronized for using syncBuffer
	 */
	public synchronized void add(JarEntry srcEntry, InputStream in) {

		try {
			JarEntry entry = new JarEntry(srcEntry);
			if (srcEntry.isDirectory()) {
				jarro.putNextEntry(entry);
				jarro.closeEntry();
			}
			else {
				jarro.putNextEntry(entry);
				int cnt = in.read(syncBuffer);  
				while (cnt > 0) {
					jarro.write(syncBuffer, 0, cnt);
					cnt = in.read(syncBuffer);  
				}
				jarro.closeEntry();
				in.close();
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		
		try {
			jarro.flush();
			jarro.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
