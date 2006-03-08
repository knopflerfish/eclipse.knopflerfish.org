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

package org.knopflerfish.eclipse.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageSelectionDialog extends ElementListSelectionDialog {

  private Map packages;
  private Version version = Version.emptyVersion;
  
  // Widgets
  private Combo wVersionCombo;
  
  public PackageSelectionDialog(Shell activeShell, PackageLabelProvider provider) {
    super(activeShell, provider);
  }

  public void setPackages(Map map) {
    packages = map;
    setElements(map.keySet().toArray(new String[map.size()]));
  }
  
  
  public Version getVersion() {
    return version;
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.Dialog methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {
    Composite wDialogComposite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    wDialogComposite.setLayout(layout);
    
    // Add default dialog area
    super.createDialogArea(wDialogComposite);
    
    // Version composite
    Composite wVersionComposite = new Composite(wDialogComposite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wVersionComposite.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wVersionComposite.setLayoutData(gd);
    Label wVersionLabel = new Label(wVersionComposite, SWT.LEFT);
    wVersionLabel.setText("Version:");
    wVersionCombo = new Combo(wVersionComposite, SWT.DROP_DOWN);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wVersionCombo.setLayoutData(gd);
    
    return wDialogComposite;
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    // Set version
    if (!wVersionCombo.isEnabled()) {
      version = Version.emptyVersion;
    } else {
      try {
        version = Version.parseVersion(wVersionCombo.getText());
      } catch (IllegalArgumentException e) {
        version = Version.emptyVersion;
      }
    }
    
    super.okPressed();
  }
    
  /****************************************************************************
   * org.eclipse.ui.dialogs.AbstractElementListSelectionDialog methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#handleSelectionChanged()
   */
  protected void handleSelectionChanged() {
    super.handleSelectionChanged();
    
    Object[] elements = getSelectedElements();
    if (elements == null || elements.length != 1) {
      wVersionCombo.setEnabled(false);
    } else {
      try {
        wVersionCombo.setEnabled(true);
        String name =(String) elements[0];
        List l = (List) packages.get(name);
        ArrayList versions = new ArrayList();
        if (l != null) {
          for (Iterator i=l.iterator(); i.hasNext(); ) {
            PackageDescription pd = (PackageDescription) i.next();
            Version version = pd.getSpecificationVersion();
            if (version != null && !version.equals(Version.emptyVersion)) {
              versions.add(version.toString());
            }
          }
        }
        wVersionCombo.setItems((String[]) versions.toArray(new String[versions.size()]));
      } catch (Throwable t) {
        wVersionCombo.setEnabled(false);
      }
    }
  }

}
