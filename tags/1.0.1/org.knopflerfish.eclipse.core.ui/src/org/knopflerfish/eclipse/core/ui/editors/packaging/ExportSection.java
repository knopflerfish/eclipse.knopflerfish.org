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

package org.knopflerfish.eclipse.core.ui.editors.packaging;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.project.BundleProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ExportSection extends SectionPart {

  // Section title and description
  private static final String TITLE = 
    "Export";
  private static final String DESCRIPTION = 
    "Packages and exports the bundle";

  final BundleProject project;
  final FormEditor editor;
  
  // SWT Widgets
  private Button    wExportButton;

  public ExportSection(Composite parent, FormToolkit toolkit, int style, BundleProject project, FormEditor editor) {
    super(parent, toolkit, style);
    
    this.project = project;
    this.editor = editor;
    
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
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();
  }
  
  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.editors.ManifestSectionPart#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public void createClient(Section section, FormToolkit toolkit) {

    // Set section layout
    GridData data = new GridData(SWT.FILL);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.TOP;
    section.setLayoutData(data);

    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 1;
    container.setLayout(layout);

    // Create widgets
    wExportButton = toolkit.createButton(container, "Export...", SWT.PUSH);
    wExportButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        // Check if editor is dirty
        if (editor.isDirty()) {
          // Prompt user to save
          MessageDialog msgDialog = new MessageDialog(
              Display.getCurrent().getActiveShell(),
              "Save manifest",
              null,
              "Manifest has changed, do you want to save it before exporting bundle?",
              MessageDialog.QUESTION,
              new String[] {"Yes","No", "Cancel"},
              0);
          msgDialog.setBlockOnOpen(true);
          int ret = msgDialog.open();
          if (ret == 0) {
            // Ok, save manifest
            editor.doSave(null);
          } else if (ret == 2) {
            // Cancel
            return;
          }
        }
        
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFileName(project.getFileName());
        String path = dialog.open();
        if (path != null) {
          try {
            project.getBundlePackDescription().export(project, path);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }
}
