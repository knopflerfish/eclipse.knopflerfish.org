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

import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.knopflerfish.eclipse.core.BundleProject;
import org.knopflerfish.eclipse.core.IOsgiVendor;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiInstall;
import org.knopflerfish.eclipse.core.OsgiVendor;

/**
 * @author ar
 */
public class ProjectWizardPage extends WizardPage {
  private static final String DEFAULT_SRC_FOLDER = "src";
  private static final String DEFAULT_OUT_FOLDER = "out";
  
  private static final String ERROR = "error";

  private int MARGIN_HEIGHT = 11;
  private int MARGIN_WIDTH = 9;
  private int MARGIN = 5;

  private ISelection selection;
  
  // Widgets
  private Text    wProjectNameText;
  private Button  wProjectContentsDefaultButton;
  private Label   wProjectContentsDirectoryLabel;
  private Text    wProjectContentsDirectoryText;
  private Button  wProjectContentsBrowseButton;  
  private Text    wProjectSettingsSrcText;
  private Text    wProjectSettingsOutText;
  private Combo   wProjectLibrariesEnvCombo;
  

  public ProjectWizardPage(ISelection selection) {
    super("projectWizardPage");
    setTitle("Bundle Project");
    setDescription("Create a new bundle project.");
    this.selection = selection;
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

    // Project Name
    Label wProjectNameLabel = new Label(composite, SWT.LEFT);
    wProjectNameLabel.setText("Project Name:");
    wProjectNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    wProjectNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLocation();
        e.widget.setData(ERROR, verifyProjectName());
        updateStatus();
      }
    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wProjectNameText.setLayoutData(gd);
    
    // Project Contents
    Group wProjectContentsGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wProjectContentsGroup.setLayout(layout);
    wProjectContentsGroup.setText("Project Contents");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wProjectContentsGroup.setLayoutData(gd);
    
    wProjectContentsDefaultButton = new Button(wProjectContentsGroup, SWT.CHECK);
    wProjectContentsDefaultButton.setText("Use default");
    wProjectContentsDefaultButton.setSelection(true);
    
    wProjectContentsDefaultButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        boolean selection = ((Button) e.widget).getSelection();
        // Enable/disbale manual location widgets
        wProjectContentsDirectoryLabel.setEnabled(!selection);
        wProjectContentsDirectoryText.setEnabled(!selection);
        wProjectContentsBrowseButton.setEnabled(!selection);
        updateLocation();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wProjectContentsDefaultButton.setLayoutData(gd);

    wProjectContentsDirectoryLabel = new Label(wProjectContentsGroup, SWT.LEFT);
    wProjectContentsDirectoryLabel.setText("Directory:");
    wProjectContentsDirectoryLabel.setEnabled(false);

    wProjectContentsDirectoryText = new Text(wProjectContentsGroup, SWT.SINGLE | SWT.BORDER);
    wProjectContentsDirectoryText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyProjectLocation();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wProjectContentsDirectoryText.setLayoutData(gd);
    wProjectContentsDirectoryText.setEnabled(false);
    
