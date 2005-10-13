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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleManifest extends Manifest {
  
  // OSGi specified attributes
  public static final String BUNDLE_SYMBOLIC_NAME   = "Bundle-SymbolicName";
  public static final String BUNDLE_NAME            = "Bundle-Name";
  public static final String BUNDLE_CLASSPATH       = "Bundle-ClassPath";
  public static final String BUNDLE_CATEGORY        = "Bundle-Category";
  public static final String BUNDLE_VERSION         = "Bundle-Version";
  public static final String BUNDLE_ACTIVATOR       = "Bundle-Activator";
  public static final String BUNDLE_VENDOR          = "Bundle-Vendor";
  public static final String BUNDLE_CONTACT         = "Bundle-ContactAddress";
  public static final String BUNDLE_COPYRIGHT       = "Bundle-Copyright";
  public static final String BUNDLE_DESCRIPTION     = "Bundle-Description";
  public static final String BUNDLE_DOCURL          = "Bundle-DocURL";
  public static final String BUNDLE_UPDATELOCATION  = "Bundle-UpdateLocation";
  public static final String BUNDLE_NATIVECODE      = "Bundle-NativeCode";
  public static final String BUNDLE_EXEC_ENV        = "Bundle-RequiredExecutionEnvironment";
  public static final String EXPORT_PACKAGE         = "Export-Package";
  public static final String IMPORT_PACKAGE         = "Import-Package";
  public static final String DYNAMIC_IMPORT_PACKAGE = "DynamicImport-Package";
  
  // Build information attributes
  public static final String BUILT_FROM            = "Built-From";
  public static final String BUILD_DATE            = "Build-Date";
  
  public BundleManifest() {
    super();
    if (!getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION)) {
      getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
  }
  
  public BundleManifest(Manifest manifest) {
    super(manifest);
  }
  
  public BundleManifest(InputStream is) throws IOException {
    super(is);
    if (!getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION)) {
      getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
  }
  
  /****************************************************************************
   * Setters and getters for OSGi attributes
   ***************************************************************************/
  
  public SymbolicName getSymbolicName() {
    String name = getAttribute(BUNDLE_SYMBOLIC_NAME);
    if (name == null || name.trim().length() == 0) {
      return null;
    }
    // Replace ':' with '_' in order work as hint in classpath container 
    return new SymbolicName(name.replace(':','_'));
  }
  
  public void setSymbolicName(SymbolicName value) {
    if(value == null) {
      setAttribute(BUNDLE_SYMBOLIC_NAME, null);
    } else {
      setAttribute(BUNDLE_SYMBOLIC_NAME, value.toString());
    }
  }
  
  public String getName() {
    return getAttribute(BUNDLE_NAME);
  }
  
  public void setName(String value) {
    setAttribute(BUNDLE_NAME, value);
  }
  
  public Version getVersion() {
    String s = getAttribute(BUNDLE_VERSION);
    Version version = Version.emptyVersion;
    try {
      version = Version.parseVersion(s);
    } catch (IllegalArgumentException e) {
    }
    return version;
  }
  
  public void setVersion(Version value) {
    if(value == null) {
      setAttribute(BUNDLE_VERSION, null);
    } else {
      setAttribute(BUNDLE_VERSION, value.toString());
    }
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
  
  public String getDocumentationUrl() {
    return getAttribute(BUNDLE_DOCURL);
  }
  
  public void setDocumentationUrl(String value) {
    setAttribute(BUNDLE_DOCURL, value);
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
  
  public String[] getBundleClassPath() {
    String attr = getAttribute(BUNDLE_CLASSPATH);
    ArrayList classPath = new ArrayList();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        classPath.add(st.nextToken().trim());
      }
    }
    
    return (String[]) classPath.toArray(new String[classPath.size()]);
  }
  
  public void setBundleClassPath(String[] value) {
    if (value == null) {
      setAttribute(BUNDLE_CLASSPATH, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i]);
      }
      setAttribute(BUNDLE_CLASSPATH, buf.toString());
    }
  }
  
  public PackageDescription[] getImportedPackages() {
    String attr = getAttribute(IMPORT_PACKAGE);
    if (attr == null) return new PackageDescription[0];
    
    ArrayList importedPackages = new ArrayList();
    StringTokenizer st = new StringTokenizer(attr, ",");
    while(st.hasMoreTokens()) {
      try {
        importedPackages.add(new PackageDescription(st.nextToken()));
      } catch(Exception e) {}
    }
    
    return (PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]);
  }
  
  public void setImportedPackages(PackageDescription[] value) {
    if (value == null) {
      setAttribute(IMPORT_PACKAGE, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i].toString());
      }
      setAttribute(IMPORT_PACKAGE, buf.toString());
    }
  }
  
  public String[] getDynamicImportedPakages() {
    String attr = getAttribute(DYNAMIC_IMPORT_PACKAGE);
    ArrayList packages = new ArrayList();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        packages.add(st.nextToken().trim());
      }
    }
    
    return (String[]) packages.toArray(new String[packages.size()]);
  }
  
  public void setDynamicImportedPakages(String[] value) {
    if (value == null) {
      setAttribute(DYNAMIC_IMPORT_PACKAGE, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i]);
      }
      setAttribute(DYNAMIC_IMPORT_PACKAGE, buf.toString());
    }
  }
  
  // TODO : Move 
  private final static int IN_ELEMENT = 0;
  private final static int IN_ATTRIBUTE_NAME = 1;
  private final static int IN_ATTRIBUTE_VALUE = 2;
  private final static int IN_QUOTED_ATTRIBUTE_VALUE = 3;
  
  public PackageDescription[] getExportedPackages() {
    String attr = getAttribute(EXPORT_PACKAGE);
    if (attr == null) return new PackageDescription[0];
    
    ArrayList exportedPackages = new ArrayList();
    
    int state = IN_ELEMENT;
    StringBuffer packageString = new StringBuffer();
    StringTokenizer st = new StringTokenizer(attr, " ;=,\"", true);
    while(st.hasMoreTokens()) {
      String token = null;
      if (state == IN_QUOTED_ATTRIBUTE_VALUE) {
        token = st.nextToken("\"");
      } else {
        token = st.nextToken(" ;=,\"");
      }
      
      if (token.equals(",")) {
        try {
          exportedPackages.add(new PackageDescription(packageString.toString()));
        } catch(Exception e) {}
        packageString.setLength(0);
        state = IN_ELEMENT;
      } else {
        packageString.append(token);
        if (token.equals(";")) {
          state = IN_ATTRIBUTE_NAME;
        } else if (token.equals("=")) {
          state = IN_ATTRIBUTE_VALUE;
        } else if (token.equals("\"") && state == IN_ATTRIBUTE_VALUE) {
          state = IN_QUOTED_ATTRIBUTE_VALUE;
        } else if (token.equals("\"") && state == IN_QUOTED_ATTRIBUTE_VALUE) {
          state = IN_ATTRIBUTE_VALUE;
        }
      }
    }
    
    if (packageString.length() > 0) {
      try {
        exportedPackages.add(new PackageDescription(packageString.toString()));
      } catch(Exception e) {}
    }
    
    return (PackageDescription[]) exportedPackages.toArray(new PackageDescription[exportedPackages.size()]);
  }
  
  public void setExportedPackages(PackageDescription[] value) {
    if (value == null) {
      setAttribute(EXPORT_PACKAGE, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i].toString());
      }
      setAttribute(EXPORT_PACKAGE, buf.toString());
    }
  }
  
  public NativeCodeClause[] getNativeCodeClauses() {
    String attr = getAttribute(BUNDLE_NATIVECODE);
    if (attr == null) return new NativeCodeClause[0];
    
    ArrayList nativeCodeClauses = new ArrayList();
    StringTokenizer st = new StringTokenizer(attr, ",");
    while(st.hasMoreTokens()) {
      try {
        nativeCodeClauses.add(new NativeCodeClause(st.nextToken()));
      } catch(Exception e) {}
    }
    
    return (NativeCodeClause[]) nativeCodeClauses.toArray(new NativeCodeClause[nativeCodeClauses.size()]);
  }
  
  public String[] getExecutionEnvironments() {
    String attr = getAttribute(BUNDLE_EXEC_ENV);
    ArrayList environments = new ArrayList();
    if (attr != null) {
      StringTokenizer st = new StringTokenizer(attr, ",");
      while(st.hasMoreTokens()) {
        environments.add(st.nextToken().trim());
      }
    }
    
    return (String[]) environments.toArray(new String[environments.size()]);
  }
  
  public void setExecutionEnvironments(String[] value) {
    if (value == null) {
      setAttribute(BUNDLE_EXEC_ENV, null);
    } else {
      StringBuffer buf = new StringBuffer("");
      for(int i=0; i<value.length;i++) {
        if (i != 0) {
          buf.append(", ");
        }
        buf.append(value[i]);
      }
      setAttribute(BUNDLE_EXEC_ENV, buf.toString());
    }
  }
  
  /****************************************************************************
   * Public utility methods
   ***************************************************************************/
  public boolean hasExportedPackage(PackageDescription pd) {
    PackageDescription [] exportedPackages = getExportedPackages();
    for (int i=0; i<exportedPackages.length; i++) {
      if (exportedPackages[i].isCompatible(pd)) return true;
    }
    return false;
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
