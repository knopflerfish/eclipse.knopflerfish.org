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

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.project.BundleProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

  /****************************************************************************
   * org.eclipse.core.resources.IResourceDeltaVisitor methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource res = delta.getResource();
 
    if (delta.getKind() == IResourceDelta.REMOVED) return false;

    switch(res.getType()) {
    case IResource.FILE:
      // Do not do anything on marker changes
      int flags = delta.getFlags();
      if ( (flags ^ IResourceDelta.MARKERS) == 0) {
        return false;
      }

      IFile file = (IFile) res;
      IProject project = file.getProject();
      if (IBundleProject.CLASSPATH_FILE.equals(file.getName())) {

        if (!isClasspathSynched(project)) {
          // Update manifest
          Runnable runnable = new SynchManifestRunnable(project);
          new Thread(runnable).start();
        }
      } else if (IBundleProject.MANIFEST_FILE.equals(file.getName())) {
        if (!isClasspathSynched(project)) {
          // Update classpath
          Runnable runnable = new SynchClasspathRunnable(project);
          new Thread(runnable).start();
        }
      }
      return false;
    case IResource.FOLDER:
      return false;
    case IResource.PROJECT:
      // Only interested in bundle projects
      if (((IProject) res).isOpen()) {
        return ((IProject) res).hasNature(Osgi.NATURE_ID);
      }
      return false; 
    case IResource.ROOT:
      return true;
    default:  
      return false;
    }
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/

  private boolean isClasspathSynched(IProject project) throws CoreException {

    boolean synchClasspath = false;
    
    // Get references to project
    IJavaProject javaProject = JavaCore.create(project);
    BundleProject bundleProject = new BundleProject(javaProject);

    // Check if bundle classpath is out of synch
    Map contents = bundleProject.getBundlePackDescription().getContentsMap(false);
    String[] bundleClassPath = bundleProject.getBundleManifest().getBundleClassPath();
    if (bundleClassPath.length == 0) {
      bundleClassPath = new String[] {"."};
    }
    IClasspathEntry [] rawClassPath = javaProject.getRawClasspath();
    
    int idx = 0;
    for (int i=0; i<rawClassPath.length; i++) {
      IClasspathEntry entry = rawClassPath[i];

      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        if (idx == 0 && bundleClassPath.length == 0) {
          continue;
        } else if (idx > bundleClassPath.length-1) {
          // Check array bounds
          synchClasspath = true;
          break;
        } else if (!".".equals(bundleClassPath[idx])) {
          synchClasspath = true;
          break;
        } else {
          idx = idx +1;
        }
      } else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        // Check that library is a member of this project
        IResource lib = project.findMember(entry.getPath().removeFirstSegments(1));
        if (lib != null) {
          // Check array bounds
          if (idx > bundleClassPath.length-1) {
            synchClasspath = true;
            break;
          }
          
          IPath path = (IPath) contents.get(bundleClassPath[idx]);
          if (!entry.getPath().equals(path)) {
            synchClasspath = true;
            break;
          }
          idx = idx+1;
        }
      }
    }

    if(idx != bundleClassPath.length) {
      synchClasspath = true; 
    }
    
    return !synchClasspath;
  }
}

