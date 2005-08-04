package org.knopflerfish.eclipse.core.ui.launcher.main;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;

public class SystemPropertySorter extends ViewerSorter {
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public int compare(Viewer viewer, Object o1, Object o2) {
    int c1 = category(o1);
    int c2 = category(o2);
    if (c1 != c2) return c1-c2;
    
    String n1 = null;
    String n2 = null;
    if (o1 instanceof SystemPropertyGroup) {
      n1 = ((SystemPropertyGroup) o1).getName();
      n2 = ((SystemPropertyGroup) o2).getName();
    } else if (o1 instanceof SystemProperty) {
      n1 = ((SystemProperty) o1).getName();
      n2 = ((SystemProperty) o2).getName();
    }
    if (n1 == null) n1 = "";
    if (n2 == null) n2 = "";
    return n1.toLowerCase().compareTo(n2.toLowerCase());
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
   */
  public int category(Object o) {
    if (o instanceof IOsgiInstall) {
      return 0;
    } else if (o instanceof SystemPropertyGroup) {
      // Check if user defined group
      SystemPropertyGroup group = (SystemPropertyGroup) o;
      if (MainTab.USER_GROUP.equals(group.getName())) {
        return 11;
      } else {
        return 10;
      }
    } else if (o instanceof SystemProperty) {
      return 100;
    } else {
      return -1;
    }
  }
}
