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

package org.knopflerfish.eclipse.repository.framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleRepository implements IBundleRepository {

  private ArrayList packages = new ArrayList();
  private HashMap bundles = new HashMap(); // BundleIdentity, String
  private HashMap libraries = new HashMap(); // BundleIdentity, ArrayList of String
  private ArrayList manifests = new ArrayList(); // BundleIdentity, String
  
  public BundleRepository(String name) {
    
    // Initialize caches
    FrameworkPreference frameworkPreference = OsgiPreferences.getFramework(name);
    IOsgiBundle [] bundles = frameworkPreference.getBundles();
    for (int i=0; i<bundles.length; i++) {
      BundleManifest bm = bundles[i].getBundleManifest();
      // Manifests
      manifests.add(bm);
      // Packages 
      this.packages.addAll(Arrays.asList(bm.getExportedPackages()));
      // Bundles
      BundleIdentity id = new BundleIdentity(bm.getSymbolicName(), bm.getVersion());
      this.bundles.put(id, bundles[i].getPath());
      // Check if this bundles has any more libraries to extract
      String[] paths = bm.getBundleClassPath();
      try {
        JarFile jarFile = null;
        try {
          jarFile = new JarFile(bundles[i].getPath());
          ArrayList libs = new ArrayList();
          for (int j=0; j<paths.length; j++) {
            if (".".equals(paths[j])) continue;
            
            ZipEntry entry = jarFile.getEntry(paths[j]);
            if (entry != null) {
              InputStream is = jarFile.getInputStream(entry);
              String path = RepositoryPlugin.getDefault().storeFile(is, paths[j], id);
              if (path != null) {
                libs.add(path);
              }
              is.close();
            }
          }
          
          libraries.put(id, libs);
        } finally {
          if (jarFile != null) {
            jarFile.close();
            jarFile = null;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleRepositoryType methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepository#getExportedPackages()
   */
  public PackageDescription[] getExportedPackages() {
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepository#getExportingBundles(org.knopflerfish.eclipse.core.manifest.PackageDescription)
   */
  public BundleManifest[] getExportingBundles(PackageDescription pd) {
    ArrayList manifests = new ArrayList(); 
    for (Iterator i=this.manifests.iterator(); i.hasNext();) {
      BundleManifest manifest = (BundleManifest) i.next();
      if (manifest.hasExportedPackage(pd)) {
        manifests.add(manifest);
      }
    }
    return (BundleManifest[]) manifests.toArray(new BundleManifest[manifests.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepository#getBundleLibraries(org.knopflerfish.eclipse.core.manifest.BundleIdentity)
   */
  public IOsgiLibrary[] getBundleLibraries(BundleIdentity id) {

    ArrayList libs =  new ArrayList();
    String bundlePath = (String) bundles.get(id);
    if (bundlePath != null) {
      try {
        libs.add(new OsgiBundle(new File(bundlePath)));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    ArrayList paths = (ArrayList) libraries.get(id);
    if (paths != null) {
      for (Iterator i=paths.iterator(); i.hasNext();) {
        String libraryPath = (String) i.next();
        try {
          libs.add(new OsgiLibrary(new File(libraryPath)));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return (IOsgiLibrary[]) libs.toArray(new IOsgiLibrary[libs.size()]);
  }

}
