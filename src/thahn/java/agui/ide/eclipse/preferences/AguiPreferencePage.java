package thahn.java.agui.ide.eclipse.preferences;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class AguiPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	public AguiPreferencePage() {
	}
	
	public static final String 									PAGE_ID 			= "thahn.java.agui.ide.eclipse.preferences.AguiPreferencePage";
	
	private Text 												sdkLocationText;
	private Table 												sdkJarTable;
	private List<AguiSdkInfo> 									sdkLists = Lists.newArrayList();

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
		sdkLocationText.addKeyListener(sdkLocationkeyListener);
		// set default
		String sdkLocation = AguiPrefs.getInstance().getSdkLocation();
		if (!Strings.isNullOrEmpty(sdkLocation)) {
			sdkLocationText.setText(sdkLocation);
		}
		// init table 
		initTable();

		return container;
	}
	
	private void initTable() {
		// insert table item
		String[] columns = new String[]{"Target Name", "Version", "API Level"};
		for (String columnText : columns) {
			TableColumn column = new TableColumn(sdkJarTable, SWT.NONE);
			column.setText(columnText);
		}
		// refresh table
		refreshTable();
		// pack column
		for (TableColumn column : sdkJarTable.getColumns()) {
			column.pack();
		}
	}

	private void refreshTable() {
		// clean all
		cleanTable();
        // platforms path
		String sdkLocation = AguiPrefs.getInstance().getSdkLocation();
		if (Strings.isNullOrEmpty(sdkLocation)) {
			return ;  
		}
		// refresh table
        File platformsFile = Paths.get(sdkLocation, AguiConstants.PATH_PLATFORMS).toFile();
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
	
	private void cleanTable() {
		sdkLists.clear();
		sdkJarTable.removeAll();
	}
	
	private void setSdkLocation(String text) {
		AguiPrefs.getInstance().setSdkLocation(text);
		refreshTable();
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
					setSdkLocation(path);
				} else {
					cleanTable();
					AguiPlugin.displayError("Error", "Xml format is wrong");	
				}
			} else {
				cleanTable();
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
	
	private KeyListener sdkLocationkeyListener = new KeyListener() {
		
		@Override
		public void keyReleased(KeyEvent e) {
			String text = sdkLocationText.getText();
			if (!Strings.isNullOrEmpty(text) && new File(text).exists()) {
				setSdkLocation(text);
			} else {
				cleanTable();
				AguiPlugin.displayError("Invalid", "Invalid path");
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			
		}
	};
	
	@Override
	protected void performApply() {
		super.performApply();
	}

	@Override
	public boolean performOk() {
		return super.performOk();
//		AguiPrefs.getInstance().setSdkLocation(text);
//		AguiPrefs.getInstance().setSdkJarLocation(item.sdkBasePath);
//		AguiPrefs.getInstance().setSdkVersionSelection(item.manifestInfo.versionCode);
	}

	public class AguiSdkInfo {
		public String sdkBasePath;
		public ManifestInfo manifestInfo; 
	}
}
