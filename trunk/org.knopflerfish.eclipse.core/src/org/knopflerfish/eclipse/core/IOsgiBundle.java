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

/**
 * @author ar
 */
public interface IOsgiBundle extends IOsgiLibrary {

  public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
  public static final String BUNDLE_NAME          = "Bundle-Name";
  public static final String BUNDLE_VERSION       = "Bundle-Version";
  public static final String BUNDLE_ACTIVATOR     = "Bundle-Activator";
  public static final String BUNDLE_VENDOR        = "Bundle-Vendor";
  public static final String BUNDLE_CONTACT       = "Bundle-ContactAddress";
  public static final String BUNDLE_COPYRIGHT     = "Bundle-Copyright";
  public static final String BUNDLE_DESCRIPTION   = "Bundle-Description";
  public static final String BUNDLE_DOCURL        = "Bundle-DocURL";
  public static final String EXPORT_PACKAGE       = "Export-Package";
  public static final String IMPORT_PACKAGE       = "Import-Package";

  public static final String BUILT_FROM         = "Built-From";
  
  /**
   * @return Returns the name.
   */
  public String getName();
  
  /**
   * @return Returns the version.
   */
  public String getVersion();

  /**
   * @return Returns the activator.
   */
  public String getActivator();
  
  public PackageDescription[] getImportedPackages(); 

  public PackageDescription[] getExportedPackages(); 
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getPath()
   */
  public String getPath();

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getSourceDirectory()
   */
  public String getSourceDirectory();
}
