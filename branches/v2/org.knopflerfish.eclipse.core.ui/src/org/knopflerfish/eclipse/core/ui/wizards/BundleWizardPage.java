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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.SymbolicName;
import org.knopflerfish.eclipse.core.project.ProjectUtil;
import org.osgi.framework.Version;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @author Mats-Ola Persson, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleWizardPage extends WizardPage {
  private static final String PROP_INITIALIZED = "initialized";
  
  private static final String DEFAULT_BUNDLE_VERSION = "1.0";
  private static final String DEFAULT_ACTIVATOR_CLASS_NAME = "Activator";
  
  private static final String[] BUNDLE_MANIFEST_VERSIONS = {"2", ""};

  private int MARGIN_HEIGHT = 11;
  private int MARGIN_WIDTH = 9;
  private int MARGIN = 5;

  private ProjectWizardPage projectPage;
  
  // Widgets
  private Text    wBundleNameText;
  Text    wBundleSymbolicNameText;
  private Text    wBundleVersionText;
  private Text    wBundleDescriptionText;
  private Text    wBundleVendorText;
  private Button  wCreateBundleActivatorButton;
  private Label   wBundleActivatorPackageLabel;
  Text    wBundleActivatorPackageText;
  private Label   wBundleActivatorClassLabel;
  private Text    wBundleActivatorClassText;
  private Combo   wBundleManifestVersionMenu;

  
  public BundleWizardPage(ISelection selection, ProjectWizardPage projectPage) {
    super("bundleWizardPage");
    setTitle("Bundle Data");
    setDescription("Enter bundle data.");
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
          e.widget.setData(PROP_INITIALIZED, Boolean.TRUE);
        }
        if (!((Boolean) wBundleActivatorPackageText.getData(PROP_INITIALIZED)).booleanValue()) {
          wBundleActivatorPackageText.setText(wBundleSymbolicNameText.getText());
          wBundleActivatorPackageText.setData(PROP_INITIALIZED, Boolean.FALSE);
        }
        verify();
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
        verify();
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
        verify();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleNameText.setLayoutData(gd);
    
    // Bundle Manifest Version
    Label wBundleManifestVersionNameLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleManifestVersionNameLabel.setText("Manifest Version:");
    wBundleManifestVersionMenu = new Combo(wBundleManifestGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
    wBundleManifestVersionMenu.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        verify();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        verify();
      }
    });
    for (int i = 0; i < BUNDLE_MANIFEST_VERSIONS.length; i++) {
      wBundleManifestVersionMenu.add(BUNDLE_MANIFEST_VERSIONS[i]);
    }
