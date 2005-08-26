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

package org.knopflerfish.eclipse.core;

import java.io.File;
import java.io.IOException;

import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiBundle extends OsgiLibrary implements IOsgiBundle {

  private BundleManifest bundleManifest;

  public OsgiBundle(File jar) throws IOException {
    super(jar);

    if (getManifest() != null) {
      bundleManifest = new BundleManifest(getManifest());
    }
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IOsgiBundle methods
   ***************************************************************************/

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getBundleManifest()
   */
  public BundleManifest getBundleManifest() {
    return bundleManifest;
  }
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#hasExportedPackage(org.knopflerfish.eclipse.core.PackageDescription)
   */
  public boolean hasExportedPackage(PackageDescription pkg) {
    PackageDescription [] exportedPackages = bundleManifest.getExportedPackages();
    for (int i=0; i<exportedPackages.length; i++) {
      if (exportedPackages[i].isCompatible(pkg)) return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#hasCategory(java.lang.String)
   */
  public boolean hasCategory(String cat) {
    String [] categories = null;
    if (bundleManifest != null) {
      categories = bundleManifest.getCategories();
    }
    if (cat == null || categories == null) return false;

    for (int i=0; i<categories.length; i++) {
      if (cat.equals(categories[i])) return true;
    }

    return false;
  }
  
  public String getDescription()
  {
  	return bundleManifest.getDescription();
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if(obj == null || !(obj instanceof OsgiBundle)) return false;
    
    return ((OsgiBundle) obj).getPath().equals(getPath());
  }
}
