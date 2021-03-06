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

package org.knopflerfish.eclipse.framework.eclipse31;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

public class FrameworkDefinition implements IFrameworkDefinition {
  
  // System properties used for exporting system packages
  private final static String SYSPKG = "org.osgi.framework.system.packages";
  
  private static String PATH_PROPERTY_FILE = "resources/framework.props";
  
  private static String [] PATH_FRAMEWORK_LIB = new String [] {
    "plugins/org.eclipse.osgi_3.1.0.jar",
    "plugins/org.eclipse.osgi_3.1.1.jar"
  };
  
  // Paths relative root directory for definition
  private static String PATH_MAINLIB_3_1_0     = 
    "plugins/org.eclipse.osgi_3.1.0.jar";
  private static String PATH_MAINLIB_SRC_3_1_0 =
    "plugins/org.eclipse.rcp.source_3.1.0/src/org.eclipse.osgi_3.1.0/src.zip";
  private static String PATH_MAINLIB_3_1_1     = 
    "plugins/org.eclipse.osgi_3.1.1.jar";
  private static String PATH_MAINLIB_SRC_3_1_1 =
    "plugins/org.eclipse.rcp.source_3.1.0/src/org.eclipse.osgi_3.1.1/src.zip";
  private static String[][] PATH_MAINLIBS = {
    new String[] {PATH_MAINLIB_3_1_0, PATH_MAINLIB_SRC_3_1_0},
    new String[] {PATH_MAINLIB_3_1_1, PATH_MAINLIB_SRC_3_1_1}
  };
  
  private static String[] PATH_RUNTIME_LIBRARIES = new String[] {
    "plugins/org.eclipse.osgi_3.1.0.jar",
    "plugins/org.eclipse.osgi_3.1.1.jar"
  };
  
  private static String PATH_BUNDLE_DIR = "plugins";
  
