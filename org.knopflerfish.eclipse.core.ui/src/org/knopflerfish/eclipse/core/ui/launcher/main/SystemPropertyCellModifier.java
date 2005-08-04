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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;

public class SystemPropertyCellModifier implements ICellModifier {
  
  private MainTab mainTab;
  
  public SystemPropertyCellModifier(MainTab mainTab) {
    this.mainTab = mainTab;
  }
  public boolean canModify(Object element, String cellProperty) {
    if (element instanceof IOsgiInstall) {
      return false;
    } else if (element instanceof SystemPropertyGroup) {
      return false;
    } else if (element instanceof SystemProperty) {
      SystemProperty p = (SystemProperty) element;
      if (MainTab.PROP_VALUE.equals(cellProperty)) {
        return true;
      } else if (MainTab.PROP_NAME.equals(cellProperty) && 
          MainTab.USER_GROUP.equals(p.getSystemPropertyGroup().getName())) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
  public Object getValue(Object element, String cellProperty) {
    SystemProperty property = (SystemProperty) element;
    if (MainTab.PROP_NAME.equals(cellProperty)) {
      return property.getName();
    } else if (MainTab.PROP_VALUE.equals(cellProperty)) {
      String value = property.getValue();
      if (value == null || value.trim().length()==0) {
        value = property.getDefaultValue();
      }
      if (value == null || value.trim().length()==0) {
        value = "";
      }
      return value;
    } else {
      return null;
    }
  }
  
  public void modify(Object element, String cellProperty, Object value) {
    if (element instanceof Item && ((Item) element).getData() instanceof SystemProperty) {
      SystemProperty property = (SystemProperty) ((Item) element).getData();
      if (MainTab.PROP_NAME.equals(cellProperty)) {
        property.setName((String) value);
        mainTab.update(property);
      } else if (MainTab.PROP_VALUE.equals(cellProperty)) {
        property.setValue((String) value);
        mainTab.update(property);
        mainTab.update(property.getSystemPropertyGroup());
      }
    }
  }
  
}
