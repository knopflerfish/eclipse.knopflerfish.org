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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiLibrary implements IOsgiLibrary {

  private final File file;
  private final JarFile jar;
  private final Manifest manifest;
  private String source;
  private boolean userDefined =false;

  public OsgiLibrary(File f) throws IOException {
    file = f;
    
    if (!f.exists() || !f.isFile()) {
      throw new IOException("Library does not exist.");
    }
    
    jar = new JarFile(file);
    manifest = jar.getManifest();
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IOsgiLibrary methods
   ***************************************************************************/

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getName()
   */
  public String getName() {
    return file.getName();
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getPath()
   */
  public String getPath() {
    return file.getAbsolutePath();
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getSource()
   */
  public String getSource() {
    return source;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#setSource(java.lang.String)
   */
  public void setSource(String source) {
    this.source = source;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#getManifest()
   */
  public Manifest getManifest() {
    return manifest;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#isUserDefined()
   */
  public boolean isUserDefined() {
    return userDefined;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiLibrary#setUserDefined(boolean)
   */
  public void setUserDefined(boolean userDefined) {
    this.userDefined = userDefined;
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return file.getAbsolutePath().hashCode();
  }
  
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof OsgiLibrary)) {
      return false;
    }
    
    OsgiLibrary lib = (OsgiLibrary) o;

    return file.getAbsolutePath().equals(lib.file.getAbsolutePath());
}
}
