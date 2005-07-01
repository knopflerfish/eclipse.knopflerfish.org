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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author Anders Rimén
 */
public class ExportSection extends SectionPart {

  // Section title and description
  private static final String TITLE = 
    "Export";
  private static final String DESCRIPTION = 
    "This section yada yada yada...";

  // SWT Widgets
  private Button    wExportButton;

  public ExportSection(Composite parent, FormToolkit toolkit, int style) {
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
    layout.numColumns = 1;
    container.setLayout(layout);

    // Create widgets
    wExportButton = toolkit.createButton(container, "Export...", SWT.PUSH);
    wExportButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        //FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFileName("gurka.jar");
        String path = dialog.open();
        if (path != null) {
          /*
          wLocationText.setText(path);
          if (wDefaultButton.getSelection()) {
            setDefaultSettings();
          }
          verifyAll();
          */
        }
        
      }
    });
    
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
