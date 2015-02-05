package thahn.java.agui.ide.eclipse.wizard;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public class MessageBoxUtils {
	public static void showMessageBox(String title, String msg) {
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		box.setText(title);
		box.setMessage(msg);
		box.open();
	}
}
