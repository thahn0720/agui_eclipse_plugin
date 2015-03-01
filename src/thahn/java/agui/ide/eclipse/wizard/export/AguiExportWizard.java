package thahn.java.agui.ide.eclipse.wizard.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class AguiExportWizard extends Wizard implements IExportWizard {

	private ExportWizardPage mPages[] = new ExportWizardPage[1];
	private IProject mProject;
	
	/**
     * Base page class for the ExportWizard page. This class add the {@link #onShow()} callback.
     */
    static abstract class ExportWizardPage extends WizardPage {

        /** bit mask constant for project data change event */
        protected static final int DATA_PROJECT = 0x001;
        /** bit mask constant for keystore data change event */
        protected static final int DATA_KEYSTORE = 0x002;
        /** bit mask constant for key data change event */
        protected static final int DATA_KEY = 0x004;

        protected static final VerifyListener sPasswordVerifier = new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // verify the characters are valid for password.
                int len = e.text.length();

                // first limit to 127 characters max
                if (len + ((Text)e.getSource()).getText().length() > 127) {
                    e.doit = false;
                    return;
                }

                // now only take non control characters
                for (int i = 0 ; i < len ; i++) {
                    if (e.text.charAt(i) < 32) {
                        e.doit = false;
                        return;
                    }
                }
            }
        };

        /**
         * Bit mask indicating what changed while the page was hidden.
         * @see #DATA_PROJECT
         * @see #DATA_KEYSTORE
         * @see #DATA_KEY
         */
        protected int mProjectDataChanged = 0;

        ExportWizardPage(String name) {
            super(name);
        }

        abstract void onShow();

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                onShow();
                mProjectDataChanged = 0;
            }
        }

        final void projectDataChanged(int changeMask) {
            mProjectDataChanged |= changeMask;
        }

        /**
         * Calls {@link #setErrorMessage(String)} and {@link #setPageComplete(boolean)} based on a
         * {@link Throwable} object.
         */
        protected void onException(Throwable t) {
            String message = getExceptionMessage(t);

            setErrorMessage(message);
            setPageComplete(false);
        }
    }

	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        // get the project from the selection
        Object selected = selection.getFirstElement();

        if (selected instanceof IProject) {
            mProject = (IProject)selected;
        } else if (selected instanceof IAdaptable) {
            IResource r = (IResource)((IAdaptable)selected).getAdapter(IResource.class);
            if (r != null) {
                mProject = r.getProject();
            }
        }
	}

	@Override
    public void addPages() {
        addPage(mPages[0] = new ProjectCheckPage(this, "Export"));
//        addPage(mKeystoreSelectionPage = mPages[1] = new KeystoreSelectionPage(this, PAGE_KEYSTORE_SELECTION));
//        addPage(mKeyCreationPage = mPages[2] = new KeyCreationPage(this, PAGE_KEY_CREATION));
//        addPage(mKeySelectionPage = mPages[3] = new KeySelectionPage(this, PAGE_KEY_SELECTION));
//        addPage(mKeyCheckPage = mPages[4] = new KeyCheckPage(this, PAGE_KEY_CHECK));
    }
	
	@Override
	public boolean performFinish() {
		
		return false;
	}
	
	IProject getProject() {
        return mProject;
    }
	
    void setProject(IProject project) {
        mProject = project;

        updatePageOnChange(ExportWizardPage.DATA_PROJECT);
    }
    
    void updatePageOnChange(int changeMask) {
        for (ExportWizardPage page : mPages) {
            page.projectDataChanged(changeMask);
        }
    }
	
	/**
     * Returns the {@link Throwable#getMessage()}. If the {@link Throwable#getMessage()} returns
     * <code>null</code>, the method is called again on the cause of the Throwable object.
     * <p/>If no Throwable in the chain has a valid message, the canonical name of the first
     * exception is returned.
     */
    static String getExceptionMessage(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            // no error info? get the stack call to display it
            // At least that'll give us a better bug report.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(baos));
            message = baos.toString();
        }

        return message;
    }
}