//    gd = new GridData(GridData.FILL_HORIZONTAL);
//    wBundleManifestVersionMenu.setLayoutData(gd);
    wBundleManifestVersionMenu.select(0);

    // Bundle Description
    Label wBundleDescriptionLabel = new Label(wBundleManifestGroup, SWT.LEFT);
    wBundleDescriptionLabel.setText("Bundle Description:");

    wBundleDescriptionText = new Text(wBundleManifestGroup, SWT.SINGLE | SWT.BORDER);
    wBundleDescriptionText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verify();
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
       verify();
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
        verify();
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
        verify();
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
        verify();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleActivatorClassText.setLayoutData(gd);
   
    // Set default values
    wBundleVersionText.setText(DEFAULT_BUNDLE_VERSION);
    wBundleActivatorClassText.setText(DEFAULT_ACTIVATOR_CLASS_NAME);
    
    verify();
    updateActivatorControls();
    setControl(composite);
    
   
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
  void updateActivatorControls() {
    boolean enable = wCreateBundleActivatorButton.getSelection();
    // Enable/disbale manual location widgets
    wBundleActivatorClassLabel.setEnabled(enable);
    wBundleActivatorClassText.setEnabled(enable);
    wBundleActivatorPackageLabel.setEnabled(enable);
    wBundleActivatorPackageText.setEnabled(enable);
  }
  
  /****************************************************************************
   * Verify UI Input methods
   ***************************************************************************/
  
  private void verify() {
    Issue[] issues = 
      new Issue[] { verifyBundleName(), verifyBundleVersion(),
         verifyBundleDescription(), verifyBundleVendor(), 
         verifyActivatorPackageName(), verifyActivatorClassName(), 
         verifyBundleSymbolicName(), isR4() ? verifyR4() : null };
    
    boolean error = false;
    for (int i = 0; i < issues.length; i++) {
      if (issues[i] != null && issues[i].severity == IMessageProvider.ERROR) {
        setMessage(issues[i].msg, IMessageProvider.ERROR);
        error = true;
      }
    }
    
    for (int i = 0; i < issues.length; i++) {
      if (issues[i] != null && issues[i].severity == IMessageProvider.WARNING) {
        setMessage(issues[i].msg, IMessageProvider.WARNING);
      }
    }
    
    setPageComplete(!error);
    setMessage(null);
  }
  
  Issue verifyBundleSymbolicName() {
    String name = wBundleSymbolicNameText.getText();
    if (name == null || name.trim().length()== 0) {
      return new Issue("Symbolic name must be set.", IMessageProvider.ERROR);
    } else {
      IStatus status = JavaConventions.validatePackageName(name);
      if (status.getSeverity() == IStatus.ERROR) {
        if (isR4()) {
          String errorMsg = "Symbolic name must be based on the reverse domain name convention.";
          return new Issue(errorMsg, IMessageProvider.ERROR);
        } else {
          String errorMsg = "Symbolic name should be based on the reverse domain name convention.";
          return new Issue(errorMsg, IMessageProvider.WARNING);
        }
      }
    }
    return null;
  }
  
  Issue verifyBundleName() {
    //String name = getBundleName();
    
    // TODO:Check that project name is not empty
    
    // TODO:Check that project name is valid
    
    // TODO:Check that project name not already exists
    return null;
  }

  Issue verifyBundleVersion() {
    String version = wBundleVersionText.getText();
    try { 
      Version.parseVersion(version);
      return null;
    } catch (IllegalArgumentException e) {
      return new Issue("Version improperly formatted, format major('.'minor('.'micro('.'qualifier)?)?)?", IMessageProvider.ERROR);
    }
  }
  
  Issue verifyR4() {
    String version = wBundleVersionText.getText();
    String symbolicName = wBundleSymbolicNameText.getText();
    IBundleProject[] projects = ProjectUtil.getBundleProjects();
    
    try {
      for (int i = 0; i < projects.length; i++) {
        BundleManifest manifest = projects[i].getBundleManifest();
        SymbolicName sn = manifest.getSymbolicName();
        Version ver = manifest.getVersion();
        if (sn.getSymbolicName().equals(symbolicName) &&
            new Version(version).equals(ver)) {
          return new Issue("Manifest Version 2 requires Symbolic Name and Version to be unique.", IMessageProvider.ERROR);
        } 
      }
    } catch (IllegalArgumentException e) {
      // ignore. Happens when version is not correctly parsed. 
      // Other checks will take care of this.
    }
    return null;
  }
  
  Issue verifyBundleDescription() {
    //String description = getBundleDescription();
    return null;
  }
  
  Issue verifyBundleVendor() {
    //String vendor = getBundleVendor();
    return null;
  }

  Issue verifyActivatorPackageName() {
    String packageName = getActivatorPackageName();
    if (isCreateBundleActivator()) {
      if (packageName == null || packageName.trim().length() == 0) {
        return new Issue("The use of default package is discouraged.", IMessageProvider.WARNING);
      } else {
        IStatus status = JavaConventions.validatePackageName(packageName);
        switch (status.getSeverity()) {
        case IStatus.ERROR:
          return new Issue(status.getMessage(), IMessageProvider.ERROR);
        case IStatus.WARNING:
          return new Issue(status.getMessage(), IMessageProvider.WARNING);
        default:
          return null;
        }
      }
    }
    return null;
  }

  Issue verifyActivatorClassName() {
    String className = getActivatorClassName();
    if (isCreateBundleActivator()) {
      IStatus status = JavaConventions.validateJavaTypeName(className);
      switch (status.getSeverity()) {
      case IStatus.ERROR:
        return new Issue(status.getMessage(), IMessageProvider.ERROR);
      case IStatus.WARNING:
        return new Issue(status.getMessage(), IMessageProvider.WARNING);
      default:
      }
    }
    return null;
  }
  
  /****************************************************************************
   * Getters/Setters
   ***************************************************************************/
  public SymbolicName getBundleSymbolicName() {
    String val = null;
    if (!((Boolean) wBundleSymbolicNameText.getData(PROP_INITIALIZED)).booleanValue()) {
      val=projectPage.getProjectName();
    } else {
      val=wBundleSymbolicNameText.getText();
    }
    
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return new SymbolicName(val.trim());
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
    }
    return val.trim();
  }
  
  public Version getBundleVersion() {
    String val = wBundleVersionText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return Version.parseVersion(val.trim());
  }

  public String getBundleDescription() {
    String val = wBundleDescriptionText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return val.trim();
  }

  public String getBundleVendor() {
    String val = wBundleVendorText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return val.trim();
  }

  public String getActivatorPackageName() {
    String val = wBundleActivatorPackageText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return val.trim();
  }
  
  public String getActivatorClassName() {
    String val = wBundleActivatorClassText.getText();
    if (val == null || val.trim().length()==0) {
      return null;
    }
    return val.trim();
  }
  
  public boolean isCreateBundleActivator() {
    return wCreateBundleActivatorButton.getSelection();
  }

  public String getBundleManifestVersion() {
    return wBundleManifestVersionMenu.getText().trim();
  }
  
  private boolean isR4() {
    return "2".equals(getBundleManifestVersion());
  }
  
  private static class Issue {
    final String msg;
    final int severity;
    
    Issue(String msg, int severity) {
      this.msg = msg;
      this.severity = severity;
    }
  }
}
