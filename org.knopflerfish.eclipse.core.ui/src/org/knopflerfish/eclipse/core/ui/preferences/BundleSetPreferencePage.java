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
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiInstall;
import org.knopflerfish.eclipse.core.OsgiVendor;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author Anders Rimén
 */
public class BundleSetPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  private static String DESCRIPTION = 
    "Add, remove or edit bundle sets.\n"+
    "The checked bundle set will be used by default when launching a framework.";
  private static String TABLE_TITLE =
    "Defined bundle sets:";

  private OsgiVendor osgiVendor;
  private List bundleSets;
  private Image bundleSetImage = null;
  
  // Widgets
  private Table     wBundleSetTable;
  private Button    wEditBundleSetButton;
  private Button    wRemoveBundleSetButton;

  public BundleSetPreferencePage() {
    noDefaultAndApplyButton();
    
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", "icons/obj16/bundle_set_obj.gif");
    if (id != null) {
      bundleSetImage = id.createImage();
    }
    
    // Load preferences
    bundleSets = Osgi.getBundleSets();
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
    if (bundleSetImage != null) {
      bundleSetImage.dispose();
      bundleSetImage = null;
    }
  }
  
  /****************************************************************************
   * org.eclipse.jface.preference.PreferencePage methods
   ***************************************************************************/

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Composite page = new Composite(parent, 0);
    FormLayout layout = new FormLayout();
    page.setLayout(layout);
    
    // Description
    Label wDescriptionLabel = new Label(page, SWT.LEFT | SWT.WRAP);
    wDescriptionLabel.setText(DESCRIPTION);
    FormData data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(0,0);
    data.right = new FormAttachment(100,0);
    wDescriptionLabel.setLayoutData(data);
    
    // Table title
    Label wTitleLabel = new Label(page, SWT.LEFT);
    wTitleLabel.setText(TABLE_TITLE);
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wDescriptionLabel, 5, SWT.BOTTOM);
    wTitleLabel.setLayoutData(data);
    
    // Buttons
    Button wAddBundleSetButton = new Button(page, SWT.CENTER);
    wAddBundleSetButton.setText("Add...");
    data = new FormData();
    data.top = new FormAttachment(wTitleLabel, 5, SWT.BOTTOM);
    data.right = new FormAttachment(100, 0);
    data.left = new FormAttachment(100, -76);
    wAddBundleSetButton.setLayoutData(data);
    wAddBundleSetButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        FrameworkDefinitionDialog dialog = 
          new FrameworkDefinitionDialog(((Button) e.widget).getShell(), getOsgiInstallNames(), null); 
        if (dialog.open() == Window.OK) {
          OsgiInstall osgiInstall = dialog.getOsgiInstall();
          if (bundleSets.size() == 0) {
            osgiInstall.setDefaultDefinition(true);
          }
          bundleSets.add(osgiInstall);
          addOsgiInstall(osgiInstall);
          packTableColumns(wBundleSetTable);
        }
      }
    });
    
    wEditBundleSetButton = new Button(page, SWT.CENTER);
    wEditBundleSetButton.setText("Edit...");
    data = new FormData();
    data.top = new FormAttachment(wAddBundleSetButton, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wAddBundleSetButton, 0, SWT.RIGHT);
    data.left = new FormAttachment(wAddBundleSetButton, 0, SWT.LEFT);
    wEditBundleSetButton.setLayoutData(data);
    wEditBundleSetButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wBundleSetTable.getSelectionIndex();
        if (idx != -1) {
          OsgiInstall osgiInstall = (OsgiInstall) bundleSets.get(idx);
          
          FrameworkDefinitionDialog dialog = 
            new FrameworkDefinitionDialog(((Button) e.widget).getShell(), getOsgiInstallNames(), osgiInstall); 
          if (dialog.open() == Window.OK) {
            bundleSets.set(idx, dialog.getOsgiInstall());
            updateOsgiInstall(idx);
            packTableColumns(wBundleSetTable);
          }
        }
      }
    });
    
    wRemoveBundleSetButton = new Button(page, SWT.CENTER);
    wRemoveBundleSetButton.setText("Remove");
    data = new FormData();
    data.top = new FormAttachment(wEditBundleSetButton, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wEditBundleSetButton, 0, SWT.RIGHT);
    data.left = new FormAttachment(wEditBundleSetButton, 0, SWT.LEFT);
    wRemoveBundleSetButton.setLayoutData(data);
    wRemoveBundleSetButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wBundleSetTable.getSelectionIndex();
        if (idx != -1) {
          OsgiInstall osgiInstall = (OsgiInstall) bundleSets.remove(idx);
          wBundleSetTable.remove(idx);
          if (osgiInstall.isDefaultDefinition() && bundleSets.size()>0) {
            ((OsgiInstall) bundleSets.get(0)).setDefaultDefinition(true);
            updateOsgiInstall(0);
          }
          updateButtons();
          packTableColumns(wBundleSetTable);
        }
      }
    });
    
    // Table
    wBundleSetTable = new Table(page, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wTitleLabel, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wAddBundleSetButton, -5, SWT.LEFT);
    data.bottom = new FormAttachment(100, 0);
    wBundleSetTable.setLayoutData(data);
    wBundleSetTable.setHeaderVisible(true);
    wBundleSetTable.setLinesVisible(true);
    wBundleSetTable.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        // Selection
        Table table = (Table) e.widget;
        if ( (e.detail & SWT.CHECK) != 0 ) {
          TableItem item = (TableItem) e.item;
          int idx = table.indexOf(item);
          ((OsgiInstall) item.getData()).setDefaultDefinition(true);
          if (item.getChecked()) {
            // Uncheck all others
            TableItem [] items = table.getItems();
            for(int i=0; i<items.length; i++) {
              if(i != idx) {
                items[i].setChecked(false);
                ((OsgiInstall) items[i].getData()).setDefaultDefinition(false);
              }
            }
          } else {
            item.setChecked(true);
          }
        } else {
          updateButtons();
        }
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        int idx = wBundleSetTable.getSelectionIndex();
        if (idx != -1) {
          OsgiInstall osgiInstall = (OsgiInstall) bundleSets.get(idx);
          FrameworkDefinitionDialog dialog = 
            new FrameworkDefinitionDialog(((Table) e.widget).getShell(), getOsgiInstallNames(), osgiInstall); 
          if (dialog.open() == Window.OK) {
            bundleSets.set(idx, dialog.getOsgiInstall());
            updateOsgiInstall(idx);
          }
        }
      }
      
    });
    
    // Table columns
    TableColumn wNameTableColumn = new TableColumn(wBundleSetTable, SWT.LEFT);
    wNameTableColumn.setText("Name");
    
    TableColumn wDescriptionTableColumn = new TableColumn(wBundleSetTable, SWT.LEFT);
    wDescriptionTableColumn.setText("Description");

    // Add framework definitions
    if (bundleSets != null) {
      for (int i=0; i<bundleSets.size(); i++) {
        addOsgiInstall( (OsgiInstall) bundleSets.get(i));
      }
    }
    
    updateButtons();
    packTableColumns(wBundleSetTable);
    
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
    /*
    try {
      osgiVendor.setOsgiInstalls(bundleSets);
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    */
    return true;
  }
  

  /****************************************************************************
   * Private worker methods
   ***************************************************************************/

  private void updateButtons() {
    boolean enabled = (wBundleSetTable.getSelectionIndex() != -1);
    wEditBundleSetButton.setEnabled(enabled);
    wRemoveBundleSetButton.setEnabled(enabled);
  }

  private void addOsgiInstall(OsgiInstall osgiInstall) {
    TableItem item = new TableItem(wBundleSetTable, 0);
    item.setData(osgiInstall);
    item.setChecked(osgiInstall.isDefaultDefinition());
    item.setText(0, osgiInstall.getName());
    item.setImage(0, bundleSetImage);
    item.setText(1, osgiInstall.getLocation());
  }
  
  private void updateOsgiInstall(int idx) {
    OsgiInstall osgiInstall = (OsgiInstall) bundleSets.get(idx);
    
    TableItem item = wBundleSetTable.getItem(idx);
    item.setData(osgiInstall);
    item.setChecked(osgiInstall.isDefaultDefinition());
    item.setText(0, osgiInstall.getName());
    item.setImage(0, bundleSetImage);
    item.setText(1, osgiInstall.getLocation());
  }
  
  private ArrayList getOsgiInstallNames() {
    ArrayList names = new ArrayList();
    
    for (int i=0; i<wBundleSetTable.getItemCount(); i++) {
      TableItem item = wBundleSetTable.getItem(i);
      names.add(item.getText(0));
    }
    
    return names;
  }

  private void packTableColumns(Table table) {
    if(table == null) return;
    TableColumn [] columns = table.getColumns();
    if (columns == null) return;
    for(int i=0;i<columns.length;i++) {
      columns[i].pack();
    }
  }
}
