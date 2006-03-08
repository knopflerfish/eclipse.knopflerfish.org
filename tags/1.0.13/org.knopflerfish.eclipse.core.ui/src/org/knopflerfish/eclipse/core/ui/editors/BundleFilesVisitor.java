package org.knopflerfish.eclipse.core.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.knopflerfish.eclipse.core.IBundleProject;

public class BundleFilesVisitor implements IResourceDeltaVisitor {

  private final IProject project;
  private boolean manifestChanged = false;
  private boolean packDescriptionChanged = false;
  private boolean manifestRemoved = false;
  private boolean packDescriptionRemoved = false;
  private boolean projectRemoved = false;
  private IFile manifestFile = null;
  private IFile packDescriptionFile = null;

  public BundleFilesVisitor(IProject project) {
    this.project = project;
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
      IFile file = (IFile) res;
      if (IBundleProject.MANIFEST_FILE.equals(file.getName())) {
        manifestRemoved = delta.getKind() == IResourceDelta.REMOVED;
        manifestChanged = true;
        manifestFile = file;
        return true;
      } else if (IBundleProject.BUNDLE_PACK_FILE.equals(file.getName())) {
        packDescriptionRemoved = delta.getKind() == IResourceDelta.REMOVED;
        packDescriptionChanged = true;
        packDescriptionFile = file;
        return true;
      } else {
        return false;
      }
    case IResource.FOLDER:
      return false;
    case IResource.PROJECT:
      String name = ((IProject) res).getName();
      if (project.getName().equals(name)) {
        if (delta.getKind() == IResourceDelta.REMOVED) {
          projectRemoved = true;
        }
        return true;
      }
      return false;
    case IResource.ROOT:
      return true;
    default:  
      return false;
    }
  }

  /****************************************************************************
   * Public getters
   ***************************************************************************/
  public boolean isProjectRemoved() {
    return projectRemoved;
  }
  
  public boolean isManifestRemoved() {
    return manifestRemoved;
  }

  public boolean isPackDescriptionRemoved() {
    return packDescriptionRemoved;
  }

  public boolean isManifestChanged() {
    return manifestChanged;
  }

  public boolean isPackDescriptionChanged() {
    return packDescriptionChanged;
  }

  public IFile getManifestFile() {
    return manifestFile;
  }

  public IFile getPackDescriptionFile() {
    return packDescriptionFile;
  }

}
