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

package org.knopflerfish.eclipse.core.project.classpath;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.manifest.SymbolicName;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;
import org.knopflerfish.eclipse.core.project.BundleProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleContainer implements IClasspathContainer {
  
  public static final String CONTAINER_PATH = "org.knopflerfish.eclipse.core.BUNDLE_CONTAINER";

  private final IPath path;
  private final SymbolicName symbolicName;
  private String name;
  //private PackageDescription[] packages;
  private final IJavaProject project;
  
  public BundleContainer(IPath path, SymbolicName symbolicName, IJavaProject project) {
    this.path = path;
    this.symbolicName = symbolicName;
    this.project = project;
  }
  
  /****************************************************************************
   * org.eclipse.jdt.core.IClasspathContainer methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
   */
  public IClasspathEntry[] getClasspathEntries() {
    
    PackageDescription[] packages = null;
    IClasspathEntry ice = ClasspathUtil.findClasspathEntry(project, path);
    packages = ClasspathUtil.getPackages(ice);
    
    // Check if workspace contains bundle
    try {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject [] projects = root.getProjects();
      for(int i=0; projects != null && i<projects.length; i++) {
        if (projects[i].isOpen() && projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject javaProject = JavaCore.create(projects[i]);
          IBundleProject bundleProject = new BundleProject(javaProject);
          BundleManifest manifest = bundleProject.getBundleManifest();
          if (symbolicName.equals(bundleProject.getId().getSymbolicName())) {
            // Check that project exports the needed packages
            boolean hasPackages = true;
            if (packages != null) {
              for (int j=0; j<packages.length; j++) {
                if (!manifest.hasExportedPackage(packages[j])) {
                  hasPackages = false;
                }
              }
            }
            if (!hasPackages) continue;
            
            // Found project exporting the needed packages, create
            // classpath entry
            name = manifest.getName();
            IClasspathEntry entry = 
              JavaCore.newProjectEntry(javaProject.getPath());
            return new IClasspathEntry[] {entry};
          }
        }
      }
    } catch (CoreException e) {
      OsgiPlugin.log(e.getStatus());
    }

    // Check if repository contains bundle
    RepositoryPreference[] repositoryPref = OsgiPreferences.getBundleRepositories();
    for (int i=0; i<repositoryPref.length; i++) {
      if (!repositoryPref[i].isActive()) continue;
      
      IBundleRepositoryType repositoryType = Osgi.getBundleRepositoryType(repositoryPref[i].getType());
      if (repositoryType == null) continue;
      
      IBundleRepository repository = repositoryType.createRepository(repositoryPref[i].getConfig());
      if (repository == null) continue;

      IOsgiLibrary[] libraries = repository.getBundleLibraries(symbolicName, packages);
      if (libraries == null || libraries.length == 0) continue;

      ArrayList entries = new ArrayList();
      boolean libsOk = true;
      for(int j=0; j<libraries.length; j++) {
        if (libraries[j] instanceof IOsgiBundle) {
          name = ((IOsgiBundle) libraries[j]).getBundleManifest().getName();
        }
        Path path = new Path(libraries[j].getPath());
        if (!path.toFile().exists()) {
          libsOk = false;
          break;
        }
        Path src = null;
        if (libraries[j].getSource() != null) {
          try {
            src = new Path(libraries[j].getSource());
            if (!src.toFile().exists()) {
              src = null;
            }
          } catch (Exception ignore) {
          }
        }
        if (libsOk) {
          entries.add(JavaCore.newLibraryEntry(path, src, null, false));
        }
      }
      return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
    }

    return new IClasspathEntry[] {}; 
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
   */
  public String getDescription() {
    StringBuffer buf = new StringBuffer("Bundle [");
    if (name != null) {
      buf.append(name);
    } else {
      buf.append(symbolicName.toString());
    }
    buf.append("]");
    return buf.toString();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
   */
  public int getKind() {
    return IClasspathContainer.K_SYSTEM;
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
   */
  public IPath getPath() {
    return path;
  }
}
