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

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ManifestFormEditor extends FormPage implements IDocumentListener {

  // Model objects
  private final BundleProject project;
  private BundleDocument doc;
  
  // Sections
  private GeneralSection generalSection;
  private PackageSection packageSection;
  private NativeCodeSection nativeCodeSection;
  private ClasspathSection classPathSection;

  public ManifestFormEditor(FormEditor editor, String id, String title, BundleProject project) {
    super(editor, id, title);
    this.project = project;
  }
  
  public BundleDocument getDocument() {
    return doc;
  }
  
  public void refresh() {
    if (generalSection != null) {
      generalSection.refresh();
    }
    if (classPathSection != null) {
      classPathSection.refresh();
    }
    if (packageSection != null) {
      packageSection.refresh();
    }
    if (nativeCodeSection != null) {
      nativeCodeSection.refresh();
    }
  }
  
  public void attachDocument(BundleDocument doc) {
    // Remove listener from old doc
    if (this.doc != null) {
      this.doc.removeDocumentListener(this);
    }
    // Add listener on new doc
    this.doc = doc;
    if (this.doc != null) {
      this.doc.addDocumentListener(this);
    }

    IManagedForm form = getManagedForm();
    if (form == null) return;
    form.setInput(doc);

    firePropertyChange(PROP_INPUT);
  }
  
  /****************************************************************************
   * org.eclipse.ui.IWorkbenchPart methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();

    if (this.doc != null) {
      this.doc.removeDocumentListener(this);
    }
  }
  
  /****************************************************************************
   * org.eclipse.ui.forms.editor.FormPage methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
   */
  public void createFormContent(IManagedForm managedForm) {
    if (doc != null) {
      managedForm.setInput(doc);
    }

    // Create form
    FormToolkit toolkit = managedForm.getToolkit();   
    ScrolledForm form = managedForm.getForm(); 
    form.setText(getTitle()); 
    Composite body = form.getBody();

    // Set layout manager
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.makeColumnsEqualWidth = true;
    body.setLayout(layout);
    
    // Create sections
    generalSection = new GeneralSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR, project);
    generalSection.initialize(managedForm);

    classPathSection = new ClasspathSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR, project);
    classPathSection.initialize(managedForm);
    
    packageSection = new PackageSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR, project);
    packageSection.initialize(managedForm);

    nativeCodeSection = new NativeCodeSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE, project);
    nativeCodeSection.initialize(managedForm);
    
    // Add sections to form
    managedForm.addPart(generalSection);
    managedForm.addPart(classPathSection);
    managedForm.addPart(packageSection);
    managedForm.addPart(nativeCodeSection);
    
    // Pass markers to graphical view
    try {
      IFileEditorInput manifestInput = (IFileEditorInput) getEditorInput();
      IFile manifestFile = manifestInput.getFile();
      setErrors(manifestFile.findMarkers(null, true, IResource.DEPTH_INFINITE));
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
   * org.eclipse.jface.text.IDocumentListener methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent event) {
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentChanged(DocumentEvent event) {
    if (isActive()) return;
    
    IManagedForm form = getManagedForm();
    if (form == null) return;

    IFormPart[] parts = form.getParts();
    if (parts != null) {
      for(int i=0; i<parts.length;i++) {
        ((SectionPart) parts[i]).markStale();
      }
    }
  }
  
  public void setErrors(IMarker[] markers) {
    if (generalSection != null) {
      ArrayList errorsGeneralSection = new ArrayList();
      for (int i=0; i<markers.length; i++) {
        try {
          if (markers[i].getType().equals(BundleProject.MARKER_BUNDLE_ACTIVATOR)) {
            errorsGeneralSection.add(BundleManifest.BUNDLE_ACTIVATOR);
          }
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
      
      generalSection.setErrors(errorsGeneralSection);
    }
    
  }
}
