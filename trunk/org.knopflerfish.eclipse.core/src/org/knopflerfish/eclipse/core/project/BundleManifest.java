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

package org.knopflerfish.eclipse.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.knopflerfish.eclipse.core.PackageDescription;

/**
 * @author Anders Rimén
 */
public class BundleManifest extends Manifest {

  public static final String BUNDLE_SYMBOLIC_NAME  = "Bundle-SymbolicName";
  public static final String BUNDLE_NAME           = "Bundle-Name";
  public static final String BUNDLE_CATEGORY       = "Bundle-Category";
  public static final String BUNDLE_VERSION        = "Bundle-Version";
  public static final String BUNDLE_ACTIVATOR      = "Bundle-Activator";
  public static final String BUNDLE_VENDOR         = "Bundle-Vendor";
  public static final String BUNDLE_CONTACT        = "Bundle-ContactAddress";
  public static final String BUNDLE_COPYRIGHT      = "Bundle-Copyright";
  public static final String BUNDLE_DESCRIPTION    = "Bundle-Description";
  public static final String BUNDLE_DOCURL         = "Bundle-DocURL";
  public static final String BUNDLE_UPDATELOCATION = "Bundle-UpdateLocation";
  public static final String EXPORT_PACKAGE        = "Export-Package";
  public static final String IMPORT_PACKAGE        = "Import-Package";

  public static final String BUILT_FROM            = "Built-From";
  
  public BundleManifest() {
    super();
    if (!getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION)) {
      getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
  }

  public BundleManifest(InputStream is) throws IOException {
    super(is);
    if (!getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION)) {
      getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
  }

  public BundleManifest(Manifest man) {
    super(man);
    if (!getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION)) {
      getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
  }


  
  /****************************************************************************
   * Setters and getters for OSGi attributes
   ***************************************************************************/
  
  public String getSymbolicName() {
    return getAttribute(BUNDLE_SYMBOLIC_NAME);
  }
  
  public void setSymbolicName(String value) {
    setAttribute(BUNDLE_SYMBOLIC_NAME, value);
  }
  
  public String getName() {
    return getAttribute(BUNDLE_NAME);
  }

  public void setName(String value) {
    setAttribute(BUNDLE_NAME, value);
  }
  
  public String getVersion() {
    return getAttribute(BUNDLE_VERSION);
  }

  public void setVersion(String value) {
    setAttribute(BUNDLE_VERSION, value);
  }
  
  public String getVendor() {
    return getAttribute(BUNDLE_VENDOR);
  }

  public void setVendor(String value) {
    setAttribute(BUNDLE_VENDOR, value);
  }
  
  public String getDescription() {
    return getAttribute(BUNDLE_DESCRIPTION);
  }

  public void setDescription(String value) {
    setAttribute(BUNDLE_DESCRIPTION, value);
  }
  
  public String getActivator() {
    return getAttribute(BUNDLE_ACTIVATOR);
  }

  public void setActivator(String value) {
    setAttribute(BUNDLE_ACTIVATOR, value);
  }

  public String getUpdateLocation() {
    return getAttribute(BUNDLE_UPDATELOCATION);
  }

  public void setUpdateLocation(String value) {
    setAttribute(BUNDLE_UPDATELOCATION, value);
  }
  
  
  public String[] getCategories() {
    String attr = getAttribute(BUNDLE_CATEGORY);
    ArrayList categories = new ArrayList();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        categories.add(st.nextToken().trim());
      }
    }
    
    return (String[]) categories.toArray(new String[categories.size()]);
  }

  public void setCategories(String[]  value) {
    if (value == null) {
      setAttribute(BUNDLE_CATEGORY, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i]);
      }
      setAttribute(BUNDLE_CATEGORY, buf.toString());
    }
  }
  
  public PackageDescription[] getImportedPackages() {
    String attr = getAttribute(IMPORT_PACKAGE);
    if (attr == null) return null;
    
    ArrayList importedPackages = new ArrayList();
    StringTokenizer st = new StringTokenizer(attr, ",");
    while(st.hasMoreTokens()) {
      try {
        importedPackages.add(new PackageDescription(st.nextToken()));
      } catch(Exception e) {}
    }
    
    return (PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]);
  }

  public PackageDescription[] getExportedPackages() {
    String attr = getAttribute(EXPORT_PACKAGE);
    if (attr == null) return null;
    
    ArrayList exportedPackages = new ArrayList();
    StringTokenizer st = new StringTokenizer(attr, ",");
    while(st.hasMoreTokens()) {
      try {
        exportedPackages.add(new PackageDescription(st.nextToken()));
      } catch(Exception e) {}
    }
    
    return (PackageDescription[]) exportedPackages.toArray(new PackageDescription[exportedPackages.size()]);
  }

  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  public String getAttribute(String key) {
    Attributes attr = getMainAttributes();
    if (attr == null) return null;
    return attr.getValue(key);
  }
  
  public void setAttribute(String key, String value) {
    Attributes attr = getMainAttributes();
    if (attr == null) return;
    if (value == null) {
      attr.remove(key);
    } else {
      attr.putValue(key, value);
    }
  }
}
