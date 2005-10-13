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

package org.knopflerfish.eclipse.core.project;

import org.eclipse.core.runtime.IPath;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BuildPath {
  
  private IPath containerPath;
  private PackageDescription packageName;
  private BundleIdentity bundleIdentity;
  private String bundleName;
  
  public BuildPath(IPath path, PackageDescription pd, BundleIdentity id, String name) {
    containerPath = path;
    packageName = pd;
    bundleIdentity = id;
    bundleName = name;
  }
  
  /****************************************************************************
   * Getters and setters
   ***************************************************************************/
  
  public IPath getContainerPath() {
    return containerPath;
  }
  
  public void setContainerPath(IPath path) {
    containerPath = path;
  }
  
  public PackageDescription getPackageDescription() {
    return packageName;
  }
  
  public void setPackageDescription(PackageDescription description) {
    packageName = description;
    
  }
  public BundleIdentity getBundleIdentity() {
    return bundleIdentity;
  }
  
  public void setBundleIdentity(BundleIdentity id) {
    bundleIdentity = id; 
  }
  
  public String getBundleName() {
    return bundleName;
  }
  
  public void setBundleName(String name) {
    bundleName = name;
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BuildPath)) return false;
    
    BuildPath bp = (BuildPath) o;
    boolean equal = ((containerPath == null && bp.containerPath == null ) || 
        (containerPath != null && containerPath.equals(bp.containerPath))) &&
        ((packageName == null && bp.packageName == null ) || 
            (packageName != null && packageName.equals(bp.packageName))) &&
        ((bundleName == null && bp.bundleName == null ) || 
            (bundleName != null && bundleName.equals(bp.bundleName)));
    
    return equal;
  }

  /*
   *  (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Path [");
    if (containerPath != null) {
      buf.append(containerPath.toString());
    }
    buf.append("] Package [");
    if (packageName != null) {
      buf.append(packageName);
    }
    buf.append("]");
    return buf.toString();
  }

}
