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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.internal.OsgiPlugin;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.manifest.SymbolicName;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.classpath.BundleContainer;
import org.knopflerfish.eclipse.core.project.classpath.BundleContainerInitializer;
import org.knopflerfish.eclipse.core.project.classpath.ClasspathUtil;
import org.knopflerfish.eclipse.core.project.classpath.FrameworkContainer;
import org.osgi.framework.Version;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @author Mats-Ola Persson, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleProject implements IBundleProject {
  
  // Constant marker identities
  public static final String MARKER_MANIFEST = "manifest";
  public static final String MARKER_BUNDLE_ACTIVATOR = "org.knopflerfish.eclipse.core.activator";
  public static final String MARKER_BUNDLE_NAME = "org.knopflerfish.eclipse.core.name";
  public static final String MARKER_BUNDLE_SYMBOLICNAME = "org.knopflerfish.eclipse.core.symbolicName";
  public static final String MARKER_BUNDLE_VERSION = "org.knopflerfish.eclipse.core.version";
  public static final String MARKER_BUNDLE_UPDATELOCATION = "org.knopflerfish.eclipse.core.updateLocation";
  public static final String MARKER_BUNDLE_DOCURL = "org.knopflerfish.eclipse.core.docUrl";
  public static final String MARKER_BUNDLE_EXEC_ENV = "org.knopflerfish.eclipse.core.execEnv";
  public static final String MARKER_BUNDLE_CLASSPATH = "org.knopflerfish.eclipse.core.classpath";
  public static final String MARKER_EXPORT_PACKAGES  = "org.knopflerfish.eclipse.core.packageExports";
  public static final String MARKER_IMPORT_PACKAGES  = "org.knopflerfish.eclipse.core.packageImports";
  public static final String MARKER_DYNAMIC_IMPORT_PACKAGES  = "org.knopflerfish.eclipse.core.packageDynamicImports";
  public static final String MARKER_SYMBOLICNAME_AND_VERSION_CLASH = "org.knopflerfish.eclipse.core.symbolicNameVersionClash";
  
  private final IProject project;
  private final IJavaProject javaProject;
  
  public BundleProject(String name) {
    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspace.getProject(name);
    IJavaProject javaProject = JavaCore.create(project);
    this.javaProject = javaProject;
    this.project =javaProject.getProject();
  }
  
  public BundleProject(IJavaProject project) {
    this.javaProject = project;
    this.project =javaProject.getProject();
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleProject implementation
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getId()
   */
  public BundleIdentity getId() throws CoreException {
    BundleManifest bm = getBundleManifest();
    return new BundleIdentity(bm.getSymbolicName(), bm.getVersion());
  }
  
  public IProject getProject() {
    return project;
  }
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleProject#getJavaProject()
   */
  public IJavaProject getJavaProject() {
    return javaProject;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundleManifest()
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
          is = manifestFile.getContents(true);
          manifest = new BundleManifest(is);
        } else {
          manifest = new BundleManifest();
        }
      } finally {
        if (is != null) {
          is.close();
        }
      }
    } catch (Exception e) {
      IStatus status =
        new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core", IStatus.OK, 
            "Failed to get manifest for project "+javaProject.getProject().getName(), e);
      OsgiPlugin.log(status);
    }
    
    return manifest;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#setBundleManifest(org.knopflerfish.eclipse.core.manifest.BundleManifest)
   */
  public void setBundleManifest(BundleManifest manifest) throws CoreException {
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
    } catch (IOException e) {
      OsgiPlugin.throwCoreException("Failed to set manifest for project "+javaProject.getProject().getName(), e);
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundlePackDescription()
   */
  public BundlePackDescription getBundlePackDescription() throws CoreException {
    
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
    } catch (IOException e) {
      OsgiPlugin.throwCoreException("Failed to get bundle pack description for project "+javaProject.getProject().getName(), e);
    }
    return packDescription;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#setBundlePackDescription(org.knopflerfish.eclipse.core.project.BundlePackDescription)
   */
  public void setBundlePackDescription(BundlePackDescription packDescription) throws CoreException {
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
    } catch (IOException e) {
      OsgiPlugin.throwCoreException("Failed to set bundle pack description for project "+javaProject.getProject().getName(), e);
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getExportablePackageNames()
   */
  public String[] getExportablePackageNames() throws JavaModelException {
    Map map = getExportablePackageNames(getFragmentRoots());
    return (String[]) map.keySet().toArray(new String[map.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getReferencedPackageNames()
   */
  public String[] getReferencedPackageNames() throws JavaModelException {
    TreeSet<String> packageNames = new TreeSet<String>();
    
    IPackageFragmentRoot[] roots = getFragmentRoots();
    TreeSet packages = new TreeSet();
    packages.addAll(getExportablePackageNames(roots).keySet());
    Map classFiles = getClassFiles(roots);
    packages.addAll(classFiles.keySet());
    Map compilationUnits = getCompilationUnit(roots);
    packages.addAll(compilationUnits.keySet());

    for (Iterator i=compilationUnits.values().iterator(); i.hasNext();) {
      ICompilationUnit unit = (ICompilationUnit) i.next();
      IImportContainer container = unit.getImportContainer();
      if (!container.exists()) continue;
      IJavaElement[] imports = container.getChildren();
      for (int j=0; j<imports.length;j++) {
        String s = imports[j].getElementName();
        if (s.startsWith("java.")) continue;
        int idx = s.lastIndexOf('.');
        if (idx != -1) {
          String name = s.substring(0,idx);
          if (!packages.contains(name) && !packageNames.contains(name)) {
            packageNames.add(name);
          }
        }
      }
    }
    return packageNames.toArray(new String[packageNames.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.project.IBundleProject#getBundleActivators()
   */
  public IType[] getBundleActivators() throws JavaModelException {
    ArrayList activators = new ArrayList();
    
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
    
    return (IType[]) activators.toArray(new IType[activators.size()]);
  }
  
  /****************************************************************************
   * Buildpath methods
   ***************************************************************************/
  public BuildPath[] getBuildPaths() {
    ArrayList paths = new ArrayList();
    
    try {
      IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
      for(int i=0; i<rawClasspath.length; i++) {
        IPath path = rawClasspath[i].getPath();
        if (path.toString().startsWith(FrameworkContainer.CONTAINER_PATH)) { 
          IAccessRule[] rules = rawClasspath[i].getAccessRules();
          for (int j=0; j<rules.length; j++) {
            if (rules[j].getKind() == IAccessRule.K_ACCESSIBLE) {
              PackageDescription pd = ClasspathUtil.createPackageDescription(rawClasspath[i], rules[j]);
              paths.add(new BuildPath(path, pd, null, "Framework"));
            }
          }
        } else if (path.toString().startsWith(BundleContainer.CONTAINER_PATH)) {
          IAccessRule[] rules = rawClasspath[i].getAccessRules();
          for (int j=0; j<rules.length; j++) {
            if (rules[j].getKind() == IAccessRule.K_ACCESSIBLE) {
              PackageDescription pd = ClasspathUtil.createPackageDescription(rawClasspath[i], rules[j]);
              // Get bundle identity from classpath entry
              BundleIdentity id = BundleContainerInitializer.getBundleIdentity(rawClasspath[i].getPath());
              String name = ClasspathUtil.getClasspathAttribute(rawClasspath[i], ClasspathUtil.ATTR_BUNDLENAME);
              paths.add(new BuildPath(path, pd, id, name));
            }
          }
        }
      }
    } catch (Throwable t) {
    }
    return (BuildPath[]) paths.toArray(new BuildPath[paths.size()]);
  }
  
  public BuildPath getBuildPath(PackageDescription pd) {
    if (pd == null) return null;
    
    IAccessRule rule = ClasspathUtil.createAccessRule(pd);
    IClasspathEntry entry = ClasspathUtil.findClasspathEntry(javaProject, rule);
    IPath path = null;
    BundleIdentity id = null;
    String name = null;
    if (entry != null) {
      path = entry.getPath();
      name = "Framework";
      if (path.toString().startsWith(BundleContainer.CONTAINER_PATH)) {
        id = BundleContainerInitializer.getBundleIdentity(entry.getPath());
        name = ClasspathUtil.getClasspathAttribute(entry, ClasspathUtil.ATTR_BUNDLENAME); 
      }
    }
    return new BuildPath(path, pd, id, name);
  }
  
  public void addBuildPath(BuildPath path, boolean updateManifest) throws CoreException {
    if (path == null || path.getContainerPath() == null || 
        path.getPackageDescription() == null) return;
    
    // Check type of path
    if (path.getContainerPath().toString().startsWith(FrameworkContainer.CONTAINER_PATH)) {
      importFrameworkPackage(path.getPackageDescription(), updateManifest);
    } else if (path.getContainerPath().toString().startsWith(BundleContainer.CONTAINER_PATH)) {
      importBundlePackage(path, updateManifest);
    } 
  }
  
  public void removeBuildPath(BuildPath path, boolean updateManifest) throws CoreException {
    if (path == null || path.getContainerPath() == null || 
        path.getPackageDescription() == null) return;
    
    // Check type of path
    if (path.getContainerPath().toString().startsWith(FrameworkContainer.CONTAINER_PATH)) {
      removeFrameworkPackage(path.getPackageDescription(), updateManifest);
    } else if (path.getContainerPath().toString().startsWith(BundleContainer.CONTAINER_PATH)) {
      removeBundlePackage(path, updateManifest);
    } 
  }
  
  
  /****************************************************************************
   * Classpath package methods
   ***************************************************************************/
  public FrameworkPreference getFramework() throws JavaModelException {
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    for(int i=0; i<rawClasspath.length; i++) {
      // Find container exporting this package
      if (rawClasspath[i].getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
        continue;
      }
      IPath containerPath = rawClasspath[i].getPath();
      String hint = containerPath.lastSegment();
      if (containerPath.toString().startsWith(FrameworkContainer.CONTAINER_PATH)) {
        // Find framework distribution
        FrameworkPreference distribution = null;
        if (hint != null) {
          distribution = OsgiPreferences.getFramework(hint);
        }
        if (distribution == null) {
          distribution = OsgiPreferences.getDefaultFramework();
        }
        
        return distribution;
      }
    }
    return null;
  }
  
  /****************************************************************************
   * Update Classpath methods
   ***************************************************************************/
  
  private void importFrameworkPackage(PackageDescription pd,
                                      boolean updateManifest)
    throws CoreException
  {

    // Update access rules for framework container
    ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(
        Arrays.asList(javaProject.getRawClasspath()));
    int idx = -1;
    for (int i = 0; i < entries.size(); i++) {
      // Find framework container
      IClasspathEntry entry = entries.get(i);
      if (entry.getPath().toString()
          .startsWith(FrameworkContainer.CONTAINER_PATH)) {
        idx = i;
        break;
      }
    }

    // Check if access rule aleady exist
    IAccessRule rule = ClasspathUtil.createAccessRule(pd);
    if (idx != -1) {
      boolean exist = false;
      IClasspathEntry oldEntry = entries.get(idx);

      ArrayList<IAccessRule> rules = new ArrayList<IAccessRule>(
          Arrays.asList(oldEntry.getAccessRules()));
      // Check if rule exists
      for (int i = 0; i < rules.size(); i++) {
        if (rule.equals(rules.get(i))) {
          exist = true;
          break;
        }
      }

      // Update classpath
      if (!exist) {
        rules.add(0, rule);

        IClasspathEntry newEntry = JavaCore.newContainerEntry(new Path(
            FrameworkContainer.CONTAINER_PATH), rules
            .toArray(new IAccessRule[rules.size()]), null, false);
        entries.remove(idx);
        entries.add(idx, newEntry);

        javaProject.setRawClasspath(
            entries.toArray(new IClasspathEntry[entries.size()]), null);
      }
    }

    // Update manifest
    if (updateManifest) {
      // Add package to manifest
      BundleManifest manifest = getBundleManifest();
      ArrayList<PackageDescription> importedPackages = new ArrayList<PackageDescription>(
          Arrays.asList(manifest.getImportedPackages()));
      boolean changed = false;
      if (!importedPackages.contains(pd)) {
        importedPackages.add(pd);
        changed = true;
      }

      if (changed) {
        manifest.setImportedPackages(importedPackages
            .toArray(new PackageDescription[importedPackages.size()]));
        setBundleManifest(manifest);
      }
    }
  }
  
  public void removeFrameworkPackage(PackageDescription pd,
                                     boolean updateManifest)
    throws CoreException
  {

    // Update access rules for framework container
    ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(
        Arrays.asList(javaProject.getRawClasspath()));
    int idx = -1;
    for (int i = 0; i < entries.size(); i++) {
      // Find framework container
      IClasspathEntry entry = entries.get(i);
      if (entry.getPath().toString()
          .startsWith(FrameworkContainer.CONTAINER_PATH)) {
        idx = i;
        break;
      }
    }

    // Check if access rule exist
    IAccessRule rule = ClasspathUtil.createAccessRule(pd);
    if (idx != -1) {
      boolean exist = false;
      IClasspathEntry oldEntry = entries.get(idx);

      ArrayList<IAccessRule> rules = new ArrayList<IAccessRule>(
          Arrays.asList(oldEntry.getAccessRules()));
      // Check if rule exists
      for (int i = 0; i < rules.size(); i++) {
        if (rule.equals(rules.get(i))) {
          exist = true;
          break;
        }
      }

      // Update classpath
      if (exist) {
        rules.remove(rule);
        IClasspathEntry newEntry = JavaCore.newContainerEntry(new Path(
            FrameworkContainer.CONTAINER_PATH), rules
            .toArray(new IAccessRule[rules.size()]), null, false);
        entries.remove(idx);
        entries.add(idx, newEntry);

        javaProject.setRawClasspath(
            entries.toArray(new IClasspathEntry[entries.size()]), null);
      }
    }

    // Update manifest
    if (updateManifest) {
      // Add package to manifest
      BundleManifest manifest = getBundleManifest();
      ArrayList<PackageDescription> importedPackages = new ArrayList<PackageDescription>(
          Arrays.asList(manifest.getImportedPackages()));
      boolean changed = false;
      if (importedPackages.contains(pd)) {
        importedPackages.remove(pd);
        changed = true;
      }

      if (changed) {
        manifest.setImportedPackages(importedPackages
            .toArray(new PackageDescription[importedPackages.size()]));
        setBundleManifest(manifest);
      }
    }
  }
  
  public void importBundlePackage(BuildPath bp, boolean updateManifest)
    throws CoreException
  {

    // Update access rules for bundle container
    ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(
        Arrays.asList(javaProject.getRawClasspath()));
    int idx = -1;
    for (int i = 0; i < entries.size(); i++) {
      // Find bundle container
      IClasspathEntry entry = entries.get(i);
      if (entry.getPath().toString().startsWith(BundleContainer.CONTAINER_PATH)) {
        if (bp
            .getBundleIdentity()
            .getSymbolicName()
            .equals(
                BundleContainerInitializer.getBundleIdentity(entry.getPath())
                    .getSymbolicName())) {
          idx = i;
          break;
        }
      }
    }

    // Check if access rule already exist
    IAccessRule rule = ClasspathUtil.createAccessRule(bp
        .getPackageDescription());
    if (idx != -1) {
      boolean exist = false;
      IClasspathEntry oldEntry = (IClasspathEntry) entries.get(idx);

      ArrayList<IAccessRule> rules = new ArrayList<IAccessRule>(
          Arrays.asList(oldEntry.getAccessRules()));
      // Check if rule exists
      for (int i = 0; i < rules.size(); i++) {
        if (rule.equals(rules.get(i))) {
          exist = true;
          break;
        }
      }

      // Update classpath
      if (!exist) {
        rules.add(0, rule);
        IClasspathEntry newEntry = JavaCore.newContainerEntry(new Path(
            BundleContainer.CONTAINER_PATH + "/"
                + bp.getBundleIdentity().getSymbolicName().toString()), rules
            .toArray(new IAccessRule[rules.size()]), null, false);
        entries.remove(idx);
        entries.add(idx, newEntry);

        javaProject.setRawClasspath(
            entries.toArray(new IClasspathEntry[entries.size()]), null);
      }
    } else {
      // Add new entry
      IAccessRule defaultRule = JavaCore.newAccessRule(new Path("**/*"),
          IAccessRule.K_NON_ACCESSIBLE);
      IClasspathEntry newEntry = JavaCore.newContainerEntry(new Path(
          BundleContainer.CONTAINER_PATH + "/"
              + bp.getBundleIdentity().getSymbolicName().toString()),
          new IAccessRule[] { rule, defaultRule }, null, false);
      entries.add(newEntry);

      javaProject.setRawClasspath((IClasspathEntry[]) entries
          .toArray(new IClasspathEntry[entries.size()]), null);
    }

    // Update manifest
    if (updateManifest) {
      // Add package to manifest
      BundleManifest manifest = getBundleManifest();
      ArrayList<PackageDescription> importedPackages = new ArrayList<PackageDescription>(
          Arrays.asList(manifest.getImportedPackages()));
      boolean changed = false;
      if (!importedPackages.contains(bp.getPackageDescription())) {
        importedPackages.add(bp.getPackageDescription());
        changed = true;
      }

      if (changed) {
        manifest.setImportedPackages(importedPackages
            .toArray(new PackageDescription[importedPackages.size()]));
        setBundleManifest(manifest);
      }
    }
  }
  
  public void removeBundlePackage(BuildPath bp, boolean updateManifest)
    throws CoreException
  {

    // Update access rules for bundle container
    ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(
        Arrays.asList(javaProject.getRawClasspath()));
    int idx = -1;
    for (int i = 0; i < entries.size(); i++) {
      // Find bundle container
      IClasspathEntry entry = entries.get(i);
      if (entry.getPath().toString().startsWith(BundleContainer.CONTAINER_PATH)) {
        if (bp.getBundleIdentity().equals(
            BundleContainerInitializer.getBundleIdentity(entry.getPath()))) {
          idx = i;
          break;
        }
      }
    }

    // Check if access rule aleady exist
    IAccessRule rule = ClasspathUtil.createAccessRule(bp
        .getPackageDescription());
    if (idx != -1) {
      boolean exist = false;
      IClasspathEntry oldEntry = (IClasspathEntry) entries.get(idx);

      ArrayList<IAccessRule> rules = new ArrayList<IAccessRule>(
          Arrays.asList(oldEntry.getAccessRules()));
      // Check if rule exists
      for (int i = 0; i < rules.size(); i++) {
        if (rule.equals(rules.get(i))) {
          exist = true;
          break;
        }
      }

      // Update classpath
      if (exist) {
        rules.remove(rule);
        entries.remove(idx);
        if (rules.size() > 1) {
          // Update entry
          IClasspathEntry newEntry = JavaCore.newContainerEntry(new Path(
              BundleContainer.CONTAINER_PATH + "/"
                  + bp.getBundleIdentity().toString()), rules
              .toArray(new IAccessRule[rules.size()]), null, false);
          entries.add(idx, newEntry);
        }

        javaProject.setRawClasspath(
            entries.toArray(new IClasspathEntry[entries.size()]), null);
      }
    }

    // Update manifest
    if (updateManifest) {
      // Add package to manifest
      BundleManifest manifest = getBundleManifest();
      ArrayList<PackageDescription> importedPackages = new ArrayList<PackageDescription>(
          Arrays.asList(manifest.getImportedPackages()));
      boolean changed = false;
      if (importedPackages.contains(bp.getPackageDescription())) {
        importedPackages.remove(bp.getPackageDescription());
        changed = true;
      }

      if (changed) {
        manifest.setImportedPackages(importedPackages
            .toArray(new PackageDescription[importedPackages.size()]));
        setBundleManifest(manifest);
      }
    }
  }
  
  /****************************************************************************
   * Check methods
   ***************************************************************************/
  
  public void checkManifest() throws CoreException {
    
    IFile manifestFile = javaProject.getProject().getFile(MANIFEST_FILE);
    
    InputStream is = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      try {
        is = manifestFile.getContents(true);
        
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
      IStatus status =
        new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core", IStatus.OK, 
            "Failure reading manifest contents", t);
      OsgiPlugin.log(status);
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
    
    // Check Bundle symbolic name
    SymbolicName symbolicName = manifest.getSymbolicName();
    String error = null;
    int severity = IMarker.SEVERITY_WARNING;
    manifestFile.deleteMarkers(MARKER_BUNDLE_SYMBOLICNAME, false, IResource.DEPTH_INFINITE);
    if (symbolicName == null || symbolicName.getSymbolicName().trim().length() == 0) {
      error = "Symbolic name must be set.";
      severity = IMarker.SEVERITY_ERROR;
    } else {
      IStatus status = JavaConventions.validatePackageName(symbolicName.getSymbolicName());
      if (status.getSeverity() == IStatus.ERROR) {
        error = "Symbolic name is not a valid package name.";
        severity = manifest.isR4() ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
      }
    }
    updateMarker(MARKER_BUNDLE_SYMBOLICNAME, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_SYMBOLIC_NAME), 
        error, 
        severity,
        manifestFile);
    
    // Check Bundle version
    updateMarker(MARKER_BUNDLE_VERSION, 
        ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_VERSION), 
        checkManifestBundleVersion(manifest), 
        IMarker.SEVERITY_ERROR,
        manifestFile);
    
    manifestFile.deleteMarkers(MARKER_SYMBOLICNAME_AND_VERSION_CLASH, false, IResource.DEPTH_INFINITE);
    if (manifest.isR4()) {
      // Check uniqueness
      IBundleProject[] projects = ProjectUtil.getBundleProjects();
      for (int i = 0; i < projects.length; i++) {
        if (!projects[i].getProject().getName().equals(getProject().getName()) && 
            getId().equals(projects[i].getId())) {
          String errorMsg = "Bundle-ManifestVersion set to 2 requires SymbolicName and Version to be unique.";
          
          updateMarker(MARKER_SYMBOLICNAME_AND_VERSION_CLASH,
              ManifestUtil.findAttributeLine(manifestContents, BundleManifest.BUNDLE_MANIFESTVERSION),
              errorMsg,
              IMarker.SEVERITY_ERROR,
              manifestFile);
        }
      }
    }
    
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
    
    // Check Imports, get needed packages and check against manifest
    // Warn if imports are done that are not needed
    // Error if imports other to java. are done and are not specified in manifest.
    List neededPackageNames = Arrays.asList(getReferencedPackageNames());
    PackageDescription[] importedPackages = manifest.getImportedPackages();
    ArrayList importedPackageNames = new ArrayList();
    for (int i=0; i<importedPackages.length; i++) {
      importedPackageNames.add(importedPackages[i].getPackageName());
    }
    
    error = null;
    severity = IMarker.SEVERITY_WARNING;
    int line = ManifestUtil.findAttributeLine(manifestContents, BundleManifest.IMPORT_PACKAGE); 
    manifestFile.deleteMarkers(MARKER_IMPORT_PACKAGES, false, IResource.DEPTH_INFINITE);
    for(int i=0; i<neededPackageNames.size();i++) {
      String name = (String) neededPackageNames.get(i);
      if (!importedPackageNames.contains(name)) {
        IMarker marker = manifestFile.createMarker(MARKER_IMPORT_PACKAGES);
        if (marker.exists()) {
          marker.setAttribute(IMarker.MESSAGE, "The package "+name+" is used but not imported in manifest.");
          marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
          if (line != -1) {
            marker.setAttribute(IMarker.LINE_NUMBER, line);
          }
        }
        
      }
    }
    for(int i=0; i<importedPackageNames.size();i++) {
      String name = (String) importedPackageNames.get(i);
      if (!neededPackageNames.contains(name)) {
        IMarker marker = manifestFile.createMarker(MARKER_IMPORT_PACKAGES);
        if (marker.exists()) {
          marker.setAttribute(IMarker.MESSAGE, "The package "+name+" is imported in manifest but is never directly referenced in the source code.");
          marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
          if (line != -1) {
            marker.setAttribute(IMarker.LINE_NUMBER, line);
          }
        }
        
      }
    }
  }
  
  public String checkManifestBundleActivator(BundleManifest manifest) throws CoreException {
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
    }
    return null;
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
  
  public String checkManifestBundleVersion(BundleManifest manifest) {
    
    String s = manifest.getAttribute(BundleManifest.BUNDLE_VERSION);
    try {
      Version.parseVersion(s);
      return null;
    } catch (IllegalArgumentException e) {
      return "Version improperly formatted, format major('.'minor('.'micro('.'qualifier)?)?)?";
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
    }
    return null;
  }
  
  public String checkManifestExecutionEnvironment(BundleManifest manifest) {
    // Check that execution environments are valid
    String[] environments = manifest.getExecutionEnvironments();
    EnvironmentPreference[] prefEnv = OsgiPreferences.getExecutionEnvironments();
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
    }
    return null;
  }
  
  public String checkManifestBundleClassPath(BundleManifest manifest) throws CoreException { 
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
  
  public String checkPackageExports(BundleManifest manifest) throws JavaModelException {
    Map exportablePackages = getExportablePackageNames(getFragmentRoots());
    PackageDescription[] packages = manifest.getExportedPackages();
    for(int i=0; i<packages.length;i++) {
      if (!exportablePackages.containsKey(packages[i].getPackageName())) {
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
  
  
  private BundlePackDescription loadBundlePackDescription() throws CoreException {
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

  /****************************************************************************
   * Private worker methods for checking contents of bundle project
   ***************************************************************************/
  
  private IPackageFragmentRoot[] getFragmentRoots() throws JavaModelException {
    ArrayList fragmentRoots = new ArrayList();
    
    IProject project = javaProject.getProject();
    IPackageFragmentRoot[] fragmentRoot = javaProject.getAllPackageFragmentRoots();
    for (int i=0; i<fragmentRoot.length; i++) {
      if (fragmentRoot[i].isExternal()) continue;
      if (fragmentRoot[i].getKind() != IPackageFragmentRoot.K_SOURCE && 
          !fragmentRoot[i].isArchive()) continue;
      
      IResource resource = fragmentRoot[i].getCorrespondingResource();
      IProject fragmentProject = project;
      if (resource != null) {
        fragmentProject = resource.getProject();
      }
      if (project.equals(fragmentProject)) {
        fragmentRoots.add(fragmentRoot[i]);
      }
    }
    
    return (IPackageFragmentRoot[]) fragmentRoots.toArray(new IPackageFragmentRoot[fragmentRoots.size()]);
  }
  
  private Map getExportablePackageNames(IPackageFragmentRoot[] roots) throws JavaModelException {
    HashMap packageNames = new HashMap();
    
    for (int i=0; i<roots.length; i++) {
      IJavaElement[] elements = roots[i].getChildren();
      for (int j=0; j<elements.length; j++) {
        if (elements[j].getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
          // Check if package fragment has any classes to export
          IPackageFragment fragment = (IPackageFragment) elements[j];
          String name = fragment.getElementName().trim();
          if (name.length() == 0 || "meta-inf".equalsIgnoreCase(name)) continue; 
          boolean javaResources = fragment.containsJavaResources();
          boolean nonJavaResources = fragment.getNonJavaResources().length > 0;
          
          if (javaResources || nonJavaResources) {
            packageNames.put(name, roots[i]);
          }
        }
      }
    }
    return packageNames;
  }
  
  private Map getClassFiles(IPackageFragmentRoot[] roots) throws JavaModelException {
    HashMap classFiles = new HashMap();
    
    for (int i=0; i<roots.length; i++) {
      IJavaElement[] elements = roots[i].getChildren();
      for (int j=0; j<elements.length; j++) {
        IJavaElement element = elements[j];
        int type = element.getElementType();
        
        if (type == IJavaElement.PACKAGE_FRAGMENT) {
          IPackageFragment fragment = (IPackageFragment) element;
          String packageName = fragment.getElementName();
          StringBuffer buf = new StringBuffer(packageName);
          buf.append('.');
          int bufLen = buf.length();
          
          IClassFile[] files = fragment.getClassFiles();
          for (int k=0; k<files.length; k++) {
            IClassFile classFile = files[k];
            buf.setLength(bufLen);
            buf.append(classFile.getElementName());
            // Remove trailing '.class'
            buf.setLength(buf.length()-6);
            // Fix internal class names
            String className = buf.toString().replace('$', '.');
            classFiles.put(className, classFile);
          }
        }
      }
    }
    return classFiles;
  }
  
  private Map getCompilationUnit(IPackageFragmentRoot[] roots) throws JavaModelException {
    HashMap compilationUnits = new HashMap();
    
    for (int i=0; i<roots.length; i++) {
      IJavaElement[] elements = roots[i].getChildren();
      for (int j=0; j<elements.length; j++) {
        IJavaElement element = elements[j];
        int type = element.getElementType();
        
        if (type == IJavaElement.PACKAGE_FRAGMENT) {
          IPackageFragment fragment = (IPackageFragment) element;
          String packageName = fragment.getElementName();
          StringBuffer buf = new StringBuffer(packageName);
          buf.append('.');
          int bufLen = buf.length();
          
          ICompilationUnit[] units = fragment.getCompilationUnits();
          for (int k=0; k<units.length; k++) {
            ICompilationUnit unit = units[k];
            buf.setLength(bufLen);
            buf.append(unit.getElementName());
            // Remove trailing '.java'
            buf.setLength(buf.length()-5);
            // TODO:Fix internal classes for compilation units
            String className = buf.toString();
            compilationUnits.put(className, unit);
          }
        }
      }
    }
    return compilationUnits;
  }
  

}

