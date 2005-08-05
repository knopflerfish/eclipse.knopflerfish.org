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

package org.knopflerfish.eclipse.core.ui.launcher.bundle;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class AvailableBundlesLabelProvider extends LabelProvider {
  
  private static String IMAGE_BUNDLE      = "icons/obj16/jar_b_obj.gif";
  private static String IMAGE_BUNDLE_SRC  = "icons/obj16/jar_bsrc_obj.gif";
  private static String IMAGE_BUNDLE_OVR  = "icons/ovr16/bundle_ovr.gif";
  
  private Image imageBundle = null;
  private Image imageBundleSrc = null;
  private Image imageProject = null;
  private Image imageWorkspace  = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
  private HashMap images = new HashMap();
  
  public AvailableBundlesLabelProvider() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_SRC);
    if (id != null) {
      imageBundleSrc = id.createImage();
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_OVR);
    if (id != null) {
      Image bundleOvrImage = id.createImage();
      imageProject = new Image(null, imageWorkspace.getBounds());
      GC gc = new GC(imageProject);
      gc.drawImage(imageWorkspace, 0, 0);
      gc.drawImage(bundleOvrImage, imageProject.getBounds().width-bundleOvrImage.getBounds().width, 0);
      gc.dispose();      
      bundleOvrImage.dispose();
    }
    
    String[] names = Osgi.getFrameworkDefinitionNames();
    for (int i=0;i<names.length; i++) {
      String imagePath = Osgi.getFrameworkDefinitionImage(names[i]);
      String pluginId = Osgi.getFrameworkDefinitionId(names[i]);
      
      id = OsgiUiPlugin.imageDescriptorFromPlugin(pluginId, imagePath);
      if (id != null) {
        Image image = id.createImage();
        if (image != null) {
          images.put(names[i], image);
        }
      }
    }
    
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {
    if (imageBundle != null) {
      imageBundle.dispose();
      imageBundle = null;
    }
    if (imageBundleSrc != null) {
      imageBundleSrc.dispose();
      imageBundleSrc = null;
    }
    if (imageProject != null) {
      imageProject.dispose();
      imageProject = null;
    }
    for (Iterator i=images.values().iterator(); i.hasNext();){
      Image image = (Image) i.next();
      image.dispose();
     }
  }  
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  public Image getImage(Object o) {
    if (!(o instanceof IAvailableTreeElement)) return null;
    
    IAvailableTreeElement e = (IAvailableTreeElement) o;
    
    switch (e.getType()) {
      case IAvailableTreeElement.TYPE_BUNDLE:
        IOsgiBundle bundle = ((AvailableElementBundle) e).getBundle();
        if (bundle != null && bundle.getSource() != null) {
          return imageBundleSrc;
        } else {
          return imageBundle;
        }
      case IAvailableTreeElement.TYPE_OSGI_INSTALL:
        IOsgiInstall osgiInstall = ((AvailableElementInstall) e).getOsgiInstall();
        return (Image) images.get(osgiInstall.getType());
      case IAvailableTreeElement.TYPE_PROJECT:
        return imageProject;
      case IAvailableTreeElement.TYPE_WORKSPACE:
        return imageWorkspace;
      default :
        return null;
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  public String getText(Object o) {
    if (!(o instanceof IAvailableTreeElement)) return o.toString();
    
    IAvailableTreeElement e = (IAvailableTreeElement) o;
    switch (e.getType()) {
    case IAvailableTreeElement.TYPE_PROJECT:
      // Name
      String name = e.getName();
      return name == null ? "" : name;
    default :
      // Name
      name = e.getName();
      return name == null ? "" : name;
    }
  }
}
