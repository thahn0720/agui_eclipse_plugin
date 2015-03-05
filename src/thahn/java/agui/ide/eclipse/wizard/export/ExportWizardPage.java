package thahn.java.agui.ide.eclipse.wizard.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.jface.wizard.WizardPage;

/**
 * Base page class for the ExportWizard page. This class add the
 * {@link #onShow()} callback.
 */
public abstract class ExportWizardPage extends WizardPage {

	/** bit mask constant for project data change event */
	protected static final int	DATA_PROJECT		= 0x001;
	/** bit mask constant for keystore data change event */
	protected static final int	DATA_KEYSTORE		= 0x002;
	/** bit mask constant for key data change event */
	protected static final int	DATA_KEY			= 0x004;

	/**
	 * Bit mask indicating what changed while the page was hidden.
	 * 
	 * @see #DATA_PROJECT
	 * @see #DATA_KEYSTORE
	 * @see #DATA_KEY
	 */
	protected int				mProjectDataChanged	= 0;

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
	 * Calls {@link #setErrorMessage(String)} and
	 * {@link #setPageComplete(boolean)} based on a {@link Throwable} object.
	 */
	protected void onException(Throwable t) {
		String message = getExceptionMessage(t);
		setErrorMessage(message);
		setPageComplete(false);
	}

	/**
	 * Returns the {@link Throwable#getMessage()}. If the
	 * {@link Throwable#getMessage()} returns <code>null</code>, the method is
	 * called again on the cause of the Throwable object.
	 * <p/>
	 * If no Throwable in the chain has a valid message, the canonical name of
	 * the first exception is returned.
	 */
	private String getExceptionMessage(Throwable t) {
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