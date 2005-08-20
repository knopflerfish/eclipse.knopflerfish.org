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

package org.knopflerfish.eclipse.core.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.editors.build.form.BuildDocument;
import org.knopflerfish.eclipse.core.ui.editors.build.form.BuildFormEditor;
import org.knopflerfish.eclipse.core.ui.editors.manifest.form.ManifestFormEditor;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleEditor extends FormEditor implements IResourceChangeListener {
  
  private static final String PAGE_OVERVIEW_ID      = "overviewId";
  private static final String PAGE_OVERVIEW_TITLE   = "Overview";
  private static final String PAGE_BUILD_ID         = "buildId";
  private static final String PAGE_BUILD_TITLE      = "Build";
  
  private IFile manifestFile;
  private IFile packFile;
  private IFileEditorInput manifestInput;
  private IFileEditorInput bundlePackInput;
  private ManifestFormEditor manifestFormEditor;
  private TextEditor manifestTextEditor;
  private BuildFormEditor buildFormEditor;
  private BundleProject project;
  
  private IDocumentProvider provider;

  /****************************************************************************
   * org.eclipse.ui.IWorkbenchPart methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    
    // Add resource change listener
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.removeResourceChangeListener(this);
    
    disconnectBundlePack();
    disconnectManifest();
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
      connectManifest(manifestInput);
      connectBundlePack(bundlePackInput);
      
      // Add resource change listener
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    
      setPartName("Bundle "+manifestFile.getProject().getName());
        
      // Text manifest editor
      manifestTextEditor = new TextEditor();
      
      // Graphical manifest Editor
      manifestFormEditor =  new ManifestFormEditor(this, PAGE_OVERVIEW_ID, PAGE_OVERVIEW_TITLE, project);
        
      // Graphical manifest Editor
      buildFormEditor =  new BuildFormEditor(this, PAGE_BUILD_ID, PAGE_BUILD_TITLE, project);

      // Add editor pages to form editor
      addPage(manifestFormEditor);
      addPage(buildFormEditor);
      addPage(manifestTextEditor, manifestInput);
      setPageText(2, manifestFile.getName());
      
      IDocument manifestDoc = provider.getDocument(manifestInput);
      manifestFormEditor.attachDocument(manifestDoc);
      
      IDocument packDoc = provider.getDocument(bundlePackInput);
      buildFormEditor.attachDocument(new BuildDocument(manifestDoc, packDoc));
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
   * org.eclipse.ui.part.EditorPart methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
   */
  protected void setInput(IEditorInput input) {
    super.setInput(input);
    provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void doSave(IProgressMonitor monitor) {
    // Commit form pages
    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) throws CoreException {
        // Commit changes
        manifestFormEditor.doSave(monitor);
        buildFormEditor.doSave(monitor);
        // Save documents
        provider.saveDocument(monitor, manifestInput, buildFormEditor.getDocument().getManifestDocument(), true);
        provider.saveDocument(monitor, bundlePackInput, buildFormEditor.getDocument().getPackDocument(), true);
        firePropertyChange(PROP_DIRTY);
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
    if (buildFormEditor != null) {
      dirty = dirty || buildFormEditor.isDirty();
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
      IResourceDelta delta = event.getDelta();
      final BundleFilesVisitor visitor = 
        new BundleFilesVisitor(project.getJavaProject().getProject());
      delta.accept(visitor);
      
      // Check if manifest or pack description has changed
      if (!visitor.isManifestChanged() && !visitor.isPackDescriptionChanged()) {
        // Nothing changed
        return;
      }
        
      // If editors prompt user that files have changed
      if (isDirty()) {
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            MessageDialog dialog = new MessageDialog(
                Display.getDefault().getActiveShell(),
                "Files changed",
                null,
                "Bundle files has changed, do you want to reload them?",
                MessageDialog.QUESTION,
                new String[] {"Yes", "No"},
                0
                );
            if (dialog.open() == 0) {
              refreshEditors(visitor);
            }
          }
        });
      } else {
        refreshEditors(visitor);
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  
  private void refreshEditors(final BundleFilesVisitor visitor) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
          System.err.println("BundleEditor - refreshEditors");
          // Connect inputs
          if (visitor.isManifestChanged()) {
            IFileEditorInput input = new FileEditorInput(visitor.getManifestFile());
            connectManifest(input);
          }
          if (visitor.isPackDescriptionChanged()) {
            IFileEditorInput input = new FileEditorInput(visitor.getPackDescriptionFile());
            connectBundlePack(input);
          }
          
          // Update manifest editor
          if (visitor.isManifestChanged()) {
            if (manifestTextEditor.isDirty()) {
              manifestTextEditor.doRevertToSaved();
            }
            IDocument doc = provider.getDocument(manifestInput);
            manifestFormEditor.attachDocument(doc);
            manifestFormEditor.refresh();
          }
          
          // Update build editor
          if (visitor.isManifestChanged() || visitor.isPackDescriptionChanged()) {
            IDocument manifestDoc = provider.getDocument(manifestInput);
            IDocument packDoc = provider.getDocument(bundlePackInput);
            buildFormEditor.attachDocument(new BuildDocument(manifestDoc, packDoc));
            buildFormEditor.refresh();
          }

          firePropertyChange(PROP_DIRTY);
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    });
  }
  
  private void connectBundlePack(IFileEditorInput bundlePackInput) throws CoreException {
    if (provider == null) return;

    disconnectBundlePack();
    
    if (bundlePackInput != null) {
      provider.connect(bundlePackInput);
      this.bundlePackInput = bundlePackInput;
    }
  }
  
  private void connectManifest(IFileEditorInput manifestInput) throws CoreException {
    if (provider == null) return;

    disconnectManifest();
    
    if (manifestInput != null) {
      provider.connect(manifestInput);
      this.manifestInput = manifestInput;
    }
  }
  
  private void disconnectBundlePack() {
    if (provider == null) return;
    
    if (bundlePackInput != null) {
      provider.disconnect(bundlePackInput);
    }
  }
  
  private void disconnectManifest() {
    if (provider == null) return;
    
    if (manifestInput != null) {
      provider.disconnect(manifestInput);
    }
  }
}
