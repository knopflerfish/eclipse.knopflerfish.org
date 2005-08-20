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
 * @author Anders Rim�n, Gatespace Telematics
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
      //workspace.run(this, project, IWorkspace.AVOID_UPDATE, null);
      workspace.run(this, null, IWorkspace.AVOID_UPDATE, null);
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
    String[] bundleClasspath = bundleProject.getBundleManifest().getBundleClassPath();
    if (bundleClasspath.length == 0) {
      bundleClasspath = new String[] {"."};
    }
    Map contents = packDescription.getContentsMap(false);
    ArrayList projectClasspath = new ArrayList();
   
    int idx = 0;
    for (int i=0; i<bundleClasspath.length; i++) {
      String path = bundleClasspath[i];
      System.err.println("BundleClasspath "+i+", path="+path);
      if (".".equals(path)) {
        System.err.println("SOURCE");
        // Find source entry
        int srcIdx = findSourceEntry(idx, entries);
        if (srcIdx != -1) {
          copyEntries(idx, srcIdx, entries, projectClasspath, false);
          projectClasspath.add(entries[srcIdx]);
          idx = srcIdx+1;
        } else{
          projectClasspath.add(createDefaultSourceEntry());
          copyEntries(idx, entries.length, entries, projectClasspath, false);
          idx = entries.length;
        }
      } else {
        System.err.println("LIBRARY");
        // Find library entry
        IPath libPath = (IPath) contents.get(path);
        System.err.println("Lib path :"+libPath);
        if (libPath == null) {
          // Something wrong with contents file, nothing to do just continue
          continue;
        }
        int srcIdx = findLibraryEntry(idx, entries, libPath);
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
      copyEntries(idx, entries.length, entries, projectClasspath, bundleClasspath.length == 0);
      idx = entries.length;
    }
    
    // Update raw classpath
    System.err.println("Original Classpath ");
    for (int i=0; i<entries.length;i++) {
      System.err.println("Entry "+i+" :"+entries[i].toString());
    }
    IClasspathEntry[] rawClasspath = 
      (IClasspathEntry[]) projectClasspath.toArray(new IClasspathEntry[projectClasspath.size()]);
    System.err.println("Update Classpath with ");
    for (int i=0; i<rawClasspath.length;i++) {
      System.err.println("Entry "+i+" :"+rawClasspath[i].toString());
    }
    IJavaProject javaProject = bundleProject.getJavaProject();
    IPath outPath = javaProject.getOutputLocation();
    System.err.println("Output path :"+outPath.toString());
    
    javaProject.setRawClasspath(
        rawClasspath,
        outPath,
        null);
  }
  
  private int findSourceEntry(int startIdx, IClasspathEntry[] entries) {
    System.err.println("findSourceEntry");
    if (entries == null || startIdx < 0 || startIdx >= entries.length) {
      return -1;
    }
    
    for (int i=startIdx; i<entries.length; i++) {
      if(entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        System.err.println("Found source entry at idx "+i);
        return i;
      }
    }
    
    return -1;
  }

  private int findLibraryEntry(int startIdx, IClasspathEntry[] entries, IPath path) {
    System.err.println("findLibraryEntry");
    if (entries == null || startIdx < 0 || startIdx >= entries.length || path == null) {
      return -1;
    }
    
    for (int i=startIdx; i<entries.length; i++) {
      if(entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        if (entries[i].getPath().equals(path)) {
          System.err.println("Found library entry at idx "+i);
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
        null);
    
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
