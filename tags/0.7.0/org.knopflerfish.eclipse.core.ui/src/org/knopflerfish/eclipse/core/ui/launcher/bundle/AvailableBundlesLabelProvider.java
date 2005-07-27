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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author Anders Rim�n
 */
public class AvailableBundlesLabelProvider extends LabelProvider {
  
  private static String IMAGE_BUNDLE      = "icons/obj16/bundle_obj.gif";
  private static String IMAGE_FISH        = "icons/obj16/knopflerfish_obj.gif";
  private static String IMAGE_BUNDLE_OVR  = "icons/ovr16/bundle_ovr.gif";
  
  private Image imageBundle = null;
  private Image imageKnopflerfish = null;
  private Image imageProject = null;
  private Image sharedImageWorkspace  = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
  
  public AvailableBundlesLabelProvider() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_FISH);
    if (id != null) {
      imageKnopflerfish = id.createImage();
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_OVR);
    if (id != null) {
      Image bundleOvrImage = id.createImage();
      imageProject = new Image(null, sharedImageWorkspace.getBounds());
      GC gc = new GC(imageProject);
      gc.drawImage(sharedImageWorkspace, 0, 0);
      gc.drawImage(bundleOvrImage, imageProject.getBounds().width-bundleOvrImage.getBounds().width, 0);
      gc.dispose();      
      bundleOvrImage.dispose();
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
    if (imageKnopflerfish != null) {
      imageKnopflerfish.dispose();
      imageKnopflerfish = null;
    }
    if (imageProject != null) {
      imageProject.dispose();
      imageProject = null;
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
        return imageBundle;
      case IAvailableTreeElement.TYPE_OSGI_INSTALL:
        return imageKnopflerfish;
      case IAvailableTreeElement.TYPE_PROJECT:
        return imageProject;
      case IAvailableTreeElement.TYPE_WORKSPACE:
        return sharedImageWorkspace;
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
