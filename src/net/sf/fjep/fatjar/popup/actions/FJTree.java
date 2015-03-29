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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FJTree {

	private FJTree parent;
	private int nodeType;
	private String displayName;
	private Object buildInfo;
	private ArrayList children;
	private int checkState;
	private boolean childrenRead;
	
	public static final int NT_ROOT            = 	            1 ;
	public static final int NT_FILE            =              2   ;
	public static final int NT_DIR             =            4     ;
	public static final int NT_JAR     	       =          8  +2   ;
	public static final int NT_PROJECT_OUTPUT  =       16  +4     ;
	public static final int NT_CLASSES         =    32     +4     ;
	public static final int NT_ADD_DIR         = 64        +4     ;

	public static final int CS_CHECKED 	       = 1;
	public static final int CS_UNCHECKED 	   = 2;
	public static final int CS_GRAYED          = 3;
	
	public FJTree(FJTree parent, int nodeType, String displayName, Object buildInfo, int checkState) {
		this.parent = parent;
		this.nodeType = nodeType;
		this.displayName = displayName;
		this.buildInfo = buildInfo;
		this.checkState = checkState;
		children = new ArrayList();
		childrenRead = false;
	}
	
	public void addChild(FJTree fjTree) {
		children.add(fjTree);
	}

	/**
	 * build info for NT_FILE und NT_DIR ist vom Type File
	 */
	public void addChild(int nodeType, String displayName, Object buildInfo, int checkState) {
		addChild(new FJTree(this, nodeType, displayName, buildInfo, checkState));
	}

	public boolean hasChildren() {
		
		boolean result = false;
		if (isType(NT_DIR) || isType(NT_ROOT)) {
			result = (children.size() > 0) || !childrenRead;
		}
		return result;
	}

	public boolean isType(int type) {
		return (nodeType & type) == type;
	}

	public Object[] getChildren() {
		if (!childrenRead)
			readChildren();
		Object[] result = (Object[]) children.toArray(new Object[children.size()]);
		return result;
	}

	public FJTree getParent() {
		return parent;
	}

	private void readChildren() {

		childrenRead = true;
		if (isType(NT_DIR)) {
			File f = (File)buildInfo;
			String[] filesAndDirs = f.list();
			if (filesAndDirs == null)
				filesAndDirs = new String[0];
			for (int i=0; i<filesAndDirs.length; i++) {
				String name = filesAndDirs[i];
				File fChild = new File(f, name);
				if (fChild.isDirectory()) {
					addChild(NT_DIR, name + "/", fChild, checkState);
				}
			}
			for (int i=0; i<filesAndDirs.length; i++) {
				String name = filesAndDirs[i];
				File fChild = new File(f, name);
				if (fChild.isFile()) {
					addChild(NT_FILE, name, fChild, checkState);
				}
			}
		}
	}

	public void setChecked(boolean checked) {
		if (checked)
			checkState = CS_CHECKED;
		else
			checkState = CS_UNCHECKED;
	}
	
	public int getCheckState() {
		return checkState;
	}
	
	public String toString() {
		return displayName;
	}

	public FJTree[] getReadChildren() {		
		return (FJTree[]) children.toArray(new FJTree[children.size()]);
	}

	public void setGrayChecked() {
		checkState = CS_GRAYED;
		
	}

	/**
	 * works only on NT_FILE and NT_DIR
	 * @return
	 */
	public String getAbsPath() {

		String result = null;
		if (isType(NT_FILE) || isType(NT_DIR)) {
			File f = (File) buildInfo;
			try {
				result = f.getCanonicalPath();
			} catch (IOException e) {}
		}
		return result;
	}

	public String getDisplayPath() {
		String result;
		if (isType(NT_ROOT))
			result = "<root>";
		else if (isType(NT_PROJECT_OUTPUT))
			result = displayName.replaceAll(".*'(.*)'.*", "<po|$1>");
		else if (isType(NT_CLASSES))
			result = displayName.replaceAll(".*'(.*)'.*", "<cl|$1>");
		else if (isType(NT_JAR))
			result = "<jar|" + displayName + ">";
		else if (isType(NT_ADD_DIR))
			result = displayName.replaceAll(".*'(.*)': (.*)", "<inc|$1/$2>");
		else
			result = parent.getDisplayPath() + "~" + displayName;
		return result;
	}
	
	public boolean isAguiResource() {
		return "res".equals(displayName);
	}
}
