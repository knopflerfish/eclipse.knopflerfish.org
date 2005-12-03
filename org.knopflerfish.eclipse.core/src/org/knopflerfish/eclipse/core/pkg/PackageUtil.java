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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.manifest.SymbolicName;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.classpath.BundleContainer;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageUtil {

  /****************************************************************************
   * Get exported packages methods
   ***************************************************************************/
  
  /**
   * Returns all available packages for a given project. The packages return 
   * are exported by the framework, bundle projects or repositories. 
   * 
   * @param project Bundle project
   * 
   * @return array of available packages
   */
  public static PackageDescription[] getExportedPackages(BundleProject project){
    ArrayList packages = new ArrayList();
    
    // Get packages exported by framework
    packages.addAll(Arrays.asList(getFrameworkExportedPackages(project)));
    // Get packages exported by bundle projects
    packages.addAll(Arrays.asList(getProjectExportedPackages()));
    // Get packages exported by repositories
    packages.addAll(Arrays.asList(getRepositoryExportedPackages()));

    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }

  
  public static PackageDescription[] getFrameworkExportedPackages(BundleProject project) {
    ArrayList packages = new ArrayList();
    try {
      // Get packages exported by the currently used framework
      FrameworkPreference framework = project.getFramework();
      IFrameworkDefinition definition = Osgi.getFrameworkDefinition(framework.getType());
      packages.addAll(Arrays.asList(definition.getExportedPackages(framework.getRuntimeLibraries())));
    } catch (Throwable t) {
    }
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }
  
  public static PackageDescription[] getProjectExportedPackages() {
    ArrayList packages = new ArrayList();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject [] projects = root.getProjects();
    for(int i=0; projects != null && i<projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject javaProject = JavaCore.create(projects[i]);
          IBundleProject bundleProject = new BundleProject(javaProject);
          packages.addAll(Arrays.asList(bundleProject.getBundleManifest().getExportedPackages()));
        }
      } catch (CoreException e) {
        // Failed to check project nature.
      }
    }
    
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }
  
  public static PackageDescription[] getRepositoryExportedPackages() {
    ArrayList packages = new ArrayList();
    
    RepositoryPreference[] repositoryPref = OsgiPreferences.getBundleRepositories();
    for (int i=0; i<repositoryPref.length; i++) {
      if (!repositoryPref[i].isActive()) continue;
      
      IBundleRepositoryType repositoryType = Osgi.getBundleRepositoryType(repositoryPref[i].getType());
      if (repositoryType == null) continue;
      
      IBundleRepository repository = repositoryType.createRepository(repositoryPref[i].getConfig());
      if (repository == null) continue;

      packages.addAll(Arrays.asList(repository.getExportedPackages()));
    }

    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }

  /****************************************************************************
   * Methods for finding framework/bundles which exports a given package
   ***************************************************************************/
  
  public static boolean frameworkExportsPackage(BundleProject project, PackageDescription pd) {
    try {
      // Get packages exported by the currently used framework
      FrameworkPreference framework = project.getFramework();
      IFrameworkDefinition definition = Osgi.getFrameworkDefinition(framework.getType());
      PackageDescription[] packages = definition.getExportedPackages(framework.getRuntimeLibraries());
      for (int i=0; i<packages.length; i++) {
        if (packages[i].isCompatible(pd)) {
          return true;
        }
      }
    } catch (Throwable t) {
    }
    return false;
  }
  
  public static BuildPath[] getExportingProjectBundles(PackageDescription pd) {
    ArrayList bundleIds = new ArrayList(); 
    
    // Workspace root
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    // Projects
    IProject [] projects = root.getProjects();
    IPath path = new Path(BundleContainer.CONTAINER_PATH);
    for(int i=0; projects != null && i<projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject project = JavaCore.create(projects[i]);
          IBundleProject bundleProject = new BundleProject(project);
          BundleManifest manifest = bundleProject.getBundleManifest();
          if (manifest.hasExportedPackage(pd)) {
            BundleIdentity id = bundleProject.getId();
            String  name = manifest.getName();
            bundleIds.add(new BuildPath(path.append("/"+id.getSymbolicName().toString()), pd, id, name));
          }
        }
      } catch (CoreException e) {
        // Failed to check project nature.
      }
    }
    
    return (BuildPath[]) bundleIds.toArray(new BuildPath[bundleIds.size()]);
  }

  public static BuildPath[] getExportingRepositoryBundles(PackageDescription pd) {
    ArrayList bundleIds = new ArrayList(); 
    
    RepositoryPreference[] repositoryPref = OsgiPreferences.getBundleRepositories();
    IPath path = new Path(BundleContainer.CONTAINER_PATH);
    for (int i=0; i<repositoryPref.length; i++) {
      if (!repositoryPref[i].isActive()) continue;
      
      IBundleRepositoryType repositoryType = Osgi.getBundleRepositoryType(repositoryPref[i].getType());
      if (repositoryType == null) continue;
      
      IBundleRepository repository = repositoryType.createRepository(repositoryPref[i].getConfig());
      if (repository == null) continue;

      BundleManifest[] manifests = repository.getExportingBundles(pd);
      if (manifests == null) continue;

      for(int j=0; j<manifests.length; j++) {
        SymbolicName symbolicName = manifests[j].getSymbolicName();
        // Use name if symbolic name is not set
        if (symbolicName == null && manifests[j].getName() != null) {
          symbolicName = new SymbolicName(manifests[j].getName());
        }
        // Skip if Symbolic name is not set 
        if (symbolicName == null) continue;
        BundleIdentity id = new BundleIdentity(symbolicName, manifests[j].getVersion());
        bundleIds.add(new BuildPath(path.append("/"+id.getSymbolicName().toString()), pd, id, manifests[j].getName()));
      }
    }
    
    return (BuildPath[]) bundleIds.toArray(new BuildPath[bundleIds.size()]);
  }  
  
  public static Version[] getFrameworkPackageVersions(BundleProject project, String packageName) {
    ArrayList versions = new ArrayList();
    
    // Framework
    try {
      // Get packages exported by the currently used framework
      FrameworkPreference framework = project.getFramework();
      IFrameworkDefinition definition = Osgi.getFrameworkDefinition(framework.getType());
      List packages = Arrays.asList(definition.getExportedPackages(framework.getRuntimeLibraries()));
      for(Iterator i=packages.iterator(); i.hasNext();) {
        PackageDescription pd = (PackageDescription) i.next();
        if (packageName.equals(pd.getPackageName())) {
          if (!versions.contains(pd.getSpecificationVersion())) {
            versions.add(pd.getSpecificationVersion());
          }
        }
      }
    } catch (Throwable t) {}

    return (Version[]) versions.toArray(new Version[versions.size()]);
  }

  public static Version[] getProjectPackageVersions(String packageName) {
    ArrayList versions = new ArrayList();
    
    // Projects
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject [] projects = root.getProjects();
    PackageDescription pd = new PackageDescription(packageName, null);
    for(int i=0; projects != null && i<projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject project = JavaCore.create(projects[i]);
          IBundleProject bundleProject = new BundleProject(project);
          PackageDescription [] exportedPackages = 
            bundleProject.getBundleManifest().getExportedPackages();
          for (int j=0; j<exportedPackages.length; j++) {
            if (exportedPackages[j].isCompatible(pd)) {
              versions.add(exportedPackages[j].getSpecificationVersion());
            }
          }
        }
      } catch (CoreException e) {
        // Failed to check project nature.
      }
    }
    
    return (Version[]) versions.toArray(new Version[versions.size()]);
  }

  public static Version[] getRepositoryPackageVersions(String packageName) {
    ArrayList versions = new ArrayList();
    
    // Repositories
    RepositoryPreference[] repositoryPref = OsgiPreferences.getBundleRepositories();
    for (int i=0; i<repositoryPref.length; i++) {
      if (!repositoryPref[i].isActive()) continue;
      
      IBundleRepositoryType repositoryType = Osgi.getBundleRepositoryType(repositoryPref[i].getType());
      if (repositoryType == null) continue;
      
      IBundleRepository repository = repositoryType.createRepository(repositoryPref[i].getConfig());
      if (repository == null) continue;

      Version[] repositoryVersions = repository.getPackageVersions(packageName);
      if (repositoryVersions != null) {
        versions.addAll(Arrays.asList(repositoryVersions));
      }
    }
    return (Version[]) versions.toArray(new Version[versions.size()]);
  }
}
