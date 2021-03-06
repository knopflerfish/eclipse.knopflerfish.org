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

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public interface IBundleRepositoryType {

  /** 
   * Checks if the configuration is valid for this
   * repository type.
   * 
   * @param config configuration
   * 
   * @return true if valid; otherwise false.
   */
  //public boolean isValidConfig(String config);

  /** 
   * Returns an array of suitable configurations
   * that can be used by this repository type.
   * Null is returned if no suggestions can be made. 
   * 
   * @return list of config suggestions
   */
  //public String[] getConfigSuggestions();

  /**
   * Returns a repository configuration handler which can be used to
   * create UI control for configuring this repository.
   * 
   * @param config default configuration
   * 
   * @return bundle repository configuration handler
   */
  public IBundleRepositoryConfig getRepositoryConfig();
  
  /**
   * Create a bundle repository from the given configuration.
   * 
   * @param config configuration
   * 
   * @return bundle repository
   */
  public IBundleRepository createRepository(String config);

  /**
   * Refreshes all cached repositories refreshing any 
   * cached data.
   */
  public void refreshRepositories();
}
