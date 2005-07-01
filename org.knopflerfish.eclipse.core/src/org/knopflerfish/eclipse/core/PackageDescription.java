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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Anders Rim�n
 */
public class PackageDescription {
  public static String SPECIFICATION_VERSION = "specification-version";

  private String packageName;
  private Map attributes = new HashMap();
  
  public PackageDescription(String name, String version) {
    this.packageName = name;
    if (version != null) {
      attributes.put(SPECIFICATION_VERSION, version);
    }
  }
  
  public PackageDescription(String s) {
    StringTokenizer st = new StringTokenizer(s, ";");
    
    // First token is package name
    packageName = st.nextToken().trim();
    
    // Attributes
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
        attributes.put(attr, value);
      }
    }
  }
  
  public String getPackageName() {
    return packageName;
  }
  
  public String getSpecificationVersion() {
    return getAttribute(SPECIFICATION_VERSION);
  }
  
  public String getAttribute(String attr) {
    return (String) attributes.get(attr);
  }

  public boolean isCompatible(PackageDescription pkg) {
    // Check package name
    if (pkg == null || pkg.getPackageName() == null) return false;
    if (!packageName.equals(pkg.getPackageName())) return false;

    // Package name the same, check specification version
    if (pkg.getSpecificationVersion() == null) return true;
    String version = getSpecificationVersion();
    if (version == null) return false;
    
    return true;
    // TODO: Check if versions are compatible
    //StringTokenizer st1 = new StringTokenizer(version);
    //StringTokenize
  }

}
