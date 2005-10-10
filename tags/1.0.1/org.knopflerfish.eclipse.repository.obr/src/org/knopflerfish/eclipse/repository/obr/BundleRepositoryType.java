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

package org.knopflerfish.eclipse.repository.obr;

import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleRepositoryType implements IBundleRepositoryType {
  
  private static String[] OBR_URLS = new String[] {
    "http://oscar-osgi.sourceforge.net/repo/repository.xml"
  };

  /****************************************************************************
   * org.knopflerfish.eclipse.core.IBundleRepositoryType methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#isValidConfig(java.lang.String)
   */
  public boolean isValidConfig(String config) {
    // Check that config is a valid URL
    return true;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#getConfigSuggestions()
   */
  public String[] getConfigSuggestions() {
    return OBR_URLS;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#createRepository(java.lang.String)
   */
  public IBundleRepository createRepository(String config) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IBundleRepositoryType#refreshRepositories()
   */
  public void refreshRepositories() {
    // TODO Auto-generated method stub
    
  }
}
