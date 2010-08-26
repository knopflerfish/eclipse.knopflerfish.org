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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public interface IBundleProject {
  
  // Constant file names
  public static final String CLASSPATH_FILE = ".classpath";
  public static final String MANIFEST_FILE  = "bundle.manifest";
  public static final String BUNDLE_PACK_FILE = ".bundle-pack";

  /**
   * Returns the a unique bundle identifier 
   * for this bundle project. Bundle identifer 
   * consists of the symbolic name and version.
   * 
   * @return bundle identifier
   * 
   * @throws CoreException if failure reading id 
   * from bundle manifest
   */
  public BundleIdentity getId() throws CoreException;

  /**
   * Returns the IProject on which this IBundleProject was created.
   * 
   * @return the IProject on which this IBundleProject was created
   */
  public IProject getProject();

  /**
   * Returns the java project associated with this
   * bundle project.
   * 
   * @return java project
   */
  public IJavaProject getJavaProject();
  
  /**
   * Returns the bundle manifest for this bundle project.
   * 
   * @return bundle manifest
   * 
   * @throws CoreException if failure reading 
   * bundle manifest
   */
  public BundleManifest getBundleManifest();
  
  /**
   * Sets the bundle manifest for this bundle project.
   * 
   * @param manifest bundle manifest
   * 
   * @throws CoreException if failure setting 
   * bundle manifest
   */
  public void setBundleManifest(BundleManifest manifest) throws CoreException;

  /**
   * Returns the pack description for this bundle project.
   * 
   * @return bundle pack description
   * 
   * @throws CoreException if failure reading 
   * pack description
   */
  public BundlePackDescription getBundlePackDescription() throws CoreException;
  
  /**
   * Sets the pack description for this bundle project.
   * 
   * @param packDescription bundle pack description
   * 
   * @throws CoreException if failure setting 
   * pack description
   */
  public void setBundlePackDescription(BundlePackDescription packDescription) throws CoreException;

  /**
   * Returns all packages available for export in this
   * project.
   * 
   * @return array of package names
   * 
   * @throws JavaModelException if failure getting 
   * package names
   */
  public String[] getExportablePackageNames() throws JavaModelException;
  
  /**
   * Returns all packages referenced from import declarations
   * in this project.
   * 
   * @return array of package names
   * 
   * @throws JavaModelException if failure getting 
   * package names
   */
  public String[] getReferencedPackageNames() throws JavaModelException;

  /**
   * Returns all implementations of BundleActivator in this
   * project.
   * 
   * @return array of BundleActivator implementations
   * 
   * @throws JavaModelException if failure getting 
   * activators
   */
  public IType[] getBundleActivators() throws JavaModelException;
}
