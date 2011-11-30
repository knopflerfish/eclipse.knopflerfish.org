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

package org.knopflerfish.eclipse.repository.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;
import org.knopflerfish.eclipse.core.IBundleRepositoryConfig;

public class BundleRepositoryConfig implements IBundleRepositoryConfig
{
  
  private final List<ModifyListener> listeners = new ArrayList<ModifyListener>();

  // Widgets
  private Composite    wConfigControl;
  private Text         wDirectoryText;
  private Button       wDirectoryButton;
  private String value;

  //***************************************************************************
  // IBundleRepositoryConfig methods
  //***************************************************************************
  
  public Control createConfigArea(Composite parent)
  {
    if (wConfigControl != null) {
      return wConfigControl;
    }
    
    wConfigControl = new Composite(parent, SWT.NONE);
    
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    wConfigControl.setLayout(layout);
    
    // Library path
    wDirectoryText = new Text(wConfigControl, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    wDirectoryText.setLayoutData(data);
    wDirectoryText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        updateValue(e);
      }
    });
    setValue(value);
    
    wDirectoryButton = new Button(wConfigControl, SWT.NONE);
    wDirectoryButton.setText("Browse...");
    wDirectoryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(((Button) e.widget).getShell());
        String path = dialog.open();
        if (path != null) {
          wDirectoryText.setText(path);
        }
      }
    });
    
    return wConfigControl;
  }

  public void setValue(String v)
  {
    value = v;
    if (wDirectoryText == null) {
      return;
    }
    if (value == null) {
      wDirectoryText.setText("");
    } else if (wDirectoryText.getText() != value) {
      wDirectoryText.setText(value);
    }
  }

  public String getValue()
  {
    return value;
  }

  public boolean isValid()
  {
    // Check that configuration is a valid directory
    try {
      File f = new File(getValue());
      return (f.exists() && f.isDirectory());
    } catch (Throwable t) {
      return false;
    }
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
    value = wDirectoryText.getText();
    for(Iterator<ModifyListener> i=listeners.iterator(); i.hasNext();) {
      ModifyListener l = i.next();
      try {
        l.modifyText(e);
      } catch (Throwable t) {
        // Ignore
      }
    }
  }

}
