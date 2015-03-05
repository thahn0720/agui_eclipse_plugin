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
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.fjep.fatjar.builder.ByteArrayFileSystemElement;
import net.sf.fjep.fatjar.builder.FatJarBuilder;
import net.sf.fjep.fatjar.builder.FileSystemSourceFilter;
import net.sf.fjep.fatjar.builder.IFileSystemSource;
import net.sf.fjep.fatjar.builder.JARFileSystemSource;
import net.sf.fjep.fatjar.builder.NativeFileSystemSource;
import net.sf.fjep.fatjar.builder.OneJarBuilder;
import net.sf.fjep.fatjar.builder.VirtualFileSystemSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;



public class FJBuildTask extends Task {

    private ArrayList sources;
    
    private String output;
    public  String  getOutput()               { return output; }
    public  void    setOutput(String output)  { this.output = output; }

    private boolean onejar = false;
    public  boolean getOnejar()               { return onejar; }
    public  void    setOnejar(boolean onejar)  { this.onejar = onejar; }

    private boolean escapeUCase = false;
    public  boolean getEscapeUCase()               { return escapeUCase; }
    public  void    setEscapeUCase(boolean escapeUCase)  { this.escapeUCase = escapeUCase; }

    private FJManifestType manifestType = null;
    private FJAutoJarType autojarType = null;
    
    public FJBuildTask() {
        sources = new ArrayList();
    }

    // The method executing the task
    public void execute() throws BuildException {
        try {
            if (onejar)
                doBuildOneJar();
            else
                doBuildFatJar();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("build '" + output + "' error: " + e.getMessage());
        }
    }

