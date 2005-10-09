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
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ClasspathUtil {
  /*
  public static final String ATTR_SYMBOLICNAME = "symbolic-name";
  public static final String ATTR_BUNDLEVERSION = "bundle-version";
  */
  public static final String ATTR_BUNDLENAME = "bundle-name";
  /*
  public static final String TYPE      = "type";
  public static final String FRAMEWORK = "framework";
  public static final String BUNDLE    = "bundle";
  public static final String PROJECT   = "project";
  */
  
  public static IAccessRule createAccessRule(PackageDescription packageDescription){
    String pattern = packageDescription.getPackageName().replace('.', '/');
    if (!pattern.endsWith("*") && !pattern.endsWith("/")) {
      pattern = pattern + "/";
    }
    IAccessRule rule = JavaCore.newAccessRule(new Path(pattern), IAccessRule.K_ACCESSIBLE);
    return rule;
  }

  public static PackageDescription createPackageDescription(IAccessRule rule) {
    String pattern = rule.getPattern().toString();
    String packageName = pattern.replace('/', '.');
    if (packageName.endsWith(".")) {
      packageName = packageName.substring(0, packageName.length()-1);
    }
    if (packageName.startsWith(".")) {
      packageName = packageName.substring(1);
    }
    return new PackageDescription(packageName, null);
  }

  public static IClasspathEntry getClasspathEntry(PackageDescription packageDescription, IJavaProject project) {
    IClasspathEntry entry;
    
    // Check if entry already exist
    IAccessRule rule = ClasspathUtil.createAccessRule(packageDescription);
    entry = findClasspathEntry(packageDescription, project, rule);
    if (entry != null) {
      return entry;
    }
    /*
    // Loop through existing entries and find containers capable of exporting package
    Map map = findClasspathEntry(packageDescription, project);
    List entries = (List) map.get(ClasspathUtil.FRAMEWORK);
    if (entries.size() > 0) {
      return (IClasspathEntry) entries.get(0);
    }
    entries = (List) map.get(ClasspathUtil.PROJECT);
    if (entries.size() > 0) {
      return (IClasspathEntry) entries.get(0);
    }
    entries = (List) map.get(ClasspathUtil.BUNDLE);
    if (entries.size() > 0) {
      return (IClasspathEntry) entries.get(0);
    }
    */
    
    return null;
  }
  
  public static IClasspathEntry findClasspathEntry(PackageDescription packageDescription, IJavaProject project, IAccessRule rule) {
    try {
      IClasspathEntry[] entries = project.getRawClasspath();
      for(int i=0; i<entries.length; i++) {
        // Find container exporting this package
        IPath path = entries[i].getPath();
        if (path.toString().startsWith(FrameworkContainer.CONTAINER_PATH) || 
            path.toString().startsWith(BundleContainer.CONTAINER_PATH)) {
          // Check access rules
          IAccessRule[] rules = entries[i].getAccessRules();
          for(int j=0; j<rules.length; j++) {
            if (rule.equals(rules[j])) {
              return entries[i];
            }
          }
        }
      }
    } catch (Throwable t) {}
    
    return null;
  }

  /*
  public static Map findClasspathEntry(PackageDescription packageDescription, IJavaProject project) {
    HashMap entries = new HashMap();
    ArrayList frameworkEntries = new ArrayList(); 
    ArrayList bundleEntries = new ArrayList(); 
    ArrayList projectEntries = new ArrayList();
    entries.put(ClasspathUtil.FRAMEWORK, frameworkEntries);
    entries.put(ClasspathUtil.BUNDLE, bundleEntries);
    entries.put(ClasspathUtil.PROJECT, projectEntries);
    try {
      IClasspathEntry[] rawClasspath = project.getRawClasspath();
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
          
          IFrameworkDefinition framework = Osgi.getFrameworkDefinition(distribution.getType());
          PackageDescription[] exportedPackages = framework.getExportedPackages(distribution.getRuntimeLibraries());
          for (int j=0; j<exportedPackages.length; j++) {
            if (exportedPackages[j].isCompatible(packageDescription)) {
              frameworkEntries.add(rawClasspath[i]);
              break;
            }
          }
        } else if (containerPath.toString().startsWith(BundleContainer.CONTAINER_PATH)) {
          // Find bundle
          OsgiBundle bundle = new OsgiBundle(new File(hint));
          if (bundle.hasExportedPackage(packageDescription)) {
            bundleEntries.add(rawClasspath[i]);
          }
        }
      }
    } catch (Throwable t) {}
    
    return entries;
  }
  */
  
  /*
  public static String getClasspathType(IClasspathEntry entry) {
    return getClasspathAttribute(entry, TYPE);
  }
  */
  
  /*
  public static BundleIdentity getClasspathBundle(IClasspathEntry entry) {
    BundleIdentity bundleIdentity = null;
    
    String symbolicName  = getClasspathAttribute(entry, ATTR_SYMBOLICNAME);
    if (symbolicName != null) {
      Version version = Version.parseVersion(getClasspathAttribute(entry, ATTR_BUNDLEVERSION));
      String name = getClasspathAttribute(entry, ATTR_BUNDLENAME);
      bundleIdentity = new BundleIdentity(new SymbolicName(symbolicName), version, name);
    }
    
    return bundleIdentity;
  }
  */
  
  public static String getClasspathAttribute(IClasspathEntry entry, String attribute) {
    if (entry == null || attribute == null) return null;
    
    IClasspathAttribute[] attributes = entry.getExtraAttributes();
    if (attributes == null) return null;
    
    for(int i=0; i<attributes.length; i++) {
      if (attribute.equals(attributes[i].getName())) {
        return attributes[i].getValue();
      }
    }
    
    return null;
  }
  
  public static void updateEnvironmentContainers() {
    try {
      EnvironmentPreference[] environments = OsgiPreferences.getExecutionEnvironments();
      
      // Get bundle projects
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject [] projects = root.getProjects();
      ArrayList projectList = new ArrayList();
      for(int i=0; projects != null && i<projects.length; i++) {
        if (projects[i].isOpen() && projects[i].hasNature(Osgi.NATURE_ID)) {
          projectList.add(JavaCore.create(projects[i]));
        }
      }
      
      IJavaProject [] javaProjects = (IJavaProject[]) projectList.toArray(new IJavaProject[projectList.size()]);
      IClasspathContainer [] containers = new IClasspathContainer[projectList.size()];
      
      // Update containers
      for (int i=0; i<environments.length; i++) {
        EnvironmentPreference environment = environments[i];
        IClasspathContainer container = new ExecutionEnvironmentContainer(environment);
        Arrays.fill(containers, container);
        IPath path = new Path(ExecutionEnvironmentContainer.CONTAINER_PATH);
        if (environment.isDefaultEnvironment()) {
          JavaCore.setClasspathContainer(path, javaProjects, containers, null);
        }
        path = path.append(environment.getName());
        JavaCore.setClasspathContainer(path, javaProjects, containers, null);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public static void updateFrameworkContainers() {
    try {
      FrameworkPreference[] distributions = OsgiPreferences.getFrameworks();
      
      // Get bundle projects
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject [] projects = root.getProjects();
      ArrayList projectList = new ArrayList();
      for(int i=0; projects != null && i<projects.length; i++) {
        if (projects[i].hasNature(Osgi.NATURE_ID)) {
          projectList.add(JavaCore.create(projects[i]));
        }
      }
      
      IJavaProject [] javaProjects = (IJavaProject[]) projectList.toArray(new IJavaProject[projectList.size()]);
      IClasspathContainer [] containers = new IClasspathContainer[projectList.size()];
      
      // Update containers
      for (int i=0; i<distributions.length; i++) {
        FrameworkPreference distribution = distributions[i];
        IClasspathContainer container = new FrameworkContainer(distribution);
        Arrays.fill(containers, container);
        IPath path = new Path(FrameworkContainer.CONTAINER_PATH);
        if (distribution.isDefaultDefinition()) {
          JavaCore.setClasspathContainer(path, javaProjects, containers, null);
        }
        path = path.append(distribution.getName());
        JavaCore.setClasspathContainer(path, javaProjects, containers, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}
