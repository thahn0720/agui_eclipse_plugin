package thahn.java.agui.ide.eclipse.wizard;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

import thahn.java.agui.ide.eclipse.project.AguiConstants;

public class ProjectHelper {
	
	 /**
     * Preferred compiler level, i.e. "1.6".
     */
    public final static String COMPILER_COMPLIANCE_PREFERRED = JavaCore.VERSION_1_7;
	
    public static IClasspathEntry[] addEntryToClasspath(
            IClasspathEntry[] entries, IClasspathEntry newEntry) {
        int n = entries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[n + 1];
        System.arraycopy(entries, 0, newEntries, 0, n);
        newEntries[n] = newEntry;
        return newEntries;
    }
    
    /**
     * Makes the given project use JDK 6 (or more specifically,
     * {@link AdtConstants#COMPILER_COMPLIANCE_PREFERRED} as the compilation
     * target, regardless of what the default IDE JDK level is, provided a JRE
     * of the given level is installed.
     *
     * @param javaProject the Java project
     * @throws CoreException if the IDE throws an exception setting the compiler
     *             level
     */
    @SuppressWarnings("restriction") // JDT API for setting compliance options
    public static void enforcePreferredCompilerCompliance(IJavaProject javaProject)
            throws CoreException {
        String compliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (compliance == null ||
                JavaModelUtil.isVersionLessThan(compliance, COMPILER_COMPLIANCE_PREFERRED)) {
            IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
            for (int i = 0; i < types.length; i++) {
                IVMInstallType type = types[i];
                IVMInstall[] installs = type.getVMInstalls();
                for (int j = 0; j < installs.length; j++) {
                    IVMInstall install = installs[j];
                    if (install instanceof IVMInstall2) {
                        IVMInstall2 install2 = (IVMInstall2) install;
                        // Java version can be 1.6.0, and preferred is 1.6
                        if (install2.getJavaVersion().startsWith(COMPILER_COMPLIANCE_PREFERRED)) {
                            Map<String, String> options = javaProject.getOptions(false);
                            JavaCore.setComplianceOptions(COMPILER_COMPLIANCE_PREFERRED, options);
                            JavaModelUtil.setDefaultClassfileOptions(options,
                                    COMPILER_COMPLIANCE_PREFERRED);
                            javaProject.setOptions(options);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns an {@link IFile} object representing the manifest for the given project.
     *
     * @param project The project containing the manifest file.
     * @return An IFile object pointing to the manifest or null if the manifest
     *         is missing.
     */
    public static IFile getManifest(IProject project) {
        IResource r = project.findMember("/"+AguiConstants.AGUI_MANIFEST);

        if (r == null || r.exists() == false || (r instanceof IFile) == false) {
            return null;
        }
        return (IFile) r;
    }
}
