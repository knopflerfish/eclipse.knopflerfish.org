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

package org.knopflerfish.eclipse.core.manifest;

import java.util.StringTokenizer;

import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleIdentity {
  
  public static String SEPARATOR = ";";
  public static String VERSION = "version";
  
  private SymbolicName symbolicName;
  private Version bundleVersion;
  
  public BundleIdentity(String s) {
    StringTokenizer st = new StringTokenizer(s, SEPARATOR);
    
    // First token is symbolic name
    symbolicName = new SymbolicName(st.nextToken().trim());
    
    // Attributes
    bundleVersion = Version.emptyVersion;
    while(st.hasMoreTokens()) {
      String parameter = st.nextToken();
      int idx = parameter.indexOf('=');
      if (idx != -1) {
        String attr = parameter.substring(0, idx).trim();
        String value = parameter.substring(idx+1).trim();
        // Removes qoutes if qouted
        if (value.startsWith("\"") && value.endsWith("\"") ) {
          value = value.substring(1, value.length()-1);
        }
        if (VERSION.equals(attr)) {
          bundleVersion = Version.parseVersion(value);
        }
      }
    }
  }
  
  public BundleIdentity(SymbolicName symbolicName, Version version) {
    this.symbolicName = symbolicName;
    if (version == null) {  
      bundleVersion = Version.emptyVersion;
    } else {
      bundleVersion = version;
    }
  }

  /****************************************************************************
   * Getters and setters
   ***************************************************************************/
  
  public Version getBundleVersion() {
    return bundleVersion;
  }

  public SymbolicName getSymbolicName() {
    return symbolicName;
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BundleIdentity)) return false;
    
    BundleIdentity id = (BundleIdentity) o;
    
    return symbolicName.equals(id.symbolicName) && 
      bundleVersion.equals(id.bundleVersion);
  }
  
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(symbolicName.getSymbolicName());
    buf.append(";");
    buf.append(VERSION);
    buf.append("=");
    buf.append(bundleVersion.toString());
    return buf.toString();
  }

  /*
   *  (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return toString().hashCode();
  }
}
