/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
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
import java.util.List;
import java.util.Map;
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
import org.knopflerfish.eclipse.core.manifest.SymbolicName;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class BundleRepository implements IBundleRepository {

  private final String name;

  private List<PackageDescription> packages = new ArrayList<PackageDescription>();
  private Map<String, List<String>> symbolicNames = new HashMap<String, List<String>>();
  private Map<String, List<String>> libraries = new HashMap<String, List<String>>();
  private List<BundleManifest> manifests = new ArrayList<BundleManifest>();

  public BundleRepository(String name)
  {

    this.name = name;

    // Initialize caches
    FrameworkPreference frameworkPreference = OsgiPreferences
        .getFramework(name);
    IOsgiBundle[] bundles = frameworkPreference.getBundles();
    for (int i = 0; i < bundles.length; i++) {
      BundleManifest bm = bundles[i].getBundleManifest();
      // Bundles, skip bundles with no symbolic name
      SymbolicName symbolicName = bm.getSymbolicName();
      // Use name if symbolic name is not set
      if (symbolicName == null && bm.getName() != null) {
        symbolicName = new SymbolicName(bm.getName());
      }
      // Skip if Symbolic name is not set
      if (symbolicName == null)
        continue;

      List<String> paths = symbolicNames.get(symbolicName.getSymbolicName());
      if (paths == null) {
        paths = new ArrayList<String>();
      }
      paths.add(bundles[i].getPath());
      symbolicNames.put(symbolicName.getSymbolicName(), paths);

      BundleIdentity id = new BundleIdentity(symbolicName, bm.getVersion());
      // Manifests
      manifests.add(bm);
      // Packages
      this.packages.addAll(Arrays.asList(bm.getExportedPackages()));
      // Check if this bundles has any more libraries to extract
      String[] classPaths = bm.getBundleClassPath();
      try {
        JarFile jarFile = null;
        try {
          jarFile = new JarFile(bundles[i].getPath());
          List<String> libs = new ArrayList<String>();
          for (int j = 0; j < classPaths.length; j++) {
            if (".".equals(classPaths[j]))
              continue;

            ZipEntry entry = jarFile.getEntry(classPaths[j]);
            if (entry != null) {
              InputStream is = jarFile.getInputStream(entry);
              String path = RepositoryPlugin.getDefault().storeFile(is,
                  classPaths[j], id);
              if (path != null) {
                libs.add(path);
              }
              is.close();
            }
          }

          libraries.put(bundles[i].getPath(), libs);
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
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IBundleRepository#getExportedPackages()
   */
  public PackageDescription[] getExportedPackages()
  {
    return packages.toArray(new PackageDescription[packages.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IBundleRepository#getExportingBundles(org
   * .knopflerfish.eclipse.core.manifest.PackageDescription)
   */
  public BundleManifest[] getExportingBundles(PackageDescription pd)
  {
    List<BundleManifest> manifests = new ArrayList<BundleManifest>();
    for (BundleManifest manifest : this.manifests) {
      if (manifest.hasExportedPackage(pd)) {
        manifests.add(manifest);
      }
    }
    return manifests.toArray(new BundleManifest[manifests.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IBundleRepository#getBundleLibraries(org.
   * knopflerfish.eclipse.core.manifest.SymbolicName,
   * org.knopflerfish.eclipse.core.manifest.PackageDescription[])
   */
  public IOsgiLibrary[] getBundleLibraries(SymbolicName symbolicName,
                                           PackageDescription[] packages)
  {
    if (symbolicName == null)
      return null;

    List<String> paths = symbolicNames.get(symbolicName.getSymbolicName());
    if (paths == null || paths.size() == 0)
      return null;

    for (String path : paths) {
      try {
        OsgiBundle bundle = new OsgiBundle(new File(path));
        boolean hasPackages = true;
        for (int j = 0; packages != null && j < packages.length; j++) {
          if (!bundle.hasExportedPackage(packages[j])) {
            hasPackages = false;
            break;
          }
        }
        if (hasPackages) {
          List<IOsgiLibrary> libs = new ArrayList<IOsgiLibrary>();
          libs.add(bundle);
          List<String> libraryPaths = libraries.get(path);
          if (libraryPaths != null) {
            for (String libraryPath : libraryPaths) {
              try {
                libs.add(new OsgiLibrary(new File(libraryPath)));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          return libs.toArray(new IOsgiLibrary[libs.size()]);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IBundleRepository#getBundles()
   */
  public IOsgiBundle[] getBundles()
  {
    FrameworkPreference frameworkPreference = OsgiPreferences
        .getFramework(name);
    return frameworkPreference.getBundles();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IBundleRepository#getPackageVersions(java
   * .lang.String)
   */
  public Version[] getPackageVersions(String packageName)
  {
    if (packageName == null)
      return null;
    ArrayList<Version> versions = new ArrayList<Version>();

    for (PackageDescription pd : packages) {
      if (packageName.equals(pd.getPackageName())) {
        if (!versions.contains(pd.getVersion())) {
          versions.add(pd.getVersion());
        }
      }
    }

    return versions.toArray(new Version[versions.size()]);
  }

}