    private void doBuildFatJar() throws IOException {
        
        System.out.println("Build Output='" + output + "'");
        FatJarBuilder builder = new FatJarBuilder();
        builder.setEscapeUCase(escapeUCase);
        if (manifestType != null) {
            System.out.println(manifestType.toString());
            String manifestText = manifestType.getManifestText();
            VirtualFileSystemSource vsource = new VirtualFileSystemSource();
            vsource.add(new ByteArrayFileSystemElement("META-INF", "MANIFEST.MF", manifestText.getBytes()));
            builder.addSource(vsource);
            builder.addSource(manifestType.getHelperResources());
            builder.addConflictResolver(manifestType.getConflictResolver());
            builder.setRemoveSigners(manifestType.isRemovesigners());
        }
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof FJFileSourceType) {
                FJFileSourceType fjFileSource = (FJFileSourceType) o;
                System.out.println("adding native file system source '" + fjFileSource.getPath() + "'");
                File folder = new File(fjFileSource.getPath());
                String relPath = fjFileSource.getRelPath();
                IFileSystemSource fileSystemSource = new NativeFileSystemSource(folder, relPath);
                String[] excludes = fjFileSource.getExcludes();
                String[] excludesRX = fjFileSource.getExcludesRX();
                if (excludes.length + excludesRX.length > 0) {
                    System.out.println("excluding " + excludes.length + " entries and " + excludesRX.length + " patterns");
                    fileSystemSource = new FileSystemSourceFilter(fileSystemSource, excludes, excludesRX);
                }
                builder.addSource(fileSystemSource);
            }
            else if (o instanceof FJJarSourceType) {
                FJJarSourceType fjJarSource = (FJJarSourceType) o;
                System.out.println("adding jar file source '" + fjJarSource.getFile() + "'");
                String jarFilename = fjJarSource.getFile();
                String relPath = fjJarSource.getRelPath();
                IFileSystemSource fileSystemSource = new JARFileSystemSource(jarFilename, relPath);
                String[] excludes = fjJarSource.getExcludes();
                String[] excludesRX = fjJarSource.getExcludesRX();
                if (excludes.length + excludesRX.length > 0) {
                    System.out.println("excluding " + excludes.length + " entries and " + excludesRX.length + " patterns");
                    fileSystemSource = new FileSystemSourceFilter(fileSystemSource, excludes, excludesRX);
                }
                builder.addSource(fileSystemSource);
            }
        }
        if ((autojarType != null) && (autojarType.visitClasses != null) && (autojarType.visitClasses.size() > 0)) {
            int cntClasses = autojarType.visitClasses.size();
            String[] vclasses = new String[cntClasses];
            for (int i = 0; i < cntClasses; i++) {
                FJClassType fjClass = (FJClassType)autojarType.visitClasses.get(i);
                vclasses[i] = fjClass.getClassname().replace('.', '/');
            }
            builder.addVisitClasses(vclasses);
            builder.setSearchClassForName(autojarType.isSearchclassforname());
        }
        builder.build(output);
    }

    private void doBuildOneJar() throws IOException {
        
        System.out.println("Build One-Jar Output='" + output + "'");
        OneJarBuilder builder = new OneJarBuilder(output.replace('.', '_') + "_temp_eraseme");
        if (manifestType != null) {
            System.out.println(manifestType.toString());
            String manifestText = manifestType.getInnerOneJarManifestText();
            VirtualFileSystemSource vsource = new VirtualFileSystemSource();
            vsource.add(new ByteArrayFileSystemElement("META-INF", "MANIFEST.MF", manifestText.getBytes()));
            builder.addSource(vsource);
            builder.addConflictResolver(manifestType.getConflictResolver());
        }
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof FJFileSourceType) {
                FJFileSourceType fjFileSource = (FJFileSourceType) o;
                System.out.println("adding native file system source '" + fjFileSource.getPath() + "'");
                File folder = new File(fjFileSource.getPath());
                String relPath = fjFileSource.getRelPath();
                IFileSystemSource fileSystemSource = new NativeFileSystemSource(folder, relPath);
                String[] excludes = fjFileSource.getExcludes();
                String[] excludesRX = fjFileSource.getExcludesRX();
                if (excludes.length + excludesRX.length > 0) {
                    System.out.println("excluding " + excludes.length + " entries and " + excludesRX.length + " patterns");
                    fileSystemSource = new FileSystemSourceFilter(fileSystemSource, excludes, excludesRX);
                }
                builder.addSource(fileSystemSource);
            }
            else if (o instanceof FJJarSourceType) {
                FJJarSourceType fjJarSource = (FJJarSourceType) o;
                System.out.println("adding jar file source '" + fjJarSource.getFile() + "'");
                String jarFilename = fjJarSource.getFile();
                String relPath = fjJarSource.getRelPath();

// Bug-Fix reported by micrac [Bug 1268275]
//                VirtualFileSystemSource vsource = new VirtualFileSystemSource();
//                File f = new File(jarFilename);
//                vsource.add(new NativeFileSystemElement(f.getParentFile(), "lib", f.getName()));
//                builder.addSource(vsource);

                IFileSystemSource fileSystemSource = new JARFileSystemSource(jarFilename, relPath);
                String[] excludes = fjJarSource.getExcludes();
                String[] excludesRX = fjJarSource.getExcludesRX();
                if (excludes.length + excludesRX.length > 0) {
                    System.out.println("excluding " + excludes.length + " entries and " + excludesRX.length + " patterns");
                    fileSystemSource = new FileSystemSourceFilter(fileSystemSource, excludes, excludesRX);
                }
                builder.addSource(fileSystemSource);
            }
        }
        builder.setOnejarManifestText(manifestType.getOuterOneJarManifestText());
        builder.setOnejarHelperResource(manifestType.getHelperResources());
        builder.build(output);
    }

    public void addConfigured(FJFileSourceType source) {
        sources.add(source);
    }
    public void addConfigured(FJJarSourceType source) {
        sources.add(source);
    }
    public void addConfigured(FJManifestType manifestType) {
        this.manifestType = manifestType;
    }
    public void addConfigured(FJAutoJarType autojarType) {
        this.autojarType = autojarType;
    }
    
//    public static void main(String[] args) {
//
//        FJBuildTask fjBuild = new FJBuildTask();
//        fjBuild.setOutput("U:\\test.jar");
//        fjBuild.setOnejar(true);
//        
//        FJManifestType fjManifest = new FJManifestType();
//        fjManifest.setMainclass("mainb.MainB");
//        fjBuild.addConfigured(fjManifest);
//
//        FJFileSourceType fjFileClasses = new FJFileSourceType();
//        fjFileClasses.setPath("U:\\fatjar-tesprojects\\test-mainB\\classes");
//        fjBuild.addConfigured(fjFileClasses);
//
//        FJJarSourceType fjJarLibA = new FJJarSourceType();
//        fjJarLibA.setFile("U:\\fatjar-tesprojects\\test-mainB\\lib\\test-libA_fat.jar");
//        fjBuild.addConfigured(fjJarLibA);
//
//        FJJarSourceType fjJarLibB = new FJJarSourceType();
//        fjJarLibB.setFile("U:\\fatjar-tesprojects\\test-mainB\\lib\\test-libB_fat.jar");
//        fjBuild.addConfigured(fjJarLibB);
//
//        fjBuild.execute();
//    }

}