package thahn.java.agui.ide.eclipse.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import thahn.java.agui.Global;
import thahn.java.agui.res.ManifestParser;
import thahn.java.agui.res.ManifestParser.ManifestInfo;
import thahn.java.agui.utils.Log;

public final class BaseProjectHelper {

    /**
     * Project filter to be used with {@link BaseProjectHelper#getAndroidProjects(IProjectFilter)}.
     */
    public static interface IProjectFilter {
        boolean accept(IProject project);
    }
    
    /**
     * Returns the list of android-flagged projects. This list contains projects that are opened
     * in the workspace and that are flagged as android project (through the android nature)
     * @param filter an optional filter to control which android project are returned. Can be null.
     * @return an array of IJavaProject, which can be empty if no projects match.
     */
    public static IJavaProject[] getAndroidProjects(IProjectFilter filter) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(workspaceRoot);

        return getAndroidProjects(javaModel, filter);
    }

    /**
     * Returns the list of android-flagged projects for the specified java Model.
     * This list contains projects that are opened in the workspace and that are flagged as android
     * project (through the android nature)
     * @param javaModel the Java Model object corresponding for the current workspace root.
     * @param filter an optional filter to control which android project are returned. Can be null.
     * @return an array of IJavaProject, which can be empty if no projects match.
     */
    public static IJavaProject[] getAndroidProjects(IJavaModel javaModel,
            IProjectFilter filter) {
        // get the java projects
        IJavaProject[] javaProjectList = null;
        try {
            javaProjectList  = javaModel.getJavaProjects();
        }
        catch (JavaModelException jme) {
            return new IJavaProject[0];
        }

        // temp list to build the android project array
        ArrayList<IJavaProject> androidProjectList = new ArrayList<IJavaProject>();

        // loop through the projects and add the android flagged projects to the temp list.
        for (IJavaProject javaProject : javaProjectList) {
            // get the workspace project object
            IProject project = javaProject.getProject();

            // check if it's an android project based on its nature
            if (isAguiProject(project)) {
                if (filter == null || filter.accept(project)) {
                    androidProjectList.add(javaProject);
                }
            }
        }

        // return the android projects list.
        return androidProjectList.toArray(new IJavaProject[androidProjectList.size()]);
    }
    
    public static boolean isAguiProject(IProject project) {
        // check if it's an android project based on its nature
        try {
            return project.hasNature(AguiNature.NATURE_ID);
        } catch (CoreException e) {
            // this exception, thrown by IProject.hasNature(), means the project either doesn't
            // exist or isn't opened. So, in any case we just skip it (the exception will
            // bypass the ArrayList.add()
        }

        return false;
    }
    
    public static IFile getManifest(IProject project) {
        IResource r = project.findMember("/"+ "AguiManifest.xml");

        if (r == null || r.exists() == false || (r instanceof IFile) == false) {
            return null;
        }
        return (IFile) r;
    }
    
    public static AguiProjectInfo getAguiProjectInfo(IProject project) {
    	AguiProjectInfo aguiInfo = null;
    	IResource res = project.findMember("/" + AguiConstants.AGUI_MANIFEST);
		IFile[] mf = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(res.getRawLocation());
		if(mf == null || mf.length == 0) {
			Log.e("ResourceBuilder", "aguimanifest.xml does not exist.");
		} else {
			String mfPath = mf[0].getRawLocationURI().getRawPath();
			String projectPath = mfPath.substring(1, mfPath.lastIndexOf("/"));
	    	ManifestParser mfParser = new ManifestParser(null);
			mfParser.parse(projectPath);
			ManifestInfo manifestInfo = mfParser.getManifestInfo();
			if(manifestInfo != null) {
				aguiInfo = new AguiProjectInfo();
				aguiInfo.projectName = project.getName();
				aguiInfo.mainActivityName = manifestInfo.mainActivity;
				aguiInfo.packageName = manifestInfo.packageName;
				aguiInfo.projectPath = projectPath;
			}
		}
        return aguiInfo;
    }
    
    /**
     * Returns the {@link IJavaProject} for a {@link IProject} object.
     * <p/>
     * This checks if the project has the Java Nature first.
     * @param project
     * @return the IJavaProject or null if the project couldn't be created or if the project
     * does not have the Java Nature.
     * @throws CoreException if this method fails. Reasons include:
     * <ul><li>This project does not exist.</li><li>This project is not open.</li></ul>
     */
    public static IJavaProject getJavaProject(IProject project) throws CoreException {
        if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
            return JavaCore.create(project);
        }
        return null;
    }
    
    /**
     * Returns the {@link IFolder} representing the output for the project for Android specific
     * files.
     * <p>
     * The project must be a java project and be opened, or the method will return null.
     * @param project the {@link IProject}
     * @return an IFolder item or null.
     */
    public final static IFolder getJavaOutputFolder(IProject project) {
        try {
            if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                // get a java project from the normal project object
                IJavaProject javaProject = JavaCore.create(project);

                IPath path = javaProject.getOutputLocation();
                path = path.removeFirstSegments(1);
                return project.getFolder(path);
            }
        } catch (JavaModelException e) {
            // Let's do nothing and return null
        } catch (CoreException e) {
            // Let's do nothing and return null
        }
        return null;
    }
}