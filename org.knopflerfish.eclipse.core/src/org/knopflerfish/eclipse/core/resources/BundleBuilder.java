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

package org.knopflerfish.eclipse.core.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class BundleBuilder extends IncrementalProjectBuilder {

  /* (non-Javadoc)
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
      throws CoreException {
    return null;
    /*
    System.err.println("BundleBuilder - build");
    IProject project = getProject();
    //description.
    IWorkspace workspace = project.getWorkspace();
    //workspace.
    //IWorkspaceDescription description = workspace.getDescription();
    //description.
    IJavaProject javaProject = JavaCore.create(project);
    IPath out = javaProject.getOutputLocation();
    //IPath out2 = javaProject.readOutputLocation();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IFolder folder = root.getFolder(out);
    System.err.println("Folder :"+folder);
    
    try {
      Manifest manifest = new Manifest();
      Attributes attributes = manifest.getMainAttributes();
      attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
      createJar("bundle.jar", manifest, folder);
    } catch(IOException e) {
      e.printStackTrace();
    }
    return null;
    */
  }
  
  void createJar(String jarName, Manifest manifest, IFolder out) throws IOException {
    
    JarOutputStream jos = null;
    try {
      File jarFile = new File(out.getFile(jarName).getRawLocation().toString());
      jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);

      // Put class-files
      File outFolder = new File(out.getRawLocation().toString());
      File [] classFiles = outFolder.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".class");
        }
        
      });
      if (classFiles != null) {
        for (int i=0; i<classFiles.length; i++) {
          
        }
      }
      
      System.err.println("done");
      
    } finally {
      if (jos != null) {
        jos.flush();
        jos.finish();
        jos.close();
      }
    }
    
    
  }
}
