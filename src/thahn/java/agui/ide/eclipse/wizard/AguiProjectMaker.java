package thahn.java.agui.ide.eclipse.wizard;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

import thahn.java.agui.ide.eclipse.preferences.AguiPreferenceConstants;
import thahn.java.agui.ide.eclipse.preferences.AguiPrefs;
import thahn.java.agui.ide.eclipse.project.AguiNature;
import thahn.java.agui.ide.eclipse.wizard.template.ResourceIndicator;

public class AguiProjectMaker {
	
	public static final String												START_SEPARATOR						=   "${";
	public static final String												END_SEPARATOR						=   "}";
	public static final int													MF_PACKAGE_NAME_HASH 				= 	"${package}".hashCode();
	public static final int													MF_MAIN_ACTIVITY_NAME_HASH			= 	"${mainActivityName}".hashCode();
	public static final int													STRING_APP_NAME_HASH				= 	"${app_name}".hashCode();
	public static final String												MF_PACKAGE_NAME 					= 	"\\$\\{package\\}";
	public static final String												MF_MAIN_ACTIVITY_NAME 				= 	"\\$\\{mainActivityName\\}";
	public static final String												STRING_APP_NAME 					= 	"\\$\\{app_name\\}";
	
	private String															mApplicationName;
	private String															mPackageName;
	private String															mActivityName;
	
	public AguiProjectMaker(String mApplicationName, String mPackageName, String mActivityName) {
		this.mApplicationName = mApplicationName;
		this.mPackageName = mPackageName;
		this.mActivityName = mActivityName;
	}

