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

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.Path;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.pkg.PackageUtil;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.classpath.FrameworkContainer;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ImportPackageModel {

  private final BundleProject project;
  private ArrayList paths = new ArrayList();

  public ImportPackageModel(BundleManifest manifest, BundleProject project) {
    this.project = project;
    setManifest(manifest);
  }
  
  public void setManifest(BundleManifest manifest) {
    paths.clear();
    if (manifest == null) return;
    
    // Link import packages to project containers
    PackageDescription[] importedPackages = manifest.getImportedPackages();
    for(int i=0; i<importedPackages.length; i++) {
      paths.add(project.getBuildPath(importedPackages[i]));
    }
  }
  
  public void updateManifest(BundleManifest manifest) {
    if (manifest == null) return;
    
    ArrayList importedPackages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
    
    for(Iterator i=paths.iterator(); i.hasNext(); ) {
      BuildPath path = (BuildPath) i.next();
      PackageDescription packageDescription =  path.getPackageDescription();
      if (!importedPackages.remove(packageDescription)) {
        i.remove();
      }
    }
    
    for(Iterator i=importedPackages.iterator(); i.hasNext();) {
      PackageDescription pd = (PackageDescription) i.next();
      BuildPath bp = project.getBuildPath(pd);
      boolean hasExportedPackage = project.hasExportedPackage(pd);

      // Get default bundle for package
      
      // Check if framework exports package
      if (!hasExportedPackage && bp.getContainerPath() == null) {
        if (PackageUtil.frameworkExportsPackage(project, bp.getPackageDescription())) {
          bp.setBundleName("Framework");
          bp.setContainerPath(new Path(FrameworkContainer.CONTAINER_PATH));
        }
      }
      
      // Check bundle entries from projects
      if (!hasExportedPackage && bp.getContainerPath() == null) {
        BuildPath [] projectPaths = PackageUtil.getExportingProjectBundles(bp.getPackageDescription());
        if (projectPaths != null && projectPaths.length > 0) {
          bp = projectPaths[0];
        }
      }
      // Check bundle entries from repositories
      if (!hasExportedPackage && bp.getContainerPath() == null) {
        BuildPath [] projectPaths = PackageUtil.getExportingRepositoryBundles(bp.getPackageDescription());
        if (projectPaths != null && projectPaths.length > 0) {
          bp = projectPaths[0];
        }
      }
      
      paths.add(bp);
    }
    
  }
  
  public BuildPath[] getPaths() {
    return (BuildPath[]) paths.toArray(new BuildPath[paths.size()]);
  }
  
}
