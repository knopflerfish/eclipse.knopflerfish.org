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

package org.knopflerfish.eclipse.core.ui.editors.build.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
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
import org.knopflerfish.eclipse.core.project.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.BundleResource;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BuildFormEditor extends FormPage implements IDocumentListener {
  
  // Model objects
  private final BundleProject project;
  private BuildDocument doc;
  
  // Sections
  private ExportSection exportSection;
  private ClasspathSection classPathSection;
  private ContentsSection resourceSection;
  
  public BuildFormEditor(FormEditor editor, String id, String title, BundleProject project) {
    super(editor, id, title);
    this.project = project;
  }

  public BuildDocument getDocument() {
    return doc;
  }
  
  public void refresh() {
    if (exportSection != null) {
      exportSection.refresh();
    }
    if (resourceSection != null) {
      resourceSection.refresh();
    }
    if (classPathSection != null) {
      classPathSection.refresh();
    }
  }
  
  public void markContentsStale() {
    resourceSection.markStale();
  }

  public void markClasspathStale() {
    classPathSection.markStale();
  }
  
  public void attachDocument(BuildDocument doc) {
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
    ColumnLayout layout = new ColumnLayout();
    layout.maxNumColumns = 1;
    layout.minNumColumns = 1;
    body.setLayout(layout);
    
    // Create sections
    exportSection = new ExportSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR, project.getJavaProject().getProject());
    exportSection.initialize(managedForm);
    
    classPathSection = new ClasspathSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR, project, this);
    classPathSection.initialize(managedForm);

    resourceSection = new ContentsSection(body, toolkit, 
        Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE, project.getJavaProject().getProject(), this);
    resourceSection.initialize(managedForm);
    
    // Add sections to form
    managedForm.addPart(exportSection);
    managedForm.addPart(classPathSection);
    managedForm.addPart(resourceSection);
  }
  
  /****************************************************************************
   * org.eclipse.jface.text.IDocumentListener methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent event) {
    System.err.println("---- Document about to Changed ----");
    if (isActive()) return;
    
    BundleManifest oldManifest = new BundleManifest(ManifestUtil.createManifest(doc.getManifestDocument()));
    List oldClassPath = Arrays.asList(oldManifest.getBundleClassPath());
    StringBuffer buf = new StringBuffer(event.getDocument().get());
    int startIdx = event.getOffset();
    int endIdx = startIdx + event.getLength();
    buf = buf.replace(startIdx, endIdx, event.getText());
    BundleManifest newManifest = new BundleManifest(ManifestUtil.createManifest(buf.toString().getBytes()));
    List newClassPath = Arrays.asList(newManifest.getBundleClassPath());
    
    // Check if bundle class path changed
    for (int i=0; i<oldClassPath.size(); i++) {
      String path = (String) oldClassPath.get(i);
      System.err.println("Old classpath entry "+i+":"+path);
      if (!newClassPath.contains(path)) {
        System.err.println("Remove classpath resource :"+path);
        removeClasspathResource(path);
      }
    }
    Map contents = getBundlePackDescription().getContentsMap(false);
    for (int i=0; i<newClassPath.size(); i++) {
      String path = (String) newClassPath.get(i);
      System.err.println("New classpath entry "+i+":"+path);
      if (!oldClassPath.contains(path)) {
        IPath file = (IPath) contents.get(path);
        if (file == null) {
          IProject p = project.getJavaProject().getProject();
          System.err.println("Find member "+path+" in project "+p.getName());
          IResource resource = p.findMember(path);
          
          if (resource != null && resource.getType() == IResource.FILE) {
            file = resource.getFullPath();
          }
        }
        
        if (file != null) {
          System.err.println("Add classpath resource :"+path);
          addClasspathResource(file);
        }
      }
    }
    
    
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentChanged(DocumentEvent event) {
    System.err.println("---- Document Changed ----");
    if (isActive()) return;
    
    // Update pack description document if needed
    BundleManifest manifest = getManifest();
    BundlePackDescription description = getBundlePackDescription();
    //Map contents = description.getContentsMap();
    
    
    IManagedForm form = getManagedForm();
    if (form == null) return;
    
    IFormPart[] parts = form.getParts();
    if (parts != null) {
      for(int i=0; i<parts.length;i++) {
        ((SectionPart) parts[i]).markStale();
      }
    }
  }

  private BundlePackDescription getBundlePackDescription() {
    try {
      BundlePackDescription bundlePackDescription = new BundlePackDescription(
          project.getJavaProject().getProject(), 
          new ByteArrayInputStream(doc.getPackDocument().get().getBytes()));
      
      return bundlePackDescription;
    } catch (Throwable t) {
      return new BundlePackDescription(project.getJavaProject().getProject());
    }
  }
  
  private void setBundlePackDescription(BundlePackDescription bundlePackDescription) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bundlePackDescription.save(baos);
      baos.flush();
      doc.getPackDocument().set(baos.toString());
    } catch (Throwable t) {
    }
  }
  
  
  private BundleManifest getManifest() {
    BundleManifest manifest = new BundleManifest(ManifestUtil.createManifest(doc.getManifestDocument()));
    
    return manifest;
  }

  private void removeClasspathResource(String name) {
    // Refresh values from document
    BundlePackDescription bundlePackDescription = getBundlePackDescription();
    Map contents = bundlePackDescription.getContentsMap(false);
    bundlePackDescription.removeResource((IPath) contents.get(name));
    setBundlePackDescription(bundlePackDescription);
  }

  private void addClasspathResource(IPath path) {
    if (path == null) return;
    BundlePackDescription bundlePackDescription = getBundlePackDescription();
    bundlePackDescription.removeResource(path);
    BundleResource resource = new BundleResource(
        BundleResource.TYPE_CLASSPATH,
        path,
        path.removeFirstSegments(1).toString(),
        null);
    bundlePackDescription.addResource(resource);
    setBundlePackDescription(bundlePackDescription);
  }
  
}