    wProjectContentsBrowseButton = new Button(wProjectContentsGroup, SWT.CENTER);
    wProjectContentsBrowseButton.setText("Browse...");
    wProjectContentsBrowseButton.setEnabled(false);
    wProjectContentsBrowseButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(((Button) e.widget).getShell());
        String path = dialog.open();
        if (path != null) {
          wProjectContentsDirectoryText.setText(path);
          verifyProjectLocation();
        }
      }
    });

    
    // Project Settings
    Group wProjectSettingsGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wProjectSettingsGroup.setLayout(layout);
    wProjectSettingsGroup.setText("Project Settings");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wProjectSettingsGroup.setLayoutData(gd);
    
    Label wProjectSettingsSrcLabel = new Label(wProjectSettingsGroup, SWT.LEFT);
    wProjectSettingsSrcLabel.setText("Source Folder:");
    wProjectSettingsSrcText = new Text(wProjectSettingsGroup, SWT.SINGLE | SWT.BORDER);
    wProjectSettingsSrcText.setText(DEFAULT_SRC_FOLDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wProjectSettingsSrcText.setLayoutData(gd);
    
    Label wProjectSettingsOutLabel = new Label(wProjectSettingsGroup, SWT.LEFT);
    wProjectSettingsOutLabel.setText("Output Folder");
    wProjectSettingsOutText = new Text(wProjectSettingsGroup, SWT.SINGLE | SWT.BORDER);
    wProjectSettingsOutText.setText(DEFAULT_OUT_FOLDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wProjectSettingsOutText.setLayoutData(gd);
    
    // Project Libraries
    Group wProjectLibrariesGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wProjectLibrariesGroup.setLayout(layout);
    wProjectLibrariesGroup.setText("Project Libraries");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wProjectLibrariesGroup.setLayoutData(gd);
    
    Label wProjectLibrariesEnvLabel = new Label(wProjectLibrariesGroup, SWT.LEFT);
    wProjectLibrariesEnvLabel.setText("OSGi Environment:");
    wProjectLibrariesEnvCombo = new Combo(wProjectLibrariesGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    IOsgiVendor v = Osgi.getVendor(OsgiVendor.VENDOR_NAME);
    List l = null;
    int selectIdx = 0;
    if (v != null && (l = v.getOsgiInstalls()) != null) {
      for(int i=0; i<l.size();i++) {
        OsgiInstall osgiInstall = (OsgiInstall) l.get(i);
        
        wProjectLibrariesEnvCombo.add(osgiInstall.getName());
        if (osgiInstall.isDefaultDefinition()) selectIdx=i;
      }
    }
    wProjectLibrariesEnvCombo.select(selectIdx);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wProjectLibrariesEnvCombo.setLayoutData(gd);
    
    // Update state
    wProjectNameText.setData(ERROR, verifyProjectName());
    updateLocation();
    updateStatus();
    
    setControl(composite);
  }
  
  /****************************************************************************
   * UI control methods
   ***************************************************************************/
  private void updateLocation() {
    if (isDefaultProjectLocation()) {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IPath path = root.getLocation();
      path = path.append(getProjectName());
      wProjectContentsDirectoryText.setText(path.toString());
    }
  }
  
  private void updateStatus() {
    
    // Check that widgets contains valid values
    
    String error = (String) wProjectNameText.getData(ERROR);
    if (error != null) {
      setPageComplete(false);
      //setErrorMessage(error);
      //setMessage(error, DialogPage.WARNING);
      setMessage(error, DialogPage.ERROR);
      return;
    }
    
    setPageComplete(true);
    setMessage(null);
  }
  
  /****************************************************************************
   * Verify UI Input methods
   ***************************************************************************/
  private String verifyProjectName() {
    String name = getProjectName();
    String error = null;

    // TODO:Check that project name is not empty
    if (name == null || name.trim().length() == 0) {
      error = "Enter project name";
    }
    
    // TODO:Check that project name is valid
    
    // TODO:Check that project name not already exists
    
    return error;
  }
  
  private boolean verifyProjectLocation() {
    String location = getProjectLocation();
    
    // TODO:Check that project location is valid folder name
    
    // TODO:Check that project location not already exists
    
    return true;
  }

  /****************************************************************************
   * Getters/Setters
   ***************************************************************************/
  protected String getProjectName() {
    return wProjectNameText.getText();
  }
  
  protected boolean isDefaultProjectLocation() {
    return wProjectContentsDefaultButton.getSelection();
  }

  protected String getProjectLocation() {
    return wProjectContentsDirectoryText.getText();
  }
  
  protected String getSourceFolder() {
    return wProjectSettingsSrcText.getText();
  }

  protected String getOutputFolder() {
    return wProjectSettingsOutText.getText();
  }
  
  protected String getEnvironmentName() {
    return wProjectLibrariesEnvCombo.getText();
  }

  public BundleProject getProject() {
    BundleProject project = new BundleProject(getProjectName());
    if (isDefaultProjectLocation()) {
      project.setLocation(null);
    } else {
      project.setLocation(new Path(getProjectLocation()));
    }
    
    project.setSourceFolder(new Path(getSourceFolder()));
    project.setOutputFolder(new Path(getOutputFolder()));
    IOsgiVendor v = Osgi.getVendor(OsgiVendor.VENDOR_NAME);
    project.setOsgiInstall(v.getOsgiInstall(getEnvironmentName()));
    
    return project;
  }
}
