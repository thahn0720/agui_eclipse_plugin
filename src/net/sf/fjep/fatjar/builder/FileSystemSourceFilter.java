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

/**
 * Exclude files and folders from any IFilesSystemSource.
 * An one element read-ahead (next) is done in hasMoreElements().
 */
public class FileSystemSourceFilter implements IFileSystemSource {

	private IFileSystemSource source;
	private IFileSystemElement next;
    private String[] excludes = null;
    private String[] excludeFolders = null;
    private String[] rxExcludes = null;
	
	/**
	 * filter out all files and folder listed in excludes.
	 * excludes can not contain any wildcards and must be the full
	 * (relative) path, e.g. "vpath/work" to exclude the folder "vpath/work"
	 * @param source the FileSystemSource to filter
	 * @param excludes allow "/" or "\\" as separator-char. 
	 *        Empty array and null mean unchanged source
	 */
	public FileSystemSourceFilter(IFileSystemSource source, String[] excludes, String[] rxExcludes) {
		this.source = source;
		if ((excludes != null) && (excludes.length>0)) {
			this.excludes = new String[excludes.length];
            this.excludeFolders = new String[excludes.length];
			for (int i = 0; i < excludes.length; i++) {
                String exclude = excludes[i].replace('/', File.separatorChar).replace('\\', File.separatorChar);
				this.excludes[i] = exclude;
				if (exclude.endsWith(File.separator)) {
                    this.excludeFolders[i] = exclude;
                }
                else {
                    this.excludeFolders[i] = exclude + File.separatorChar;
                }
			}
		}
        if ((rxExcludes != null) && (rxExcludes.length>0)) {
            this.rxExcludes = rxExcludes;
        }
		next = null;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemSource#getDescription()
	 */
	public String getDescription() {
		String result = source.getDescription();
		if (excludes != null)
			result += " (filtered " + Integer.toString(excludes.length) + " elements)";
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemSource#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		if (next == null)
			next = nextElement();
		return next != null;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemSource#nextElement()
	 */
	public IFileSystemElement nextElement() {
		
		if (next == null) {
			while (source.hasMoreElements()) {
				IFileSystemElement element = source.nextElement();
				if (checkNotExcluded(element)) {
					next = element;
					break;
				}
			}
		}
		IFileSystemElement result = next;
		next = null;
		return result;
	}

	/**
	 * return true if fileSystemElement (or any parent folder) is not in excludes
	 * @param fileSystemElement
	 * @return
	 */
	private boolean checkNotExcluded(IFileSystemElement fileSystemElement) {

		boolean result=true;
		String path = fileSystemElement.getFullName();
        if ((result == true) && (rxExcludes != null)) {
            for (int i = 0; i < rxExcludes.length; i++) {
                String rxExclude = rxExcludes[i];
                if (path.matches(rxExclude)) {
                    result=false;
                    break;
                }
            }
        }
        if ((result == true) && (excludes != null)) {
            for (int i = 0; i < excludes.length; i++) {
                String exclude = excludes[i];
                if (path.equals(excludes[i])) {
                    result=false;
                    break;
                }
                if (path.startsWith(excludeFolders[i])) {
                    result=false;
                    break;
                }
            }
        }
		return result;
	}

	
}
