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

/**
 * implements a one element read ahead queue for IFileSystemSource
 */
public abstract class AbstractFileSystemSource implements IFileSystemSource {

	protected String description; 
	private IFileSystemElement next = null;
	
	/**
     * @return short text describing this FileSystemSource
     * for informational purpose only 
     */
	public String getDescription() {
		return description;
	}

    /**
     * read one element ahead.
     * @return <code>true</code> if the next element is not null
     */
	public boolean hasMoreElements() {
		if (next == null)
			next = nextUnqueuedElement();
		boolean result = (next != null);
		return result;
	}
	
	/**
     * @return return next element from queue.
     * if queue is empty return result of nextUnqueuedElement()
     */
	public IFileSystemElement nextElement() {
		IFileSystemElement result = next;
		next = null;
		if (result == null)
			result = nextUnqueuedElement();
		return result;
	}

	/**
	 * this method returns the next unqueued element.
	 * if there are no more elements null has to be returned.
	 * hasMoreElements() and nextElement() are based on this
	 * abstract method.
	 */
	protected abstract IFileSystemElement nextUnqueuedElement();
				  
}
