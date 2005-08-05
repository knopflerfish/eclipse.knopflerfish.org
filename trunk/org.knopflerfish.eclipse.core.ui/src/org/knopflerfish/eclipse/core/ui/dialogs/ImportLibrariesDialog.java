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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ImportLibrariesDialog extends Dialog {

  private static final int NUM_CHARS_WIDTH = 60;
  
  private ArrayList frameworks;
  private final String title;

  private String name;
  private boolean importRuntimeLibraries;
  private boolean importBuildPathLibraries;
  private boolean importBundles;
  private boolean onlyUserDefined;
  
  // Widgets
  private Combo  wFrameworkCombo;
  private Button wImportRuntimeButton;
  private Button wImportBuildButton;
  private Button wImportBundleButton;
  private Button wImportUserButton;

  public ImportLibrariesDialog(Shell parentShell, ArrayList frameworks, String title) {
    super(parentShell);

    this.frameworks = frameworks;
    this.title = title;
  }

  /****************************************************************************
   * org.eclipse.jface.window.Window Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    
    setValues(frameworks);
    
    return c;
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.Dialog Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {

    name = wFrameworkCombo.getItem(wFrameworkCombo.getSelectionIndex());
    importRuntimeLibraries = wImportRuntimeButton.getSelection();
    importBuildPathLibraries = wImportBuildButton.getSelection();
    importBundles = wImportBundleButton.getSelection();
    onlyUserDefined = wImportUserButton.getSelection();

    // Set return code and close window
    setReturnCode(Window.OK);
    close();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite)super.createDialogArea(parent);
    
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    composite.setLayout(layout);
    
    // Framework
    Label wFrameworkLabel = new Label(composite, SWT.LEFT);
    wFrameworkLabel.setText("Framework name:");
    wFrameworkCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wFrameworkCombo.setLayoutData(gd);

    // Import group
    Group wImportGroup = new Group(composite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.numColumns = 1;
    wImportGroup.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalIndent = 10;
    gd.horizontalSpan = 2;
    wImportGroup.setLayoutData(gd);
    wImportGroup.setText("Type of libraries to import");
    
    wImportRuntimeButton = new Button(wImportGroup, SWT.CHECK);
    wImportRuntimeButton.setText("Import runtime libraries.");

    wImportBuildButton = new Button(wImportGroup, SWT.CHECK);
    wImportBuildButton.setText("Import build path libraries.");

    wImportBundleButton = new Button(wImportGroup, SWT.CHECK);
    wImportBundleButton.setText("Import bundles.");
    
    wImportUserButton = new Button(wImportGroup, SWT.CHECK);
    wImportUserButton.setText("Only import user defined libraries.");
    gd = new GridData();
    gd.verticalIndent = 10;
    wImportUserButton.setLayoutData(gd);

    return composite;
  }
    
  /****************************************************************************
   * Private Worker Methods
   ***************************************************************************/
  
  private void setValues(ArrayList frameworks) {

    wFrameworkCombo.removeAll();
    if (frameworks != null) {
      for(int i=0; i<frameworks.size();i++) {
        wFrameworkCombo.add((String) frameworks.get(i));
      }
    }
    wFrameworkCombo.select(0);
    
    wImportRuntimeButton.setSelection(true);
    wImportBuildButton.setSelection(true);
    wImportBundleButton.setSelection(true);
    wImportUserButton.setSelection(true);
  }
  
  public String getFrameworkName() {
    return name;
  }
  
  public boolean isImportBuildPathLibraries() {
    return importBuildPathLibraries;
  }

  public boolean isImportBundles() {
    return importBundles;
  }

  public boolean isImportRuntimeLibraries() {
    return importRuntimeLibraries;
  }

  public boolean isOnlyUserDefined() {
    return onlyUserDefined;
  }
 }
