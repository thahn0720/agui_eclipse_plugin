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

import java.io.InputStream;

/**
 * proxy class for any IFileSystemElement doing Upper-Case escaping
 */
public class UCaseFileSystemElement implements IFileSystemElement {

	private IFileSystemElement element;
	private boolean escape;

	public UCaseFileSystemElement(IFileSystemElement element, boolean escape) {
		this.element = element;
		this.escape = escape;
	}
	
	private String transform(String path) {
		String result;
		if (escape) {
			result = escapeUCase(path);
		}
		else {
			result = unescapeUCase(path);
		}
		return result;
	}
	
	private String escapeUCase(String unescapedPath) {
		String escapedPath = unescapedPath.replaceAll("([A-Z^])", "^$1");
		return escapedPath;
	}
	
	private String unescapeUCase(String escapedPath) {
		String unescapedPath = escapedPath.replaceAll("[\\^]([A-Z^])", "$1");
		return unescapedPath;
	}
	
	public String getFolder() {
		return transform(element.getFolder());
	}
	public String getFullName() {
		return transform(element.getFullName());
	}
	public String getName() {
		return transform(element.getName());
	}
	public long getSize() {
		return element.getSize();
	}
	public InputStream getStream() {
		return element.getStream();
	}
	public boolean isFolder() {
		return element.isFolder();
	}
	public long lastModificationTime() {
		return element.lastModificationTime();
	}
	public String toString() {
		return super.toString();
	}
}
