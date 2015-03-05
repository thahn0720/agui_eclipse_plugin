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
package net.sf.fjep.fatjar.extensionpoints;

import java.io.File;
import java.io.IOException;

import net.sf.fjep.fatjar.builder.FileSystemSourceFilter;
import net.sf.fjep.fatjar.builder.IFileSystemSource;
import net.sf.fjep.fatjar.builder.IJarBuilder;
import net.sf.fjep.fatjar.builder.JARFileSystemSource;
import net.sf.fjep.fatjar.builder.JarBuilder;
import net.sf.fjep.fatjar.builder.NativeFileSystemSource;


public class JarUtilFactory implements IJarUtilFactory {

        public IJarBuilder createJarBuilder(String jarFileName) {
                return new JarBuilder(jarFileName);
        }

        public IFileSystemSource createJARFileSystemSource(String jarFilename, String relFolder) {

                IFileSystemSource result = null;
                try {
                        result = new JARFileSystemSource(jarFilename, relFolder);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return result;
        }
        
        public IFileSystemSource createNativeFileSystemSource(File folder, String relFolder) {

                IFileSystemSource result = new NativeFileSystemSource(folder, relFolder);
                return result;
        }

        /* (non-Javadoc)
         * @see net.sf.fjep.fatjar.extensionpoints.IJarUtilFactory#createFileSystemSourceFilter(net.sf.fjep.fatjar.builder.IFileSystemSource, java.lang.String[])
         */
        public IFileSystemSource createFileSystemSourceFilter(IFileSystemSource source, String[] excludes, String[] regexpExcludes) {

                IFileSystemSource result = new FileSystemSourceFilter(source, excludes, regexpExcludes);
                return result;
        }
        
}
