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

package org.knopflerfish.eclipse.core.project;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleBuilder extends IncrementalProjectBuilder {
  
  /* (non-Javadoc)
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {

    // Get output directory
    IProject project = getProject();
    IJavaProject javaProject = JavaCore.create(project);
    final BundleProject bundleProject = new BundleProject(javaProject);
    BundlePackDescription bundlePackDescription = bundleProject.getBundlePackDescription();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IFolder folder = root.getFolder(javaProject.getOutputLocation());
    File outDir = new File(folder.getLocation().toString());

    switch(kind) {
    case AUTO_BUILD:
    case FULL_BUILD:
    case INCREMENTAL_BUILD:
      // Check if any errors
      try {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(new IWorkspaceRunnable() {
          public void run(IProgressMonitor monitor) throws CoreException {
            bundleProject.checkManifest();
          }
        }, null, IWorkspace.AVOID_UPDATE, null);
      } catch (Throwable t) {}
      
      // Build bundle JAR
      File jarFile = new File(outDir, bundleProject.getFileName());
      try {
        bundlePackDescription.export(bundleProject, jarFile.getAbsolutePath());
        // Refresh the resource hierarchy
        IFile file = folder.getFile(bundleProject.getFileName());
        file.refreshLocal(1, null);
      } catch (Throwable t) {
        OsgiPlugin.throwCoreException("Failed to build JAR file for project "+getProject().getName(), t);
      }
      break;
    case CLEAN_BUILD:
      // Remove JAR files in output directory
      File[] children = outDir.listFiles();
      if (children != null) {
        for (int i=0; i<children.length; i++) {
          if (children[i].isFile() && children[i].getName().toLowerCase().endsWith(".jar")) {
            children[i].delete();
          }
        }
      }
      break;
    }
    return null;
  }
}
