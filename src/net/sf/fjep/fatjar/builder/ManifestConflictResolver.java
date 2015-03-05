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

import net.sf.fjep.utils.FileUtils;


public class ManifestConflictResolver implements IConflictResolver {

    private boolean mergeManifests;
    
    
    public ManifestConflictResolver(boolean mergeManifests) {
        this.mergeManifests = mergeManifests;
    }
    
    /**
     * merge old manifest-body into new manifest
     */
    public boolean handleConflict(File conflictOutputFile, IFileSystemElement fileSystemElement) {
        
        boolean resolved = false;
        if (conflictOutputFile.getName().equalsIgnoreCase("MANIFEST.MF") || conflictOutputFile.getName().equalsIgnoreCase("^M^A^N^I^F^E^S^T.^M^F")) {
//            System.out.println("merging '" + conflictOutputFile + "' new element='" + fileSystemElement + "'");
            String oldManifest = FileUtils.readContent(conflictOutputFile);
//            System.out.println("oldManifest=[" + oldManifest + "]");
            String newManifest = FileUtils.readContent(fileSystemElement.getStream());
//            System.out.println("newManifest=[" + newManifest + "]");
            String mergeManifest = mergeManifests(oldManifest, newManifest);
//            System.out.println("mergeManifest=[" + mergeManifest + "]");
            FileUtils.writeToFile(conflictOutputFile, mergeManifest);
            resolved = true;
        }
        return resolved;
    }

    /**
     * @param oldManifest
     * @param newManifest
     * @return
     */
    private String mergeManifests(String oldManifest, String newManifest) {
        String result;
        if (!mergeManifests) {
            result = oldManifest;
        }
        else {
            String newIndividualSection = getIndividualSection(newManifest);
            result = oldManifest;
            if (!oldManifest.endsWith("\r\n")) {
                result += "\r\n\r\n";
            } else if (!oldManifest.endsWith("\r\n\r\n")) {
                result += "\r\n";
            }
            result += newIndividualSection;
        }
        return result;
    }

    /**
     * @param manifestText
     * @return
     */
    private String getIndividualSection(String manifestText) {
        String result = "";
        int lastpos = 0;
        int pos = manifestText.indexOf('\n', lastpos);
        while (pos >0) {
            String line = manifestText.substring(lastpos, pos);
            if (line.trim().equals("")) {
                result = manifestText.substring(pos+1);
                break;
            }
            lastpos = pos+1;
            pos = manifestText.indexOf('\n', lastpos);
        }
        return result;
    }

}
