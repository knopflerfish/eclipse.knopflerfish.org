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

package org.knopflerfish.eclipse.core;

import java.io.File;
import java.util.Map;

import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rim�n, Makewave
 * @see http://www.makewave.com/
 */
public interface IFrameworkDefinition {

  /** 
   * Checks if the framework can be found in 
   * the given directory.
   * 
   * @return true if valid directory; otherwise false.
   */
  public boolean isValidDir(File dir);
  
  public IOsgiLibrary getMainLibrary(File dir);
  
  public IOsgiLibrary[] getRuntimeLibraries(File dir);
  
  public IOsgiBundle[] getBundles(File dir);

  public IOsgiBundle[] getBundles(File dir, String path);

  public String[] getBundleDirectories(File dir);
  
  public PropertyGroup[] getSystemPropertyGroups();
  
  
  public PackageDescription[] getExportedPackages(IOsgiLibrary[] libraries);
  
  public PackageDescription[] getSystemPackages(File dir, Map<String, String> systemProperties);
  
  /** 
   * Create a runtime framework configuration.
   * 
   * @param installDir directory where framework is installed
   * @param workDir directory where instance data for this configuration
   * shall be stored
   * 
   * @return framework configuration
   */
  public IFrameworkConfiguration createConfiguration(String installDir, String workDir);
  
}
