/*
 * Copyright (c) 2003-2010, KNOPFLERFISH project
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

import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.ui.launcher.main.MainTab;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class SelectedBundlesLabelProvider
    implements ITableLabelProvider, IColorProvider {

  private static String IMAGE_BUNDLE = "icons/obj16/jar_b_obj.gif";
  private static String IMAGE_BUNDLE_SRC = "icons/obj16/jar_bsrc_obj.gif";
  private static String IMAGE_BUNDLE_OVR = "icons/ovr16/bundle_ovr.gif";

  private int initialStartLevel = MainTab.DEFAULT_START_LEVEL;

  // Resources
  private Color colorError = null;
  private Color colorWarning = null;
  private Image imageBundle = null;
  private Image imageBundleSrc = null;
  private Image imageProject = null;
  private Image sharedImageWorkspace = PlatformUI.getWorkbench()
      .getSharedImages()
      .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);

  public SelectedBundlesLabelProvider()
  {
    ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(
        "org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin(
        "org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_SRC);
    if (id != null) {
      imageBundleSrc = id.createImage();
    }
    id = AbstractUIPlugin.imageDescriptorFromPlugin(
        "org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_OVR);
    if (id != null) {
      Image bundleOvrImage = id.createImage();
      imageProject = new Image(null, sharedImageWorkspace.getBounds());
      GC gc = new GC(imageProject);
      gc.drawImage(sharedImageWorkspace, 0, 0);
      gc.drawImage(bundleOvrImage, imageProject.getBounds().width
          - bundleOvrImage.getBounds().width, 0);
      gc.dispose();
      bundleOvrImage.dispose();
    }

    colorError = new Color(null, 255, 0, 0);
    colorWarning = new Color(null, 255, 255, 0);
  }

  void setInitialStartLevel(int level)
  {
    initialStartLevel = level;
  }

  // ***************************************************************************
  // org.eclipse.jface.viewers.IBaseLabelProvider Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
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
    if (colorError != null) {
      colorError.dispose();
      colorError = null;
    }
    if (colorWarning != null) {
      colorWarning.dispose();
      colorWarning = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface
   * .viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.
   * Object, java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property)
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
   * .jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener)
  {
  }

  // ***************************************************************************
  // org.eclipse.jface.viewers.ITableLabelProvider Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.
   * Object, int)
   */
  public Image getColumnImage(Object o, int columnIndex)
  {
    SelectedBundleElement e = (SelectedBundleElement) o;

    if (columnIndex == 0) {
      if (e.getType() == SelectedBundleElement.TYPE_BUNDLE) {
        if (e.getLaunchInfo().getSource() != null) {
          return imageBundleSrc;
        }
        return imageBundle;
      }
      return imageProject;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object
   * , int)
   */
  public String getColumnText(Object o, int columnIndex)
  {
    SelectedBundleElement e = (SelectedBundleElement) o;

    switch (columnIndex) {
    case 0:
      // Name
      String name = e.getName();
      if (e.getType() == SelectedBundleElement.TYPE_BUNDLE_PROJECT) {
        name = e.getPath();
      }
      return name;
    case 1:
      // Version
      Version version = e.getVersion();
      return version == null ? "" : version.toString();
    case 2:
      // Start level
      int startLevel = e.getLaunchInfo().getStartLevel();
      StringBuffer buf = new StringBuffer();
      buf.append(startLevel);
      if (initialStartLevel < startLevel) {
        buf.append(" > Initial Start Level (");
        buf.append(initialStartLevel);
        buf.append(")");
      }
      return buf.toString();
    case 3:
      // Mode
      return BundleLaunchInfo.MODES[e.getLaunchInfo().getMode()];
    case 4:
      // Error
      PackageDescription[] missingPackages = e.getMissingPackages();
      if (missingPackages != null && missingPackages.length > 0) {
        String s = Arrays.toString(missingPackages);
        // Remove brackets
        return s.substring(1, s.length() - 1);
      } else {
        return "";
      }
    default:
      return "";
    }
  }

  // ***************************************************************************
  // org.eclipse.jface.window.Window Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
   */
  public Color getForeground(Object o)
  {
    SelectedBundleElement e = (SelectedBundleElement) o;
    PackageDescription[] missingPackages = e.getMissingPackages();
    Color c = null;
    if (missingPackages != null && missingPackages.length > 0) {
      for (int i = 0; i < missingPackages.length; i++) {
        if (!missingPackages[i].isOptional()) {
          c = colorError;
          break;
        }
      }
    }
    return c;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
   */
  public Color getBackground(Object o)
  {
    return null;
  }
}
