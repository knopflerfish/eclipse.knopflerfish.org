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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
import org.knopflerfish.eclipse.core.project.OsgiClasspathContainer;
import org.knopflerfish.eclipse.core.project.OsgiContainerInitializer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;

public class FrameworkPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  private static String DESCRIPTION = 
    "Add, remove or edit OSGi frameworks.\n"+
    "The checked framework will be used by default to build and run OSGi bundles.";
  private static String TABLE_TITLE =
    "Installed OSGi frameworks:";
  
  private List osgiInstalls;
  private HashMap images = new HashMap();
  
  // Widgets
  private Table     wDefinitionsTable;
  private Button    wEditDefinitionButton;
  private Button    wRemoveDefinitionButton;
  
  public FrameworkPreferencePage() {
    noDefaultAndApplyButton();
    
    String[] names = Osgi.getFrameworkDefinitionNames();
    for (int i=0;i<names.length; i++) {
      String imagePath = Osgi.getFrameworkDefinitionImage(names[i]);
      String pluginId = Osgi.getFrameworkDefinitionId(names[i]);
      
      ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin(pluginId, imagePath);
      if (id != null) {
        Image image = id.createImage();
        if (image != null) {
          images.put(names[i], image);
        }
      }
    }
    
    // Load preferences
    osgiInstalls = Osgi.getOsgiInstalls();
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
        FrameworkDefinitionDialog dialog = 
          new FrameworkDefinitionDialog(((Button) e.widget).getShell(), getOsgiInstallNames(), null); 
        if (dialog.open() == Window.OK) {
          OsgiInstall osgiInstall = dialog.getOsgiInstall();
          if (osgiInstalls.size() == 0) {
            osgiInstall.setDefaultDefinition(true);
          }
          osgiInstalls.add(osgiInstall);
          addOsgiInstall(osgiInstall);
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
          OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.get(idx);
          
          FrameworkDefinitionDialog dialog = 
            new FrameworkDefinitionDialog(((Button) e.widget).getShell(), getOsgiInstallNames(), osgiInstall); 
          if (dialog.open() == Window.OK) {
            osgiInstalls.set(idx, dialog.getOsgiInstall());
            updateOsgiInstall(idx);
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
          OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.remove(idx);
          wDefinitionsTable.remove(idx);
          if (osgiInstall.isDefaultDefinition() && osgiInstalls.size()>0) {
            ((OsgiInstall) osgiInstalls.get(0)).setDefaultDefinition(true);
            updateOsgiInstall(0);
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
        int idx = wDefinitionsTable.getSelectionIndex();
        if (idx != -1) {
          OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.get(idx);
          FrameworkDefinitionDialog dialog = 
            new FrameworkDefinitionDialog(((Table) e.widget).getShell(), getOsgiInstallNames(), osgiInstall); 
          if (dialog.open() == Window.OK) {
            osgiInstalls.set(idx, dialog.getOsgiInstall());
            updateOsgiInstall(idx);
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
    if (osgiInstalls != null) {
      for (int i=0; i<osgiInstalls.size(); i++) {
        addOsgiInstall( (OsgiInstall) osgiInstalls.get(i));
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
    // Save Knopflerfish instances to preference store
    try {
      Osgi.setOsgiInstalls(osgiInstalls);
      
      // Get java projects
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject [] projects = root.getProjects();
      ArrayList javaProjectList = new ArrayList();
      for(int i=0; projects != null && i<projects.length; i++) {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          javaProjectList.add(JavaCore.create(projects[i]));
        }
      }
      
      IJavaProject [] javaProjects = (IJavaProject[]) javaProjectList.toArray(new IJavaProject[javaProjectList.size()]);
      IClasspathContainer [] containers = new IClasspathContainer[javaProjectList.size()];
      
      // Update containers
      for (int i=0; osgiInstalls != null && i<osgiInstalls.size(); i++) {
        OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.get(i);
        IClasspathContainer container = new OsgiClasspathContainer(osgiInstall);
        Arrays.fill(containers, container);
        IPath path = new Path(OsgiContainerInitializer.KF_CONTAINER);
        if (osgiInstall.isDefaultDefinition()) {
          JavaCore.setClasspathContainer(path,javaProjects,containers,null);
        }
        path = path.append(osgiInstall.getName());
        JavaCore.setClasspathContainer(path,javaProjects,containers,null);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }
  
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  private void updateButtons() {
    boolean enabled = (wDefinitionsTable.getSelectionIndex() != -1);
    wEditDefinitionButton.setEnabled(enabled);
    wRemoveDefinitionButton.setEnabled(enabled);
  }
  
  private void addOsgiInstall(OsgiInstall osgiInstall) {
    TableItem item = new TableItem(wDefinitionsTable, 0);
    item.setData(osgiInstall);
    item.setChecked(osgiInstall.isDefaultDefinition());
    item.setText(0, osgiInstall.getName());
    item.setImage(0, (Image) images.get(osgiInstall.getType()));
    item.setText(1, osgiInstall.getLocation());
  }
  
  private void updateOsgiInstall(int idx) {
    OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.get(idx);
    
    TableItem item = wDefinitionsTable.getItem(idx);
    item.setData(osgiInstall);
    item.setChecked(osgiInstall.isDefaultDefinition());
    item.setText(0, osgiInstall.getName());
    item.setImage(0, (Image) images.get(osgiInstall.getType()));
    item.setText(1, osgiInstall.getLocation());
  }
  
  private ArrayList getOsgiInstallNames() {
    ArrayList names = new ArrayList();
    
    for (int i=0; i<wDefinitionsTable.getItemCount(); i++) {
      TableItem item = wDefinitionsTable.getItem(i);
      names.add(item.getText(0));
    }
    
    return names;
  }
}