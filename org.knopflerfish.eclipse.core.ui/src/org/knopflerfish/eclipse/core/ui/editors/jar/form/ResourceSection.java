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

package org.knopflerfish.eclipse.core.ui.editors.jar.form;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén
 */
public class ResourceSection extends SectionPart {

  private static final int NUM_TREE_ROWS    = 5;
  
  // Section title and description
  private static final String TITLE = 
    "Resources";
  private static final String DESCRIPTION = 
    "This section lists the resources to be inluded in bundle JAR-file.";

  // SWT Widgets
  private Button    wResourceAddButton;
  private Button    wResourceRemoveButton;
  

  public ResourceSection(Composite parent, FormToolkit toolkit, int style) {
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
    /*
    IManagedForm managedForm = getManagedForm();
    IDocument doc = (IDocument) managedForm.getInput();

    if (manifest == null || manifestWorkingCopy == null) return;
    
    Table wCategoryTable = wCategoryTableViewer.getTable();
    try {
      String attribute = (String) wCategoryTable.getData(PROP_NAME);
      if (attribute != null) {
        String value = manifestWorkingCopy.getAttribute(BundleManifest.BUNDLE_CATEGORY);
        if (value == null) value = "";
        ManifestSectionUtil.setManifestAttribute(doc, attribute, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    */
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();

    // Refresh values from document
    /*
    manifest = new BundleManifest(ManifestSectionUtil.createManifest((IDocument) getManagedForm().getInput()));
    manifestWorkingCopy = new BundleManifest(manifest);
    if (wCategoryTableViewer != null) {
      wCategoryTableViewer.setInput(manifestWorkingCopy);
    }
    //wCategoryTableViewer.getTable().pack(true);
    packTableColumns(wCategoryTableViewer.getTable());
    */
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
    // Resource Tree
    Tree wResourceTree = toolkit.createTree(container, SWT.MULTI | SWT.FULL_SELECTION);
    wResourceTree.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

    TableWrapData wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wResourceTree, NUM_TREE_ROWS);
    wResourceTree.setLayoutData(wd);
    
    // Resource Buttons
    wResourceAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wResourceAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // TODO
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wResourceAddButton.setLayoutData(wd);
    
    wResourceRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wResourceRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // TODO
      }
    });
    wResourceRemoveButton.setEnabled(false);
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wResourceRemoveButton.setLayoutData(wd);
    
    /*
    Table wCategoryTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wCategoryTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    wCategoryTable.setData(PROP_DIRTY, new Boolean(false));
    wCategoryTable.setData(PROP_NAME, BundleManifest.BUNDLE_CATEGORY);

    //Table wCategoryTable = new Table(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
    wCategoryTableViewer = new TableViewer(wCategoryTable);
    wCategoryTableViewer.setContentProvider(new CategoryContentProvider());
    //wCategoryTableViewer.setSorter(new SorterStartLevel());
    
    //wCategoryTable.setHeaderVisible(true);
    //wCategoryTable.setLinesVisible(true);
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
    

    wCategoryRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wCategoryRemoveButton.setEnabled(false);
    wCategoryRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wCategoryTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        List categories = new ArrayList(Arrays.asList(manifestWorkingCopy.getCategories()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          categories.remove(name);
        }
        manifestWorkingCopy.setCategories((String[]) categories.toArray(new String[categories.size()]));
        wCategoryTableViewer.refresh();
        updateDirtyState();
        
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
          List categories = new ArrayList(Arrays.asList(manifestWorkingCopy.getCategories()));
          if (p.getValue() != null && !categories.contains(p.getValue())) {
            categories.add(p.getValue());
            manifestWorkingCopy.setCategories((String[]) categories.toArray(new String[categories.size()]));
            wCategoryTableViewer.refresh();
            updateDirtyState();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wCategoryAddButton.setLayoutData(wd);
    */
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }

  public void updateDirtyState() {
    // Loop through components and check dirty state
    //boolean dirty = false;

    /*
    if (manifest == null || manifestWorkingCopy == null) return;
    
    String cat = manifest.getAttribute(BundleManifest.BUNDLE_CATEGORY);
    if (cat == null) cat = "";
    String catWC = manifestWorkingCopy.getAttribute(BundleManifest.BUNDLE_CATEGORY);
    if (catWC == null) catWC = "";
     
    if (!catWC.equals(cat)) {
      markDirty();
    }
    */
  }
  
}
