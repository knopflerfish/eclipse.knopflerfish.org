/*
 * Copyright (c) 2003-2005, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.SharedImages;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class DynamicImportProvider extends ViewerSorter implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

  // Images 
  private Image imgPackageError;
  
  public DynamicImportProvider() {
    imgPackageError = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_ERROR),
        UiUtils.LEFT, UiUtils.BOTTOM
    );
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.ViewerSorter methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public int compare(Viewer viewer, Object o1, Object o2) {
    String s1 = (String) o1;
    String s2 = (String) o2;
    
    return s1.compareTo(s2);
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    if ( !(inputElement instanceof BundleManifest)) return null;
    
    BundleManifest manifest = (BundleManifest) inputElement; 
    
    return manifest.getDynamicImportedPakages();
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableLabelProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {
    if (imgPackageError != null) {
      imgPackageError.dispose();
      imgPackageError = null;
    }
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
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    String name = (String) element;
    if (checkPackageName(name)) {
      return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
    }
    return imgPackageError;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    return (String) element;
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableColorProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
   */
  public Color getForeground(Object element, int columnIndex) {
    String name = (String) element;
    if (checkPackageName(name)) {
      return null;
    }
    return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
   */
  public Color getBackground(Object element, int columnIndex) {
    return null;
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  
  private boolean checkPackageName(String name) {
    if (name == null) return false;
    
    if ("*".equals(name)) return true;
    
    if (name.endsWith(".*")) {
      name = name.substring(0, name.length()-2);
    }
    IStatus status = JavaConventions.validatePackageName(name);
    return (status.getCode() == IStatus.OK);
  }
}
