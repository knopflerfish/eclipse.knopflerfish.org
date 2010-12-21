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

package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class RepositoryContentProvider implements IStructuredContentProvider, ITableLabelProvider {
  
  private HashMap images = new HashMap();

  public RepositoryContentProvider() {
    
    // Initialize image map
    String[] names = Osgi.getBundleRepositoryTypeNames();
    for (int i=0;i<names.length; i++) {
      String imagePath = Osgi.getBundleRepositoryTypeImage(names[i]);
      String pluginId = Osgi.getBundleRepositoryTypeId(names[i]);
      
      ImageDescriptor id = null;
      if (imagePath != null) {
        id = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imagePath);
      } else {
        id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui",
        "icons/obj16/knopflerfish_obj.gif");
      }      
      if (id != null) {
        Image image = id.createImage();
        if (image != null) {
          images.put(names[i], image);
        }
      }
    }
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    List repositories = (List) inputElement;
    return (RepositoryPreference[]) repositories.toArray(new RepositoryPreference[repositories.size()]);
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
    RepositoryPreference repository = (RepositoryPreference) element;
    if (columnIndex == 0) {
      return (Image) images.get(repository.getType());
    }
    return null;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    RepositoryPreference repository = (RepositoryPreference) element;
    switch (columnIndex) {
    case 0:
      return repository.getName();
    case 1:
      return repository.getType();
    default:
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
    for (Iterator i=images.values().iterator(); i.hasNext();){
      Image image = (Image) i.next();
      image.dispose();
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
}
