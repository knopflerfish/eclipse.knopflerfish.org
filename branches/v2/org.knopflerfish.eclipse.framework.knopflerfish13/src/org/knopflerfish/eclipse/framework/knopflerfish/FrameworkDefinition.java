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

package org.knopflerfish.eclipse.framework.knopflerfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkDefinition implements IFrameworkDefinition {
  
  // System properties used for exporting system packages
  private final static String SYSPKG = "org.osgi.framework.system.packages";
  private final static String SYSPKG_FILE = "org.osgi.framework.system.packages.file";
  private final static String EXPORT13 = "org.knopflerfish.framework.system.export.all_13";
  
  private final static String PATH_PROPERTY_FILE = "resources/framework.props";
  
  private final static String [] PATH_FRAMEWORK_LIB = new String [] {
    "knopflerfish.org/osgi/framework.jar",
    "osgi/framework.jar",
    "framework.jar"
  };
  
  // Paths relative root directory for definition  
  private final static String PATH_MAINLIB     = "framework.jar";
  private final static String PATH_MAINLIB_SRC = "framework/src";
  private final static String PATH_PACKAGES13  = "packages1.3.txt";

  private final static String PATH_JAR_DIR            ="jars";
  private final static String PATH_BUNDLE_DIR         ="bundles";
  
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
    
    File file = new File(root, PATH_MAINLIB);
    OsgiLibrary mainLib = null;
    try {
      mainLib = new OsgiLibrary(file);
      File src = new File(root, PATH_MAINLIB_SRC);
      if (src.exists()) {
        mainLib.setSource(src.getAbsolutePath());
      }
    } catch (IOException e) {}
    
    return mainLib;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getRuntimeLibraries(java.io.File)
   */
  public IOsgiLibrary[] getRuntimeLibraries(File dir) {
    ArrayList libraries = new ArrayList();
    
    IOsgiLibrary mainLib = getMainLibrary(dir);
    if (mainLib != null) {
      libraries.add(mainLib);
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
      File jarDir = new File(root, PATH_JAR_DIR);
      ArrayList jars = getJars(jarDir);
      for (int i=0 ; i<jars.size(); i++) {
        try {
          OsgiBundle bundle = new OsgiBundle((File) jars.get(i));
          // Find source
          String builtFrom = null;
          if (bundle.getBundleManifest() != null) {
            builtFrom = bundle.getBundleManifest().getAttribute(BundleManifest.BUILT_FROM);
          }
          if (builtFrom != null) {
            int idx = builtFrom.lastIndexOf(PATH_BUNDLE_DIR);
            if (idx != -1) {
              File bundleDir = new File(root, builtFrom.substring(idx));
              File srcDir = new File(bundleDir, "src");
              if (srcDir.exists() && srcDir.isDirectory()) {
                bundle.setSource(srcDir.getAbsolutePath());
              }
            }
          }
          
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
  public SystemPropertyGroup[] getSystemPropertyGroups() {
    ArrayList groups = new ArrayList();

    // Load and set properties
    InputStream is = null;
    try {
      try {
        is = KnopflerfishPlugin.getDefault().openStream(new Path(PATH_PROPERTY_FILE));
        Properties props = new Properties();
        props.load(is);
        
        int numProps = Integer.parseInt(props.getProperty("framework.property.num", "0"));
        for (int i=0; i<numProps; i++) {
          String propBase = "framework.property."+i;
          String group = props.getProperty(propBase+".group");
          if (group != null) {
            SystemPropertyGroup propertyGroup = new SystemPropertyGroup(group);
            if (groups.contains(propertyGroup)) {
              propertyGroup = (SystemPropertyGroup) groups.get(groups.indexOf(propertyGroup));
            } else {
              groups.add(propertyGroup);
            }

            String name = props.getProperty(propBase+".name");
            if (name != null) {
              SystemProperty property = new SystemProperty(name);

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

    return (SystemPropertyGroup[]) groups.toArray(new SystemPropertyGroup[groups.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#createConfiguration()
   */
  public IFrameworkConfiguration createConfiguration(String installDir, String workDir) {
    if (workDir == null) return null;
    
    File dir = new File(workDir);
    if (!dir.exists() || !dir.isDirectory()) return null;
    
    return new FrameworkConfiguration(dir);
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
              descriptions.add(packages[j]);
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
    
    String export13 = (String) systemProperties.get(EXPORT13);
    if (dir != null && export13 != null && "true".equals(export13.trim())) {
      File root = getRootDir(dir);
      File file = new File(root, PATH_PACKAGES13);
      addSysPackagesFromFile(sp, file);
    }
    
    String sysPkgFile = (String) systemProperties.get(SYSPKG_FILE);
    if (sysPkgFile != null) {
      addSysPackagesFromFile(sp, new File(sysPkgFile));
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
        root = libFile.getParentFile();
      }
    }
    
    return root;
  }

  private ArrayList getJars(File f) { 
    ArrayList jars = new ArrayList();
    if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
      jars.add(f); 
    } else if (f.isDirectory()) {
      File [] list = f.listFiles();
      for(int i=0; list != null && i<list.length; i++) {
        jars.addAll(getJars(list[i]));
      }
    }
    return jars;
  }
  
  /**
   * Read a file with package names and add them to a stringbuffer.
   */
  void addSysPackagesFromFile(StringBuffer sp, File f) {
    if (f == null || !f.exists() || !f.isFile()) return;
    
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(f));
      String line;
      for(line = in.readLine(); line != null; 
      line = in.readLine()) {
        line = line.trim();
        if(line.length() > 0 && !line.startsWith("#")) {
          sp.append(line);
          sp.append(",");
        }
      } 
    } catch (IOException e) {
      // Failed to read file, ignore this
    } finally {
      try {   in.close();  } catch (Exception ignored) { }
    }
  }
}
