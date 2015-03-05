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
package net.sf.fjep.anttask;

import java.util.ArrayList;


public class FJFileSourceType {

    private ArrayList excludes;
    private ArrayList excludesRX;

    public FJFileSourceType() {
        excludes = new ArrayList();
        excludesRX = new ArrayList();
    }

    private String path;
    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}

    private String relPath = "";
    public String getRelPath() {return relPath;}
    public void setRelPath(String relPath) {this.relPath = relPath;}

    public void addConfigured(FJExcludeType exclude) {
        if (!exclude.getRelPath().equals("")) {
            excludes.add(exclude.getRelPath());
        }
        if (!exclude.getRegexp().equals("")) {
            excludesRX.add(exclude.getRegexp());
        }
    }
    public String[] getExcludes() {
        String[] result = (String[]) excludes.toArray(new String[excludes.size()]);
        return result;
    }

    public String[] getExcludesRX() {
        String[] result = (String[]) excludesRX.toArray(new String[excludesRX.size()]);
        return result;
    }

    
}
