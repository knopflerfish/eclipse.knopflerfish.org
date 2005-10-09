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
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.classpath.ClasspathUtil;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkPage extends PreferencePage implements IWorkbenchPreferencePage {
  private static final String DESCRIPTION = 
    "Add, remove or edit OSGi frameworks.\n"+
    "The checked framework will be used by default to run OSGi bundles.";
  private static final String TABLE_TITLE =
    "Installed OSGi frameworks:";
  
  List distributions;
  private HashMap images = new HashMap();
  
  Table     wDefinitionsTable;
  private Button    wEditDefinitionButton;
  private Button    wRemoveDefinitionButton;
  
  public FrameworkPage() {
    noDefaultAndApplyButton();
    
    String[] names = Osgi.getFrameworkDefinitionNames();
    for (int i=0;i<names.length; i++) {
      String imagePath = Osgi.getFrameworkDefinitionImage(names[i]);
      String pluginId = Osgi.getFrameworkDefinitionId(names[i]);
      
      ImageDescriptor id = null;
      if (imagePath != null) {
        id = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imagePath);
      } else {
        id = AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui",
        "icons/obj16/_knopflerfish_obj.gif");
      }      
      if (id != null) {
        Image image = id.createImage();
        if (image != null) {
          images.put(names[i], image);
        }
      }
    }
    
    // Load preferences
    distributions = new ArrayList(Arrays.asList(OsgiPreferences.getFrameworks()));
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
    for (Iterator i=images.values().iterator(); i.hasNext();){
     Image image = (Image) i.next();
     image.dispose();
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
    Button wAddDefinitionButton = new Button(page, SWT.CENTER);
    wAddDefinitionButton.setText("Add...");
    data = new FormData();
    data.top = new FormAttachment(wTitleLabel, 5, SWT.BOTTOM);
    data.right = new FormAttachment(100, 0);
    data.left = new FormAttachment(100, -76);
    wAddDefinitionButton.setLayoutData(data);
    wAddDefinitionButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        FrameworkDialog dialog = 
          new FrameworkDialog(((Button) e.widget).getShell(), getFrameworkNames(), null); 
        if (dialog.open() == Window.OK) {
          FrameworkPreference framework = dialog.getFrameworkDistribution();
          if (distributions.size() == 0) {
            framework.setDefaultDefinition(true);
          }
          distributions.add(framework);
          addFrameworkDistribution(framework);
          UiUtils.packTableColumns(wDefinitionsTable);
        }
      }
    });
    
    wEditDefinitionButton = new Button(page, SWT.CENTER);
    wEditDefinitionButton.setText("Edit...");
    data = new FormData();
    data.top = new FormAttachment(wAddDefinitionButton, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wAddDefinitionButton, 0, SWT.RIGHT);
    data.left = new FormAttachment(wAddDefinitionButton, 0, SWT.LEFT);
    wEditDefinitionButton.setLayoutData(data);
    wEditDefinitionButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wDefinitionsTable.getSelectionIndex();
        if (idx != -1) {
          FrameworkPreference distribution = (FrameworkPreference) distributions.get(idx);
          
          FrameworkDialog dialog = 
            new FrameworkDialog(((Button) e.widget).getShell(), getFrameworkNames(), distribution); 
          if (dialog.open() == Window.OK) {
            distributions.set(idx, dialog.getFrameworkDistribution());
            updateFrameworkDistribution(idx);
            UiUtils.packTableColumns(wDefinitionsTable);
          }
        }
      }
    });
    
    wRemoveDefinitionButton = new Button(page, SWT.CENTER);
    wRemoveDefinitionButton.setText("Remove");
    data = new FormData();
    data.top = new FormAttachment(wEditDefinitionButton, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wEditDefinitionButton, 0, SWT.RIGHT);
    data.left = new FormAttachment(wEditDefinitionButton, 0, SWT.LEFT);
    wRemoveDefinitionButton.setLayoutData(data);
    wRemoveDefinitionButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wDefinitionsTable.getSelectionIndex();
        if (idx != -1) {
          FrameworkPreference framework = (FrameworkPreference) distributions.remove(idx);
          wDefinitionsTable.remove(idx);
          if (framework.isDefaultDefinition() && distributions.size()>0) {
            ((FrameworkPreference) distributions.get(0)).setDefaultDefinition(true);
            updateFrameworkDistribution(0);
          }
          updateButtons();
          UiUtils.packTableColumns(wDefinitionsTable);
        }
      }
    });
    
    // Table
    wDefinitionsTable = new Table(page, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wTitleLabel, 5, SWT.BOTTOM);
    data.right = new FormAttachment(wAddDefinitionButton, -5, SWT.LEFT);
    data.bottom = new FormAttachment(100, 0);
    wDefinitionsTable.setLayoutData(data);
    wDefinitionsTable.setHeaderVisible(true);
    wDefinitionsTable.setLinesVisible(true);
    wDefinitionsTable.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        // Selection
        Table table = (Table) e.widget;
        if ( (e.detail & SWT.CHECK) != 0 ) {
          TableItem item = (TableItem) e.item;
          int idx = table.indexOf(item);
          ((FrameworkPreference) item.getData()).setDefaultDefinition(true);
          if (item.getChecked()) {
            // Uncheck all others
            TableItem [] items = table.getItems();
            for(int i=0; i<items.length; i++) {
              if(i != idx) {
                items[i].setChecked(false);
                ((FrameworkPreference) items[i].getData()).setDefaultDefinition(false);
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
        int idx = wDefinitionsTable.getSelectionIndex();
        if (idx != -1) {
          FrameworkPreference distribution = (FrameworkPreference) distributions.get(idx);
          FrameworkDialog dialog = 
            new FrameworkDialog(((Table) e.widget).getShell(), getFrameworkNames(), distribution); 
          if (dialog.open() == Window.OK) {
            distributions.set(idx, dialog.getFrameworkDistribution());
            updateFrameworkDistribution(idx);
          }
        }
      }
      
    });
    
    // Table columns
    TableColumn wNameTableColumn = new TableColumn(wDefinitionsTable, SWT.LEFT);
    wNameTableColumn.setText("Name");
    
    TableColumn wLocationTableColumn = new TableColumn(wDefinitionsTable, SWT.LEFT);
    wLocationTableColumn.setText("Location");
    
    // Add framework definitions
    if (distributions != null) {
      for (int i=0; i<distributions.size(); i++) {
        addFrameworkDistribution( (FrameworkPreference) distributions.get(i));
      }
    }
    
    updateButtons();
    UiUtils.packTableColumns(wDefinitionsTable);
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
    // Save framework distributions to preference store
    try {
      OsgiPreferences.setFrameworks(
          (FrameworkPreference[]) distributions.toArray(new FrameworkPreference[distributions.size()]));
      
      ClasspathUtil.updateFrameworkContainers();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  void updateButtons() {
    boolean enabled = (wDefinitionsTable.getSelectionIndex() != -1);
    wEditDefinitionButton.setEnabled(enabled);
    wRemoveDefinitionButton.setEnabled(enabled);
  }
  
  void addFrameworkDistribution(FrameworkPreference distribution) {
    TableItem item = new TableItem(wDefinitionsTable, 0);
    item.setData(distribution);
    item.setChecked(distribution.isDefaultDefinition());
    item.setText(0, distribution.getName());
    item.setImage(0, (Image) images.get(distribution.getType()));
    item.setText(1, distribution.getLocation());
  }
  
  void updateFrameworkDistribution(int idx) {
    FrameworkPreference distribution = (FrameworkPreference) distributions.get(idx);
    
    TableItem item = wDefinitionsTable.getItem(idx);
    item.setData(distribution);
    item.setChecked(distribution.isDefaultDefinition());
    item.setText(0, distribution.getName());
    item.setImage(0, (Image) images.get(distribution.getType()));
    item.setText(1, distribution.getLocation());
  }
  
  ArrayList getFrameworkNames() {
    ArrayList names = new ArrayList();
    
    for (int i=0; i<wDefinitionsTable.getItemCount(); i++) {
      TableItem item = wDefinitionsTable.getItem(i);
      names.add(item.getText(0));
    }
    
    return names;
  }
}