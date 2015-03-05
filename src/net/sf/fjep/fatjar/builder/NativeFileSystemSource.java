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
import java.util.Enumeration;
import java.util.Stack;

/**
 * enumerate over a file/folder structure
 */
public class NativeFileSystemSource extends AbstractFileSystemSource {

	private String relFolder;
	private Stack folderEnumStack;
	private FolderEnum currentFolderEnum;
	private Stack relFolderStack;
	private String currentRelFolder;
	private Stack folderStack;
	private File folder;
    private Stack elementStack;

	/**
	 * enumerate over the elements in a folder
	 */
	private class FolderEnum implements Enumeration {
		
		private String[] folderInfo;
		private int index;
        public FolderEnum(File folder) {
            folderInfo = folder.list();
            index = 0;
        }       
        public FolderEnum(String[] folderInfo) {
            this.folderInfo = folderInfo;
            index = 0;
        }       
		public boolean hasMoreElements() {
			boolean result = false;
			if (folderInfo != null)
				result = (index < folderInfo.length);
			return result;
		}
		public Object nextElement() {
			String result = folderInfo[index];
			index += 1;
			return result;
		}
		
		public String toString() {
			StringBuffer result = new StringBuffer();
			for (int i=0; i<folderInfo.length; i++) {
				if (i>0)
					result.append(',');
				if (i==index)
					result.append("(*)");
				result.append(folderInfo[i]);
			}
			return result.toString();
		}
}
	
	
	/**
	 * 
	 */
	public NativeFileSystemSource(File folder, String relFolder) {

        this.folder = folder;
		this.relFolder = relFolder;
		description = "native " + relFolder;
		folderEnumStack = new Stack();
		currentFolderEnum = new FolderEnum(folder);
		relFolderStack = new Stack();
		currentRelFolder = relFolder;
		folderStack = new Stack();
        
        if (folder.isFile()) {
            System.out.println("SPECIAL handling for file instead of folder!");
            String name = folder.getName();
            if (relFolder.equals(name)) {
                this.relFolder = "";
                this.currentRelFolder = this.relFolder; 
            }
            else if (relFolder.replace('\\', '/').endsWith('/' + name)) {
                this.relFolder = relFolder.substring(0, relFolder.replace('\\', '/').lastIndexOf('/'));
                this.currentRelFolder = this.relFolder; 
            }
            this.folder = folder.getParentFile();
            currentFolderEnum = new FolderEnum(new String[]{name});
        }

	}


	public IFileSystemElement nextUnqueuedElement() {
		
		IFileSystemElement result = null;
		String name = null;
		if (currentFolderEnum.hasMoreElements())
			name = (String) currentFolderEnum.nextElement();
		while ((name == null) && !folderStack.isEmpty()) {
			currentRelFolder = (String) relFolderStack.pop();
			folder = (File) folderStack.pop(); 
			currentFolderEnum = (FolderEnum) folderEnumStack.pop(); 
			if (currentFolderEnum.hasMoreElements())
				name = (String) currentFolderEnum.nextElement();
		}
		if (name != null) {
			File f = new File(folder, name);
			if (f.isDirectory()) { 
				folderEnumStack.push(currentFolderEnum);
				currentFolderEnum = new FolderEnum(f);
				folderStack.push(folder);
				folder = f;
				relFolderStack.push(currentRelFolder);
				if (currentRelFolder.equals(""))
					currentRelFolder = name;
				else
					currentRelFolder += File.separatorChar + name;  
				if (currentFolderEnum.hasMoreElements())
					result = nextUnqueuedElement();
				else
					result = new NativeFileSystemElement(folder, currentRelFolder, null);
			}
			else {
				result = new NativeFileSystemElement(folder, currentRelFolder, name);
			}
		}
		return result;
	}

}
