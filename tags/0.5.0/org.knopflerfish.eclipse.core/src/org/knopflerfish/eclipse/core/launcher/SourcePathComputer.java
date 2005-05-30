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

package org.knopflerfish.eclipse.core.launcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathContainerSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.IOsgiVendor;
import org.knopflerfish.eclipse.core.Osgi;

/**
 * @author ar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SourcePathComputer extends JavaSourcePathComputer implements ISourcePathComputer {
  public static String ID="org.knopflerfish.eclipse.core.launcher.SourcePathComputer";

  public SourcePathComputer() {
    System.err.println("SourcePathComputer");
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputer#getId()
   */
  public String getId() {
    System.err.println("SourcePathComputer:getId()");
    return ID;
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
   */
  public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
    System.err.println("SourcePathComputer:computeSourceContainers()");
    ISourceContainer[] containers = super.computeSourceContainers(configuration, monitor);

    ArrayList containerList = new ArrayList();
    if (containers != null) {
      for (int i=0; i<containers.length;i++) {
        containerList.add(containers[i]);
      }
    }
    
    // Add project source code
    Map map = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, (Map) null);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (Iterator i = map.keySet().iterator();i.hasNext();) {
      String name = (String) i.next();
      IProject project = root.getProject(name);
      IJavaProject javaProject = JavaCore.create(project);
      containerList.add(new JavaProjectSourceContainer(javaProject));
    }
    
    // Add framework libraries source code
    String vendorName = OsgiLaunchDelegate.getOsgiVendorName(configuration);
    IOsgiVendor vendor = Osgi.getVendor(vendorName);
    String installName = OsgiLaunchDelegate.getOsgiInstallName(configuration);
    IOsgiInstall osgiInstall = vendor.getOsgiInstall(installName);
    IOsgiLibrary [] libraries = osgiInstall.getLibraries();
    if (libraries != null) {
      for (int i=0; i<libraries.length;i++) {
        String src = libraries[i].getSourceDirectory();
        if (src != null) {
          Path path = new Path(src);
          containerList.add(new DirectorySourceContainer(path, true));
        }
      }
    }
    
    return (ISourceContainer []) containerList.toArray(new ISourceContainer[containerList.size()]);
  }

}
