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
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class AvailableBundlesLabelProvider extends LabelProvider implements IFontProvider {
  
  private static String IMAGE_BUNDLE      = "icons/obj16/jar_b_obj.gif";
  private static String IMAGE_BUNDLE_SRC  = "icons/obj16/jar_bsrc_obj.gif";
  private static String IMAGE_BUNDLE_OVR  = "icons/ovr16/bundle_ovr.gif";
  
  // Resources
  private Image imageBundle = null;
  private Image imageBundleSrc = null;
  private Image imageProject = null;
  private Image imageWorkspace  = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
  private HashMap images = new HashMap();
  private Font  fontInactive = null;
  
  public AvailableBundlesLabelProvider() {
    ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_SRC);
    if (id != null) {
      imageBundleSrc = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_OVR);
    if (id != null) {
      Image bundleOvrImage = id.createImage();
      imageProject = new Image(null, imageWorkspace.getBounds());
      GC gc = new GC(imageProject);
      gc.drawImage(imageWorkspace, 0, 0);
      gc.drawImage(bundleOvrImage, imageProject.getBounds().width-bundleOvrImage.getBounds().width, 0);
      gc.dispose();      
      bundleOvrImage.dispose();
    }
    
    // Initialize image map
    String[] names = Osgi.getBundleRepositoryTypeNames();
    for (int i=0;i<names.length; i++) {
      String imagePath = Osgi.getBundleRepositoryTypeImage(names[i]);
      String pluginId = Osgi.getBundleRepositoryTypeId(names[i]);
      
      id = null;
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
    
    // Create font used for inactive repositories
    if (fontInactive == null) {
      Font font = Display.getCurrent().getSystemFont();
      FontData fontData = font.getFontData()[0];
      fontData.setStyle(SWT.ITALIC);
      fontInactive = new Font(Display.getCurrent(), fontData);
    }
    
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.ILabelProvider methods
   ***************************************************************************/
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
        }
        return imageBundle;
      case IAvailableTreeElement.TYPE_REPOSITORY:
        RepositoryPreference pref = ((AvailableElementRepository) e).getRepositoryPreference();
        return (Image) images.get(pref.getType());
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
      String name = e.getPath();
      return name == null ? "" : name;
    default :
      // Name
      name = e.getName();
      return name == null ? "" : name;
    }
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.IFontProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
   */
  public Font getFont(Object o) {
    if (!(o instanceof IAvailableTreeElement)) return null;
    
    IAvailableTreeElement e = (IAvailableTreeElement) o;

    if (e.getType() == IAvailableTreeElement.TYPE_REPOSITORY) {
      RepositoryPreference pref = ((AvailableElementRepository) e).getRepositoryPreference();
      if (!pref.isActive()) {
        return fontInactive;
      }
    }
    return null;
  }
  
  
}
