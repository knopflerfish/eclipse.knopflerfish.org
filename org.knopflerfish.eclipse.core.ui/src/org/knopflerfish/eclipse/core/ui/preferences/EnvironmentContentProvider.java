package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.List;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;

public class EnvironmentContentProvider extends ViewerSorter implements IStructuredContentProvider, ITableLabelProvider {
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ViewerSorter methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
   */
  public int category(Object element) {
    EnvironmentPreference environment = (EnvironmentPreference) element;
    
    return environment.getType();
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public int compare(Viewer viewer, Object o1, Object o2) {
    int c1 = category(o1);
    int c2 = category(o2);
    if (c1 != c2) {
      return c1-c2;
    }
    EnvironmentPreference e1 = (EnvironmentPreference) o1;
    EnvironmentPreference e2 = (EnvironmentPreference) o2;
    return e1.getName().compareTo(e2.getName());
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    List environments = (List) inputElement;
    return (EnvironmentPreference[]) environments.toArray(new EnvironmentPreference[environments.size()]);
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IContentProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableLabelProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    if (columnIndex == 0) {
      return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY);
    }
    return null;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    EnvironmentPreference environment = (EnvironmentPreference) element;
    if (columnIndex == 0) {
      return environment.getName();
    }
    switch (environment.getType()) {
    case EnvironmentPreference.TYPE_JRE:
      return "";
    case EnvironmentPreference.TYPE_OSGI:
      return "OSGi Defined";
    case EnvironmentPreference.TYPE_USER:
      return "User Defined";
    default:
      return "Unknown type";
    }
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IBaseLabelProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener) {
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener) {
  }
}
