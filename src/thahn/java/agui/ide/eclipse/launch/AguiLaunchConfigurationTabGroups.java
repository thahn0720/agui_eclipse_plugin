package thahn.java.agui.ide.eclipse.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

public class AguiLaunchConfigurationTabGroups extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
//                new MainLaunchConfigTab(),
        		new AguiMainTab(), //new JavaMainTab(),
                new AguiArgumentsTab(), //new JavaArgumentsTab(),
                new EnvironmentTab(),
//                new EmulatorConfigTab(ILaunchManager.RUN_MODE.equals(mode)),
                new CommonTab()
            };
        // org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab
        setTabs(tabs);
	}
}
