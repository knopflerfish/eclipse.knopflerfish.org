/*
 * Copyright (c) 2003-2010, KNOPFLERFISH project
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

package org.knopflerfish.eclipse.core.ui.launcher.bundle;

import java.util.List;

import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class SelectedBundleElement {
  static public int TYPE_BUNDLE_PROJECT = 0;
  static public int TYPE_BUNDLE = 1;

  private IOsgiBundle bundle;
  private IBundleProject project;
  private final BundleLaunchInfo launchInfo;
  private PackageDescription[] missingPackages;

  public SelectedBundleElement(IOsgiBundle bundle, BundleLaunchInfo info)
  {
    // Bundle
    this.bundle = bundle;
    this.launchInfo = info;
  }

  public SelectedBundleElement(IBundleProject project, BundleLaunchInfo info)
  {
    // Bundle Project
    this.project = project;
    this.launchInfo = info;
  }

  public BundleLaunchInfo getLaunchInfo()
  {
    return launchInfo;
  }

  public IOsgiBundle getBundle()
  {
    return bundle;
  }

  public String getPath()
  {
    if (bundle != null) {
      return bundle.getPath();
    }
    return project.getJavaProject().getProject().getName();
  }

  public int getType()
  {
    if (bundle != null) {
      return TYPE_BUNDLE;
    }
    return TYPE_BUNDLE_PROJECT;
  }

  public String getName()
  {
    if (bundle != null) {
      return bundle.getName();
    }
    return project.getBundleManifest().getName();
  }

  public Version getVersion()
  {
    if (bundle != null) {
      if (bundle.getBundleManifest() == null)
        return null;
      return bundle.getBundleManifest().getVersion();
    }
    return project.getBundleManifest().getVersion();
  }

  public PackageDescription[] getImportedPackages()
  {
    if (bundle != null) {
      if (bundle.getBundleManifest() == null)
        return null;
      return bundle.getBundleManifest().getImportedPackages();
    }
    return project.getBundleManifest().getImportedPackages();
  }

  public PackageDescription[] getExportedPackages()
  {
    if (bundle != null) {
      if (bundle.getBundleManifest() == null)
        return null;
      return bundle.getBundleManifest().getExportedPackages();
    }
    return project.getBundleManifest().getExportedPackages();
  }

  public void setMissingPackages(List<PackageDescription> p)
  {
    if (p != null) {
      missingPackages = p.toArray(new PackageDescription[p.size()]);
    } else {
      missingPackages = new PackageDescription[0];
    }
  }

  public PackageDescription[] getMissingPackages()
  {
    return missingPackages;
  }

  public String toString()
  {
    return getName();
  }

}
