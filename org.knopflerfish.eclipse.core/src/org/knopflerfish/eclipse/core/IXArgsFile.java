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

package org.knopflerfish.eclipse.core;

import java.util.Set;

/**
 * Interface representing an xargs file
 * 
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public interface IXArgsFile
{

  /**
   * Returns a set of all framework property names.
   * 
   * @return framework property names
   */
  public Set<String> getFrameworkPropertyNames();

  /**
   * Returns the framework property with given name or null if it does not
   * exist.
   * 
   * @return framework property
   */
  public IXArgsProperty getFrameworkProperty(String name);

  /**
   * Returns a set of all system property names.
   * 
   * @return system property names
   */
  public Set<String> getSystemPropertyNames();

  /**
   * Returns the system property with given name or null if it does not exist.
   * 
   * @return system property
   */
  public IXArgsProperty getSystemProperty(String name);

  /**
   * Returns the property with given name or null if it does not exist.
   * Framework properties are returned before system properties.
   * 
   * @return framework or system property
   */
  public IXArgsProperty getProperty(String name);

  /**
   * Returns the bundles to be installed.
   * 
   * @return bundles
   */
  public Set<IXArgsBundle> getBundles();
  
  /**
   * Returns the start level set in the xargs file.
   * 
   * @return start level
   */
  public int getStartLevel();

  /**
   * Returns if the platform shall be started empty.
   * 
   * @return clear persistent data
   */
  public boolean clearPersistentData();
}
