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

package org.knopflerfish.eclipse.repository.framework;

import java.util.ArrayList;
import java.util.List;

import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;

/**
 * @author Anders Rim�n, Makewave
 * @see http://www.makewave.com/
 */
public class BundleRepositoryType implements IBundleRepositoryType {

  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleRepositoryType methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#isValidConfig(java.lang.String)
   */
  public boolean isValidConfig(String config) {
    // Check that config is a valid framework name
    return (OsgiPreferences.getFramework(config) != null);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#getConfigSuggestions()
   */
  public String[] getConfigSuggestions() {
    List<String> names = new ArrayList<String>();
    FrameworkPreference[] frameworks = OsgiPreferences.getFrameworks();
    for(int i=0; i<frameworks.length;i++) {
      names.add(frameworks[i].getName());
    }
    return names.toArray(new String[names.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#createRepository(java.lang.String)
   */
  public IBundleRepository createRepository(String config) {
    IBundleRepository repository = RepositoryPlugin.repositoriesCache.get(config);
    if (repository == null) {
      repository = new BundleRepository(config);
      RepositoryPlugin.repositoriesCache.put(config, repository);
    } 
    return repository;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#refreshRepositories()
   */
  public void refreshRepositories() {
    RepositoryPlugin.repositoriesCache.clear();
  }
}