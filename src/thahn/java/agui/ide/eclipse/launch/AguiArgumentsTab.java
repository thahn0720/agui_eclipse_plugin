package thahn.java.agui.ide.eclipse.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaWorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import thahn.java.agui.ide.eclipse.project.BaseProjectHelper;
import thahn.java.agui.ide.eclipse.project.AguiProjectInfo;

public class AguiArgumentsTab extends JavaLaunchTab {//extends JavaArgumentsTab {

	// Program arguments widgets
	protected Label fPrgmArgumentsLabel;
	protected Text fPrgmArgumentsText;

	// VM arguments widgets
	protected VMArgumentsBlock fVMArgumentsBlock;
	
	// Working directory
	protected WorkingDirectoryBlock fWorkingDirectoryBlock;
		
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
		
	public AguiArgumentsTab() {
		fVMArgumentsBlock = createVMArgsBlock();
		fWorkingDirectoryBlock = createWorkingDirBlock();
	}
	
	protected VMArgumentsBlock createVMArgsBlock() {
		return new VMArgumentsBlock();
	}
	
	/**
	 * Creates a control to specify a working directory.
	 * 
	 * @return the new {@link WorkingDirectoryBlock}
	 * @since 3.4
	 */
	protected WorkingDirectoryBlock createWorkingDirBlock() {
		return new JavaWorkingDirectoryBlock();
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		comp.setLayout(layout);
		comp.setFont(font);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		setControl(comp);
		setHelpContextId();
		
//		Group group = new Group(comp, SWT.NONE);
//		group.setFont(font);
//		layout = new GridLayout();
//		group.setLayout(layout);
//		group.setLayoutData(new GridData(GridData.FILL_BOTH));
//		String controlName = (LauncherMessages.JavaArgumentsTab__Program_arguments__5); 
//		group.setText(controlName);
		
		fPrgmArgumentsText = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		fPrgmArgumentsText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE:
					case SWT.TRAVERSE_PAGE_NEXT:
					case SWT.TRAVERSE_PAGE_PREVIOUS:
						e.doit = true;
						break;
					case SWT.TRAVERSE_RETURN:
					case SWT.TRAVERSE_TAB_NEXT:
					case SWT.TRAVERSE_TAB_PREVIOUS:
						if ((fPrgmArgumentsText.getStyle() & SWT.SINGLE) != 0) {
							e.doit = true;
						} else {
							if (!fPrgmArgumentsText.isEnabled() || (e.stateMask & SWT.MODIFIER_MASK) != 0) {
								e.doit = true;
							}
						}
						break;
				}
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
//		fPrgmArgumentsText.setLayoutData(gd);
		fPrgmArgumentsText.setFont(font);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});
//		ControlAccessibleListener.addListener(fPrgmArgumentsText, group.getText());
//		String buttonLabel = LauncherMessages.JavaArgumentsTab_5;  
//		Button pgrmArgVariableButton = createPushButton(group, buttonLabel, null); 
//		pgrmArgVariableButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
//		pgrmArgVariableButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
//				dialog.open();
//				String variable = dialog.getVariableExpression();
//				if (variable != null) {
//                    fPrgmArgumentsText.insert(variable);
//				}
//			}
//		});
		fPrgmArgumentsText.setVisible(false);
		
		fVMArgumentsBlock.createControl(comp);
		
		fWorkingDirectoryBlock.createControl(comp);		
	}
	
	/**
	 * Set the help context id for this launch config tab.  Subclasses may
	 * override this method.
	 */
	protected void setHelpContextId() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMENTS_TAB);		
	}
			
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
	}
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid(config);
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		fVMArgumentsBlock.setDefaults(config);
		fWorkingDirectoryBlock.setDefaults(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			StringBuilder argBuilder = new StringBuilder();//"E:\\Workspace\\runtime-EclipseApplication\\s7").append(" ").append("s7.s7");
			String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				if(BaseProjectHelper.isAguiProject(project) && projectName.equals(project.getName())) {
					AguiProjectInfo info = BaseProjectHelper.getAguiProjectInfo(project);
					argBuilder.append(info.projectPath).append(" ").append(info.mainActivityName);
				}
			}
			fPrgmArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argBuilder.toString())); //$NON-NLS-1$
			fVMArgumentsBlock.initializeFrom(configuration);
			fWorkingDirectoryBlock.initializeFrom(configuration);
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getAttributeValueFrom(fPrgmArgumentsText));
		fVMArgumentsBlock.performApply(configuration);
		fWorkingDirectoryBlock.performApply(configuration);
	}

	/**
	 * Returns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @param text the widget to get the value from
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.JavaArgumentsTab__Arguments_16; 
	}	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
		fVMArgumentsBlock.setLaunchConfigurationDialog(dialog);
	}	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	@Override
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
	}	
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	@Override
	public String getId() {
		return "org.eclipse.jdt.debug.ui.javaArgumentsTab"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingDirectoryBlock.initializeFrom(workingCopy);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}
}
