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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ImportPackageModel;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestFormEditor;
import org.knopflerfish.eclipse.core.ui.editors.packaging.PackagingFormEditor;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleEditor extends FormEditor implements IResourceChangeListener {
  
  private static final String PAGE_MANIFEST_ID     = "manifestId";
  private static final String PAGE_MANIFEST_TITLE  = "Bundle Manifest";
  private static final String PAGE_PACKAGING_ID    = "packagingId";
  private static final String PAGE_PACKAGING_TITLE = "Bundle Packaging";
  
  private IFile manifestFile;
  private IFile packFile;
  IFileEditorInput manifestInput;
  IFileEditorInput bundlePackInput;
  ManifestFormEditor manifestFormEditor;
  TextEditor manifestTextEditor;
  PackagingFormEditor buildFormEditor;
  BundleProject bundleProject;
  
  IDocumentProvider provider;

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
      IProject project = manifestFile.getProject();
      bundleProject = new BundleProject(JavaCore.create(project));
      packFile = project.getFile(IBundleProject.BUNDLE_PACK_FILE);
      IFileEditorInput bundlePackInput = new FileEditorInput(packFile);
      connectManifest(manifestInput);
      connectBundlePack(bundlePackInput);
      
      // Add resource change listener
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    
      setPartName("Bundle "+project.getName());
        
      // Text manifest editor
      manifestTextEditor = new TextEditor();
      
      // Graphical manifest Editor
      manifestFormEditor =  new ManifestFormEditor(this, PAGE_MANIFEST_ID, PAGE_MANIFEST_TITLE, bundleProject);
        
      // Graphical build Editor
      buildFormEditor =  new PackagingFormEditor(this, PAGE_PACKAGING_ID, PAGE_PACKAGING_TITLE, bundleProject);

      // Add editor pages to form editor
      addPage(manifestFormEditor);
      addPage(buildFormEditor);
      addPage(manifestTextEditor, manifestInput);
      setPageText(2, manifestFile.getName());
      
      IDocument manifestDoc = provider.getDocument(manifestInput);
      IDocument packDoc = provider.getDocument(bundlePackInput);
      ImportPackageModel model = 
        new ImportPackageModel(
            ManifestUtil.createManifest(manifestDoc.get().getBytes()),
            bundleProject);

      BundleDocument buildDoc = new BundleDocument(manifestDoc, packDoc, model);
      manifestFormEditor.attachDocument(buildDoc);
      buildFormEditor.attachDocument(buildDoc);

    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
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
        IDocument manifestDoc = buildFormEditor.getDocument().getManifestDocument();
        IDocument packDoc = buildFormEditor.getDocument().getPackDocument();
        provider.saveDocument(monitor, manifestInput, manifestDoc, true);
        provider.saveDocument(monitor, bundlePackInput, packDoc, true);

        // Update project buildpath
        ImportPackageModel model = manifestFormEditor.getDocument().getImportPackageModel();
        BundleManifest manifest = ManifestUtil.createManifest(manifestDoc.get().getBytes());
        model.updateManifest(manifest);
        ArrayList newPaths = new ArrayList(Arrays.asList(model.getPaths()));
        ArrayList oldPaths = new ArrayList(Arrays.asList(bundleProject.getBuildPaths()));

        // Remove overlapping paths
        for (Iterator i=newPaths.iterator(); i.hasNext();) {
          BuildPath bp = (BuildPath) i.next();
          if (oldPaths.remove(bp)) {
            i.remove();
          }
        }
        
        // Remove old paths
        for (Iterator i=oldPaths.iterator(); i.hasNext();) {
          BuildPath bp = (BuildPath) i.next();
          bundleProject.removeBuildPath(bp, false);
        }
        
        // Add new paths
        for (Iterator i=newPaths.iterator(); i.hasNext();) {
          BuildPath bp = (BuildPath) i.next();
          bundleProject.addBuildPath(bp, false);
        }
        
        firePropertyChange(PROP_DIRTY);

      }
    };
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    try {
      workspace.run(runnable,null, IWorkspace.AVOID_UPDATE, monitor);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
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
        new BundleFilesVisitor(bundleProject.getJavaProject().getProject());
      delta.accept(visitor);
      
      // Check if project is closed or removed or manifest file was  deleted
      if (visitor.isManifestRemoved() || visitor.isProjectRemoved()) {
        // Close editor
        close(false); 
        return;
      }
      
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
      OsgiUiPlugin.log(e.getStatus());
    }
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  
  void refreshEditors(final BundleFilesVisitor visitor) {

    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
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
            // Pass markers to graphical view
            IMarker[] markers = visitor.getManifestFile().findMarkers(null, true, IResource.DEPTH_INFINITE);
            manifestFormEditor.setErrors(markers);
            IDocument manifestDoc = provider.getDocument(manifestInput);
            IDocument packDoc = provider.getDocument(bundlePackInput);
            ImportPackageModel model = 
              new ImportPackageModel(
                  ManifestUtil.createManifest(manifestDoc.get().getBytes()),
                  bundleProject);

            manifestFormEditor.attachDocument(new BundleDocument(manifestDoc, packDoc, model));
            manifestFormEditor.refresh();
          }
          
          // Update build editor
          if (visitor.isManifestChanged() || visitor.isPackDescriptionChanged()) {
            IDocument manifestDoc = provider.getDocument(manifestInput);
            IDocument packDoc = provider.getDocument(bundlePackInput);
            ImportPackageModel model = 
              new ImportPackageModel(
                  ManifestUtil.createManifest(manifestDoc.get().getBytes()),
                  bundleProject);

            buildFormEditor.attachDocument(new BundleDocument(manifestDoc, packDoc, model));
            buildFormEditor.refresh();
          }

          firePropertyChange(PROP_DIRTY);
        } catch (CoreException e) {
          OsgiUiPlugin.log(e.getStatus());
        }
      }
    });
  }
  
  void connectBundlePack(IFileEditorInput bundlePackInput) throws CoreException {
    if (provider == null) return;

    disconnectBundlePack();
    
    if (bundlePackInput != null) {
      provider.connect(bundlePackInput);
      this.bundlePackInput = bundlePackInput;
    }
  }
  
  void connectManifest(IFileEditorInput manifestInput) throws CoreException {
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
  
  protected void firePropertyChange(final int propertyId) {
    super.firePropertyChange(propertyId);
  }
}
