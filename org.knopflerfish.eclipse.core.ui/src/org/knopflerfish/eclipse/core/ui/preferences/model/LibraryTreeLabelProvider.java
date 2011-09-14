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

package org.knopflerfish.eclipse.core.ui.preferences.model;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class LibraryTreeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
  private static String IMAGE_RUNTIME     = "icons/obj16/runtime_jar_obj.gif";
  private static String IMAGE_BUNDLE      = "icons/obj16/jar_b_obj.gif";
  private static String IMAGE_BUNDLE_SRC  = "icons/obj16/jar_bsrc_obj.gif";
  
  private Font  fontUser = null;
  private Image imageRuntime = null;
  private Image imageBundle = null;
  private Image imageBundleSrc = null;
  
  public LibraryTreeLabelProvider() {
    
    ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_SRC);
    if (id != null) {
      imageBundleSrc = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_RUNTIME);
    if (id != null) {
      imageRuntime = id.createImage();
    }
    
    // Create font used in property tree for non-default values
    if (fontUser == null) {
      Font font = Display.getCurrent().getSystemFont();
      FontData fontData = font.getFontData()[0];
      fontData.setStyle(SWT.BOLD);
      //Display.getCurrent().getActiveShell()
      fontUser = new Font(Display.getCurrent(), fontData);
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
    if (imageBundle != null) {
      imageBundle.dispose();
      imageBundle = null;
    }
    if (imageBundleSrc != null) {
      imageBundleSrc.dispose();
      imageBundleSrc = null;
    }
    if (imageRuntime != null) {
      imageRuntime.dispose();
      imageRuntime = null;
    }
    if (fontUser != null) {
      fontUser.dispose();
      fontUser = null;
     }
  }  
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ILabelProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  public Image getImage(Object o) {
    if (!(o instanceof ILibraryTreeElement)) return null;
    
    ILibraryTreeElement e = (ILibraryTreeElement) o;
    
    switch (e.getType()) {
    case ILibraryTreeElement.TYPE_ROOT:
      return null;
    case ILibraryTreeElement.TYPE_RUNTIME_ROOT:
      return imageRuntime;
    case ILibraryTreeElement.TYPE_RUNTIME:
      if (((LibraryElementRuntime) e).getLibrary().getSource() != null) {
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
      }
      return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
    case ILibraryTreeElement.TYPE_BUNDLE_ROOT:
      return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
    case ILibraryTreeElement.TYPE_BUNDLE_DIR:
      return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
    case ILibraryTreeElement.TYPE_BUNDLE:
      if (((LibraryElementBundle) e).getBundle().getSource() != null) {
        return imageBundleSrc;
      }
      return imageBundle;
    default :
      return null;
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  public String getText(Object o) {
    if (!(o instanceof ILibraryTreeElement)) return o.toString();
    
    ILibraryTreeElement e = (ILibraryTreeElement) o;
    
    switch (e.getType()) {
    case ILibraryTreeElement.TYPE_ROOT:
      return "root";
    case ILibraryTreeElement.TYPE_RUNTIME_ROOT:
      return "Runtime Libraries";
    case ILibraryTreeElement.TYPE_RUNTIME:
      return ((LibraryElementRuntime) e).getLibrary().getName();
    case ILibraryTreeElement.TYPE_BUNDLE_ROOT:
      return "Bundles";
    case ILibraryTreeElement.TYPE_BUNDLE:
      return ((LibraryElementBundle) e).getBundle().getName();
    case ILibraryTreeElement.TYPE_BUNDLE_DIR:
      return ((LibraryElementBundleDirectory) e).getBundleDirectory();
    default :
      return "huh?";
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
    if (!(o instanceof ILibraryTreeElement)) return null;
    
    ILibraryTreeElement e = (ILibraryTreeElement) o;
    
    switch (e.getType()) {
    case ILibraryTreeElement.TYPE_ROOT:
      return null;
    case ILibraryTreeElement.TYPE_RUNTIME_ROOT:
      return null;
    case ILibraryTreeElement.TYPE_RUNTIME:
      if (((LibraryElementRuntime) e).getLibrary().isUserDefined()) {
        return fontUser;
      }
      return null;
    case ILibraryTreeElement.TYPE_BUNDLE_ROOT:
      return null;
    case ILibraryTreeElement.TYPE_BUNDLE:
      if (((LibraryElementBundle) e).getBundle().isUserDefined()) {
        return fontUser;
      }
      return null;
    default :
      return null;
    }
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.IColorProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
   */
  public Color getForeground(Object element) {
    return null;
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
   */
  public Color getBackground(Object element) {
    return null;
  }

}
