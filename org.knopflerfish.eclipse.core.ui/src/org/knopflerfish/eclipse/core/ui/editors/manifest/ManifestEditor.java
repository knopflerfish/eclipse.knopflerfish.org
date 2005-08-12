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

import java.awt.Window;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.editors.jar.form.JarFormEditor;
import org.knopflerfish.eclipse.core.ui.editors.jar.text.JarTextEditor;
import org.knopflerfish.eclipse.core.ui.editors.manifest.form.ManifestFormEditor;
import org.knopflerfish.eclipse.core.ui.editors.manifest.text.ManifestTextEditor;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ManifestEditor extends FormEditor implements IResourceChangeListener, IResourceDeltaVisitor {
  
  private static final String PAGE_OVERVIEW_ID      = "overviewId";
  private static final String PAGE_OVERVIEW_TITLE   = "Overview";
  
  private IFile manifestFile;
  private IFile packFile;
  private ManifestFormEditor manifestFormEditor;
  private ManifestTextEditor manifestTextEditor;
  private JarFormEditor jarFormEditor;
  private JarTextEditor jarTextEditor;
  private BundleProject project;

  public void dispose() {
    super.dispose();
    // Add resource change listener
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.removeResourceChangeListener(this);
  }
  
  /****************************************************************************
   * org.eclipse.ui.forms.editor.FormEditor methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
   */
  protected void addPages() {

    // Set tab name to project name
    IEditorInput editorInput = getEditorInput();
    if (!(editorInput instanceof IFileEditorInput)) return;

    try {
      // Create editor inputs
      IFileEditorInput manifestInput = (IFileEditorInput) editorInput;
      manifestFile = manifestInput.getFile();
      project = new BundleProject(JavaCore.create(manifestFile.getProject()));
      packFile = project.getBundlePackDescriptionFile();
      IFileEditorInput bundlePackInput = new FileEditorInput(packFile);
      
      // Add resource change listener
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    
      setPartName("Bundle "+manifestFile.getProject().getName());
        
      // Text manifest editor
      manifestTextEditor = new ManifestTextEditor();
      
      // Graphical manifest Editor
      manifestFormEditor =  new ManifestFormEditor(this, PAGE_OVERVIEW_ID, PAGE_OVERVIEW_TITLE, project);
        
      // Text manifest editor
      jarTextEditor = new JarTextEditor();
      
      // Graphical manifest Editor
      jarFormEditor =  new JarFormEditor(this, PAGE_OVERVIEW_ID, PAGE_OVERVIEW_TITLE, jarTextEditor);

      // Add editor pages to form editor
      addPage(manifestFormEditor);
      addPage(manifestTextEditor, manifestInput);
      setPageText(1, manifestFile.getName());
      IDocumentProvider provider = manifestTextEditor.getDocumentProvider();
      IDocument doc = provider.getDocument(manifestInput);
      manifestFormEditor.attachDocument(doc);
        
      // Add editor pages to form editor
      // TODO : Use graphical jar editor instead
      //addPage(jarFormEditor, input);
      addPage(jarTextEditor, bundlePackInput);
      setPageText(2, manifestFile.getName());
    } catch (CoreException e) {
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
    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) throws CoreException {
        manifestFormEditor.doSave(monitor);
        manifestTextEditor.doSave(monitor);
        jarFormEditor.doSave(monitor);
        jarTextEditor.doSave(monitor);
      }
    };
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    try {
      workspace.run(runnable,null, IWorkspace.AVOID_UPDATE, monitor);
    } catch (CoreException e) {
      e.printStackTrace();
    }
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
    if (manifestFormEditor != null) {
      dirty = dirty || manifestFormEditor.isDirty();
    }
    if (jarFormEditor != null) {
      dirty = dirty || jarFormEditor.isDirty();
    }
    return dirty;
  }

  /****************************************************************************
   * org.eclipse.core.resources.IResourceChangeListener methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   */
  public void resourceChanged(IResourceChangeEvent event) {
    try {
      event.getDelta().accept(this);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
   * org.eclipse.core.resources.IResourceDeltaVisitor methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource res = delta.getResource();
    switch(res.getType()) {
    case IResource.FILE:
      final IFile file = (IFile) res;
      if (BundleProject.MANIFEST_FILE.equals(file.getName())) {
        // Input changed
        if (isDirty()) {
          // TODO:Only one dialg box if both pack and manifest is changed
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              MessageDialog dialog = new MessageDialog(
                  Display.getDefault().getActiveShell(),
                  "File changed",
                  null,
                  "File has changed, do you want to reload it?",
                  MessageDialog.QUESTION,
                  new String[] {"Ok", "Cancel"},
                  0
                  );
              if (dialog.open() == 0) {
                refreshManifestEditors(file);
              }
            }
          });
          System.err.println("Dirty: notify user to refresh files");
        } else {
          refreshManifestEditors(file);
        }
        return true;
      } else if (BundleProject.BUNDLE_PACK_FILE.equals(file.getName())) {
        // Input changed
        System.err.println("ManifestEditor : Pack file changed");
        if (isDirty()) { 
          System.err.println("Dirty: notify user to refresh files");
        } else {
          System.err.println("Not dirty just update editors with new input");
        }
        return true;
      } else {
        return false;
      }
    case IResource.FOLDER:
      return false;
    case IResource.PROJECT:
      String name = ((IProject) res).getName();
      if (project.getJavaProject().getProject().getName().equals(name)) {
        return true;
      } else {
        return false;
      }
    case IResource.ROOT:
      return true;
    default:  
      return false;
    }
  }
  
  private void refreshManifestEditors(final IFile file) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IFileEditorInput input = new FileEditorInput(file);
        manifestTextEditor.setInput(input);
        IDocumentProvider provider = manifestTextEditor.getDocumentProvider();
        IDocument doc = provider.getDocument(input);
        manifestFormEditor.attachDocument(doc);
        manifestFormEditor.refresh();
      }
    });
  }
}
