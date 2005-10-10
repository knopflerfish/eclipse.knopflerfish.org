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
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleRepositoryPage extends PreferencePage implements IWorkbenchPreferencePage, ICheckStateListener {
  private static final String DESCRIPTION = 
    "Add, remove or edit bundle repositories.\n"+
    "The checked repositories will be searched in the order they are listed.";
  private static final String TABLE_TITLE =
    "Installed bundle repositories:";
  
  List repositories;
  
  // Widgets
  private Button    wEditRepositoryButton;
  private Button    wRemoveRepositoryButton;
  private Button    wUpRepositoryButton;
  private Button    wDownRepositoryButton;
  
  CheckboxTableViewer   wRepositoryTableViewer;

  public BundleRepositoryPage() {
    noDefaultAndApplyButton();
    
    // Load preferences
    repositories = new ArrayList(Arrays.asList(OsgiPreferences.getBundleRepositories()));
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
    Table wRepositoryTable = new Table(page, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
    wRepositoryTable.setHeaderVisible(true);
    wRepositoryTable.setLinesVisible(true);
    TableColumn wNameTableColumn = new TableColumn(wRepositoryTable, SWT.LEFT);
    wNameTableColumn.setText("Name");
    TableColumn wTypeTableColumn = new TableColumn(wRepositoryTable, SWT.LEFT);
    wTypeTableColumn.setText("Type");
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    gd.verticalSpan = 5;
    wRepositoryTable.setLayoutData(gd);
    wRepositoryTableViewer = new CheckboxTableViewer(wRepositoryTable);
    wRepositoryTableViewer.addCheckStateListener(this);
    RepositoryContentProvider provider = new RepositoryContentProvider();
    wRepositoryTableViewer.setContentProvider(provider);
    wRepositoryTableViewer.setLabelProvider(provider);
    wRepositoryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
        BundleRepositoryDialog dialog = 
          new BundleRepositoryDialog(Display.getDefault().getActiveShell(), getRepositoryNames(), null);
        
        if (dialog.open() == Window.OK) {
          RepositoryPreference repository = dialog.getBundleRespository();
          repositories.add(repository);
          wRepositoryTableViewer.refresh();
          // Set active
          repository.setActive(true);
          wRepositoryTableViewer.setChecked(repository, true);
          UiUtils.packTableColumns(wRepositoryTableViewer.getTable());
        }
      }
    });
    
    wEditRepositoryButton = new Button(page, SWT.CENTER);
    wEditRepositoryButton.setText("Edit...");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wEditRepositoryButton.setLayoutData(gd);
    wEditRepositoryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wRepositoryTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;
        
        RepositoryPreference repository = (RepositoryPreference) selection.getFirstElement();

        BundleRepositoryDialog dialog = 
          new BundleRepositoryDialog(Display.getDefault().getActiveShell(), getRepositoryNames(), repository);
        
        if (dialog.open() == Window.OK) {
          int idx = repositories.indexOf(repository);
          if (idx != -1) {
            repositories.remove(idx);
            repositories.add(idx, dialog.getBundleRespository());
            wRepositoryTableViewer.refresh();
            UiUtils.packTableColumns(wRepositoryTableViewer.getTable());
          }
        }
      }
    });
    
    wRemoveRepositoryButton = new Button(page, SWT.CENTER);
    wRemoveRepositoryButton.setText("Remove");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wRemoveRepositoryButton.setLayoutData(gd);
    wRemoveRepositoryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wRepositoryTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;
        
        RepositoryPreference repository = (RepositoryPreference) selection.getFirstElement();
        repositories.remove(repository);
        wRepositoryTableViewer.refresh();
        updateButtons();
        UiUtils.packTableColumns(wRepositoryTableViewer.getTable());
      }
    });

    wUpRepositoryButton = new Button(page, SWT.CENTER);
    wUpRepositoryButton.setText("Up");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wUpRepositoryButton.setLayoutData(gd);
    wUpRepositoryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wRepositoryTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;

        RepositoryPreference repository = (RepositoryPreference) selection.getFirstElement();
        int idx = repositories.indexOf(repository);
        if (idx > 0) {
          repositories.remove(repository);
          repositories.add(idx-1, repository);
          wRepositoryTableViewer.setSelection(new StructuredSelection(repository), true);
          wRepositoryTableViewer.refresh();
          updateButtons();
        }
      }
    });
    
    wDownRepositoryButton = new Button(page, SWT.CENTER);
    wDownRepositoryButton.setText("Down");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wDownRepositoryButton.setLayoutData(gd);
    wDownRepositoryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wRepositoryTableViewer.getSelection();
        
        if (selection == null || selection.size() != 1) return;

        RepositoryPreference repository = (RepositoryPreference) selection.getFirstElement();
        int idx = repositories.indexOf(repository);
        if (idx < repositories.size()-1) {
          repositories.remove(repository);
          repositories.add(idx+1, repository);
          wRepositoryTableViewer.setSelection(new StructuredSelection(repository), true);
          wRepositoryTableViewer.refresh();
          updateButtons();
        }
      }
    });
    
    wRepositoryTableViewer.setInput(repositories);
    for (int i=0; i< repositories.size(); i++) {
      RepositoryPreference repository = (RepositoryPreference) repositories.get(i);
      wRepositoryTableViewer.setChecked(repository, repository.isActive());
    }
    UiUtils.packTableColumns(wRepositoryTableViewer.getTable());
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
    // Save bundle repositories to preference store
    try {
      OsgiPreferences.setBundleRepositories(
          (RepositoryPreference[]) repositories.toArray(new RepositoryPreference[repositories.size()]));
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
    RepositoryPreference repository = (RepositoryPreference) event.getElement();
    repository.setActive(event.getChecked());
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  void updateButtons() {
    IStructuredSelection selection = 
      (IStructuredSelection) wRepositoryTableViewer.getSelection();
    
    // Enable/disable remove and edit buttons
    boolean enable = false;
    boolean enableUp = false;
    boolean enableDown = false;
    if (selection != null && !selection.isEmpty()) {
      enable = true;
      if (selection.size() == 1) {
        int idx = wRepositoryTableViewer.getTable().getSelectionIndex();
        enableUp = idx > 0;
        enableDown = idx < wRepositoryTableViewer.getTable().getItemCount()-1;
      }
    }
    wEditRepositoryButton.setEnabled(enable);
    wRemoveRepositoryButton.setEnabled(enable);
    wUpRepositoryButton.setEnabled(enableUp);
    wDownRepositoryButton.setEnabled(enableDown);
  }
  
  ArrayList getRepositoryNames() {
    ArrayList names = new ArrayList();
    
    for (Iterator i=repositories.iterator(); i.hasNext();) {
      RepositoryPreference repository = (RepositoryPreference) i.next();
      names.add(repository.getName());
    }
    
    return names;
  }
}
