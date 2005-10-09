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
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleContainerInitializer extends ClasspathContainerInitializer {

  /****************************************************************************
   * org.eclipse.jdt.core.ClasspathContainerInitializer methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
   */
  public void initialize(IPath path, IJavaProject project) throws CoreException {
    
    BundleIdentity id = getBundleIdentity(path);
    if (id == null) {
      // Remove from entry from project
      try {
        ArrayList entries = new ArrayList(Arrays.asList(project.getRawClasspath()));
        for(Iterator i=entries.iterator();i.hasNext();) {
          // Find framework container
          IClasspathEntry entry = (IClasspathEntry) i.next();
          if (entry.getPath().equals(path)) {
            i.remove();
          }
        }
        project.setRawClasspath(
            (IClasspathEntry []) entries.toArray(new IClasspathEntry[entries.size()]),
            null);
      } catch (Throwable t) {}
      return;
    }

    BundleContainer container = new BundleContainer(path, id);
    JavaCore.setClasspathContainer(
        path, 
        new IJavaProject[] {project}, 
        new IClasspathContainer[] {container},
        null);        
  }
  
  /****************************************************************************
   * Utility methods
   ***************************************************************************/
  
  public static BundleIdentity getBundleIdentity(IPath path) {
    String s = path.toString();
    try {
      if (s.startsWith(BundleContainer.CONTAINER_PATH)) {
        String id = s.substring(BundleContainer.CONTAINER_PATH.length()+1);
        return new BundleIdentity(id);
      }
    } catch (Throwable t) {
    }
    return null;
  }
}
