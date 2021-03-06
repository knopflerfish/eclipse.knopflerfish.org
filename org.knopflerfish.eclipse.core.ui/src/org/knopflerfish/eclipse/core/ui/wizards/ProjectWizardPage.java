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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ProjectWizardPage extends WizardPage {
  private static final String DEFAULT_SRC_FOLDER = "src";
  private static final String DEFAULT_OUT_FOLDER = "out";
  
  private static final String ERROR     = "error";
  private static final String WARNING   = "warning";

  private int MARGIN_HEIGHT = 11;
  private int MARGIN_WIDTH = 9;
  private int MARGIN = 5;

  // Widgets
  private Text    wProjectNameText;
  private Button  wProjectContentsDefaultButton;
  Label   wProjectContentsDirectoryLabel;
  Text    wProjectContentsDirectoryText;
  Button  wProjectContentsBrowseButton;  
  private Text    wProjectSettingsSrcText;
  private Text    wProjectSettingsOutText;
  Label   wExecutionEnvironmentLabel;
  Combo   wExecutionEnvironmentCombo;
  private Button  wExecutionEnvironmentDefaultButton;
  Label   wFrameworkLabel;
  Combo   wFrameworkCombo;
  private Button  wFrameworkDefaultButton;
  

  public ProjectWizardPage(ISelection selection) {
    super("projectWizardPage");
    setTitle("Bundle Project");
    setDescription("Create a new bundle project.");
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
        verifyAll();
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
    wProjectContentsDefaultButton.setText("Use default ");
    wProjectContentsDefaultButton.setSelection(true);
    
    wProjectContentsDefaultButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        boolean selection = ((Button) e.widget).getSelection();
        // Enable/disbale manual location widgets
        wProjectContentsDirectoryLabel.setEnabled(!selection);
        wProjectContentsDirectoryText.setEnabled(!selection);
        wProjectContentsBrowseButton.setEnabled(!selection);
        updateLocation();
        verifyProjectLocation();
        updateStatus();
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
        updateStatus();
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
          updateStatus();
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
    Group wBuildPathGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wBuildPathGroup.setLayout(layout);
    wBuildPathGroup.setText("Build Path");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wBuildPathGroup.setLayoutData(gd);

    // Execution environment build path
    wExecutionEnvironmentDefaultButton = new Button(wBuildPathGroup, SWT.CHECK);
    wExecutionEnvironmentDefaultButton.setSelection(true);
    
    wExecutionEnvironmentDefaultButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        boolean selection = ((Button) e.widget).getSelection();
        // Enable/disbale manual location widgets
        wExecutionEnvironmentLabel.setEnabled(!selection);
        wExecutionEnvironmentCombo.setEnabled(!selection);
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wExecutionEnvironmentDefaultButton.setLayoutData(gd);
    
    wExecutionEnvironmentLabel = new Label(wBuildPathGroup, SWT.LEFT);
    wExecutionEnvironmentLabel.setText("Execution Environment:");
    wExecutionEnvironmentLabel.setEnabled(false);
    wExecutionEnvironmentCombo = new Combo(wBuildPathGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    EnvironmentPreference[] environments = OsgiPreferences.getExecutionEnvironments();
    EnvironmentPreference defaultEnvironment = null;
    if (environments.length > 0) {
      for(int i=0; i<environments.length;i++) {
        wExecutionEnvironmentCombo.add(environments[i].getName());
        if (environments[i].isDefaultEnvironment()) {
          defaultEnvironment = environments[i];
        }
      } 
      wExecutionEnvironmentCombo.select(0);
    } else {
      // No OSGi enviroments installed
      //wProjectLibrariesEnvCombo.setData(ERROR, "No OSGi environments are installed.");
      wExecutionEnvironmentDefaultButton.setEnabled(false);
    }
    wExecutionEnvironmentCombo.setEnabled(false);
    
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wExecutionEnvironmentCombo.setLayoutData(gd);
    wExecutionEnvironmentDefaultButton.setText("Use default execution environment"+(defaultEnvironment != null ? " ("+defaultEnvironment.getName()+")" : ""));

    // Framework build path
    wFrameworkDefaultButton = new Button(wBuildPathGroup, SWT.CHECK);
    wFrameworkDefaultButton.setSelection(true);
    
    wFrameworkDefaultButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        boolean selection = ((Button) e.widget).getSelection();
        // Enable/disbale manual location widgets
        wFrameworkLabel.setEnabled(!selection);
        wFrameworkCombo.setEnabled(!selection);
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wFrameworkDefaultButton.setLayoutData(gd);
    
    wFrameworkLabel = new Label(wBuildPathGroup, SWT.LEFT);
    wFrameworkLabel.setText("Framework:");
    wFrameworkLabel.setEnabled(false);
    wFrameworkCombo = new Combo(wBuildPathGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    FrameworkPreference[] distribution = OsgiPreferences.getFrameworks();
    FrameworkPreference defaultDistribution = null;
    if (distribution.length > 0) {
      for(int i=0; i<distribution.length;i++) {
        wFrameworkCombo.add(distribution[i].getName());
        if (distribution[i].isDefaultDefinition()) {
          defaultDistribution = distribution[i];
        }
      } 
      wFrameworkCombo.select(0);
    } else {
      // No OSGi framework installed
      //wProjectLibrariesEnvCombo.setData(ERROR, "No OSGi environments are installed.");
      wFrameworkDefaultButton.setEnabled(false);
    }
    wFrameworkCombo.setEnabled(false);
    
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wFrameworkCombo.setLayoutData(gd);
    wFrameworkDefaultButton.setText("Use default framework distribution"+(defaultDistribution != null ? " ("+defaultDistribution.getName()+")" : ""));
    
    // Update state
    verifyProjectName();
    updateLocation();
    
    setControl(composite);
    updateStatus();
  }
  
  /****************************************************************************
   * UI control methods
   ***************************************************************************/
  
  void updateLocation() {
    if (isDefaultProjectLocation()) {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IPath path = root.getLocation();
      path = path.append(getProjectName());
      wProjectContentsDirectoryText.setText(path.toString());
    }
  }
  
  void updateStatus() {
    Control c = getControl();
    if (c == null) return;
    Composite composite = (Composite) c;
    composite.getChildren();
    
    // Loop through widgets checking for errors
    String error = (String) getData(getControl(), ERROR);
    if (error != null) {
      setPageComplete(false);
      setMessage(error, IMessageProvider.ERROR);
      return;
    }
    
    // Loop through widgets checking for warnings
    String warning = (String) getData(getControl(), WARNING);
    if (warning != null) {
      setPageComplete(true);
      setMessage(warning, IMessageProvider.WARNING);
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
  void verifyAll() {
    verifyProjectName();
    verifyProjectLocation();
  }
  
  void verifyProjectName() {
    String name = getProjectName();

    // Check that project name is not empty
    if (name == null || name.trim().length() == 0) {
      wProjectNameText.setData(ERROR, "Enter project name.");
      return;
    }

    // Check that project name is valid
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus status = workspace.validateName(name, IResource.PROJECT);
    if (!status.isOK()) {
      wProjectNameText.setData(ERROR, status.getMessage());
      return;
    }
    
    // Check that project name not already exists
    IProject[] projects = workspace.getRoot().getProjects();
    for (int i=0; projects != null && i<projects.length; i++) {
      if (name.equals(projects[i].getName())) {
        wProjectNameText.setData(ERROR, "Project name is already used.");
        return;
      }
    }
    
    wProjectNameText.setData(ERROR, null);
  }
  
  void verifyProjectLocation() {
    //String location = wProjectContentsDirectoryText.getText();
    //Path path = new Path(location);
    
    // Check that project location is valid
    /*
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    String name = getProjectName();
    if (wProjectNameText.getData(ERROR) == null && name.trim().length()>0) { 
      IProject project = root.getProject(getProjectName());
      IStatus status = workspace.validateProjectLocation(project, path);
      System.err.println("Status "+status);
      if (!status.isOK()) {
        wProjectContentsDirectoryText.setData(ERROR, status.getMessage());
        return;
      }
    }
    */
    
    // TODO:Check that project location not already exists
    
    wProjectContentsDirectoryText.setData(ERROR, null);
  }

  /****************************************************************************
   * Getters/Setters
   ***************************************************************************/
  public String getProjectName() {
    return wProjectNameText.getText();
  }
  
  public boolean isDefaultProjectLocation() {
    return wProjectContentsDefaultButton.getSelection();
  }

  public IPath getProjectLocation() {
    if (isDefaultProjectLocation()) {
      return null;
    }
    return new Path(wProjectContentsDirectoryText.getText());
  }
  
  public IPath getSourceFolder() {
    return new Path(wProjectSettingsSrcText.getText());
  }

  public IPath getOutputFolder() {
    return new Path(wProjectSettingsOutText.getText());
  }

  public boolean isDefaultEnvironment() {
    return wExecutionEnvironmentDefaultButton.getSelection();
  }
  
  public String getEnvironmentName() {
    return wExecutionEnvironmentCombo.getText();
  }

  public boolean isDefaultFramework() {
    return wFrameworkDefaultButton.getSelection();
  }
  
  public String getFrameworkName() {
    return wFrameworkCombo.getText();
  }
}
