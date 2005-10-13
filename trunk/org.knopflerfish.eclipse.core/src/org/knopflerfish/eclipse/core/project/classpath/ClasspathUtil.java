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
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ClasspathUtil {

  public static final String ATTR_BUNDLENAME = "bundle-name";
  
  public static IAccessRule createAccessRule(PackageDescription packageDescription){
    String pattern = packageDescription.getPackageName().replace('.', '/');
    if (!pattern.endsWith("*") && !pattern.endsWith("/")) {
      pattern = pattern + "/";
    }
    IAccessRule rule = JavaCore.newAccessRule(new Path(pattern), IAccessRule.K_ACCESSIBLE);
    return rule;
  }

  public static PackageDescription createPackageDescription(IClasspathEntry entry, IAccessRule rule) {
    // Package name
    String pattern = rule.getPattern().toString();
    String packageName = pattern.replace('/', '.');
    if (packageName.endsWith(".")) {
      packageName = packageName.substring(0, packageName.length()-1);
    }
    if (packageName.startsWith(".")) {
      packageName = packageName.substring(1);
    }
    // Version
    Version version = Version.emptyVersion;
    try {
      version = Version.parseVersion(getClasspathAttribute(entry, packageName));
    } catch (IllegalArgumentException e) {
      version = Version.emptyVersion;
    }
    
    return new PackageDescription(packageName, version);
  }

  public static IClasspathEntry findClasspathEntry(IJavaProject project, IAccessRule rule) {
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

  public static IClasspathEntry findClasspathEntry(IJavaProject project, IPath path) {
    try {
      IClasspathEntry[] entries = project.getRawClasspath();
      for(int i=0; i<entries.length; i++) {
        // Find container exporting this package
        if (path.equals(entries[i].getPath())) {
          return entries[i];
        }
      }
    } catch (Throwable t) {}
    
    return null;
  }
  
  public static PackageDescription[] getPackages(IClasspathEntry entry) {
    if (entry == null) return null;
    
    ArrayList packages = new ArrayList();
    
    IAccessRule[] rules = entry.getAccessRules();
    for (int i=0; i<rules.length; i++) {
      if (rules[i].getKind() == IAccessRule.K_ACCESSIBLE) {
        PackageDescription pd = ClasspathUtil.createPackageDescription(entry, rules[i]);
        packages.add(pd);
      }
    }
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }
  
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

  /****************************************************************************
   * Methods for updating classpath containers
   ***************************************************************************/
  
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