	/**
	 * This creates the project in the workspace.
	 * 
	 * @param description
	 * @param projectHandle
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	void createProject(IProject proj,
			IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription description = workspace.newProjectDescription(proj.getName());
			//
			monitor.beginTask("", 2000);
			proj.create(description, new SubProgressMonitor(monitor, 10));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			proj.open(IResource.BACKGROUND_REFRESH, monitor);
			description.setLocationURI(proj.getLocationURI());
			AguiNature.addNatureToProjectDescription(proj, JavaCore.NATURE_ID, monitor);
			AguiNature.addNatureToProjectDescription(proj, AguiNature.NATURE_ID, monitor);
			AguiNature.configureBuilder(proj);
			//
			IJavaProject javaProject = JavaCore.create(proj);
			// ref agui lib
			String sdkLibLocation = AguiPrefs.getInstance().getSdkJarLocation();
            IClasspathEntry aguiLib = JavaCore.newLibraryEntry(new Path(sdkLibLocation), null, null);
			// ref java lib
			HashSet<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
			entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
			entries.add(JavaRuntime.getDefaultJREContainerEntry());
			entries.add(aguiLib);
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
			//
			monitor.worked(1000);
			//
			createAguiProject(javaProject, monitor);
			//
			monitor.worked(1000);
//			populate("src", container, proj);
			setupSourceFolders(javaProject, new String[]{"src", "gen"}, monitor);
			//
		} catch (Exception ioe) {
			ioe.printStackTrace();
			IStatus status = new Status(IStatus.ERROR, "NewFileWizard", IStatus.OK, ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}
	
	private void createAguiProject(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException, IOException {
		IProject proj = javaProject.getProject();
		IContainer container = (IContainer) proj;
		//
		createFolder(container, "src", monitor);
		createSrcPakcage(proj, "src/", mPackageName, mActivityName, monitor);
		createFolder(container, "gen", monitor);
		createSrcPakcage(proj, "gen/", mPackageName, mActivityName, monitor);
		javaProject.setOutputLocation(createFolder(container, "bin", monitor).getFullPath(), monitor);
		//
		createFolder(container, "res", monitor);
		createFolder(container, "res/drawable", monitor);
		createFolder(container, "res/drawable-hdpi", monitor);
		createFolder(container, "res/layout", monitor);
		createFolder(container, "res/values", monitor);
		//
		BufferedInputStream activityBis = new BufferedInputStream(copyTemplate("ActivityTemplate.txt"));
		addFileToProject(container, new Path("/src/"+mPackageName.replace(".", "/")+"/"+mActivityName+".java"), activityBis, monitor);
		activityBis.close();
		//
		BufferedInputStream manifestBis = new BufferedInputStream(copyTemplate("AguiManifestTemplate.xml"));//copyManifest(mPackageName, mActivityName));
		addFileToProject(container, new Path("AguiManifest.xml"), manifestBis, monitor);
		manifestBis.close();
		//
		BufferedInputStream icBis = new BufferedInputStream(AguiPlugin.getBundleAbsolutePath("icons/ic_launcher.png").openStream());
		addFileToProject(container, new Path("res/drawable-hdpi/ic_launcher.png"), icBis, monitor);
		icBis.close();
		//
		BufferedInputStream stringBis = new BufferedInputStream(copyTemplate("stringsTemplate.xml"));//copyValues(mApplicationName, mPackageName, mActivityName));
		addFileToProject(container, new Path("res/values/string.xml"), stringBis, monitor);
		stringBis.close();
		//
		BufferedInputStream layoutBis = new BufferedInputStream(copyTemplate("mainTemplate.xml"));//copyFile("mainTemplate.xml"));
		addFileToProject(container, new Path("res/layout/main.xml"), layoutBis, monitor);
		layoutBis.close();
		//
		BufferedInputStream buildConfigBis = new BufferedInputStream(copyTemplate("BuildConfigTemplate.txt"));//copyFile("mainTemplate.xml"));
		addFileToProject(container, new Path("gen/"+mPackageName.replace(".", "/")+"/BuildConfig.java"), buildConfigBis, monitor);
		buildConfigBis.close();
		//
//		proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}
	
	private IFolder createFolder(IContainer container, String folderName, IProgressMonitor monitor) throws CoreException {
		IFolder srcFolder = container.getFolder(new Path(folderName));
		srcFolder.create(true, true, monitor);
		return srcFolder;
	}
	
	private void createSrcPakcage(IProject proj, String prefix, String packageName, String activityName, IProgressMonitor monitor) throws CoreException {
		IFolder pkgFolder = proj.getFolder(prefix);
		if (packageName.contains(".")) {
		    String[] components = packageName.replace(".", "/").split("/");
	        for (String component : components) {
	            pkgFolder = pkgFolder.getFolder(component);
	            if (!pkgFolder.exists()) {
	                pkgFolder.create(true /* force */, true /* local */,
	                        new SubProgressMonitor(monitor, 10));
	            }
	        }
		} else {
			pkgFolder = pkgFolder.getFolder(packageName);
            if (!pkgFolder.exists()) {
                pkgFolder.create(true /* force */, true /* local */,
                        new SubProgressMonitor(monitor, 10));
            }
		}
	}
	
	/**
	 * Adds a new file to the project.
	 * 
	 * @param container
	 * @param path
	 * @param contentStream
	 * @param monitor
	 * @throws CoreException
	 */
	private void addFileToProject(IContainer container, Path path, InputStream contentStream, IProgressMonitor monitor) throws CoreException {
		final IFile file = container.getFile(path);

		if (file.exists()) {
			file.setContents(contentStream, true, true, monitor);
		} else {
			file.create(contentStream, true, monitor);
		}
	}
	
	private InputStream copyTemplate(String template) throws CoreException {
		return copyTemplate(ResourceIndicator.class.getResourceAsStream(template));
	}
	
	private InputStream copyTemplate(InputStream input) throws CoreException {
		StringBuilder sb = new StringBuilder();
		try {
			/* We want to be truly OS-agnostic */
			String line;
			final String newline = System.getProperty("line.separator");
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			try {
				while ((line = reader.readLine()) != null) {
					if (line.contains(START_SEPARATOR)) {
						int start = line.indexOf(START_SEPARATOR);
						int end = line.indexOf(END_SEPARATOR)+1;
						int which = line.substring(start, end).hashCode();
						if (which == MF_PACKAGE_NAME_HASH) {
							line = line.replaceAll(AguiProjectMaker.MF_PACKAGE_NAME, mPackageName);
						} else if(which == MF_MAIN_ACTIVITY_NAME_HASH) {
							line = line.replaceAll(AguiProjectMaker.MF_MAIN_ACTIVITY_NAME, mActivityName);
						} else if(which == STRING_APP_NAME_HASH) {
							line = line.replaceAll(AguiProjectMaker.STRING_APP_NAME, mApplicationName);
						}
					} 
					sb.append(line);
					sb.append(newline);
				}
			} finally {
				reader.close();
			}
		} catch (IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, "NewFileWizard", IStatus.OK,
					ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		}
		return new ByteArrayInputStream(sb.toString().getBytes());
	}
	
    /**
     * Adds the given folder to the project's class path.
     *
     * @param javaProject The Java Project to update.
     * @param sourceFolders Template Parameters.
     * @param monitor An existing monitor.
     * @throws JavaModelException if the classpath could not be set.
     */
    private void setupSourceFolders(IJavaProject javaProject, String[] sourceFolders,
            IProgressMonitor monitor) throws JavaModelException {
        IProject project = javaProject.getProject();
        // get the list of entries.
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        // remove the project as a source folder (This is the default)
        entries = removeSourceClasspath(entries, project);
        // add the source folders.
        for (String sourceFolder : sourceFolders) {
            IFolder srcFolder = project.getFolder(sourceFolder);
            // remove it first in case.
            entries = removeSourceClasspath(entries, srcFolder);
            entries = ProjectHelper.addEntryToClasspath(entries,
                    JavaCore.newSourceEntry(srcFolder.getFullPath()));
        }
        javaProject.setRawClasspath(entries, new SubProgressMonitor(monitor, 10));
    }
	
	/**
     * Removes the corresponding source folder from the class path entries if
     * found.
     *
     * @param entries The class path entries to read. A copy will be returned.
     * @param folder The parent source folder to remove.
     * @return A new class path entries array.
     */
    private IClasspathEntry[] removeSourceClasspath(IClasspathEntry[] entries, IContainer folder) {
        if (folder == null) {
            return entries;
        }
        IClasspathEntry source = JavaCore.newSourceEntry(folder.getFullPath());
        int n = entries.length;
        for (int i = n - 1; i >= 0; i--) {
            if (entries[i].equals(source)) {
                IClasspathEntry[] newEntries = new IClasspathEntry[n - 1];
                if (i > 0) System.arraycopy(entries, 0, newEntries, 0, i);
                if (i < n - 1) System.arraycopy(entries, i + 1, newEntries, i, n - i - 1);
                n--;
                entries = newEntries;
            }
        }
        return entries;
    }
	
    public void populate(String source, IContainer container, IProject project) {
        // Copy
        IFileSystem fileSystem = EFS.getLocalFileSystem();
//        File source = (File) new File();
        IFileStore sourceDir = new ReadWriteFileStore(
        		fileSystem.getStore(container.getFolder(new Path(source)).getLocationURI()//source.toURI()
                		));
        IFileStore destDir = new ReadWriteFileStore(
                fileSystem.getStore(getAbsolutePath(project)));
        try {
            sourceDir.copy(destDir, EFS.OVERWRITE, null);
        } catch (CoreException e) {
//            AdtPlugin.log(e, null);
        }
    }
    
    /**
     * Returns an absolute path to the given resource
     *
     * @param resource the resource to look up a path for
     * @return an absolute file system path to the resource
     */
    public static IPath getAbsolutePath(IResource resource) {
        IPath location = resource.getRawLocation();
        if (location != null) {
            return location.makeAbsolute();
        } else {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            IPath workspacePath = root.getLocation();
            return workspacePath.append(resource.getFullPath());
        }
    }
    
    /**
     * In a sample we never duplicate source files as read-only.
     * This creates a store that read files attributes and doesn't set the r-o flag.
     */
    private static class ReadWriteFileStore extends FileStoreAdapter {

        public ReadWriteFileStore(IFileStore store) {
            super(store);
        }

        // Override when reading attributes
        @Override
        public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
            IFileInfo info = super.fetchInfo(options, monitor);
            info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
            return info;
        }

        // Override when writing attributes
        @Override
        public void putInfo(IFileInfo info, int options, IProgressMonitor storeMonitor)
                throws CoreException {
            info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
            super.putInfo(info, options, storeMonitor);
        }

        @Deprecated
        @Override
        public IFileStore getChild(IPath path) {
            IFileStore child = super.getChild(path);
            if (!(child instanceof ReadWriteFileStore)) {
                child = new ReadWriteFileStore(child);
            }
            return child;
        }

        @Override
        public IFileStore getChild(String name) {
            return new ReadWriteFileStore(super.getChild(name));
        }
    }
}
