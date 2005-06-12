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

package org.knopflerfish.eclipse.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author ar
 */
public class BundleProject implements IBundleProject {
  private static final String MANIFEST_FILE = "bundle.manifest";
  
  private IJavaProject project;
  private Manifest manifest;
  private String name;
  private String version;
  private String activator;
  private List importedPackages = new ArrayList();
  private List exportedPackages = new ArrayList();

  public BundleProject(String name) {
    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspace.getProject(name);
    IJavaProject javaProject = JavaCore.create(project);
    init(javaProject);
  }
  
  public BundleProject(IJavaProject project) {
    init(project);
  }
  
  private void init(IJavaProject project) {
    this.project = project;
    
    // Read manifest
    InputStream is = null;
    try {
      try {
        // Get manifest
        IFile manifestFile = project.getProject().getFile(MANIFEST_FILE);
        is = manifestFile.getContents();
        manifest = new Manifest(is); 
        
        Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
          name = attributes.getValue(IOsgiBundle.BUNDLE_NAME);
          version = attributes.getValue(IOsgiBundle.BUNDLE_VERSION);
          activator = attributes.getValue(IOsgiBundle.BUNDLE_ACTIVATOR);

          // Import-Packages
          String attr = attributes.getValue(IOsgiBundle.IMPORT_PACKAGE);
          importedPackages.clear();
          if (attr != null) {
            StringTokenizer st = new StringTokenizer(attr, ",");
            while(st.hasMoreTokens()) {
              try {
                importedPackages.add(new PackageDescription(st.nextToken()));
              } catch(Exception e) {
                e.printStackTrace();
              }
            }
          }

          // Export-Packages
          attr = attributes.getValue(IOsgiBundle.EXPORT_PACKAGE);
          exportedPackages.clear();
          if (attr != null) {
            StringTokenizer st = new StringTokenizer(attr, ",");
            while(st.hasMoreTokens()) {
              try {
                exportedPackages.add(new PackageDescription(st.nextToken()));
              } catch(Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      } finally {
        if (is != null) {
          is.close();
        }
      }
    } catch (Exception e) {
      // Failed to read manifest
    }
  }
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getJavaProject()
   */
  public IJavaProject getJavaProject() {
    return project;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getManifest()
   */
  public Manifest getManifest() {
    return manifest;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getVersion()
   */
  public String getVersion() {
    return version;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getActivator()
   */
  public String getActivator() {
    return activator;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getImportedPackages()
   */
  public PackageDescription[] getImportedPackages() {
    return (PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]);
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getExportedPackages()
   */
  public PackageDescription[] getExportedPackages() {
    return (PackageDescription[]) exportedPackages.toArray(new PackageDescription[exportedPackages.size()]);
  }
}
