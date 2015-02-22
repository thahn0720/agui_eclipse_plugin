package thahn.java.agui.ide.eclipse.preferences;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import thahn.java.agui.AguiConstants;
import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;
import thahn.java.agui.ide.eclipse.wizard.TextUtils;
import thahn.java.agui.res.ManifestParser;
import thahn.java.agui.res.ManifestParser.ManifestInfo;

import com.google.common.collect.Lists;

public class AguiPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	public static final String 											PAGE_ID 			= "thahn.java.agui.ide.eclipse.preferences.AguiPreferencePage";
	
	private Text sdkLocationText;
	private Table sdkJarTable;
	private List<AguiSdkInfo> sdkLists = Lists.newArrayList();

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}
	
	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));
		
		Label lblAguiPreferences = new Label(container, SWT.NONE);
		lblAguiPreferences.setText("Agui Preferences");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblAguiSdkFolder = new Label(container, SWT.NONE);
		lblAguiSdkFolder.setAlignment(SWT.CENTER);
		lblAguiSdkFolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblAguiSdkFolder.setText("SDK Location");
		
		sdkLocationText = new Text(container, SWT.BORDER);
		sdkLocationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.setText("Browse");
		
		Label lblSdkVersion = new Label(container, SWT.NONE);
		lblSdkVersion.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
		lblSdkVersion.setText("Choose SDK Version");
		
		sdkJarTable = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		sdkJarTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		sdkJarTable.setHeaderVisible(true);
		sdkJarTable.setLinesVisible(true);
		// selection listener
		btnBrowse.addSelectionListener(browseSelectionListener);
		sdkJarTable.addSelectionListener(tableSelectionListener);
		// set default
		sdkLocationText.setText(AguiPrefs.getInstance().getSdkLocation());
		refreshTable();

		return container;
	}

	private void refreshTable() {
		// clean all
		sdkLists.clear();
		sdkJarTable.clearAll();
		// insert table item
		String[] columns = new String[]{"Target Name", "Version", "API Level"};
		for (String columnText : columns) {
			TableColumn column = new TableColumn(sdkJarTable, SWT.NONE);
			column.setText(columnText);
		}
        // platforms path
        File platformsFile = Paths.get(AguiPrefs.getInstance().getSdkLocation(), AguiConstants.PATH_PLATFORMS).toFile();
        File[] lists = platformsFile.listFiles();
        if (lists != null) {
        	for (File item : lists) {
        		AguiSdkInfo aguiSdkInfo = new AguiSdkInfo();
        		String name = item.getName();
        		File manifestBaseDir = Paths.get(platformsFile.getAbsolutePath(), name, "data").toFile();
        		ManifestParser manifestParser = new ManifestParser(null);
        		manifestParser.parse(manifestBaseDir.getAbsolutePath());
        		ManifestInfo manifestInfo = manifestParser.getManifestInfo();
        		aguiSdkInfo.sdkBasePath = manifestBaseDir.getParent(); 
        		aguiSdkInfo.manifestInfo = manifestInfo;
        		sdkLists.add(aguiSdkInfo);
    			if(manifestInfo != null) {
    				TableItem tableItem = new TableItem(sdkJarTable, SWT.NONE);
    				tableItem.setText(new String[]{name, manifestInfo.versionName, manifestInfo.versionCode});
    			} else {
    				AguiPlugin.displayError("Manifest Error", "manifest parsing error");
    			}
			}
        } else {
        	AguiPlugin.displayError("No Sdk Lib", "the sdk lib does not exist for agui.");
        }
        
		for (TableColumn column : sdkJarTable.getColumns()) {
			column.pack();
		}
		
		if (sdkJarTable.getSelectionIndex() == -1) {
			String versionCode = AguiPrefs.getInstance().getSdkVersionSelection();
			if (versionCode == null) {
				sdkJarTable.select(0);
				AguiPrefs.getInstance().setSdkVersionSelection(sdkLists.get(0).manifestInfo.versionCode);
				AguiPrefs.getInstance().setSdkJarLocation(sdkLists.get(0).sdkBasePath);
			} else {
				for (int i = 0; i < sdkLists.size(); i++) {
					AguiSdkInfo aguiSdkInfo = sdkLists.get(i);
					if (versionCode.equals(aguiSdkInfo.manifestInfo.versionCode)) {
						sdkJarTable.select(i);
						break;
					}
				}
			}
		}
	}
	
	private SelectionListener browseSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
			if (TextUtils.isNullorEmpty(sdkLocationText.getText())) {
				dialog.setText(sdkLocationText.getText());
			} 
			String path = dialog.open();
			if (path != null) {
				sdkLocationText.setText(path);
				if (new File(path).exists()) {
					AguiPrefs.getInstance().setSdkLocation(path);
					refreshTable();
				} else {
					AguiPlugin.displayError("Error", "Xml format is wrong");	
				}
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};
	
	private SelectionListener tableSelectionListener = new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			int index = sdkJarTable.getSelectionIndex();
			if (index != -1) {
				for (AguiSdkInfo item : sdkLists) {
					AguiPrefs.getInstance().setSdkVersionSelection(item.manifestInfo.versionCode);
					AguiPrefs.getInstance().setSdkJarLocation(item.sdkBasePath);
					break;
				}
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};
	
	public class AguiSdkInfo {
		public String sdkBasePath;
		public ManifestInfo manifestInfo; 
	}
}
