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

package org.knopflerfish.eclipse.core.pkg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.IBundleProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageUtil {

  public static IOsgiBundle[] findExportingBundles(PackageDescription pkg) {
    if (pkg == null) return null;
    ArrayList exportingBundles = new ArrayList(); 

    // Knopflerfish root
    List installList = Osgi.getOsgiInstalls();
    
    for(int i=0; installList != null && i<installList.size(); i++) {
      IOsgiInstall osgiInstall = (IOsgiInstall) installList.get(i);
      IOsgiBundle[] bundles = osgiInstall.getBundles();
      for(int j=0; bundles != null && j<bundles.length; j++) {
        IOsgiBundle b = bundles[j]; 
        if (b.hasExportedPackage(pkg)) {
          exportingBundles.add(b);
        }
      }
    }
    return (IOsgiBundle[]) exportingBundles.toArray(new IOsgiBundle[exportingBundles.size()]);
  }
  
  public static IBundleProject[] findExportingProjects(PackageDescription pkg) {
    if (pkg == null) return null;
    ArrayList exportingProjects = new ArrayList(); 
    
    // Workspace root
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    // Projects
    IProject [] projects = root.getProjects();
    for(int i=0; projects != null && i<projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject project = JavaCore.create(projects[i]);
          IBundleProject b = new BundleProject(project);
          
          if (b.hasExportedPackage(pkg)) {
            exportingProjects.add(b);
          }
        }
      } catch (CoreException e) {
        // Failed to check project nature.
      }
    }

    return (IBundleProject[]) exportingProjects.toArray(new IBundleProject[exportingProjects.size()]);
  }
  
  
}
