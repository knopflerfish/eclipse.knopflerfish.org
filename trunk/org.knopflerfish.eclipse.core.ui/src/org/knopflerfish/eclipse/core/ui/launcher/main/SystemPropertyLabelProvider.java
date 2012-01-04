/*
 * Copyright (c) 2003-2012, KNOPFLERFISH project
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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class SystemPropertyLabelProvider extends CellLabelProvider
/*
 * implements ITableLabelProvider, IFontProvider
 */
{

  private Font fontUser = null;

  public SystemPropertyLabelProvider()
  {

    // Create font used in property tree for non-default values
    if (fontUser == null) {
      Font font = Display.getCurrent().getSystemFont();
      FontData fontData = font.getFontData()[0];
      fontData.setStyle(SWT.BOLD);
      fontUser = new Font(Display.getCurrent(), fontData);
    }
  }

  // ***************************************************************************
  // org.eclipse.jface.viewers.IBaseLabelProvider methods
  // ***************************************************************************
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
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
    if (fontUser != null) {
      fontUser.dispose();
      fontUser = null;
    }
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
  // CellLabelProvider methods
  // ***************************************************************************

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object
   * )
   */
  public String getToolTipText(Object o)
  {
    if (o instanceof Property) {
      Property property = (Property) o;
      StringBuffer toolTip = new StringBuffer();
      String description = property.getDescription();
      if (description != null) {
        toolTip.append(description);
      }
      String defaultValue = property.getDefaultValue();
      if (defaultValue != null) {
        if (toolTip.length() > 0) {
          toolTip.append('\n');
        }
        toolTip.append("Default value : ");
        toolTip.append(defaultValue);
      }

      if (toolTip.length() > 0) {
        return toolTip.toString();
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.CellLabelProvider#getToolTipShift(java.lang.Object
   * )
   */
  public Point getToolTipShift(Object object)
  {
    return new Point(5, 5);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.CellLabelProvider#getToolTipDisplayDelayTime(
   * java.lang.Object)
   */
  public int getToolTipDisplayDelayTime(Object object)
  {
    return 2000;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.CellLabelProvider#getToolTipTimeDisplayed(java
   * .lang.Object)
   */
  public int getToolTipTimeDisplayed(Object object)
  {
    return 5000;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers
   * .ViewerCell)
   */
  public void update(ViewerCell cell)
  {
    int columnIndex = cell.getColumnIndex();
    Object o = cell.getElement();
    // Font
    Font font = null;
    if (o instanceof Property) {
      Property property = (Property) o;

      if (!MainTab.isDefaultProperty(property)) {
        font = fontUser;
      } else {
        font = null;
      }
    } else if (o instanceof PropertyGroup) {
      PropertyGroup group = (PropertyGroup) o;
      boolean isDefault = true;
      Property[] properties = group.getProperties();
      for (int i = 0; i < properties.length && isDefault; i++) {
        isDefault = MainTab.isDefaultProperty(properties[i]);
      }

      if (isDefault) {
        font = null;
      } else {
        font = fontUser;
      }
    } else {
      font = null;
    }
    cell.setFont(font);

    // Text
    String text = "";
    if (columnIndex == 0) {
      if (o instanceof FrameworkPreference) {
        FrameworkPreference distribution = (FrameworkPreference) o;
        text = distribution.getName();
      } else if (o instanceof PropertyGroup) {
        PropertyGroup group = (PropertyGroup) o;
        text = group.getName();
      } else if (o instanceof Property) {
        Property property = (Property) o;
        text = property.getName();
      } else {
        text = "";
      }
    } else if (columnIndex == 1) {
      if (o instanceof FrameworkPreference) {
        text = "";
      } else if (o instanceof PropertyGroup) {
        text = "";
      } else if (o instanceof Property) {
        Property property = (Property) o;
        String value = property.getValue();
        if (value == null || value.trim().length() == 0) {
          value = property.getDefaultValue();
        }
        if (value == null || value.trim().length() == 0) {
          value = "";
        }
        text = value;
      } else {
        text = "";
      }
    } else if (columnIndex == 2) {
      if (o instanceof FrameworkPreference) {
        text = "";
      } else if (o instanceof PropertyGroup) {
        text = "";
      } else if (o instanceof Property) {
        Property property = (Property) o;
        String type = property.getType();
        text = type;
      } else {
        text = "";
      }
    } else {
      text = "";
    }
    cell.setText(text);
  }
}
