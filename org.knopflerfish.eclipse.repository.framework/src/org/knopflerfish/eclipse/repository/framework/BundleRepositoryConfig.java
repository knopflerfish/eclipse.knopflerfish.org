/*
 * Copyright (c) 2011-2011, KNOPFLERFISH project
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

package org.knopflerfish.eclipse.repository.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.knopflerfish.eclipse.core.IBundleRepositoryConfig;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

public class BundleRepositoryConfig implements IBundleRepositoryConfig
{
  
  private final List<ModifyListener> listeners = new ArrayList<ModifyListener>();

  private Combo   wConfigCombo;
  private String value;

  //***************************************************************************
  // IBundleRepositoryConfig methods
  //***************************************************************************
  
  public Control createConfigArea(Composite parent)
  {
    if (wConfigCombo != null) {
      return wConfigCombo;
    }
    wConfigCombo = new Combo(parent, SWT.DROP_DOWN);
    wConfigCombo.setItems(getConfigSuggestions());
    wConfigCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        updateValue(e);
      }
    });
    setValue(value);
    
    return wConfigCombo;
  }

  public void setValue(String v)
  {
    value = v;
    if (wConfigCombo == null) {
      return;
    }
    if (value == null) {
      wConfigCombo.select(0);
    } else if (wConfigCombo.getText() != value) {
      wConfigCombo.select(wConfigCombo.indexOf(value));
    }
  }

  public String getValue()
  {
    return value;
  }

  public boolean isValid()
  {
    // Check that configuration is a valid framework name
    return (OsgiPreferences.getFramework(getValue()) != null);
  }

  public void addModifyListener(ModifyListener l)
  {
    if (!listeners.contains(l)) {
      listeners.add(l);
    }
  }

  public void removeModifyListener(ModifyListener l)
  {
    listeners.remove(l);
  }

  //***************************************************************************
  // Private Utility Methods
  //***************************************************************************
  private void updateValue(ModifyEvent e) {
    value = wConfigCombo.getText(); 
    for(Iterator<ModifyListener> i=listeners.iterator(); i.hasNext();) {
      ModifyListener l = i.next();
      try {
        l.modifyText(e);
      } catch (Throwable t) {
        // Ignore
      }
    }
  }

  private String[] getConfigSuggestions() {
    List<String> names = new ArrayList<String>();
    FrameworkPreference[] frameworks = OsgiPreferences.getFrameworks();
    for(int i=0; i<frameworks.length;i++) {
      names.add(frameworks[i].getName());
    }
    return names.toArray(new String[names.size()]);
  }

}
