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
import java.io.InputStream;



public class ByteArrayFileSystemElement extends AbstractFileSystemElement {

    byte[] content;
    
    public ByteArrayFileSystemElement(String folder, String name, byte[] content) {
        this.folder = folder;
        this.name = name;
        this.content = content;
    }

    /* (non-Javadoc)
     * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getSize()
     */
    public long getSize() {
        return content.length;
    }
    /* (non-Javadoc)
     * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getStream()
     */
    public InputStream getStream() {
        InputStream in = new ByteArrayInputStream(content);
        return in;
    }
    
}
