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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.knopflerfish.eclipse.core.ui.editors.manifest.form.ManifestFormEditor;
import org.knopflerfish.eclipse.core.ui.editors.manifest.text.ManifestTextEditor;

/**
 * @author Anders Rimén
 */
public class ManifestEditor extends FormEditor {

  private static final String PAGE_OVERVIEW_ID      = "overviewId";
  private static final String PAGE_OVERVIEW_TITLE   = "Overview";
  
  private ManifestFormEditor formEditor;
  private ManifestTextEditor textEditor;

  /****************************************************************************
   * org.eclipse.ui.forms.editor.FormEditor methods
   ***************************************************************************/

  /* (non-Javadoc)
   * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
   */
  protected void addPages() {
    // Set tab name to project name
    IFileEditorInput input = (IFileEditorInput) getEditorInput();
    IFile file = input.getFile();
    setPartName("Manifest "+file.getProject().getName());
    
    // Text manifest editor
    textEditor = new ManifestTextEditor();
    
    // Graphical manifest Editor
    formEditor =  new ManifestFormEditor(this, PAGE_OVERVIEW_ID, PAGE_OVERVIEW_TITLE, textEditor);
    
    try {
      // Add editor pages to form editor
      addPage(formEditor);
      addPage(textEditor, getEditorInput());
      //setPageText(1, "Manifest.mf");
      setPageText(1, file.getName());
    } catch (PartInitException e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
   * org.eclipse.ui.part.EditorPart methods
   ***************************************************************************/

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void doSave(IProgressMonitor monitor) {
    // Commit form pages
    formEditor.doSave(monitor);
    textEditor.doSave(monitor);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#doSaveAs()
   */
  public void doSaveAs() {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  public boolean isSaveAsAllowed() {
    return false;
  }

  /****************************************************************************
   * org.eclipse.ui.ISaveablePart methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.ISaveablePart#isDirty()
   */
  public boolean isDirty() {
    boolean dirty = super.isDirty();
    if (formEditor != null) {
      dirty = dirty || formEditor.isDirty();
    }
    return dirty;
  }

}
