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

package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knopflerfish.eclipse.core.preferences.ExecutionEnvironment;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.classpath.ClasspathUtil;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class EnvironmentPage extends PreferencePage implements IWorkbenchPreferencePage, ICheckStateListener {
  private static final String DESCRIPTION = 
    "Add, remove or edit execution environments.\n"+
    "The checked environment will be used by default to build bundles.";
  private static final String TABLE_TITLE =
    "Installed execution environments:";
  
  private List environments;
  
  // Widgets
  private Button wEditEnvironmentButton;
  private Button wRemoveEnvironmentButton;
  
  // jFace Widgets 
  private CheckboxTableViewer   wEnvironmentTableViewer;
  
  public EnvironmentPage() {
    noDefaultAndApplyButton();
    
    // Load preferences
    environments = new ArrayList(Arrays.asList(OsgiPreferences.getExecutionEnvironments()));
  }
  
  /****************************************************************************
   * org.eclipse.ui.IWorkbenchPreferencePage methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.IDialogPage methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose() {
  }
  
  /****************************************************************************
   * org.eclipse.jface.preference.PreferencePage methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    
    Composite page = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    page.setLayout(layout);
    
    // Description
    Label wDescriptionLabel = new Label(page, SWT.LEFT | SWT.WRAP);
    wDescriptionLabel.setText(DESCRIPTION);
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    wDescriptionLabel.setLayoutData(gd);
    
    // Table title
    Label wTitleLabel = new Label(page, SWT.LEFT);
    wTitleLabel.setText(TABLE_TITLE);
    gd = new GridData();
    gd.horizontalSpan = 2;
    wTitleLabel.setLayoutData(gd);

    // Table
    Table wEnvironmentTable = new Table(page, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
    wEnvironmentTable.setHeaderVisible(true);
    wEnvironmentTable.setLinesVisible(true);
    TableColumn wNameTableColumn = new TableColumn(wEnvironmentTable, SWT.LEFT);
    wNameTableColumn.setText("Name");
    TableColumn wTypeTableColumn = new TableColumn(wEnvironmentTable, SWT.LEFT);
    wTypeTableColumn.setText("Type");
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    gd.verticalSpan = 3;
    wEnvironmentTable.setLayoutData(gd);
    wEnvironmentTableViewer = new CheckboxTableViewer(wEnvironmentTable);
    wEnvironmentTableViewer.addCheckStateListener(this);
    EnvironmentContentProvider provider = new EnvironmentContentProvider();
    wEnvironmentTableViewer.setContentProvider(provider);
    wEnvironmentTableViewer.setLabelProvider(provider);
    wEnvironmentTableViewer.setSorter(provider);
    wEnvironmentTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });

    // Buttons
    Button wAddEnvironmentButton = new Button(page, SWT.CENTER);
    wAddEnvironmentButton.setText("Add...");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wAddEnvironmentButton.setLayoutData(gd);
    wAddEnvironmentButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        EnvironmentDialog dialog = 
          new EnvironmentDialog(Display.getDefault().getActiveShell(), getEnvironmentNames(), null);
        
        if (dialog.open() == Window.OK) {
          ExecutionEnvironment environment = dialog.getExecutionEnvironment();
          environments.add(environment);
          wEnvironmentTableViewer.refresh();
          if (environments.size() == 1) {
            environment.setDefaultEnvironment(true);
            wEnvironmentTableViewer.setChecked(environment, true);
          }
          UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
        }
      }
    });
    
    wEditEnvironmentButton = new Button(page, SWT.CENTER);
    wEditEnvironmentButton.setText("Edit...");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wEditEnvironmentButton.setLayoutData(gd);
    wEditEnvironmentButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wEnvironmentTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;
        
        ExecutionEnvironment environment = (ExecutionEnvironment) selection.getFirstElement();

        EnvironmentDialog dialog = 
          new EnvironmentDialog(Display.getDefault().getActiveShell(), getEnvironmentNames(), environment);
        
        if (dialog.open() == Window.OK) {
          int idx = environments.indexOf(environment);
          if (idx != -1) {
            environments.remove(idx);
            environments.add(idx, dialog.getExecutionEnvironment());
            wEnvironmentTableViewer.refresh();
            UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
          }
        }
      }
    });
    
    wRemoveEnvironmentButton = new Button(page, SWT.CENTER);
    wRemoveEnvironmentButton.setText("Remove");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wRemoveEnvironmentButton.setLayoutData(gd);
    wRemoveEnvironmentButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wEnvironmentTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;
        
        ExecutionEnvironment environment = (ExecutionEnvironment) selection.getFirstElement();
        environments.remove(environment);
        wEnvironmentTableViewer.refresh();
        if (environment.isDefaultEnvironment() && environments.size() > 0) {
          environment = (ExecutionEnvironment) environments.get(0);
          environment.setDefaultEnvironment(true);
          wEnvironmentTableViewer.setChecked(environment, true);
        }
        updateButtons();
        UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
      }
    });
    
    wEnvironmentTableViewer.setInput(environments);
    for (int i=0; i< environments.size(); i++) {
      ExecutionEnvironment env = (ExecutionEnvironment) environments.get(i);
      if (env.isDefaultEnvironment()) {
        wEnvironmentTableViewer.setChecked(env, true);
      }
    }
    UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
    updateButtons();
    
    return page;
  }
  
  /****************************************************************************
   * org.eclipse.jface.preference.IPreferencePage methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    // Save execution environments to preference store
    try {
      OsgiPreferences.setExecutionEnvironment(
          (ExecutionEnvironment[]) environments.toArray(new ExecutionEnvironment[environments.size()]));

      ClasspathUtil.updateEnvironmentContainers();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  /****************************************************************************
   * org.eclipse.jface.viewers.ICheckStateListener methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
   */
  public void checkStateChanged(CheckStateChangedEvent event) {
    ExecutionEnvironment environment = (ExecutionEnvironment) event.getElement();
    boolean checked = event.getChecked();

    if (!checked && environment.isDefaultEnvironment()) {
      // Can not uncheck default environment
      wEnvironmentTableViewer.setChecked(environment, true);
    }  else if (checked && !environment.isDefaultEnvironment()) {
      int idx = environments.indexOf(environment);
      for (int i=0; i< environments.size(); i++) {
        if (i == idx) continue;
        
        ExecutionEnvironment env = (ExecutionEnvironment) environments.get(i);
        if (env.isDefaultEnvironment()) {
          env.setDefaultEnvironment(false);
          wEnvironmentTableViewer.setChecked(env, false);
        }
      }
      environment.setDefaultEnvironment(true);
    }
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  private void updateButtons() {
    IStructuredSelection selection = 
      (IStructuredSelection) wEnvironmentTableViewer.getSelection();
    
    // Enable/disable remove and edit buttons
    boolean enable = false;
    if (selection != null && !selection.isEmpty()) {
      ExecutionEnvironment environment = (ExecutionEnvironment) selection.getFirstElement();
      enable = environment.getType() == ExecutionEnvironment.TYPE_USER;
    }
    wEditEnvironmentButton.setEnabled(enable);
    wRemoveEnvironmentButton.setEnabled(enable);
  }
  
  private ArrayList getEnvironmentNames() {
    ArrayList names = new ArrayList();
    
    for (Iterator i=environments.iterator(); i.hasNext();) {
      ExecutionEnvironment environment = (ExecutionEnvironment) i.next();
      names.add(environment.getName());
    }
    
    return names;
  }

}
