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

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.knopflerfish.eclipse.core.ui.editors.manifest.text.ManifestTextEditor;

/**
 * @author Anders Rim�n
 */
public class ManifestFormEditor extends FormPage {

  private static final String TITLE = "Overview";
  
  private final ManifestTextEditor manifestTextEditor;
  
  public ManifestFormEditor(FormEditor editor, String id, String title, ManifestTextEditor manifestTextEditor) {
    super(editor, id, title);
    this.manifestTextEditor = manifestTextEditor;
  }

  /****************************************************************************
   * org.eclipse.ui.forms.editor.FormPage methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
   */
  public void createFormContent(IManagedForm managedForm) {
    
    // Attach document to managed form and add document change listener
    IDocument doc = manifestTextEditor.getDocumentProvider().getDocument(getEditorInput());
    managedForm.setInput(doc);
    doc.addDocumentListener(new IDocumentListener() {
      public void documentAboutToBeChanged(DocumentEvent event) {
      }
      public void documentChanged(DocumentEvent event) {
        if (isActive()) return;
        
        IFormPart[] parts = getManagedForm().getParts();
        
        if (parts != null) {
          for(int i=0; i<parts.length;i++) {
            ((SectionPart) parts[i]).markStale();
          }
        }
      }
    });

    // Create form
    FormToolkit toolkit = managedForm.getToolkit();   
    ScrolledForm form = managedForm.getForm(); 
    form.setText(TITLE); 
    Composite body = form.getBody();

    // Set layout manager
    ColumnLayout layout = new ColumnLayout();
    layout.maxNumColumns = 2;
    layout.minNumColumns = 2;
    body.setLayout(layout);
    
    // Create sections
    /*
    OverviewGeneralSection generalSection = new OverviewGeneralSection(body, toolkit, 
        Section.DESCRIPTION | Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
    */
    OverviewGeneralSection generalSection = new OverviewGeneralSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR);
    generalSection.initialize(managedForm);

    OverviewCategorySection categorySection = new OverviewCategorySection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR);
    categorySection.initialize(managedForm);
    
    OverviewVendorSection vendorSection = new OverviewVendorSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR);
    vendorSection.initialize(managedForm);
    
    OverviewDocumentationSection docSection = new OverviewDocumentationSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR);
    docSection.initialize(managedForm);
    
    // Add sections to form
    managedForm.addPart(generalSection);
    managedForm.addPart(categorySection);
    managedForm.addPart(vendorSection);
    managedForm.addPart(docSection);
  }
}