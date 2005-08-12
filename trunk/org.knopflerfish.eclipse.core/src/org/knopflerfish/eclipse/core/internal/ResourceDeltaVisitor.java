package org.knopflerfish.eclipse.core.internal;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.project.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundleProject;

public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource res = delta.getResource();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
    switch(res.getType()) {
    case IResource.FILE:
      IFile file = (IFile) res;
      if (BundleProject.CLASSPATH_FILE.equals(file.getName())) {
        System.err.println("Classpath changed, synch manifest");
        BundleProject project = new BundleProject(JavaCore.create(file.getProject()));
        Map contents = project.getBundlePackDescription().getContentsMap(false);
        BundleManifest manifest = project.getBundleManifest();
        String[] bundleClassPath = manifest.getBundleClassPath();
        IClasspathEntry [] rawClassPath = project.getJavaProject().getRawClasspath();
        
        int idx = 0;
        boolean synchManifest = false;
        for (int i=0; i<rawClassPath.length; i++) {
          IClasspathEntry entry = rawClassPath[i];
          if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) {
            continue;
          }
          IResource lib = root.findMember(entry.getPath());
          if (project.getJavaProject().getProject().equals(lib.getProject())) {
            // Check array bounds
            if (idx > bundleClassPath.length-1) {
              synchManifest = true;
              break;
            }
            
            IPath path = (IPath) contents.get(bundleClassPath[idx]);
            if (!entry.getPath().equals(path)) {
              synchManifest = true;
              break;
            }
            idx = idx+1;
            continue;
          }
        }
        if (idx != bundleClassPath.length) {
          synchManifest = true;
        }

        if (synchManifest) {
          // Update manifest
          try {
            Runnable runnable = new SynchManifestRunnable(project, rawClassPath);
            new Thread(runnable).start();
          } catch (Throwable t) {
            t.printStackTrace();
          }
        } else {
          System.err.println("No manifest already in synch");
        }
        return true;
      } else if (BundleProject.MANIFEST_FILE.equals(file.getName())) {
        System.err.println("Manifest changed, synch classpath");
        BundleProject project = new BundleProject(JavaCore.create(file.getProject()));
        BundleManifest manifest = project.getBundleManifest();
        String[] bundleClassPath = manifest.getBundleClassPath();
        IClasspathEntry [] projectClassPath = project.getJavaProject().getRawClasspath();
        
        return true;
      } else if (BundleProject.BUNDLE_PACK_FILE.equals(file.getName())) {
        System.err.println("Pack file changed");
        return true;
      } else {
        return false;
      }
    case IResource.FOLDER:
      return false;
    case IResource.PROJECT:
      IProject project = (IProject) res;
      if (project.hasNature(Osgi.NATURE_ID)) {
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
}
