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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.Osgi;
import org.osgi.framework.Version;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @author Mats-Ola Persson, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ProjectUtil {

  /**
   * Creates a bundle filename based on project name and
   * version.
   * 
   * @param project bundle project.
   * 
   * @return filename
   */
  public static String createFileName(IBundleProject project) {
    StringBuffer buf = new StringBuffer(project.getJavaProject().getProject().getName());
    Version version = project.getBundleManifest().getVersion();
    if (version != null) {
      buf.append("-");
      buf.append(version.toString());
    }
    buf.append(".jar");
    
    return buf.toString();
    
  }

  /**
   * Returns all IFile resources with the specified extenstion, 
   * which can be found in the resource passed.
   * 
   * @param resource resource to be searched.
   * @param extension extension to be searched for.
   * @param exclude path to exclude.
   * 
   * @return array of IFile resources
   */
  public static IFile[] getFiles(IResource resource, String extension, IPath exclude) {
    if (resource instanceof IFile && extension.equalsIgnoreCase(resource.getFileExtension())) {
      return new IFile[] {(IFile) resource};
    } else if (resource instanceof IContainer) {
      IContainer container = (IContainer) resource;
      ArrayList files = new ArrayList();
      try {
        IResource[] resources = container.members();
        if (resources != null) {
          for(int i=0; i<resources.length; i++) {
            if (!resources[i].getFullPath().equals(exclude)) {
              files.addAll(Arrays.asList(getFiles(resources[i], extension, exclude)));
            }
          }
        }
      } catch (CoreException e) {
      }
      return (IFile[]) files.toArray(new IFile[files.size()]);
    } else {
      return new IFile[0];
    }
  }
  
  
  /**
   * Returns all the bundle projects of this workspace.
   * 
   * @return array of IBundleProject
   */

  public static IBundleProject[] getBundleProjects() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    ArrayList retval = new ArrayList();
    IProject[] projects = workspace.getRoot().getProjects();
    for (int i = 0; i < projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          retval.add(new BundleProject(JavaCore.create(projects[i])));
        }
      } catch (CoreException e) {
        // ignore. this means that the project has been removed.
      }
    }
    
    return (IBundleProject[]) retval.toArray(new IBundleProject[retval.size()]);
  }
}
