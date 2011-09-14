package org.knopflerfish.eclipse.core.ui.launcher;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class CustomJavaArgumentsTab extends org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab
{
  /**
   * Overrided to call initalizeFrom on all widgets.
   */
  public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
    initializeFrom(workingCopy);
  }

}
