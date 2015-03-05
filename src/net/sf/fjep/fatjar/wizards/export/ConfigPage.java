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
package net.sf.fjep.fatjar.wizards.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.help.ui.internal.browser.SystemBrowserFactory;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import thahn.java.agui.ide.eclipse.wizard.AguiPlugin;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (mpe).
 */

public class ConfigPage extends WizardPage {
    
    private JProjectConfiguration jproject; 
    private BuildProperties   props;

    private Composite   controlParent;
    private Text        jarnameText;
    private Text        launchConfigText;
    private Combo       changeLaunchConfigCombo;
    private Button      changeLaunchConfigButton;
    private Button      useManifestFileCheckbox;
    private Text        manifestfileText;
    private Button      browseManifestfileButton;
    private Button      browseManifestmainclassButton;
    private Button      manifestmergeallCheckbox;
    private Button      manifestremovesignersCheckbox;
    private Button      oneJarButton;
    private Label       oneJarExpandLabel;
    private Text        oneJarExpandText;
    private Text        manifestmainclassText;
    private Text        manifestclasspathText;
    private Text        manifestargumentsText;
    private Text        manifestvmargumentsText;
    private Button      jarnameIsExternCheckbox;
    private Button      browseJarnameButton;    
    private boolean     oneJarLicenseRequired;
    
    // Track the fact that we have just been created.
    private boolean     justCreated = false;
    

    /**
     * Constructor for SampleNewWizardPage.
     * @param pageName
     */
    public ConfigPage(JProjectConfiguration jproject, BuildProperties props) {
        super("wizardPage");
        System.out.println("ctor ConfigPage");
        setTitle("Configure Fat Jar Plug-In");
        this.props = props;
        this.jproject = jproject;
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {

        justCreated = true;
        
        controlParent = parent;

        GridData gd;
        Label label;

        Composite comp = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        comp.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        
        // The following comment indicates a new row on the grid
        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("Launch-Config:");

        launchConfigText = new Text(comp, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        launchConfigText.setLayoutData(gd);

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("Change to:");

        changeLaunchConfigCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        changeLaunchConfigCombo.setLayoutData(gd);
        changeLaunchConfigCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        changeLaunchConfigButton = new Button(comp, SWT.PUSH);
        changeLaunchConfigButton.setText("Change...");
        changeLaunchConfigButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                        handleUseLaunchConfigButton();
                }
        });
                
        //----------------------------------------------------------
        label = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("NEW-&Jar-Name:");

        jarnameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        jarnameText.setLayoutData(gd);
        jarnameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        browseJarnameButton = new Button(comp, SWT.PUSH);
        browseJarnameButton.setText("Browse...");
        browseJarnameButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                        handleJarnameBrowse();
                }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        //
        jarnameIsExternCheckbox = new Button(comp, SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        jarnameIsExternCheckbox.setLayoutData(gd);
        jarnameIsExternCheckbox.setText("use extern Jar-Name");
        jarnameIsExternCheckbox.setToolTipText("path for Jar-Name is absolute if checked or relative to project root folder when unchecked");
        jarnameIsExternCheckbox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                        boolean jarnameIsExtern = jarnameIsExternCheckbox.getSelection();
                        String jarname = jarnameText.getText();
                        if (jarnameIsExtern) {
                                jarname = jproject.getAbsProjectDir() + File.separator + jarname; 
                        }
                        else {
                                String projectDir = jproject.getAbsProjectDir() + File.separator; 
                                if (jarname.startsWith(projectDir)) {
                                        jarname = jarname.substring(projectDir.length());
                                }
                                else {
                                        jarname = File.separatorChar + jarname;
                                        jarname = jarname.substring(jarname.lastIndexOf(File.separatorChar)+1);
                                }
                        }
                        jarnameText.setText(jarname);
                }
        });
        //
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        

        //----------------------------------------------------------
        label = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        //
        useManifestFileCheckbox = new Button(comp, SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        useManifestFileCheckbox.setLayoutData(gd);
        useManifestFileCheckbox.setText("select Manifest file");
        useManifestFileCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean useManifestFile = useManifestFileCheckbox.getSelection();
                manifestfileText.setEnabled(useManifestFile);
                browseManifestfileButton.setEnabled(useManifestFile);
                manifestmainclassText.setEnabled(!useManifestFile);
                manifestclasspathText.setEnabled(!useManifestFile);
                manifestargumentsText.setEnabled(!useManifestFile);
                manifestvmargumentsText.setEnabled(!useManifestFile);
            }
        });
        //
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        
        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("&Manifest:");
        //
        manifestfileText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        manifestfileText.setLayoutData(gd);
        manifestfileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        //
        browseManifestfileButton = new Button(comp, SWT.PUSH);
        browseManifestfileButton.setText("Browse...");
        browseManifestfileButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleManifestfileBrowse();
            }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("&Main-Class:");
        //
        manifestmainclassText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        manifestmainclassText.setLayoutData(gd);
        manifestmainclassText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
                FJExpWizard fjew = (FJExpWizard)getWizard();
                if (fjew.autoJarPage != null) {
                    fjew.autoJarPage.dialogChanged();
                }
            }
        });
        //
        browseManifestmainclassButton = new Button(comp, SWT.PUSH);
        browseManifestmainclassButton.setText("Browse...");
        browseManifestmainclassButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleManifestmainclassBrowse();
            }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("&Class-Path:");
        //
        manifestclasspathText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        manifestclasspathText.setLayoutData(gd);
        manifestclasspathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("Arguments:");
        //
        manifestargumentsText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        manifestargumentsText.setToolTipText("if not empty: causes a boot-class to be added and started before original main-class");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        manifestargumentsText.setLayoutData(gd);
        manifestargumentsText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setText("Defines:");
        //
        manifestvmargumentsText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        manifestvmargumentsText.setToolTipText("multiple \"-D<propname>=<value>\" allowed. If not empty: causes a boot-class to be added and started before original main-class");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        manifestvmargumentsText.setLayoutData(gd);
        manifestvmargumentsText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        //
        manifestmergeallCheckbox = new Button(comp, SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        manifestmergeallCheckbox.setLayoutData(gd);
        manifestmergeallCheckbox.setText("merge individual-sections of all MANIFEST.MF files");
        manifestmergeallCheckbox.setEnabled(true);
        //
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        

        //----------------------------------------------------------
        label = new Label(comp, SWT.NULL);
        label.setVisible(false);
        //
        manifestremovesignersCheckbox = new Button(comp, SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        manifestremovesignersCheckbox.setLayoutData(gd);
        manifestremovesignersCheckbox.setText("remove signer files (*.SF) in META-INF");
        manifestremovesignersCheckbox.setEnabled(true);
        
        
        //----------------------------------------------------------
        label = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        //----------------------------------------------------------
        // Add a help icon for One-JAR, and a checkbox to enable
        // One-JAR support.
        Label help =  new Label(comp, SWT.NONE);
        ImageDescriptor desc = ImageDescriptor.createFromURL(
            AguiPlugin.getDefault().getBundle().getEntry("icons/help.gif"));
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        help.setLayoutData(gd);
        help.setImage(desc.createImage());
        help.setToolTipText("One-JAR Help");
        
        help.addMouseListener(new MouseListener() {
            public void mouseDoubleClick(MouseEvent e) {}
            public void mouseDown(MouseEvent e) {}
            public void mouseUp(MouseEvent e) {
                showOneJARHelp();
            }
        });

        oneJarButton = new Button(comp, SWT.CHECK);
        oneJarButton.setText("One-JAR");
        oneJarButton.setToolTipText("Build a One-JAR executable (preserves supporting Jar files)");

        oneJarButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean oneJar = oneJarButton.getSelection();
                if (oneJarLicenseRequired) {
                    int code = showOneJARLicense();
                    if (code == 1) {
                        // Didn't agree, reset the selection.
                        oneJarButton.setSelection(false);
                        return;
                    }
                    oneJarLicenseRequired = false;
                }           
                manifestmergeallCheckbox.setEnabled(!oneJar);
                manifestremovesignersCheckbox.setEnabled(!oneJar);
                oneJarExpandLabel.setEnabled(oneJar);
                oneJarExpandText.setEnabled(oneJar);
                dialogChanged();
            }
        });

        label = new Label(comp, SWT.LEFT);
        label.setVisible(true);


        //----------------------------------------------------------
        oneJarExpandLabel = new Label(comp, SWT.NULL);
        oneJarExpandLabel.setText("&One-Jar-Expand:");
        //
        oneJarExpandText = new Text(comp, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        oneJarExpandText.setLayoutData(gd);
        oneJarExpandText.setToolTipText("Comma separated list of directories/files to expand at runtime (optional)");
        oneJarExpandText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        //
        label = new Label(comp, SWT.LEFT);
        label.setVisible(true);

        if (jproject != null) {
            initialize();
            dialogChanged();
        }
        setControl(comp);
    }
    
    private void showOneJARHelp() { 
        MessageDialog dialog = new MessageDialog(getShell(), "One-JAR", null, 
                "One-JAR Help", MessageDialog.INFORMATION, 
                new String[]{"OK"} , 0) {
                
                protected Font font;
                
                public boolean close() {
                    boolean result;
                    try {
                        font.dispose();
                    } finally {
                        result = super.close();
                    }
                    return result;
                }
            
                protected Control createCustomArea(Composite parent) {
                    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                    gd.widthHint = 600;
                    gd.heightHint = 300;
                    String resource = "one-jar-help.txt";
                    StringBuffer help = null;
                    try {
                        help = readText(this.getClass().getResourceAsStream(resource));
                    } catch (IOException iox1) {
                        help = new StringBuffer();
                        help.append("Unable to locate built-in help for One-JAR at: " + resource + ": " + iox1);
                    }
                    Text text = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
                    font = JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
                    FontData fd = font.getFontData()[0];
                    // Reduce the font-size.  TODO: Should make this configurable in preferences.
                    fd.setHeight(fd.getHeight()-2);
                    font = new Font(text.getDisplay(), fd);
                    text.setFont(font);
                    text.setEditable(false);
                    text.setLayoutData(gd);
                    text.setText(help.toString());
                    
                    Hyperlink href = new Hyperlink(parent, SWT.NONE);
                    href.setText("http://one-jar.sourceforge.net");
                    href.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                    href.setUnderlined(true);
                    href.addHyperlinkListener(new IHyperlinkListener() {

                        public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                        }

                        public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                        }

                        public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                            try {
                                SystemBrowserFactory factory = new SystemBrowserFactory();
                                factory.createBrowser().displayURL(e.getLabel());
                            } catch (Exception x) {
                                MessageDialog.openError(e.display.getActiveShell(), "Unable to open " + e.getLabel(), "Unable to open browser: \n" + x.getStackTrace());
                            }
                        }
                    
                    });
                    return text;
                }};
        dialog.open();
        
    }
    
    /**
     * Show the One-JAR license in a dialogbox.  The first time the user sees this
     * they must accept its terms in order to be able to use One-JAR, under its
     * BSD-style license terms.
     * @return The code from the dialog. 0==Accept, 1==Reject.
     */
    private int showOneJARLicense() {
        // Show license.
        String buttons[] = new String[]{"Accept", "Decline"};
        MessageDialog dialog = new MessageDialog(getShell(), "One-JAR License", null, 
            "One-JAR is licensed according to the following terms.  Please visit http://one-jar.sourceforge.net for more information.", MessageDialog.INFORMATION, 
            buttons , 0) {
            protected Control createCustomArea(Composite parent) {
                // Put the license text in the dialog.
                Text text = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
                text.setEditable(false);
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.heightHint = 200;
                text.setLayoutData(gd);
                // Load the license data as a resource.  TODO: Resource this name!
                try {
                    JarInputStream jis = new JarInputStream(AguiPlugin.class.getResourceAsStream(AguiPlugin.ONE_JAR_BOOT));
                    JarEntry entry = (JarEntry)jis.getNextEntry();
                    StringBuffer license = new StringBuffer();
                    while (entry != null) {
                        if (entry.getName().equals("doc/one-jar-license.txt")) {
                            license = readText(jis);
                            break;
                        }
                        entry = (JarEntry)jis.getNextEntry();
                    }
                    text.setText(license.toString());
                    return text;
                } catch (Exception x) {
                    text.setText("The One-JAR license is available at http://one-jar.sourceforge.net");
                }
                return text;
            }
        };
        return dialog.open();
    }
    
    private StringBuffer readText(InputStream is) throws IOException {
        if (is == null) throw new IOException("input stream is null");
        StringBuffer text = new StringBuffer();
        try {
            // TODO: Tune this?
            byte buf[] = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) > 0) {
                text.append(new String(buf, 0, len));
            }
        } finally {
            is.close();
        }
        return text;
    }
    
    private void initialize() {
        
    	boolean jarname_isextern = props.isJarnameIsExtern();
        String jarname = props.getJarname();
        String manifest_file = props.getManifest_file();
        String manifest_mainclass = props.getManifest_mainclass();
        String manifest_classpath = props.getManifest_classpath();
        String manifest_arguments = props.getManifest_arguments();
        String manifest_vmarguments = props.getManifest_vmarguments();
        boolean manifest_mergeall = props.isManifest_mergeall();
        boolean manifest_removesigners = props.isManifest_removesigners();
        boolean oneJar = props.isUseOneJar();
        String oneJarExpand = props.getOnejarExpand();
        oneJarLicenseRequired = props.isOnejarLicenseRequired();
        String launchConfigName = props.getLaunchConfigName();
        
        // TODO: hier weiter
        if (!launchConfigName.equals(jproject.getLaunchConfigName())) {
            if (jproject.isNewLaunchConfig()) { 
                // this config was set in the handleUseLaunchConfigButton method, so keep it here
                launchConfigName = jproject.getLaunchConfigName();
            }
            else {
                ILaunchConfiguration lc = jproject.findConfiguration(launchConfigName);
                if (lc == null) {
                    launchConfigName = launchConfigName + " (not found)";
                }
                else {
                    JProjectConfiguration jpro = new JProjectConfiguration(jproject.getJproject(), lc);
                    jpro.setPropertiesFilename(jproject.getPropertiesFilename());
                    jpro.setNewLaunchConfig(true); // just to avoid endless recursion on errors
                    FJExpWizard fjew = (FJExpWizard)getWizard();
                    fjew.setJProject(jpro); // will call initialize (recursive)
                }
            }
        }
        launchConfigText.setText(launchConfigName);
        changeLaunchConfigCombo.setItems (jproject.getProjectConfigurations());
        changeLaunchConfigCombo.setText (jproject.getLaunchConfigName());

        jarnameText.setText(jarname);
        manifestfileText.setText(manifest_file);
        manifestmainclassText.setText(manifest_mainclass);
        manifestclasspathText.setText(manifest_classpath);
        manifestargumentsText.setText(manifest_arguments);
        manifestvmargumentsText.setText(manifest_vmarguments);
        if ((manifest_file == null) || manifest_file.equals("<createnew>")) {
            useManifestFileCheckbox.setSelection(false);
            manifestfileText.setEnabled(false);
            browseManifestfileButton.setEnabled(false);
        }
        else {
            useManifestFileCheckbox.setSelection(true);
            manifestmainclassText.setEnabled(false);
            manifestclasspathText.setEnabled(false);
        }
        oneJarButton.setSelection(oneJar);
        oneJarExpandText.setText(oneJarExpand);
        oneJarExpandLabel.setEnabled(oneJar);
        oneJarExpandText.setEnabled(oneJar);

        // TODO: fire an even to do this bit.
        manifestmergeallCheckbox.setEnabled(!oneJar);
        manifestremovesignersCheckbox.setEnabled(!oneJar);

        manifestmergeallCheckbox.setSelection(manifest_mergeall);
        manifestremovesignersCheckbox.setSelection(manifest_removesigners);
        jarnameIsExternCheckbox.setSelection(jarname_isextern);
                
    }
    

    private void handleUseLaunchConfigButton() {
        String selectedLC = changeLaunchConfigCombo.getText();
        boolean ok = MessageDialog.openQuestion(getShell(), "Fat Jar Change Launch-Config", "Changing the Launch-Config to '" + selectedLC + "' will overwrite all your current settings. Select OK to continue." );
        if (ok) {
            JProjectConfiguration jpro = new JProjectConfiguration(jproject.getJproject(), jproject.findConfiguration(selectedLC));
            jpro.setPropertiesFilename(jproject.getPropertiesFilename());
            // mark this jproject to keep the LaunchConfig (otherwise overwritten by stored properties)
            jpro.setNewLaunchConfig(true);
            FJExpWizard fjew = (FJExpWizard)getWizard();
            fjew.setJProject(jpro); // will call initialize()
            manifestmainclassText.setText(jpro.getMainClass());
            manifestargumentsText.setText(jpro.getArguments());
            manifestvmargumentsText.setText(jpro.getVMArguments());
            launchConfigText.setText(jpro.getLaunchConfigName());
        }
    }

    private void handleJarnameBrowse() {
        if (getJarnameIsExtern())
                handleExternJarnameBrowse();
        else
                handleProjectJarnameBrowse();
    }

        private void handleExternJarnameBrowse() {

                String filename = getJarname();
                String[] filterExt = { "*.jar"};
                FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
                fileDialog.setFileName(filename);
                fileDialog.setText("Save Output as ...");
                try {
                        filename = fileDialog.open();
                        if (!filename.toLowerCase().endsWith(".jar"))
                                filename += ".jar";
                        jarnameText.setText(filename);
                } catch (Exception e) {}
        }
        
        /**
         * Uses the standard container selection dialog to
         * choose the new value for the container field.
         */

        private void handleProjectJarnameBrowse() {

                String jarname = getJarname();
                SaveAsDialog dialog = new SaveAsDialog(getShell());
                String name = jarname;
                if (name == null) {
                        name = "fatjar_output.jar";
                }
                IFile iFile = jproject.getIFile(name);
                
                dialog.setOriginalFile(iFile);
                dialog.setTitle("Select Output Jar-Name"); //$NON-NLS-1$
        
                if (dialog.open() == SaveAsDialog.OK) {
                        IPath saveFile= dialog.getResult();
                        if (saveFile != null) {
                                String path = saveFile.toString();
                                String projectName = jproject.getJproject().getElementName();
                                if (!path.startsWith("/" + projectName + "/")) {
                                        MessageDialog.openInformation(getShell(), "Fat Jar Select Output", "output must be stored in project " + projectName);
                                }
                                else {
                                        if (!"jar".equals(saveFile.getFileExtension().toLowerCase()))
                                                saveFile = saveFile.addFileExtension("jar");
                                        saveFile = saveFile.removeFirstSegments(1).removeTrailingSeparator();
                                        jarnameText.setText(saveFile.toOSString());
                                }
                        }
                }
        }


    /**
     * Uses the standard container selection dialog to
     * choose the new value for the container field.
     */

    private void handleManifestfileBrowse() {

        String manifestFilename = getManifestfile();
        
        ILabelProvider lp= new WorkbenchLabelProvider();
        ITreeContentProvider cp= new WorkbenchContentProvider();

        ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(
                getShell(), lp, cp);
        dialog.setValidator(null);
        dialog.setAllowMultiple(false);
        dialog.setTitle("Select Manifest File"); //$NON-NLS-1$
//        dialog.setMessage("msg?"); //$NON-NLS-1$
        //dialog.addFilter(filter);
//        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());   
        //TODO: Validate Input, Make project list IAdaptable
//        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());   
        dialog.setInput(jproject.getProject());
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

        if (dialog.open() == ElementTreeSelectionDialog.OK) {
            Object[] elements= dialog.getResult();
            if (elements.length == 1) {
                manifestFilename = ((IResource)elements[0]).getLocation().toOSString();
//                int n = manifestFilename.indexOf(File.separatorChar,1);
//                if (n!=-1)
//                    manifestFilename = manifestFilename.substring(n+1);
                manifestfileText.setText(manifestFilename);
            }
        }
    }
    
    /**
     * Uses the standard container selection dialog to
     * choose the new value for the container field.
     */

    private void handleManifestmainclassBrowse() {

        String mainClass = getManifestmainclass();
        
        ILabelProvider lp= new WorkbenchLabelProvider();
        ITreeContentProvider cp= new WorkbenchContentProvider();

        IResource[] res=jproject.getResource();
        IJavaSearchScope searchScope= JavaSearchScopeFactory.getInstance().createJavaSearchScope(res, true);
        SelectionDialog dialog = JavaUI.createMainTypeDialog(getShell(), getContainer(), searchScope, 0, false);
        dialog.setMessage("Select Main-Class for JAR file");
        dialog.setTitle("Fat Jar Config");
        
        if (dialog.open() == SelectionDialog.OK) {
            Object[] elements= dialog.getResult();
            if (elements.length == 1) {
                SourceType mainElement = (SourceType)elements[0];
                mainClass = mainElement.getFullyQualifiedName();
                manifestmainclassText.setText(mainClass);
            }
        }
    }
    
    /**
     * Ensures that both text fields are set.
     */

    private void dialogChanged() {
        
                boolean onejar = getOneJar();
                String mainclass = getManifestmainclass();
                if (onejar && (mainclass==null || mainclass.length()==0)) {
                    updateStatus("Main-Class must be specified for One-JAR archives", null);
                    return;
                }
                String jarname = getJarname();
                if (jarname.length() == 0) {
                        updateStatus("Jar File must be specified", null);
                        return;
                }
                if (!jarname.toLowerCase().endsWith(".jar")) {
                        updateStatus(null, "Jar File extension should be \"jar\"");
                        return;
                }
                updateStatus(null, null);
    }

        private void updateStatus(String errorMessage, String warnMessage) {
                
                if (warnMessage == null)
                        setMessage(null);
                else
                        setMessage(warnMessage, IMessageProvider.WARNING);
                setErrorMessage(errorMessage);
                setPageComplete(errorMessage == null);
        }

    public String getJarname() {
        return jarnameText.getText();
    }
    public String getManifestfile() {
        return manifestfileText.getText();
    }
    public String getManifestmainclass() {
        return manifestmainclassText.getText();
    }
    public String getManifestclasspath() {
        return manifestclasspathText.getText();
    }
    public String getManifestarguments() {
        return manifestargumentsText.getText();
    }
    public String getManifestvmarguments() {
        return manifestvmargumentsText.getText();
    }
    public boolean getManifestmergeall() {
        return manifestmergeallCheckbox.getSelection();
    }
    public boolean getManifestremovesigners() {
        return manifestremovesignersCheckbox.getSelection();
    }
    public boolean getJarnameIsExtern() {
            return jarnameIsExternCheckbox.getSelection();
    }
    public boolean getOneJar() {
            return oneJarButton.getSelection();
    }
    public String getLaunchConfigName() {
        return jproject.getLaunchConfigName();
    }

    /**
     * 
     */
    public BuildProperties updateProperties() {
        String jarname = getJarname();
        String manifest_file = getManifestfile();
        String manifest_mainclass = getManifestmainclass();
        String manifest_classpath = getManifestclasspath();
        String manifest_arguments = getManifestarguments();
        String manifest_vmarguments = getManifestvmarguments();
        boolean manifest_mergeall = getManifestmergeall();
        boolean manifest_removesigners = getManifestremovesigners();
        boolean useManifestFile = useManifestFileCheckbox.getSelection();
        boolean jarnameIsExtern = getJarnameIsExtern();
        String launchConfigName = getLaunchConfigName();
        
        props.setJarnameIsExtern(jarnameIsExtern);
        props.setJarname(jarname);
        props.setUseManifestFile(useManifestFile);
        props.setManifest_file(manifest_file);
        props.setManifest_mainclass(manifest_mainclass);
        props.setManifest_classpath(manifest_classpath);
        props.setManifest_arguments(manifest_arguments);
        props.setManifest_vmarguments(manifest_vmarguments);
        props.setManifest_mergeall(manifest_mergeall);
        props.setManifest_removesigners(manifest_removesigners);
        props.setUseOneJar(oneJarButton.getSelection());
        props.setOnejarLicenseRequired(oneJarLicenseRequired);
        props.setOnejarExpand(oneJarExpandText.getText());
        props.setLaunchConfigName(launchConfigName);
        return props;
    }

    public void updateBuildProperties(BuildProperties buildProps) {
        String jarname = getJarname();
        String manifest_file = getManifestfile();
        String manifest_mainclass = getManifestmainclass();
        String manifest_classpath = getManifestclasspath();
        String manifest_arguments = getManifestarguments();
        String manifest_vmarguments = getManifestvmarguments();
        String launchConfigName = getLaunchConfigName();
        boolean manifest_mergeall = getManifestmergeall();
        boolean manifest_removesigners = getManifestremovesigners();
        boolean useManifestFile = useManifestFileCheckbox.getSelection();
        boolean jarnameIsExtern = getJarnameIsExtern();
        
        buildProps.setJarnameIsExtern(jarnameIsExtern);
        buildProps.setJarname(jarname);
        buildProps.setUseManifestFile(useManifestFile);
        buildProps.setManifest_file(manifest_file);
        buildProps.setManifest_mainclass(manifest_mainclass);
        buildProps.setManifest_classpath(manifest_classpath);
        buildProps.setManifest_arguments(manifest_arguments);
        buildProps.setManifest_vmarguments(manifest_vmarguments);
        buildProps.setManifest_mergeall(manifest_mergeall);
        buildProps.setManifest_removesigners(manifest_removesigners);
        buildProps.setLaunchConfigName(launchConfigName);
        buildProps.setUseOneJar(oneJarButton.getSelection());
        buildProps.setOnejarLicenseRequired(oneJarLicenseRequired);
        buildProps.setOnejarExpand(oneJarExpandText.getText());
    }
    
    /**
     * @param selectedJavaProject
     */
    public void setJProject(JProjectConfiguration jproject, BuildProperties props) {
        if (this.jproject != jproject) {
            this.jproject = jproject;
            this.props = props;
            setDescription("Config for project " + jproject.getName());
            initialize();
            dialogChanged();
        }
    }

}