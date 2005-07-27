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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.PackageDescription;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;

/**
 * @author Anders Rimén
 */
public class BundleProject implements IBundleProject {
  private static final String MANIFEST_FILE  = "bundle.manifest";
  private static final String BUNDLEJAR_FILE = "bundle.jar";
  
  private final IJavaProject project;
  private BundleManifest manifest;
  private BundleJar bundleJar;

  public BundleProject(String name) throws CoreException {
    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspace.getProject(name);
    IJavaProject javaProject = JavaCore.create(project);
    this.project = javaProject;
    loadManifest();
    bundleJar = createBundleJarDescription();
  }
  
  public BundleProject(IJavaProject project) throws CoreException {
    this.project = project;
    loadManifest();
    bundleJar = createBundleJarDescription();
  }
  
  public static BundleProject create(String name, IPath projectLocation, IPath srcFolder, IPath outFolder) throws CoreException {
    // Create project
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(name);
    if (project.exists()) {
      OsgiPlugin.throwCoreException("Project \"" + name + "\" already exists.", null);
    }
    
    // Create project description
    IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(name);
    
    // Set project location
    projectDescription.setLocation(projectLocation);

    // Create project
    project.create(projectDescription, null);
   
    // Open project
    project.open(null);

    // Set project natures
    projectDescription = project.getDescription();
    projectDescription.setNatureIds(new String[] {Osgi.NATURE_ID, JavaCore.NATURE_ID});
    project.setDescription(projectDescription, null);
    
    // Create source folder
    project.getFolder(srcFolder).create(true, true, null);

    // Set default classpath
    IJavaProject javaProject = JavaCore.create(project);

    // Set output location 
    Path projectFolder = new Path("/"+name);
    javaProject.setOutputLocation(projectFolder.append(outFolder), null);
    
    return new BundleProject(javaProject);
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleProject methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getJavaProject()
   */
  public IJavaProject getJavaProject() {
    return project;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundleJarDescription()
   */
  public BundleJar getBundleJarDescription() {
    return bundleJar;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getManifest()
   */
  public BundleManifest getBundleManifest() {
    return manifest;
  }
  
  public void setBundleManifest(BundleManifest manifest) {
    this.manifest = manifest;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#hasExportedPackage(org.knopflerfish.eclipse.core.PackageDescription)
   */
  public boolean hasExportedPackage(PackageDescription pkg) {
    PackageDescription [] exportedPackages = manifest.getExportedPackages();
    for (int i=0; i<exportedPackages.length; i++) {
      if (exportedPackages[i].isCompatible(pkg)) return true;
    }
    return false;
  }

  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  public BundleJar createBundleJarDescription() throws CoreException {

    // Try to load it, if it does not exist create it
    BundleJar jar = loadBundleJarDescription();
    try {
      if (jar == null) {
        // Create jar description
        jar = new BundleJar();
        // Add output folder as resource
        BundleJarResource resource = new BundleJarResource(
            project.getOutputLocation(), 
            "", 
            Pattern.compile(".*\\.class"));
        jar.addResource(resource);
        
        // Save description
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try { 
          baos = new ByteArrayOutputStream();
          jar.save(baos);
          bais = new ByteArrayInputStream(baos.toByteArray());
        
          IFile bundleJarFile = project.getProject().getFile(BUNDLEJAR_FILE);
          if (!bundleJarFile.exists()) {
            bundleJarFile.create(bais, false, null);
          }
        } finally {
          if (baos != null) baos.close();
          if (bais != null) bais.close();
          
        }
      }
    } catch (Exception e) {
      OsgiPlugin.throwCoreException("Failed to create bundle jar description", e);
    }
    return jar;
  }
  
  public BundleJar loadBundleJarDescription() throws CoreException {
    // Read bundle jar
    InputStream is = null;
    BundleJar jar  = null;
    try {
      try {
        IFile bundleJarFile = project.getProject().getFile(BUNDLEJAR_FILE);
        if (bundleJarFile.exists()) {
          is = bundleJarFile.getContents();
          jar = new BundleJar(project.getProject(), is);
        }
      } finally {
        if (is != null) {
          is.close();
        }
      }
    } catch (Exception e) {
      OsgiPlugin.throwCoreException("Failed to load bundle jar description", e);
    }
    return jar;
  }
  
  public void loadManifest() {
    // Read manifest
    InputStream is = null;
    try {
      try {
        // Get manifest
        IFile manifestFile = project.getProject().getFile(MANIFEST_FILE);
        if (manifestFile.exists()) {
          is = manifestFile.getContents();
          manifest = new BundleManifest(is);
        } else {
          manifest = new BundleManifest();
        }
      } finally {
        if (is != null) {
          is.close();
        }
      }
    } catch (Exception e) {}
  }

  public void saveManifest() {
    // Write manifest to file
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = null;
    IFile file = null;
    try {
      try {
        manifest.write(baos);
        baos.flush();
        bais = new ByteArrayInputStream(baos.toByteArray());
        file = project.getProject().getFile(MANIFEST_FILE);
        file.create(bais, true, null);
      } finally {
        baos.close();
        bais.close();
      }
    } catch (Exception e) {
    }
  }

}

