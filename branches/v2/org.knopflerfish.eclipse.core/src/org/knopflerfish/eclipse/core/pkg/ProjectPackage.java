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

package org.knopflerfish.eclipse.core.pkg;

import org.knopflerfish.eclipse.core.IBundleProject;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ProjectPackage implements IPackage {
  
  private final PackageDescription packageDescription;
  private final IBundleProject project;
  
  public ProjectPackage(PackageDescription packageDescription, IBundleProject project) {
    this.packageDescription = packageDescription;
    this.project = project;
  }

  public IBundleProject getProject() {
    return project;
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.pkg.IPackage methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.pkg.IPackage#getType()
   */
  public int getType() {
    return PROJECT;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.pkg.IPackage#getPackageDescription()
   */
  public PackageDescription getPackageDescription() {
    return packageDescription;
  }

}
