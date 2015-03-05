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

import java.util.ArrayList;


public class VirtualFileSystemSource extends AbstractFileSystemSource {

    private ArrayList elements;
    
    private int nextElementIndex = 0; 
    
    public VirtualFileSystemSource() {
        elements = new ArrayList();
    }
    
    public void add(IFileSystemElement element) {
        elements.add(element);
    }
    
    /* (non-Javadoc)
     * @see net.sf.fjep.fatjar.builder.AbstractFileSystemSource#nextUnqueuedElement()
     */
    protected IFileSystemElement nextUnqueuedElement() {
        IFileSystemElement result = null;
        if (nextElementIndex < elements.size()) {
            result = (IFileSystemElement) elements.get(nextElementIndex);
            nextElementIndex += 1;
        }
        return result;
    }

}
