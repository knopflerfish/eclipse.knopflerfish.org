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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.IBundleProject;
import org.knopflerfish.eclipse.core.project.classpath.BundleContainer;

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
   * Get container path methods
   ***************************************************************************/
  /*
  public static IPath[] getContainerPaths(BundleProject project, PackageDescription pd) {
    ArrayList paths = new ArrayList();

    // Check if framework exports package
    if (frameworkExportsPackage(project, pd)) {
      paths.add(new Path(FrameworkContainer.CONTAINER_PATH));
    }
    // Get projects which export the package
    paths.addAll(Arrays.asList(getProjectContainerPaths(pd)));
    // Get bundles which export the package
    //paths.addAll(Arrays.asList(getRepositoryExportedPackages()));
    
    // Find bundles exporting this package
    
    return (IPath[]) paths.toArray(new IPath[paths.size()]);
  }
  */
  
  public static boolean frameworkExportsPackage(BundleProject project, PackageDescription pd) {
    ArrayList packages = new ArrayList();
    try {
      // Get packages exported by the currently used framework
      FrameworkPreference framework = project.getFramework();
      IFrameworkDefinition definition = Osgi.getFrameworkDefinition(framework.getType());
      packages.addAll(Arrays.asList(definition.getExportedPackages(framework.getRuntimeLibraries())));
    } catch (Throwable t) {
    }
    return packages.contains(pd);
  }
  
  /*
  public static IPath[] getExportingRepositoryBundles(PackageDescription pkg) {
    if (pkg == null) return null;
    ArrayList exportingBundles = new ArrayList(); 
    
    RepositoryPreference[] repositoryPref = OsgiPreferences.getBundleRepositories();
    for (int i=0; i<repositoryPref.length; i++) {
      if (!repositoryPref[i].isActive()) continue;
      
      IBundleRepositoryType repositoryType = Osgi.getBundleRepositoryType(repositoryPref[i].getType());
      if (repositoryType == null) continue;
      
      IBundleRepository repository = repositoryType.createRepository(repositoryPref[i].getConfig());
      if (repository == null) continue;

      packages.addAll(Arrays.asList(repository.getExportedPackages()));
    }
    // Knopflerfish root
    FrameworkPreference[] distributions = OsgiPreferences.getFrameworks();
    
    for(int i=0; i<distributions.length; i++) {
      IOsgiBundle[] bundles = distributions[i].getBundles();
      for(int j=0; bundles != null && j<bundles.length; j++) {
        IOsgiBundle b = bundles[j]; 
        if (b.hasExportedPackage(pkg)) {
          exportingBundles.add(b);
        }
      }
    }
    return (IOsgiBundle[]) exportingBundles.toArray(new IOsgiBundle[exportingBundles.size()]);
  }
  */
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
          
          if (bundleProject.hasExportedPackage(pd)) {
            BundleIdentity id = bundleProject.getId();
            String  name = bundleProject.getBundleManifest().getName();
            bundleIds.add(new BuildPath(path, pd, id, name));
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
        BundleIdentity id = new BundleIdentity(
            manifests[j].getSymbolicName(), 
            manifests[j].getVersion());
        bundleIds.add(new BuildPath(path, pd, id, manifests[j].getName()));
      }
    }
    
    return (BuildPath[]) bundleIds.toArray(new BuildPath[bundleIds.size()]);
  }
  
  /****************************************************************************
   * Find package methods
   ***************************************************************************/
  
  /*
  public static IPackage[] findPackage(BundleProject project, String name) {
    ArrayList matchPkgs = new ArrayList();
    matchPkgs.addAll(Arrays.asList(findFrameworkPackage(project,name)));
    return (IPackage[]) matchPkgs.toArray(new IPackage[matchPkgs.size()]);
  }
  
  public static IPackage[] findFrameworkPackage(BundleProject project, String name) {
    ArrayList matchPkgs = new ArrayList();

    try {
      if (name != null) {
        // First check framework
        FrameworkPreference framework = project.getFramework();
        PackageDescription[] packages = getFrameworkExportedPackages(project);
        for (int i=0; i<packages.length;i++) {
          if (name.equals(packages[i].getPackageName())) {
            matchPkgs.add(new FrameworkPackage(packages[i], framework));
          }
        }
      }
    } catch (Throwable t) {
    }
    return (IPackage[]) matchPkgs.toArray(new IPackage[matchPkgs.size()]);
  }
  */
  
  /*
  public static IPackage[] findPackage(String name, int type) {
    ArrayList matchPkgs = new ArrayList();

    if (name != null) {
      IPackage[] pkgs = getPackages(type);
      for (int i=0; i<pkgs.length;i++) {
        if (name.equals(pkgs[i].getPackageDescription().getPackageName())) {
          matchPkgs.add(pkgs[i]);
        }
      }
    }
    return (IPackage[]) matchPkgs.toArray(new IPackage[matchPkgs.size()]);
  }
  
  public static IPackage[] findPackage(String name, Framework distribution, int type) {
    ArrayList matchPkgs = new ArrayList();

    if (name != null) {
      IPackage[] pkgs = getPackages(distribution, type);
      for (int i=0; i<pkgs.length;i++) {
        if (name.equals(pkgs[i].getPackageDescription().getPackageName())) {
          matchPkgs.add(pkgs[i]);
        }
      }
      if ((type & IPackage.PROJECT) != 0) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        pkgs = getPackages(root);
        for (int i=0; i<pkgs.length;i++) {
          if (name.equals(pkgs[i].getPackageDescription().getPackageName())) {
            matchPkgs.add(pkgs[i]);
          }
        }
      }
    }
    return (IPackage[]) matchPkgs.toArray(new IPackage[matchPkgs.size()]);
  }
  
  public static IPackage[] getPackages(int type) {
    ArrayList pkgs = new ArrayList();
    
    // Exported packages from distributions
    Framework[] distributions = OsgiPreferences.getFrameworks();
    for(int i=0; i<distributions.length; i++) {
      pkgs.addAll(Arrays.asList(getPackages(distributions[i], type)));
    }
    
    // Exported packages from projects
    if ((type & IPackage.PROJECT) != 0) {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      pkgs.addAll(Arrays.asList(getPackages(root)));
    }
    
    return (IPackage[]) pkgs.toArray(new IPackage[pkgs.size()]);
  }
  
  public static IPackage[] getPackages(Framework distribution, int type) {
    ArrayList pkgs = new ArrayList();
    
    IFrameworkDefinition framework = Osgi.getFrameworkDefinition(distribution.getType());
    
    if ( (type & IPackage.FRAMEWORK) != 0) {
      IOsgiLibrary [] libraries = distribution.getRuntimeLibraries();
      PackageDescription [] packageDescriptions = framework.getExportedPackages(libraries);
      for (int i=0; i<packageDescriptions.length; i++) {
        pkgs.add(new FrameworkPackage(packageDescriptions[i], distribution));
      }
    }
    
    if ( (type & IPackage.BUNDLE) != 0) {
      IOsgiBundle [] bundles = distribution.getBundles();
      for (int i=0; i<bundles.length; i++) {
        PackageDescription [] packageDescriptions = bundles[i].getBundleManifest().getExportedPackages();
        for (int j=0; j<packageDescriptions.length; j++) {
          pkgs.add(new BundlePackage(packageDescriptions[j], distribution, bundles[i]));
        }
      }
    }
    return (IPackage[]) pkgs.toArray(new IPackage[pkgs.size()]);
  }
  

  public static IPackage[] getPackages(IWorkspaceRoot root) {
    ArrayList pkgs = new ArrayList();
  
    IProject [] projects = root.getProjects();
    for(int i=0; projects != null && i<projects.length; i++) {
      try {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          IJavaProject project = JavaCore.create(projects[i]);
          IBundleProject b = new BundleProject(project);
          pkgs.addAll(Arrays.asList(getPackages(b)));
        }
      } catch (CoreException e) {
        // Failed to check project nature.
      }
    }
    return (IPackage[]) pkgs.toArray(new IPackage[pkgs.size()]);
  }

  public static IPackage[] getPackages(IBundleProject project) {
    ArrayList pkgs = new ArrayList();
    try {
      PackageDescription[] packageDescriptions = project.getBundleManifest().getExportedPackages();
      for (int i=0; i<packageDescriptions.length; i++) {
        pkgs.add(new ProjectPackage(packageDescriptions[i], project));
      }
    } catch (Throwable t) {}
    return (IPackage[]) pkgs.toArray(new IPackage[pkgs.size()]);
  }
  
  
  
  public static IOsgiBundle[] findExportingBundles(PackageDescription pkg) {
    if (pkg == null) return null;
    ArrayList exportingBundles = new ArrayList(); 
    
    // Knopflerfish root
    Framework[] distributions = OsgiPreferences.getFrameworks();
    
    for(int i=0; i<distributions.length; i++) {
      IOsgiBundle[] bundles = distributions[i].getBundles();
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
  */
  
}
