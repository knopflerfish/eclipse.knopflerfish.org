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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SynchClasspathRunnable implements IWorkspaceRunnable, Runnable {
  
  private final IProject project;
  private final BundleProject bundleProject;
  private final IClasspathEntry[] entries;
  
  public SynchClasspathRunnable(IProject project) throws CoreException {
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
      workspace.run(this, null, IWorkspace.AVOID_UPDATE, null);
    } catch (CoreException e) {
      OsgiPlugin.log(e.getStatus());
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
    
    // This method syncs the project class path according to what is listed in the
    // bundle manifest.
    BundlePackDescription packDescription = bundleProject.getBundlePackDescription();
    String[] bundleClasspath = bundleProject.getBundleManifest().getBundleClassPath();
    if (bundleClasspath.length == 0) {
      bundleClasspath = new String[] {"."};
    }
    Map contents = packDescription.getContentsMap(false);
    ArrayList projectClasspath = new ArrayList();
    
    int idx = 0;
    boolean srcDone = false;
    for (int i=0; i<bundleClasspath.length; i++) {
      String path = bundleClasspath[i];
      if (".".equals(path) && !srcDone) {
        // Only one source allowed
        srcDone = true;
        // Find source entries
        Integer[] srcIdx = findSourceEntries(entries);
        if (srcIdx.length > 0) {
          
          // Copy external class path entries located before first src entry
          copyEntries(idx, srcIdx[0].intValue(), entries, projectClasspath, false);
          // Group all src entries
          for(int j=0; j<srcIdx.length; j++) {
            projectClasspath.add(entries[srcIdx[j].intValue()]);
          }
          idx = srcIdx[0].intValue()+1;
        } else{
          projectClasspath.add(createDefaultSourceEntry());
          copyEntries(idx, entries.length, entries, projectClasspath, false);
          idx = entries.length;
        }
      } else {
        // Find library entry
        IPath libPath = (IPath) contents.get(path);
        if (libPath == null) {
          // Something wrong with contents file, nothing to do just continue
          continue;
        }
        int srcIdx = findLibraryEntry(entries, libPath);
        if (srcIdx != -1) {
          copyEntries(idx, srcIdx, entries, projectClasspath, false);
          projectClasspath.add(entries[srcIdx]);
          idx = srcIdx+1;
        } else{
          copyEntries(idx, entries.length, entries, projectClasspath, false);
          idx = entries.length;
          projectClasspath.add(createLibraryEntry(libPath));
        }
      }
    }
    if (idx < entries.length) {
      copyEntries(idx, entries.length, entries, projectClasspath, false);
      idx = entries.length;
    }
    
    // Update raw classpath
    IClasspathEntry[] rawClasspath = 
      (IClasspathEntry[]) projectClasspath.toArray(new IClasspathEntry[projectClasspath.size()]);
    IJavaProject javaProject = bundleProject.getJavaProject();
    IPath outPath = javaProject.getOutputLocation();
    
    javaProject.setRawClasspath(
        rawClasspath,
        outPath,
        null);
  }
  
  private Integer[] findSourceEntries(IClasspathEntry[] entries) {
    ArrayList l = new ArrayList();
    if (entries == null) {
      return (Integer[]) l.toArray(new Integer[l.size()]);
    }
    
    for (int i=0; i<entries.length; i++) {
      if(entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        l.add(new Integer(i));
      }
    }
    
    return (Integer[]) l.toArray(new Integer[l.size()]);
  }
  
  private int findLibraryEntry(IClasspathEntry[] entries, IPath path) {
    if (entries == null || path == null) {
      return -1;
    }
    
    for (int i=0; i<entries.length; i++) {
      if(entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        if (entries[i].getPath().equals(path)) {
          return i;
        }
      }
    }
    
    return -1;
  }
  
  private void copyEntries(int fromIdx, int toIdx, IClasspathEntry[] src, List dst, boolean includeSrc) {
    if (src == null || dst == null) return;
    if (fromIdx < 0 || toIdx < 0 || toIdx > src.length || fromIdx >= toIdx) return;
    
    for (int i=fromIdx; i<toIdx; i++) {
      IClasspathEntry entry = src[i];
      if (!includeSrc && entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        // Skip source entries
        continue;
      } else if(entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        IPath defaultPath = entry.getPath().removeFirstSegments(1);
        IResource lib = project.findMember(defaultPath);
        if (lib == null) {
          // Just copy external libraries or libraries from other projects
          dst.add(entry);
        }
      } else {
        dst.add(entry);
      }
    }
  }
  
  private IClasspathEntry createLibraryEntry(IPath path) {
    IClasspathEntry libEntry = JavaCore.newLibraryEntry(
        path,
        null,
        null,
        true);
    
    return libEntry;
    
  }
  
  private IClasspathEntry createDefaultSourceEntry() {
    Path projectFolder = new Path("/"+project.getName());
    IPath sourcePath = projectFolder.append("src");
    IPath[] inclusionPatterns = new IPath [] {
        new Path("**/*.java")
    };
    // Exclude subversion and CVS directories
    IPath[] exclusionPatterns = new IPath [] {
        new Path("**/.svn/**"),
        new Path("**/CVS/**")
    };
    
    IClasspathEntry sourceEntry = JavaCore.newSourceEntry(
        sourcePath,
        inclusionPatterns,
        exclusionPatterns,
        null);
    
    return sourceEntry;
  }
}
