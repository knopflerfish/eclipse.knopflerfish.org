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

package org.knopflerfish.eclipse.core.internal;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.BundleResource;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SynchManifestRunnable implements IWorkspaceRunnable, Runnable {

  private final IProject project;
  private final BundleProject bundleProject;
  private final IClasspathEntry[] entries;
  
  public SynchManifestRunnable(IProject project) throws CoreException {
    this.project = project;
    IJavaProject javaProject = JavaCore.create(project);
    this.bundleProject = new BundleProject(javaProject);
    this.entries = javaProject.getRawClasspath();
  }
  
  /****************************************************************************
   * java.lang.Runnable methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      workspace.run(this, project, IWorkspace.AVOID_UPDATE, null);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /****************************************************************************
   * org.eclipse.core.resources.IWorkspaceRunnable methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void run(IProgressMonitor monitor) throws CoreException {
    if (project == null) return;
    
    BundlePackDescription packDescription = bundleProject.getBundlePackDescription();
    packDescription.removeResource(BundleResource.TYPE_CLASSPATH);
    BundleManifest manifest = bundleProject.getBundleManifest();
    Map contents = packDescription.getContentsMap(true);
    ArrayList bundleClasspath = new ArrayList();
   
    for (int i=0; i<entries.length; i++) {
      IClasspathEntry entry = entries[i];
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && !bundleClasspath.contains(".")) {
        bundleClasspath.add(".");
      } else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        IPath defaultPath = entry.getPath().removeFirstSegments(1);
        IResource lib = project.findMember(defaultPath);
        if (lib != null) {
          String path = (String) contents.get(entry.getPath());
          if (path == null) {
            path = defaultPath.toString();
          }
          // Create map entry
          BundleResource resource = new BundleResource(
              BundleResource.TYPE_CLASSPATH,
              entry.getPath(), 
              defaultPath.toString(),
              null);
          packDescription.addResource(resource);
          bundleClasspath.add(path);
        }
      }
    }
    
    bundleProject.saveBundlePackDescription(packDescription);
    manifest.setBundleClassPath((String[]) bundleClasspath.toArray(new String[bundleClasspath.size()]));
    bundleProject.setBundleManifest(manifest);
  }

}
