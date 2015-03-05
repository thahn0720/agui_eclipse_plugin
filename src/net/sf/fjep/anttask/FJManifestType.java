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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.fjep.fatjar.builder.IConflictResolver;
import net.sf.fjep.fatjar.builder.IFileSystemElement;
import net.sf.fjep.fatjar.builder.IFileSystemSource;
import net.sf.fjep.fatjar.builder.JarStreamFileSystemSource;
import net.sf.fjep.fatjar.builder.ManifestConflictResolver;
import net.sf.fjep.fatjar.builder.VirtualFileSystemSource;
import net.sf.fjep.utils.FileUtils;



public class FJManifestType {

    private boolean mergemanifests = true;
    public boolean isMergemanifests()                     { return mergemanifests; }
    public void setMergemanifests(boolean mergemanifests) { this.mergemanifests = mergemanifests; }
    
    private boolean removesigners = true;
    public boolean isRemovesigners()                     { return removesigners; }
    public void setRemovesigners(boolean removesigners) { this.removesigners = removesigners; }
    
    private String mainclass = "";
    public String getMainclass()               { return mainclass; }
    public void setMainclass(String mainClass) { this.mainclass = mainClass; }

    private String classpath = "";
    public String getClasspath()               { return classpath; }
    public void setClassPath(String classpath) { this.classpath = classpath; }

    private String arguments = "";
    public String getArguments()               { return arguments; }
    public void setArguments(String arguments) { this.arguments = arguments; }

    private String vmarguments = "";
    public String getVmarguments()               { return vmarguments; }
    public void setVmarguments(String vmarguments) { this.vmarguments = vmarguments; }

    private String manifestfile = "";
    public String getManifestfile()                  { return manifestfile; }
    public void setManifestfile(String manifestfile) { this.manifestfile = manifestfile; }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("manifest[");
        if (mergemanifests)
            result.append("merge,");
        if (removesigners)
            result.append("removesigners,");
        if (!manifestfile.equals("")) {
            result.append("file='");
            result.append(manifestfile);
            result.append("',");
        }
        else {
            if (!mainclass.equals("")) {
                result.append("main='");
                result.append(mainclass);
                result.append("',");
            }
            if (!classpath.equals("")) {
                result.append("classpath='");
                result.append(classpath);
                result.append("',");
            }
            if (!arguments.equals("")) {
                result.append("arguments='");
                result.append(arguments);
                result.append("',");
            }
            if (!vmarguments.equals("")) {
                result.append("vmarguments='");
                result.append(vmarguments);
                result.append("',");
            }
        }
        result.setCharAt(result.length()-1, ']');
        return result.toString();
    }

    public IConflictResolver getConflictResolver() {
        ManifestConflictResolver resolver = new ManifestConflictResolver(mergemanifests);
        return resolver;
    }

    /**
     * @return empty source or source with one element "/net/sf/fjep/fatjar/bootstub/MainStub.class" if needed
     */
    public IFileSystemSource getHelperResources() {
        VirtualFileSystemSource vsource = new VirtualFileSystemSource();
        boolean needMainStub = (mainclass != null) && ((arguments != null)||(vmarguments != null)) && !mainclass.trim().equals("") && (!arguments.trim().equals("") || !vmarguments.trim().equals("")); 
        if (needMainStub) {
            InputStream in = this.getClass().getResourceAsStream("boot-stub.jar");
            if (in == null) {
                System.err.println("ERROR getting resource boot-stub.jar");
            }
            else {
                try {
                    JarStreamFileSystemSource src = new JarStreamFileSystemSource(in, "boot-stub.jar", "");
                    while(src.hasMoreElements()) {
                        IFileSystemElement elem = src.nextElement();
                        vsource.add(elem);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return vsource;
    }

    public String getManifestText() {
        String result = "";
        if (!manifestfile.equals("")) {
            result = FileUtils.readContent(new File(manifestfile));
        }
        else {
            result = "Manifest-Version: 1.0\r\n";
            result += "Created-By: Fat Jar Eclipse Plug-In\r\n";
            if ((mainclass != null) && !mainclass.trim().equals("")) {
                if (((arguments != null) && !arguments.trim().equals("")) || ((vmarguments != null) && !vmarguments.trim().equals(""))) { 
                    result += "Main-Class: net.sf.fjep.fatjar.bootstub.MainStub\r\n";
                    result += "Main-Class2: " + mainclass + "\r\n";
                    result += "Program-Arguments: " + arguments + "\r\n";
                    result += "VM-Arguments: " + vmarguments + "\r\n";
                }
                else {
                    result += "Main-Class: " + mainclass + "\r\n";
                }
            }
            if ((classpath != null) && !classpath.trim().equals("")) 
                result += "Class-Path: " + classpath + "\r\n";
            result += "\r\n";
        }
        return result;
    }

    public String getInnerOneJarManifestText() {
        String result = "";
        if (!manifestfile.equals("")) {
            result = FileUtils.readContent(new File(manifestfile));
        }
        else {
            result = "Manifest-Version: 1.0\r\n";
            result += "Created-By: Fat Jar/One-JAR Eclipse Plug-In\r\n";
            result += "Main-Class: " + mainclass + "\r\n";
            result += "\r\n";
        }
        return result;
    }

    public String getOuterOneJarManifestText() {
        String result = "";
        if (!manifestfile.equals("")) {
            result = FileUtils.readContent(new File(manifestfile));
        }
        else {
            result = "Manifest-Version: 1.0\r\n";
            result += "Created-By: Fat Jar/One-JAR Eclipse Plug-In\r\n";
            if ((mainclass != null) && !mainclass.trim().equals("")) {
                if (((arguments != null) && !arguments.trim().equals("")) || ((vmarguments != null) && !vmarguments.trim().equals(""))) { 
                    result += "Main-Class: net.sf.fjep.fatjar.bootstub.MainStub\r\n";
                    result += "Main-Class2: com.simontuffs.onejar.Boot\r\n";
                    result += "Program-Arguments: " + arguments + "\r\n";
                    result += "VM-Arguments: " + vmarguments + "\r\n";
                }
                else {
                    result += "Main-Class: com.simontuffs.onejar.Boot\r\n";
                }
            }
            if ((classpath != null) && !classpath.trim().equals("")) 
                result += "Class-Path: " + classpath + "\r\n";
            result += "\r\n";
        }
        return result;
    }

}
