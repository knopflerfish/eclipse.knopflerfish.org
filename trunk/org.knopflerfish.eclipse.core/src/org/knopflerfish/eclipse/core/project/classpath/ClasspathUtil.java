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
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.preferences.ExecutionEnvironment;
import org.knopflerfish.eclipse.core.preferences.FrameworkDistribution;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ClasspathUtil {
  public static final String TYPE      = "type";
  public static final String FRAMEWORK = "framework";
  public static final String BUNDLE    = "bundle";
  
  public static String getClasspathType(IClasspathEntry entry) {
    return getClasspathAttribute(entry, TYPE);
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
  
  public static void updateEnvironmentContainers() {
    try {
      ExecutionEnvironment[] environments = OsgiPreferences.getExecutionEnvironments();
      
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
      for (int i=0; i<environments.length; i++) {
        ExecutionEnvironment environment = environments[i];
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
      FrameworkDistribution[] distributions = OsgiPreferences.getFrameworkDistributions();
      
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
        FrameworkDistribution distribution = distributions[i];
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