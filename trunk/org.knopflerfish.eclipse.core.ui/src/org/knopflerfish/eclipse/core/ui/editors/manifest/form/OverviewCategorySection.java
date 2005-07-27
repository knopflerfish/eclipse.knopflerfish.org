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

package org.knopflerfish.eclipse.core.ui.editors.manifest.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.project.BundleManifest;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.PropertyDialog;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

/**
 * @author Anders Rimén
 */
public class OverviewCategorySection extends SectionPart {

  private static int NUM_TABLE_ROWS = 5;

  private static String TITLE_ADD_CATEGORY = "Add Category";

  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";


  // Section title and description
  private static final String TITLE = 
    "Categories";
  private static final String DESCRIPTION = 
    "This section lists the categories this bundle is part of.";

  // SWT Widgets
  private Button    wCategoryAddButton;
  private Button    wCategoryRemoveButton;
  
  // jFace Widgets 
  private TableViewer   wCategoryTableViewer;
  
  // Model objects
  private BundleManifest manifest = null;
  
  public OverviewCategorySection(Composite parent, FormToolkit toolkit, int style) {
    super(parent, toolkit, style);
    
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }

  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
   */
  public void commit(boolean onSave) {
    super.commit(onSave);
    
    // Flush values to document
    IManagedForm managedForm = getManagedForm();
    IDocument doc = (IDocument) managedForm.getInput();

    if (manifest == null) return;
    
    Table wCategoryTable = wCategoryTableViewer.getTable();
    try {
      String attribute = (String) wCategoryTable.getData(PROP_NAME);
      if (attribute != null) {
        String value = manifest.getAttribute(BundleManifest.BUNDLE_CATEGORY);
        if (value == null) value = "";
        ManifestUtil.setManifestAttribute(doc, attribute, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();

    // Refresh values from document
    manifest = new BundleManifest(ManifestUtil.createManifest((IDocument) getManagedForm().getInput()));
    if (wCategoryTableViewer != null) {
      wCategoryTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wCategoryTableViewer.getTable());
  }
  
  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.editors.ManifestSectionPart#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public void createClient(Section section, FormToolkit toolkit) {

    // Set section layout
    ColumnLayoutData data = new ColumnLayoutData();
    section.setLayoutData(data);
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        getManagedForm().getForm().reflow(true);
      }
    });

    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    container.setLayout(layout);

    // Create widgets
    Table wCategoryTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wCategoryTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    wCategoryTable.setData(PROP_DIRTY, new Boolean(false));
    wCategoryTable.setData(PROP_NAME, BundleManifest.BUNDLE_CATEGORY);

    wCategoryTableViewer = new TableViewer(wCategoryTable);
    wCategoryTableViewer.setContentProvider(new CategoryContentProvider());
    wCategoryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wCategoryTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wCategoryRemoveButton.setEnabled(enable);
      }
    });
    TableColumn wNameTableColumn = new TableColumn(wCategoryTable, SWT.LEFT);
    wNameTableColumn.setText("Name");
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wCategoryTable, NUM_TABLE_ROWS);
    wCategoryTable.setLayoutData(wd);

    wCategoryRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wCategoryRemoveButton.setEnabled(false);
    wCategoryRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wCategoryTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        List categories = new ArrayList(Arrays.asList(manifest.getCategories()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          categories.remove(name);
        }
        manifest.setCategories((String[]) categories.toArray(new String[categories.size()]));
        wCategoryTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wCategoryRemoveButton.setLayoutData(wd);
    
    wCategoryAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wCategoryAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        HashMap map = new HashMap();
        String key = "category";
        PropertyDialog.Property p = new PropertyDialog.Property(key) {
          public boolean isValid(String value) {
            if (value.indexOf(",")!= -1) {
              return false;
            } else {
              return true;
            }
          }
        };
        p.setLabel("Category:");
        map.put(key, p);
        PropertyDialog dialog = 
          new PropertyDialog(((Button) e.widget).getShell(), map, TITLE_ADD_CATEGORY);
        
        if (dialog.open() == Window.OK) {
          map = dialog.getValues();
          p = (PropertyDialog.Property) map.get(key);
          List categories = new ArrayList(Arrays.asList(manifest.getCategories()));
          if (p.getValue() != null && !categories.contains(p.getValue())) {
            categories.add(p.getValue());
            manifest.setCategories((String[]) categories.toArray(new String[categories.size()]));
            wCategoryTableViewer.refresh();
            markDirty();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wCategoryAddButton.setLayoutData(wd);
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }
  
  /****************************************************************************
   * Inner classes
   ***************************************************************************/

  class CategoryContentProvider  implements IStructuredContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
        
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      return manifest.getCategories();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
}
