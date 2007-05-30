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

package org.knopflerfish.eclipse.core.ui.launcher.main;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SystemPropertyLabelProvider implements ITableLabelProvider, IFontProvider {

  private Font  fontUser = null;

  public SystemPropertyLabelProvider() {
    
    // Create font used in property tree for non-default values
    if (fontUser == null) {
      Font font = Display.getCurrent().getSystemFont();
      FontData fontData = font.getFontData()[0];
      fontData.setStyle(SWT.BOLD);
      fontUser = new Font(Display.getCurrent(), fontData);
    }
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableLabelProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object o, int columnIndex) {
    
    if (columnIndex == 0) {
      if (o instanceof FrameworkPreference) {
        FrameworkPreference distribution = (FrameworkPreference) o;
        return distribution.getName();
      } else if (o instanceof SystemPropertyGroup) {
        SystemPropertyGroup group = (SystemPropertyGroup) o;
        return group.getName();
      } else if (o instanceof SystemProperty) {
        SystemProperty property = (SystemProperty) o;
        return property.getName();
      } else {
        return "";
      }
    } else if (columnIndex == 1) {
      if (o instanceof FrameworkPreference) {
        return "";
      } else if (o instanceof SystemPropertyGroup) {
        return "";
      } else if (o instanceof SystemProperty) {
        SystemProperty property = (SystemProperty) o;
        String value = property.getValue();
        if (value == null || value.trim().length()==0) {
          value = property.getDefaultValue();
        }
        if (value == null || value.trim().length()==0) {
          value = "";
        }
        return value;
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.IBaseLabelProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener) {
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {
    if (fontUser != null) {
      fontUser.dispose();
      fontUser = null;
     }
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

  /****************************************************************************
   * org.eclipse.jface.viewers.IFontProvider methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
   */
  public Font getFont(Object o) {
    if (o instanceof SystemProperty) {
      SystemProperty property = (SystemProperty) o;
      
      if (!MainTab.isDefaultProperty(property)) {
        return fontUser;
      }
      return null;
    } else if (o instanceof SystemPropertyGroup){
      SystemPropertyGroup group = (SystemPropertyGroup) o;
      boolean isDefault = true;
      SystemProperty[] properties = group.getProperties();
      for(int i=0; i<properties.length && isDefault; i++) {
        isDefault = MainTab.isDefaultProperty(properties[i]);
      }
      
      if (isDefault) {
        return null;
      }
      return fontUser;
    } else {
      return null;
    }
  }
}
