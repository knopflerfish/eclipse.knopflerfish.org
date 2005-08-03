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
 * @author Anders Rimén
 */
public interface IOsgiInstall {

  /** 
   * Returns the OSGi install name. The install names must be unique
   * within one vendor.
   * 
   * @return install name
   */
  String getName();

  /** 
   * Returns the class name of the main class used to launch the framework.
   * 
   * @return class name
   */
  String getMainClass();
  
  /** 
   * Returns the libraries needed to launch the framework.
   * 
   * @return libraries
   */
  IOsgiLibrary[] getRuntimeLibraries();

  /** 
   * Returns the libraries needed to launch the framework.
   * 
   * @return libraries
   */
  IOsgiLibrary[] getBuildLibraries();

  /** 
   * Returns the bundles defined by this distribution.
   * 
   * @return bundles
   */
  IOsgiBundle[] getBundles();
  
  
  /** 
   * Returns a list of system property groups understood by this 
   * framework.
   * 
   * @return properties
   */
  SystemPropertyGroup [] getSystemPropertyGroups();

  /** 
   * Add a property group to the list of group understood by this
   * framework. 
   * framework.
   * 
   * @param group property group to add
   */
  void addSystemPropertyGroup(SystemPropertyGroup group);

  void clearSystemPropertyGroups();
}
