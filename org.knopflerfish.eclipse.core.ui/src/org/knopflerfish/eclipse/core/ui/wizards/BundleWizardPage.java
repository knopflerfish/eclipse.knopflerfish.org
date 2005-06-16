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

package org.knopflerfish.eclipse.core.ui.wizards;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author ar
 */
public class BundleWizardPage extends WizardPage {
  private static final String PROP_INITIALIZED = "initialized";
  
  private static final String DEFAULT_BUNDLE_VERSION = "1.0";
  private static final String DEFAULT_ACTIVATOR_CLASS_NAME = "Activator";
  
  private static final String ERROR     = "error";
  private static final String WARNING   = "warning";

  private int MARGIN_HEIGHT = 11;
  private int MARGIN_WIDTH = 9;
  private int MARGIN = 5;

  private ISelection selection;
  private ProjectWizardPage projectPage;
  
  // Widgets
  private Text    wBundleNameText;
  private Text    wBundleSymbolicNameText;
  private Text    wBundleVersionText;
  private Text    wBundleDescriptionText;
  private Text    wBundleVendorText;
  private Button  wCreateBundleActivatorButton;
  private Label   wBundleActivatorPackageLabel;
  private Text    wBundleActivatorPackageText;
  private Label   wBundleActivatorClassLabel;
  private Text    wBundleActivatorClassText;

  
  public BundleWizardPage(ISelection selection, ProjectWizardPage projectPage) {
    super("bundleWizardPage");
    setTitle("Bundle Data");
    setDescription("Enter bundle data.");
    this.selection = selection;
    this.projectPage = projectPage;
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.IDialogPage methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginHeight = MARGIN_HEIGHT;
    layout.marginWidth = MARGIN_WIDTH;
    layout.verticalSpacing = 15;
    layout.numColumns = 2;
    
    composite.setLayout(layout);

    // Bundle Manifest Group
    Group wBundleManifestGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wBundleManifestGroup.setLayout(layout);
    wBundleManifestGroup.setText("Bundle Manifest");
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wBundleManifestGroup.setLayoutData(gd);
    
    // Bundle Symbolic Name
    Label wBundleSymbolicNameLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleSymbolicNameLabel.setText("Bundle Symbolic Name:");

    wBundleSymbolicNameText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleSymbolicNameText.setData(PROP_INITIALIZED, new Boolean(false));
    wBundleSymbolicNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (!((Boolean) e.widget.getData(PROP_INITIALIZED)).booleanValue()) {
          e.widget.setData(PROP_INITIALIZED, new Boolean(true));
        }
        if (!((Boolean) wBundleActivatorPackageText.getData(PROP_INITIALIZED)).booleanValue()) {
          wBundleActivatorPackageText.setText(wBundleSymbolicNameText.getText());
          wBundleActivatorPackageText.setData(PROP_INITIALIZED, new Boolean(false));
        }
        verifyBundleSymbolicName();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleSymbolicNameText.setLayoutData(gd);
    
    // Bundle Version
    Label wBundleVersionLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleVersionLabel.setText("Bundle Version:");

    wBundleVersionText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleVersionText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyBundleVersion();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleVersionText.setLayoutData(gd);
    
    // Bundle Name
    Label wBundleNameLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleNameLabel.setText("Bundle Name:");

    wBundleNameText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleNameText.setData(PROP_INITIALIZED, new Boolean(false));
    wBundleNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (!((Boolean) e.widget.getData(PROP_INITIALIZED)).booleanValue()) {
          e.widget.setData(PROP_INITIALIZED, new Boolean(true));
        }
        verifyBundleName();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleNameText.setLayoutData(gd);

    // Bundle Description
    Label wBundleDescriptionLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleDescriptionLabel.setText("Bundle Description:");

    wBundleDescriptionText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleDescriptionText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyBundleDescription();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleDescriptionText.setLayoutData(gd);
    
    // Bundle Vendor
    Label wBundleVendorLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleVendorLabel.setText("Bundle Vendor:");

    wBundleVendorText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleVendorText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyBundleVendor();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleVendorText.setLayoutData(gd);

    // Bundle Acivator Group
    Group wBundleActivatorGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wBundleActivatorGroup.setLayout(layout);
    wBundleActivatorGroup.setText("Bundle Activator");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wBundleActivatorGroup.setLayoutData(gd);

    // Create activator
    wCreateBundleActivatorButton = new Button(wBundleActivatorGroup, SWT.CHECK);
    wCreateBundleActivatorButton.setText("Create BundleActivator");
    wCreateBundleActivatorButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateActivatorControls();
        verifyActivatorPackageName();
        verifyActivatorClassName();
        updateStatus();
      }
    });
    wCreateBundleActivatorButton.setSelection(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wCreateBundleActivatorButton.setLayoutData(gd);
    //wCreateBundleActivatorButton.setSelection(true);

    // BundleActivator package
    wBundleActivatorPackageLabel = new Label(wBundleActivatorGroup, SWT.LEFT);
    wBundleActivatorPackageLabel.setText("Package:");

    wBundleActivatorPackageText = new Text(wBundleActivatorGroup, SWT.SINGLE | SWT.BORDER);
    wBundleActivatorPackageText.setData(PROP_INITIALIZED, new Boolean(false));
    wBundleActivatorPackageText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (!((Boolean) e.widget.getData(PROP_INITIALIZED)).booleanValue()) {
          e.widget.setData(PROP_INITIALIZED, new Boolean(true));
        }
        verifyActivatorPackageName();
        updateStatus();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleActivatorPackageText.setLayoutData(gd);

    // BundleActivator class
    wBundleActivatorClassLabel = new Label(wBundleActivatorGroup, SWT.LEFT);
    wBundleActivatorClassLabel.setText("Name:");

    wBundleActivatorClassText = new Text(wBundleActivatorGroup, SWT.SINGLE | SWT.BORDER);
    wBundleActivatorClassText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyActivatorClassName();
        updateStatus();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleActivatorClassText.setLayoutData(gd);
    
    verifyAll();
    updateActivatorControls();
    setControl(composite);
    updateStatus();
    
    // Set default values
    wBundleVersionText.setText(DEFAULT_BUNDLE_VERSION);
    wBundleActivatorClassText.setText(DEFAULT_ACTIVATOR_CLASS_NAME);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
   */
  public void setVisible(boolean visible) {
    // Set default bundle name, but only first time it is shown
    if (visible && !((Boolean) wBundleNameText.getData(PROP_INITIALIZED)).booleanValue()) {
      wBundleNameText.setText(projectPage.getProjectName());
      wBundleNameText.setData(PROP_INITIALIZED, new Boolean(false));
    }
    // Set default bundle name, but only first time it is shown
    if (visible && !((Boolean) wBundleSymbolicNameText.getData(PROP_INITIALIZED)).booleanValue()) {
      wBundleSymbolicNameText.setText(projectPage.getProjectName());
      wBundleSymbolicNameText.setData(PROP_INITIALIZED, new Boolean(false));
    }
    super.setVisible(visible);
  }
  
  /****************************************************************************
   * UI control methods
   ***************************************************************************/
  private void updateActivatorControls() {
    boolean enable = wCreateBundleActivatorButton.getSelection();
    // Enable/disbale manual location widgets
    wBundleActivatorClassLabel.setEnabled(enable);
    wBundleActivatorClassText.setEnabled(enable);
    wBundleActivatorPackageLabel.setEnabled(enable);
    wBundleActivatorPackageText.setEnabled(enable);
  }
  
  private void updateStatus() {
    
    Control c = getControl();
    Composite composite = (Composite) c;
    composite.getChildren();
    
    // Loop through widgets checking for errors
    String error = (String) getData(getControl(), ERROR);
    if (error != null) {
      setPageComplete(false);
      setMessage(error, DialogPage.ERROR);
      return;
    }
    
    // Loop through widgets checking for warnings
    String warning = (String) getData(getControl(), WARNING);
    if (warning != null) {
      setPageComplete(true);
      setMessage(warning, DialogPage.WARNING);
      return;
    }

    // Everything is ok
    setPageComplete(true);
    setMessage(null);
  }
  
  private Object getData(Control c, String key) {
    if (c == null || key == null) return null;
    
    // Check if this control contains the key
    Object data = c.getData(key);
    if (data != null) return data;
    
    if (c instanceof Composite) {
      // Check if any children contain the key
      Control [] children = ((Composite) c).getChildren();
      if (children != null) {
        for(int i=0; i<children.length; i++) {
          data = getData(children[i], key);
          if (data != null) return data;
        }
      }
    }
    return null;
  }
  
  /****************************************************************************
   * Verify UI Input methods
   ***************************************************************************/
  private void verifyAll() {
    verifyBundleName();
    verifyBundleVersion();
    verifyBundleDescription();
    verifyBundleVendor();
    verifyActivatorPackageName();
    verifyActivatorClassName();
  }
  
  private void verifyBundleSymbolicName() {
    String name = getBundleSymbolicName();
  }
  
  private void verifyBundleName() {
    String name = getBundleName();
    
    // TODO:Check that project name is not empty
    
    // TODO:Check that project name is valid
    
    // TODO:Check that project name not already exists
    
  }

  private void verifyBundleVersion() {
    String version = getBundleVersion();
  }
  
  private void verifyBundleDescription() {
    String description = getBundleDescription();
  }
  
  private void verifyBundleVendor() {
    String vendor = getBundleVendor();
  }

  private void verifyActivatorPackageName() {
    String packageName = getActivatorPackageName();
    if (isCreateBundleActivator()) {
      if (packageName == null || packageName.trim().length() == 0) {
        wBundleActivatorPackageText.setData(WARNING, "The use of default package is discouraged.");
        wBundleActivatorPackageText.setData(ERROR, null);
      } else if (packageName.startsWith(".") || packageName.endsWith(".")){
        wBundleActivatorPackageText.setData(WARNING, null);
        wBundleActivatorPackageText.setData(ERROR, "Package name is not valid. A package name cannot start or end with a dot.");
      } else if (Character.isUpperCase(packageName.charAt(0))) { 
        wBundleActivatorPackageText.setData(WARNING, "Package name is discouraged. By convention, package names usually start with a lowercase letter.");
        wBundleActivatorPackageText.setData(ERROR, null);
      } else {
        wBundleActivatorPackageText.setData(WARNING, null);
        wBundleActivatorPackageText.setData(ERROR, null);
      }
    } else {
      wBundleActivatorPackageText.setData(WARNING, null);
      wBundleActivatorPackageText.setData(ERROR, null);
    }
  }

  private void verifyActivatorClassName() {
    String className = getActivatorClassName();
    if (isCreateBundleActivator()) {
      if (className == null || className.trim().length() == 0) {
        wBundleActivatorClassText.setData(WARNING, null);
        wBundleActivatorClassText.setData(ERROR, "Name is empty.");
      } else if (className.indexOf(".") != -1){
        wBundleActivatorClassText.setData(WARNING, null);
        wBundleActivatorClassText.setData(ERROR, "Name must not be qualified.");
      } else if (Character.isLowerCase(className.charAt(0))){
        wBundleActivatorClassText.setData(WARNING, "Name is discouraged. By convention, Java type names usually start with an uppercase letter.");
        wBundleActivatorClassText.setData(ERROR, null);
      } else {
        wBundleActivatorClassText.setData(WARNING, null);
        wBundleActivatorClassText.setData(ERROR, null);
      }
    } else {
      wBundleActivatorClassText.setData(WARNING, null);
      wBundleActivatorClassText.setData(ERROR, null);
    }
  }
  
  /****************************************************************************
   * Getters/Setters
   ***************************************************************************/
  public String getBundleSymbolicName() {
    String val = null;
    if (!((Boolean) wBundleSymbolicNameText.getData(PROP_INITIALIZED)).booleanValue()) {
      val=projectPage.getProjectName();
    } else {
      val=wBundleSymbolicNameText.getText();
    }
    
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }
  
  public String getBundleName() {
    String val = null;
    if (!((Boolean) wBundleNameText.getData(PROP_INITIALIZED)).booleanValue()) {
      val=projectPage.getProjectName();
    } else {
      val=wBundleNameText.getText();
    }
    
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }
  
  public String getBundleVersion() {
    String val = wBundleVersionText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }

  public String getBundleDescription() {
    String val = wBundleDescriptionText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }

  public String getBundleVendor() {
    String val = wBundleVendorText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }

  public String getActivatorPackageName() {
    String val = wBundleActivatorPackageText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }
  
  public String getActivatorClassName() {
    String val = wBundleActivatorClassText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    } else {
      return val.trim();
    }
  }
  
  public boolean isCreateBundleActivator() {
    return wCreateBundleActivatorButton.getSelection();
  }
}
