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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.preferences.Framework;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkContainer extends ClasspathContainerInitializer  implements IClasspathContainer {
  public static final String CONTAINER_PATH = "org.knopflerfish.eclipse.core.FRAMEWORK_CONTAINER";
  
  private Framework distribution;
  
  public FrameworkContainer() {
  }
  
  public FrameworkContainer(Framework distribution) {
    this.distribution = distribution;
  }
  
  /****************************************************************************
   * org.eclipse.jdt.core.ClasspathContainerInitializer methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
   */
  public void initialize(IPath containerPath, IJavaProject project) throws CoreException {

    // Get hinted framework distribution
    String hint = containerPath.lastSegment();
    distribution = null;
    if (hint != null) {
      distribution = OsgiPreferences.getFramework(hint);
    }
    if (distribution == null) {
      distribution = OsgiPreferences.getDefaultFramework();
    }
    
    // Create classpath container
    JavaCore.setClasspathContainer(
        containerPath, 
        new IJavaProject[] {project}, 
        new IClasspathContainer[] {this},
        null);        
  }

  /****************************************************************************
   * org.eclipse.jdt.core.IClasspathContainer methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
   */
  public IClasspathEntry[] getClasspathEntries() {
    ArrayList classPath = new ArrayList();

    if (distribution != null) {
      IOsgiLibrary [] libraries = distribution.getRuntimeLibraries();
      for (int i=0; i<libraries.length; i++) {
        Path path = new Path(libraries[i].getPath());
        Path src = null;
        if (libraries[i].getSource() != null) {
          try {
            src = new Path(libraries[i].getSource());
          } catch (Exception e) {
          }
        }
        IClasspathEntry entry = JavaCore.newLibraryEntry(path, src, null, false);
        
        classPath.add(entry);
      }
    }
    
    return (IClasspathEntry[]) classPath.toArray(new IClasspathEntry[classPath.size()]); 
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
   */
  public String getDescription() {
    StringBuffer buf = new StringBuffer("Framework");
  
    if (distribution != null) {
      buf.append(" [");
      buf.append(distribution.getName());
      buf.append("]");
    }
    return buf.toString();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
   */
  public int getKind() {
    return IClasspathContainer.K_SYSTEM;
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
   */
  public IPath getPath() {
    StringBuffer buf = new StringBuffer(CONTAINER_PATH);
    
    if (distribution != null) {
      buf.append("/");
      buf.append(distribution.getName());
    }
    return new Path(buf.toString());
  }
}