  private final static String EXPORTED_PACKAGE_X_INTERNAL_ATTRIBUTE = "x-internal:";
  private final static String EXPORTED_PACKAGE_X_FRIEND_ATTRIBUTE = "x-friend:";
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IFrameworkDefinition methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#isValidDir(java.io.File)
   */
  public boolean isValidDir(File dir) {
    return getRootDir(dir) != null;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getMainLibrary(java.io.File)
   */
  public IOsgiLibrary getMainLibrary(File dir) {
    File root = getRootDir(dir);
    if (root == null) return null;
    
    for (int i=0; i<PATH_MAINLIBS.length; i++) {
      File file = new File(root, PATH_MAINLIBS[i][0]);
      if (file.exists() && file.isFile()) {
        try {
          OsgiLibrary mainLib = new OsgiLibrary(file);
          File src = new File(root, PATH_MAINLIBS[i][1]);
          if (src.exists()) {
            mainLib.setSource(src.getAbsolutePath());
          }
          return mainLib;
        } catch (IOException e) {}
      }
    }
    
    return null;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getRuntimeLibraries(java.io.File)
   */
  public IOsgiLibrary[] getRuntimeLibraries(File dir) {
    ArrayList libraries = new ArrayList();
    
    File root = getRootDir(dir);
    if (root == null) return null;
    
    for (int i=0; i<PATH_RUNTIME_LIBRARIES.length; i++) {
      File libFile = new File(root, PATH_RUNTIME_LIBRARIES[i]);
      if ( libFile.exists() && libFile.isFile()) {
        try {
          OsgiLibrary library = new OsgiLibrary(libFile);
          /*File src = new File(root, PATH_MAINLIB_SRC);
           if (src.exists()) {
           library.setSource(src.getAbsolutePath());
           }*/
          libraries.add(library);
        } catch (IOException ignore) {
        }
      }
    }
    
    return (IOsgiLibrary[]) libraries.toArray(new IOsgiLibrary[libraries.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getBundles(java.io.File)
   */
  public IOsgiBundle[] getBundles(File dir) {
    ArrayList bundles = new ArrayList();
    
    // Add bundles
    File root = getRootDir(dir);
    if (root != null) {
      File bundleDir = new File(root, PATH_BUNDLE_DIR);
      ArrayList jars = getJars(bundleDir);
      for (int i=0 ; i<jars.size(); i++) {
        try {
          OsgiBundle bundle = new OsgiBundle((File) jars.get(i));
          bundles.add(bundle);
        } catch(IOException e) {
          // Failed to create bundle from file
        }
      }
    }
    
    return (IOsgiBundle[]) bundles.toArray(new IOsgiBundle[bundles.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getSystemPropertyGroups()
   */
  public PropertyGroup[] getSystemPropertyGroups() {
    ArrayList groups = new ArrayList();
    
    // Load and set properties
    InputStream is = null;
    try {
      try {
        is = Eclipse31Plugin.getDefault().openStream(new Path(PATH_PROPERTY_FILE));
        Properties props = new Properties();
        props.load(is);
        
        int numProps = Integer.parseInt(props.getProperty("framework.property.num", "0"));
        for (int i=0; i<numProps; i++) {
          String propBase = "framework.property."+i;
          String group = props.getProperty(propBase+".group");
          if (group != null) {
            PropertyGroup propertyGroup = new PropertyGroup(group);
            if (groups.contains(propertyGroup)) {
              propertyGroup = (PropertyGroup) groups.get(groups.indexOf(propertyGroup));
            } else {
              groups.add(propertyGroup);
            }
            
            String name = props.getProperty(propBase+".name");
            if (name != null) {
              Property property = new Property(name);
              
              String description = props.getProperty(propBase+".description");
              if (description != null) {
                property.setDescription(description);
              }
              String defaultValue = props.getProperty(propBase+".default");
              if (defaultValue != null) {
                property.setDefaultValue(defaultValue);
                property.setValue(defaultValue);
              }
              String allowedValues = props.getProperty(propBase+".allowed");
              if (allowedValues != null) {
                ArrayList values = new ArrayList();
                StringTokenizer st = new StringTokenizer(allowedValues, ",");
                while(st.hasMoreTokens()) {
                  String token = st.nextToken();
                  values.add(token);
                }
                property.setAllowedValues(values);
              }
              
              propertyGroup.addSystemProperty(property);
            }
          }
        }
      } finally {
        if (is != null) is.close();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    
    return (PropertyGroup[]) groups.toArray(new PropertyGroup[groups.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#createConfiguration()
   */
  public IFrameworkConfiguration createConfiguration(String installationPath, String path) {
    if (path == null) return null;
    
    File dir = new File(path);
    if (!dir.exists() || !dir.isDirectory()) return null;
    
    return new FrameworkConfiguration(new File(installationPath, PATH_BUNDLE_DIR), dir);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getExportedPackages(org.knopflerfish.eclipse.core.IOsgiLibrary[])
   */
  public PackageDescription[] getExportedPackages(IOsgiLibrary[] libraries) {
    ArrayList descriptions = new ArrayList();
    
    if (libraries != null) {
      for (int i=0; i<libraries.length; i++) {
        try {
          OsgiBundle bundle = new OsgiBundle(new File(libraries[i].getPath()));
          
          PackageDescription [] packages = null;
          if (bundle.getBundleManifest() != null) {
            packages = bundle.getBundleManifest().getExportedPackages();
          }
          if (packages != null) {
            for (int j=0; j<packages.length; j++) {
              if (packages[j].getAttribute(EXPORTED_PACKAGE_X_INTERNAL_ATTRIBUTE) == null
                  && packages[j].getAttribute(EXPORTED_PACKAGE_X_FRIEND_ATTRIBUTE) == null)
              {
                descriptions.add(packages[j]);
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    
    return (PackageDescription[]) descriptions.toArray(new PackageDescription[descriptions.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getSystemPackages(java.io.File, java.util.Map)
   */
  public PackageDescription[] getSystemPackages(File dir, Map systemProperties) {
    if (systemProperties == null) return null;
    
    StringBuffer sp = new StringBuffer();
    String sysPkg = (String) systemProperties.get(SYSPKG);
    if (sysPkg != null) {
      sp.append(sysPkg);
    }
    
    if (sp.length() > 0) {
      sp.append(",");
    }
    
    ArrayList packages = new ArrayList();
    StringTokenizer st = new StringTokenizer(sp.toString(), ",");
    while(st.hasMoreTokens()) {
      try {
        packages.add(new PackageDescription(st.nextToken()));
      } catch(Exception e) {}
    }
    
    return (PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]);
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  public File getRootDir(File dir) {
    if (dir == null || !dir.isDirectory()) return null;
    
    File root = null;
    for (int i=0; i<PATH_FRAMEWORK_LIB.length; i++) {
      File libFile = new File(dir, PATH_FRAMEWORK_LIB[i]);
      if ( libFile.exists() && libFile.isFile()) {
        root = libFile.getParentFile().getParentFile();
      }
    }
    
    return root;
  }
  
  private ArrayList getJars(File f) {
    if (!f.isDirectory())
    {
      throw new IllegalArgumentException();
    }
    ArrayList jars = new ArrayList();
    File [] list = f.listFiles();
    for(int i=0; list != null && i<list.length; i++) {
      f = list[i];
      // Can't use non jar plugins
      if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
        jars.add(f); 
      }
    }
    return jars;
  }
}
