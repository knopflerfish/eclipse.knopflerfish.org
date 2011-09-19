package org.knopflerfish.eclipse.core.ui.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.launching.ExecutionArguments;

public class CustomJavaArgumentsTab extends org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab
{
  /**
   * Overrided to call initalizeFrom on all widgets.
   */
  /*
  public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
    initializeFrom(workingCopy);
  }
  */

  public void setInitFlag(boolean b)  {
    String programArgs = getAttributeValueFrom(fPrgmArgumentsText);
    if (programArgs == null) {
      programArgs = "";
    }
    boolean programArgsChanged = false;
    ExecutionArguments execArgs = new ExecutionArguments("", programArgs);
    List argsList = new ArrayList(Arrays.asList(execArgs.getProgramArgumentsArray()));
    if (!argsList.contains("-init")) {
      if (b) {
        argsList.add(0, "-init");
        programArgsChanged = true;
      }
    } else {
      if (!b) {
        while(argsList.contains("-init")) {
          argsList.remove("-init");
          programArgsChanged = true;
        }
      }
    }
    if (programArgsChanged) {
      StringBuffer buf = new StringBuffer();
      for (Iterator i=argsList.iterator(); i.hasNext(); ) {
        String a = (String) i.next();
        if (buf.length() > 0) {
          buf.append(" ");
        }
        buf.append(a);
      }
      if (buf.length() == 0) {
        fPrgmArgumentsText.setText("");
      } else {
        fPrgmArgumentsText.setText(buf.toString());
      }
    }
    
  }

  public boolean getInitFlag()  {
    String programArgs = getAttributeValueFrom(fPrgmArgumentsText);
    if (programArgs != null) {
      ExecutionArguments execArgs = new ExecutionArguments("", programArgs);
      List argsList = new ArrayList(Arrays.asList(execArgs.getProgramArgumentsArray()));
      return argsList.contains("-init");
    } else {
      return false;
    }
  }
}
