package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.List;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.knopflerfish.eclipse.core.IOsgiLibrary;

public class LibraryContentProvider implements IStructuredContentProvider, ITableLabelProvider {
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    List libs = (List) inputElement;
    return (IOsgiLibrary[]) libs.toArray(new IOsgiLibrary[libs.size()]);
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
    IOsgiLibrary lib = (IOsgiLibrary) element;
    
    if (columnIndex == 0) {
      if (lib.getSource() == null) {
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
      } else {
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
      }        
    } else {
      return null;
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    IOsgiLibrary lib = (IOsgiLibrary) element;

    if (columnIndex == 0) {
      return lib.getName();
    } else {
      return "";
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
