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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.pkg.IPackage;
import org.knopflerfish.eclipse.core.preferences.ExecutionEnvironment;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.classpath.ClasspathUtil;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleProject implements IBundleProject {
  // Markers
  public static final String MARKER_MANIFEST = "manifest";
  public static final String MARKER_BUNDLE_ACTIVATOR = "org.knopflerfish.eclipse.core.activator";
  public static final String MARKER_BUNDLE_NAME = "org.knopflerfish.eclipse.core.name";
  public static final String MARKER_BUNDLE_UPDATELOCATION = "org.knopflerfish.eclipse.core.updateLocation";
  public static final String MARKER_BUNDLE_DOCURL = "org.knopflerfish.eclipse.core.docUrl";
  public static final String MARKER_BUNDLE_EXEC_ENV = "org.knopflerfish.eclipse.core.execEnv";
  public static final String MARKER_BUNDLE_CLASSPATH = "org.knopflerfish.eclipse.core.classpath";
  public static final String MARKER_EXPORT_PACKAGES  = "org.knopflerfish.eclipse.core.packageExports";
  public static final String MARKER_DYNAMIC_IMPORT_PACKAGES  = "org.knopflerfish.eclipse.core.packageDynamicImports";
  
  public static final String CLASSPATH_FILE = ".classpath";
  //public static final String MANIFEST_FILE  = "MANIFEST.MF";
  public static final String MANIFEST_FILE  = "bundle.manifest";
  public static final String BUNDLE_PACK_FILE = ".bundle-pack";
  
  private final IJavaProject javaProject;
  //private BundleManifest manifest;
  
  public BundleProject(String name) throws CoreException {
    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspace.getProject(name);
    IJavaProject javaProject = JavaCore.create(project);
    this.javaProject = javaProject;
  }
  
  public BundleProject(IJavaProject project) throws CoreException {
    this.javaProject = project;
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleProject methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getJavaProject()
   */
  public IJavaProject getJavaProject() {
    return javaProject;
  }
  
  /****************************************************************************
   * Bundle Pack methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundlePackDescription()
   */
  public BundlePackDescription getBundlePackDescription() {
    
    BundlePackDescription packDescription = null;
    try {
      // Try to load it, if it does not exist create it
      packDescription = loadBundlePackDescription();
      if (packDescription == null) {
        // Create jar description
        packDescription = new BundlePackDescription(javaProject.getProject());
        // Add output folder as resource
        BundleResource resource = new BundleResource(
            BundleResource.TYPE_CLASSES,
            javaProject.getOutputLocation(), 
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
          
          IFile bundlePackFile = javaProject.getProject().getFile(BUNDLE_PACK_FILE);
          if (!bundlePackFile.exists()) {
            bundlePackFile.create(bais, false, null);
          }
        } finally {
          if (baos != null) baos.close();
          if (bais != null) bais.close();
          
        }
      }
    } catch (Exception e) {
      //OsgiPlugin.throwCoreException("Failed to create bundle jar description", e);
    }
    return packDescription;
  }
  
  public IFile getBundlePackDescriptionFile() {
    return javaProject.getProject().getFile(BUNDLE_PACK_FILE);
  }
  
  /****************************************************************************
   * Bundle Manifest methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getManifest()
   */
  public BundleManifest getBundleManifest() {
    BundleManifest manifest = null;
    
    // Read manifest
    InputStream is = null;
    try {
      try {
        // Get manifest
        IFile manifestFile = javaProject.getProject().getFile(MANIFEST_FILE);
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
    
    return manifest;
  }
  
  public void setBundleManifest(BundleManifest manifest) {
    // Write manifest to file
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = null;
    IFile file = null;
    try {
      try {
        manifest.write(baos);
        baos.flush();
        bais = new ByteArrayInputStream(baos.toByteArray());
        file = javaProject.getProject().getFile(MANIFEST_FILE);
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
  
  public IFile getBundleManifestFile() {
    return javaProject.getProject().getFile(MANIFEST_FILE);
  }
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#hasExportedPackage(org.knopflerfish.eclipse.core.PackageDescription)
   */
  public boolean hasExportedPackage(PackageDescription pkg) {
    PackageDescription [] exportedPackages = getBundleManifest().getExportedPackages();
    for (int i=0; i<exportedPackages.length; i++) {
      if (exportedPackages[i].isCompatible(pkg)) return true;
    }
    return false;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getExportablePackages()
   */
  public PackageDescription[] getExportablePackages() {
    ArrayList packages = new ArrayList();
    IProject project = javaProject.getProject();
    try {
      IPackageFragmentRoot[] fragmentRoot = javaProject.getAllPackageFragmentRoots();
      for (int i=0; i<fragmentRoot.length; i++) {
        try {
          if (fragmentRoot[i].isExternal()) continue;
          if (fragmentRoot[i].getKind() != IPackageFragmentRoot.K_SOURCE && 
              !fragmentRoot[i].isArchive()) continue;
          
          IResource resource = fragmentRoot[i].getCorrespondingResource();
          IProject fragmentProject = project;
          if (resource != null) {
            fragmentProject = resource.getProject();
          }
          if (project.equals(fragmentProject)) {
            
            IJavaElement[] elements = fragmentRoot[i].getChildren();
            for (int j=0; j<elements.length; j++) {
              if (elements[j].getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                // Check if package fragment has any classes to export
                IPackageFragment fragment = (IPackageFragment) elements[j];
                if (fragment.containsJavaResources()) {
                  packages.add(new PackageDescription(fragment.getElementName(), null));
                }
              }
            }
          }
        } catch (JavaModelException jme) {
          // Skip this entry
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundleActivators()
   */
  public IType[] getBundleActivators() {
    ArrayList activators = new ArrayList();
    
    try{
      IType activatorType = javaProject.findType("org.osgi.framework.BundleActivator");
      ITypeHierarchy hierarchy = activatorType.newTypeHierarchy(javaProject, null);
      IType[] implClasses = hierarchy.getImplementingClasses(activatorType);
      if (implClasses != null) {
        for(int i=0;i<implClasses.length;i++) {
          IPackageFragmentRoot root = (IPackageFragmentRoot) implClasses[i].getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
          if (root != null) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
              IJavaProject ancestor = (IJavaProject) root.getAncestor(IJavaElement.JAVA_PROJECT);
              if (javaProject.equals(ancestor)) {
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
      files = getFiles(javaProject.getProject(), "jar", javaProject.getOutputLocation());
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    return files;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getFileName()
   */
  public String getFileName() {
    StringBuffer buf = new StringBuffer(javaProject.getProject().getName());
    String version = getBundleManifest().getVersion();
    if (version != null && version.trim().length() > 0) {
      buf.append("-");
      buf.append(version.trim());
    }
    buf.append(".jar");
    
    return buf.toString();
  }
  
  /****************************************************************************
   * Classpath package methods
   ***************************************************************************/
  public void importPackage(IPackage pkg,boolean updateManifest) {
    if (pkg == null) return;
    
    if (pkg.getType() == IPackage.FRAMEWORK) {
      importFrameworkPackage(pkg.getPackageDescription(), updateManifest);
    } else if (pkg.getType() == IPackage.BUNDLE) {
      // TODO
    } else if (pkg.getType() == IPackage.PROJECT) {
      // TODO
    }
  }
  
  public void importFrameworkPackage(PackageDescription pd, boolean updateManifest){
    if (updateManifest) {
      // Add package to manifest
      BundleManifest manifest = getBundleManifest();
      ArrayList importedPackages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
      boolean changed = false;
      if (!importedPackages.contains(pd)) {
        importedPackages.add(pd);
        changed = true;
      }
      
      if(changed) {
        manifest.setImportedPackages((PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]));
        setBundleManifest(manifest);
      }
    }
    
    // Update access rules for framework container
    try {
      ArrayList entries = new ArrayList(Arrays.asList(javaProject.getRawClasspath()));
      int idx = -1;
      for(int i=0;i<entries.size();i++) {
        // Find framework container
        IClasspathEntry entry = (IClasspathEntry) entries.get(i);
        if (ClasspathUtil.FRAMEWORK.equals(ClasspathUtil.getClasspathType(entry))) {
          idx = i;
          break;
        }
      }
      
      // Check if access rule aleady exist
      String pattern = pd.getPackageName().replace('.', '/');
      if (!pattern.endsWith("*") && !pattern.endsWith("/")) {
        pattern = pattern + "/";
      }
      IAccessRule rule = JavaCore.newAccessRule(
          new Path(pattern), IAccessRule.K_ACCESSIBLE);
      if (idx != -1) {
        boolean exist = false;
        IClasspathEntry oldEntry = (IClasspathEntry) entries.get(idx);
        
        ArrayList rules = new ArrayList(Arrays.asList(oldEntry.getAccessRules()));
        // Check if rule exists
        for(int i=0;i<rules.size();i++) {
          if (rule.equals((IAccessRule) rules.get(i))) {
            exist = true;
            break;
          }
        }
        
        // Update classpath
        if (!exist) {
          rules.add(0, rule);
          IClasspathEntry newEntry = JavaCore.newContainerEntry(
              ((IClasspathEntry) entries.get(idx)).getPath(),
              (IAccessRule []) rules.toArray(new IAccessRule[rules.size()]),
              new IClasspathAttribute[] {JavaCore.newClasspathAttribute(ClasspathUtil.TYPE, ClasspathUtil.FRAMEWORK)},
              false
          );
          entries.remove(idx);
          entries.add(idx, newEntry);
          
          javaProject.setRawClasspath(
              (IClasspathEntry []) entries.toArray(new IClasspathEntry[entries.size()]),
              null);
        }
      }
    } catch (JavaModelException e) {
    }
  }
  
  public void removeFrameworkPackage(PackageDescription packageDescription){
    // TODO: TBI
    
  }
  
  public void importBundlePackage(PackageDescription packageDescription){
    // TODO: TBI
    
    // Add package to manifest
    
    // Add bundle container if container does not exist
    
    // Update access rules for framework container
    
  }
  
  
  public void removeBundlePackage(PackageDescription packageDescription){
    // TODO: TBI
    
  }
  
  /****************************************************************************
   * Check methods
   ***************************************************************************/
  
  public void checkManifest() throws CoreException {
    IFile manifestFile = getBundleManifestFile();
    
    InputStream is = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      try {
        is = manifestFile.getContents();
        
        byte [] buf = new byte[256];
        int numRead = 0;
        while( (numRead = is.read(buf)) != -1) {
          baos.write(buf, 0, numRead);
        }
      } finally {
        if (is != null) is.close();
        baos.flush();
        baos.close();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    StringBuffer manifestContents = new StringBuffer(baos.toString());
    BundleManifest manifest = getBundleManifest();
    
    // Check Bundle activator
    updateMarker(MARKER_BUNDLE_ACTIVATOR, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_ACTIVATOR), 
        checkManifestBundleActivator(manifest), 
        IMarker.SEVERITY_ERROR,
        manifestFile);
    
    // Check Bundle name
    updateMarker(MARKER_BUNDLE_NAME, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_NAME), 
        checkManifestBundleName(manifest), 
        IMarker.SEVERITY_WARNING,
        manifestFile);
    
    // Check Bundle update location
    updateMarker(MARKER_BUNDLE_UPDATELOCATION, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_UPDATELOCATION), 
        checkManifestUpdateLocation(manifest), 
        IMarker.SEVERITY_WARNING,
        manifestFile);
    
    // Check Bundle doc url
    updateMarker(MARKER_BUNDLE_DOCURL, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_DOCURL), 
        checkManifestDocUrl(manifest), 
        IMarker.SEVERITY_WARNING,
        manifestFile);
    
    // Check Execution Environment
    updateMarker(MARKER_BUNDLE_EXEC_ENV, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_EXEC_ENV), 
        checkManifestExecutionEnvironment(manifest), 
        IMarker.SEVERITY_WARNING,
        manifestFile);
    
    // Check Bundle classpath
    updateMarker(MARKER_BUNDLE_CLASSPATH, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_CLASSPATH), 
        checkManifestBundleClassPath(manifest), 
        IMarker.SEVERITY_ERROR,
        manifestFile);
    
    // Check Exports
    updateMarker(MARKER_EXPORT_PACKAGES, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.EXPORT_PACKAGE), 
        checkPackageExports(manifest), 
        IMarker.SEVERITY_ERROR,
        manifestFile);
    
    // Check Dynamic Imports
    updateMarker(MARKER_DYNAMIC_IMPORT_PACKAGES, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.DYNAMIC_IMPORT_PACKAGE), 
        checkPackageDynamicImports(manifest), 
        IMarker.SEVERITY_ERROR,
        manifestFile);
  }
  
  public String checkManifestBundleActivator(BundleManifest manifest) {
    // Check that bundle activator class exists
    String activator = manifest.getActivator();
    if (activator != null) {
      IType[] types = getBundleActivators();
      for(int i=0; i<types.length;i++) {
        if (activator.trim().equals(types[i].getFullyQualifiedName())) {
          return null;
        }
      }
      //Could not find activator
      return "Bundle activator "+activator+" does not exist.";
    } else {
      return null;
    }
  }
  
  public String checkManifestBundleName(BundleManifest manifest) {
    // Check that bundle name is set
    String name = manifest.getName();
    if (name == null || name.trim().length() == 0) {
      return "Bundle name not specified.";
    } else if (name.indexOf(' ') != -1) {
      return "Bundle name should not contain spaces.";
    } else {
      return null;
    }
  }
  
  public String checkManifestDocUrl(BundleManifest manifest) {
    // Check that documentation URL is valid
    String url = manifest.getDocumentationUrl();
    if (url != null) {
      try {
        new URL(url);
        return null;
      } catch (MalformedURLException e) {
        return "Malformed URL specified as documentation location ["+e.getMessage()+"]";
      }
    } else {
      return null;
    }
  }
  
  public String checkManifestExecutionEnvironment(BundleManifest manifest) {
    // Check that execution environments are valid
    String[] environments = manifest.getExecutionEnvironments();
    ExecutionEnvironment[] prefEnv = OsgiPreferences.getExecutionEnvironments();
    ArrayList list = new ArrayList();
    for(int i=0; i<prefEnv.length; i++) {
      list.add(prefEnv[i].getName());
    }
    for(int i=0; i<environments.length; i++) {
      if (!list.contains(environments[i])) {
        return "Bundle uses unknown execution environments.";
      }
    }
    return null;
  }
  
  public String checkManifestUpdateLocation(BundleManifest manifest) {
    // Check that update location is a valid URI
    String loc = manifest.getUpdateLocation();
    if (loc != null) {
      try {
        new URL(loc);
        return null;
      } catch (MalformedURLException e) {
        return "Malformed URL specified as bundle update location ["+e.getMessage()+"]";
      }
    } else {
      return null;
    }
  }
  
  public String checkManifestBundleClassPath(BundleManifest manifest) { 
    String[] classPaths = manifest.getBundleClassPath();
    Map map = getBundlePackDescription().getContentsMap(false);
    for (int i=0; i<classPaths.length; i++) {
      String path = classPaths[i];
      if (".".equals(path)) continue;
      
      if (path.startsWith("/")) path = path.substring(1);
      
      if (!map.containsKey(path)) {
        return "Bundle classpath references libraries which are not part of the bundle.";
      }
    }
    
    return null;
  }
  
  public String checkPackageExports(BundleManifest manifest) {
    List exportablePackages = Arrays.asList(getExportablePackages());
    
    PackageDescription[] packages = manifest.getExportedPackages();
    for(int i=0; i<packages.length;i++) {
      if (!exportablePackages.contains(packages[i])) {
        return "Bundle exports packages which are not in the bundle classpath.";
      }
    }
    return null;
  }
  
  public String checkPackageDynamicImports(BundleManifest manifest) {
    String[] packages = manifest.getDynamicImportedPakages();
    IStatus status;
    for(int i=0; i<packages.length;i++) {
      String name = packages[i];
      if ("*".equals(name)) continue;
      
      if (name.endsWith(".*")) {
        name = name.substring(0, name.length()-2);
      }
      status = JavaConventions.validatePackageName(name);
      if (status.getCode() != IStatus.OK) {
        return "Dynamic imported packages contains invalid package names.";
      }
    }
    return null;
  }
  
  private void updateMarker(String type, int line, String error, int severity, IFile file) throws CoreException {
    
    if (error != null) {
      IMarker[] existingMarkers = file.findMarkers(type, false, IResource.DEPTH_INFINITE);
      if (existingMarkers == null || existingMarkers.length == 0) { 
        IMarker marker = file.createMarker(type);
        if (marker.exists()) {
          marker.setAttribute(IMarker.MESSAGE, error);
          marker.setAttribute(IMarker.SEVERITY, severity);
          if (line != -1) {
            marker.setAttribute(IMarker.LINE_NUMBER, line);
          }
        }
      }
    } else {
      file.deleteMarkers(type, false, IResource.DEPTH_INFINITE);
    }
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
  
  
  public BundlePackDescription loadBundlePackDescription() throws CoreException {
    // Read bundle jar
    InputStream is = null;
    BundlePackDescription jar  = null;
    try {
      try {
        IFile bundlePackFile = javaProject.getProject().getFile(BUNDLE_PACK_FILE);
        if (bundlePackFile.exists()) {
          is = bundlePackFile.getContents(true);
          jar = new BundlePackDescription(javaProject.getProject(), is);
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
        
        IFile bundlePackFile = javaProject.getProject().getFile(BUNDLE_PACK_FILE);
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

