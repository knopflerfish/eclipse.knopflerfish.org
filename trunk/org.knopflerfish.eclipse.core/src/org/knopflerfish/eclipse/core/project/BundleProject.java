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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.knopflerfish.eclipse.core.PackageDescription;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleProject implements IBundleProject {
  public static final String CLASSPATH_FILE = ".classpath";
  public static final String MANIFEST_FILE  = "bundle.manifest";
  public static final String BUNDLE_PACK_FILE = ".bundle-pack";
  
  private final IJavaProject project;
  private BundleManifest manifest;
  private BundlePackDescription bundlePackDescription;

  public BundleProject(String name) throws CoreException {
    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspace.getProject(name);
    IJavaProject javaProject = JavaCore.create(project);
    this.project = javaProject;
    loadManifest();
    bundlePackDescription = createBundlePackDescription();
  }
  
  public BundleProject(IJavaProject project) throws CoreException {
    this.project = project;
    loadManifest();
    bundlePackDescription = createBundlePackDescription();
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
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundlePackDescription()
   */
  public BundlePackDescription getBundlePackDescription() {
    return bundlePackDescription;
  }
  public IFile getBundlePackDescriptionFile() {
    return project.getProject().getFile(BUNDLE_PACK_FILE);
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getManifest()
   */
  public BundleManifest getBundleManifest() {
    return manifest;
  }
  public IFile getBundleManifestFile() {
    return project.getProject().getFile(MANIFEST_FILE);
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

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundleActivators()
   */
  public IType[] getBundleActivators() {
    ArrayList activators = new ArrayList();

    try{
      IType activatorType = project.findType("org.osgi.framework.BundleActivator");
      ITypeHierarchy hierarchy = activatorType.newTypeHierarchy(project, null);
      IType[] implClasses = hierarchy.getImplementingClasses(activatorType);
      if (implClasses != null) {
        for(int i=0;i<implClasses.length;i++) {
          IPackageFragmentRoot root = (IPackageFragmentRoot) implClasses[i].getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
          if (root != null) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
              IJavaProject ancestor = (IJavaProject) root.getAncestor(IJavaElement.JAVA_PROJECT);
              if (project.equals(ancestor)) {
                activators.add(implClasses[i]);
              }
            }
          }
        }
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    
    return (IType[]) activators.toArray(new IType[activators.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getLocalJars()
   */
  public IFile[] getJars() {
    IFile[] files = null;
    try {
      files = getFiles(project.getProject(), "jar", project.getOutputLocation());
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    return files;
  }

  /****************************************************************************
   * Check methods
   ***************************************************************************/
  public boolean checkManifestBundleActivator() {
    // Check that bundle activator class exists
    String activator = manifest.getActivator();
    if (activator != null) {
      IType[] types = getBundleActivators();
      for(int i=0; i<types.length;i++) {
        if (activator.trim().equals(types[i].getFullyQualifiedName())) {
          return true;
        }
      }
      //Could not find activator
      return false;
    } else {
      return true;
    }
  }
  
  public boolean checkManifestBundleClassPath() { 
    // Check that all libraries defined in bundle classpath
    // are packed in the bundle

    ArrayList errors = new ArrayList();
    String[] classPaths = manifest.getBundleClassPath();
    Map map = bundlePackDescription.getContentsMap(false);
    for (int i=0; i<classPaths.length; i++) {
      String path = classPaths[i];
      if (path.startsWith("/")) path = path.substring(1);
      
      if (!map.containsKey(path)) {
        errors.add(path);
      }
    }
    
    return  errors.size() == 0;
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  private IFile[] getFiles(IResource resource, String extension, IPath exclude) {
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
  
  public BundlePackDescription createBundlePackDescription() throws CoreException {

    // Try to load it, if it does not exist create it
    BundlePackDescription packDescription = loadBundlePackDescription();
    try {
      if (packDescription == null) {
        // Create jar description
        packDescription = new BundlePackDescription();
        // Add output folder as resource
        BundleResource resource = new BundleResource(
            BundleResource.TYPE_CLASSES,
            project.getOutputLocation(), 
            "", 
            Pattern.compile(".*\\.class"));
        packDescription.addResource(resource);
        
        // Save description
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try { 
          baos = new ByteArrayOutputStream();
          packDescription.save(baos);
          bais = new ByteArrayInputStream(baos.toByteArray());
        
          IFile bundlePackFile = project.getProject().getFile(BUNDLE_PACK_FILE);
          if (!bundlePackFile.exists()) {
            bundlePackFile.create(bais, false, null);
          }
        } finally {
          if (baos != null) baos.close();
          if (bais != null) bais.close();
          
        }
      }
    } catch (Exception e) {
      OsgiPlugin.throwCoreException("Failed to create bundle jar description", e);
    }
    return packDescription;
  }
  
  public BundlePackDescription loadBundlePackDescription() throws CoreException {
    // Read bundle jar
    InputStream is = null;
    BundlePackDescription jar  = null;
    try {
      try {
        IFile bundlePackFile = project.getProject().getFile(BUNDLE_PACK_FILE);
        if (bundlePackFile.exists()) {
          is = bundlePackFile.getContents();
          jar = new BundlePackDescription(project.getProject(), is);
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

  public void saveBundlePackDescription(BundlePackDescription packDescription) throws CoreException {
    if (packDescription == null) return;
    try {
      // Save description
      ByteArrayOutputStream baos = null;
      ByteArrayInputStream bais = null;
      try { 
        baos = new ByteArrayOutputStream();
        packDescription.save(baos);
        bais = new ByteArrayInputStream(baos.toByteArray());
      
        IFile bundlePackFile = project.getProject().getFile(BUNDLE_PACK_FILE);
        if (!bundlePackFile.exists()) {
          bundlePackFile.create(bais, IResource.FORCE, null);
        } else {
          //bundlePackFile.setContents(bais, IResource.FORCE | IResource.KEEP_HISTORY, null);
          bundlePackFile.setContents(bais, IResource.KEEP_HISTORY, null);
        }
      } finally {
        if (baos != null) baos.close();
        if (bais != null) bais.close();
        
      }
    } catch (Exception e) {
      OsgiPlugin.throwCoreException("Failed to create bundle jar description", e);
    }
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

  public void saveManifest(BundleManifest manifest) {
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
        if (!file.exists()) {
          file.create(bais, IResource.FORCE, null);
        } else {
          file.setContents(bais, IResource.KEEP_HISTORY, null);
        }
      } finally {
        baos.close();
        bais.close();
      }
    } catch (Exception e) {
    }
  }

  /*
  public void updateClasspath() {
    Map map = bundlePackDescription.getContentsMap();
    HashMap resources = new HashMap();
    List pathList = new ArrayList(Arrays.asList(manifest.getBundleClassPath()));
    for (Iterator i = pathList.iterator(); i.hasNext(); ) {
      String path = ((String) i.next()).trim();
      if (path.startsWith("/")) path = path.substring(1);
      
      if (map.containsKey(path)) {
        BundleResource resource = new BundleResource((IPath) map.get(path), path, null);
        resources.put(resource.getSource().makeAbsolute().toString(), resource);
      }
    }

    ArrayList entries = new ArrayList(Arrays.asList(project.getRawClasspath()));
    boolean changed = false;
    for(Iterator i=entries.iterator();i.hasNext();) {
      IClasspathEntry entry = (IClasspathEntry) i.next();
      if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        String path = entry.getPath().makeAbsolute().toString();
        if (resources.containsKey(path)) {
          i.remove();
          changed = true;
        }
      }
    }
    if (changed) {
      project.setRawClasspath(
          (IClasspathEntry []) entries.toArray(new IClasspathEntry[entries.size()]),
          null);
    }
    
  }
  */
}

