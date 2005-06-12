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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author ar
 */
public class OsgiBundle implements IOsgiBundle {

  private final File file;
  private String name;
  private String version;
  private String activator;
  private String source;
  private String builtFrom;
  private List importedPackages = new ArrayList();
  private List exportedPackages = new ArrayList();

  public OsgiBundle(File jar) throws IOException {
    this.file = jar;

    // Set default values
    name = jar.getName();
    
    // Read manifest
    readManifest(file);
  }
  
  /****************************************************************************
   * Getters and setters
   ***************************************************************************/

  public String getName() {
    return name;
  }

  public String getBuiltFrom() {
    return builtFrom;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getVersion()
   */
  public String getVersion() {
    return version;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getActivator()
   */
  public String getActivator() {
    return activator;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getPath()
   */
  public String getPath() {
    return file.getAbsolutePath();
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getImportPackages()
   */
  public PackageDescription[] getImportedPackages() {
    return (PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]);
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getExportPackages()
   */
  public PackageDescription[] getExportedPackages() {
    return (PackageDescription[]) exportedPackages.toArray(new PackageDescription[exportedPackages.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiBundle#getSourceDirectory()
   */
  public String getSourceDirectory() {
    return source;
  }

  public void setSourceDirectory(String source) {
    this.source = source;
  }

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
  public boolean equals(Object obj) {
    if(obj == null || !(obj instanceof OsgiBundle)) return false;
    
    return ((OsgiBundle) obj).getPath().equals(getPath());
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void readManifest(File f) throws IOException {
    JarFile jarFile = new JarFile(f);
    Manifest manifest = jarFile.getManifest();
    if (manifest == null) return;

    Attributes attributes = manifest.getMainAttributes();
    if (attributes == null) return;
     
    name = attributes.getValue(BUNDLE_NAME);
    version = attributes.getValue(BUNDLE_VERSION);
    activator = attributes.getValue(BUNDLE_ACTIVATOR);
    
    // Import-Packages
    String attr = attributes.getValue(IMPORT_PACKAGE);
    importedPackages.clear();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        try {
          importedPackages.add(new PackageDescription(st.nextToken()));
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }

    // Export-Packages
    attr = attributes.getValue(EXPORT_PACKAGE);
    exportedPackages.clear();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        try {
          exportedPackages.add(new PackageDescription(st.nextToken()));
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
    
    builtFrom = attributes.getValue(BUILT_FROM);
  }

}
