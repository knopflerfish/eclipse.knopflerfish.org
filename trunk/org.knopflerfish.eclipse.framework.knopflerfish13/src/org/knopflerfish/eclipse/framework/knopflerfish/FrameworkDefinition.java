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

package org.knopflerfish.eclipse.framework.knopflerfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.IXArgsProperty;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.knopflerfish.eclipse.core.XArgsFile;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class FrameworkDefinition implements IFrameworkDefinition {

  // FWProps
  /**
   * Name of system property for basic system packages to be exported. The
   * normal OSGi exports will be added to this list.
   */
  public final static String SYSTEM_PACKAGES_BASE_PROP = "org.knopflerfish.framework.system.packages.base";

  /**
   * Property name pointing to file listing of system-exported packages
   */
  public final static String SYSTEM_PACKAGES_FILE_PROP = "org.knopflerfish.framework.system.packages.file";

  /**
   * Property name for selecting exporting profile of system packages.
   */
  public final static String SYSTEM_PACKAGES_VERSION_PROP = "org.knopflerfish.framework.system.packages.version";

  public static int javaVersionMajor = 1;
  public static int javaVersionMinor = 6;

  private final static String PATH_PROPERTY_FILE = "resources/framework.props";
  
  static public final String XARGS_INIT     = "init.xargs";
  static public final String XARGS_RESTART  = "restart.xargs";

  private final static String[] PATH_FRAMEWORK_LIB = new String[] {
      "knopflerfish.org/osgi/framework.jar", "osgi/framework.jar",
      "framework.jar" };

  // Paths relative root directory for definition
  private final static String PATH_MAINLIB = "framework.jar";
  private final static String PATH_MAINLIB_SRC = "framework/src";

  private final static String PATH_JAR_DIR = "jars";
  private final static String PATH_BUNDLE_DIR = "bundles";

  // ***************************************************************************
  // org.knopflerfish.eclipse.core.IFrameworkDefinition methods
  // ***************************************************************************/
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#isValidDir(java.io.File)
   */
  public boolean isValidDir(File dir)
  {
    return getRootDir(dir) != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getMainLibrary(java.
   * io.File)
   */
  public IOsgiLibrary getMainLibrary(File dir)
  {
    File root = getRootDir(dir);
    if (root == null)
      return null;

    File file = new File(root, PATH_MAINLIB);
    OsgiLibrary mainLib = null;
    try {
      mainLib = new OsgiLibrary(file);
      File src = new File(root, PATH_MAINLIB_SRC);
      if (src.exists()) {
        mainLib.setSource(src.getAbsolutePath());
      }
    } catch (IOException e) {
    }

    return mainLib;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getRuntimeLibraries(
   * java.io.File)
   */
  public IOsgiLibrary[] getRuntimeLibraries(File dir)
  {
    List<IOsgiLibrary> libraries = new ArrayList<IOsgiLibrary>();

    IOsgiLibrary mainLib = getMainLibrary(dir);
    if (mainLib != null) {
      libraries.add(mainLib);
    }

    return libraries.toArray(new IOsgiLibrary[libraries.size()]);
  }


  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getBundleDirectories(java.io.File)
   */
  public String[] getBundleDirectories(File dir)
  {
    List<String> dirs = new ArrayList<String>();
    
    // Find xargs file and parse path. If not found default to 'jars'
    File root = getRootDir(dir);
    
    // Find xargs file
    if (root != null) {
      try {
        // Find default xargs file
        XArgsFile xArgs = new XArgsFile(root, XARGS_INIT);
        IXArgsProperty p = xArgs.getFrameworkProperty(IXArgsProperty.PROPERTY_KF_JARS);
        if (p == null) {
          p = xArgs.getSystemProperty(IXArgsProperty.PROPERTY_KF_JARS);
        }
        if (p != null) {
          String v = p.getValue();
          //Split path
          StringTokenizer st = new StringTokenizer(p.getValue(), ";");
          while (st.hasMoreTokens()) {
            URL url = new URL(st.nextToken().trim());
            // Only add urls which reference local directories
            if ("file".equals(url.getProtocol())) {
              dirs.add(url.getPath());
            }
          }
        }
      } catch (IOException e) {
        // Failed to create xargs from file
      }
    }
    
    // Add default jars dir if none has been found in xargs file
    if (dirs.size() == 0) {
      dirs.add(PATH_JAR_DIR);
    }
    
    return dirs.toArray(new String[dirs.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getBundles(java.io.File)
   */
  public IOsgiBundle[] getBundles(File dir)
  {
    return getBundles(dir, null);
  }

  public IOsgiBundle[] getBundles(File dir, String path)
  {
    List<IOsgiBundle> bundles = new ArrayList<IOsgiBundle>();

    // Add bundles
    File root = getRootDir(dir);
    
    // Default to PATH_JAR_DIR if not set
    if (path == null) {
      path = PATH_JAR_DIR;
    }
    if (root != null) {
      File jarDir = new File(root, path);
      List<File> jars = getJars(jarDir);
      for (int i = 0; i < jars.size(); i++) {
        try {
          OsgiBundle bundle = new OsgiBundle((File) jars.get(i));
          // Find source
          String builtFrom = null;
          if (bundle.getBundleManifest() != null) {
            builtFrom = bundle.getBundleManifest().getAttribute(
                BundleManifest.BUILT_FROM);
          }
          if (builtFrom != null) {
            // Try to find source directory
            bundle.setSource(findSourceDir(builtFrom, root));
          }

          bundles.add(bundle);
        } catch (IOException e) {
          // Failed to create bundle from file
        }
      }
    }

    return bundles.toArray(new IOsgiBundle[bundles.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getSystemPropertyGroups
   * ()
   */
  public PropertyGroup[] getSystemPropertyGroups()
  {
    List<PropertyGroup> groups = new ArrayList<PropertyGroup>();

    // Load and set properties
    InputStream is = null;
    try {
      try {
        /*
         * is = KnopflerfishPlugin.getDefault().openStream( new
         * Path(PATH_PROPERTY_FILE));
         */
        is = FileLocator.openStream(
            KnopflerfishPlugin.getDefault().getBundle(), new Path(
                PATH_PROPERTY_FILE), false);
        Properties props = new Properties();
        props.load(is);

        int numProps = Integer.parseInt(props.getProperty(
            "framework.property.num", "0"));
        for (int i = 0; i < numProps; i++) {
          String propBase = "framework.property." + i;
          String group = props.getProperty(propBase + ".group");
          if (group != null) {
            PropertyGroup propertyGroup = new PropertyGroup(group);
            if (groups.contains(propertyGroup)) {
              propertyGroup = (PropertyGroup) groups.get(groups
                  .indexOf(propertyGroup));
            } else {
              groups.add(propertyGroup);
            }

            String name = props.getProperty(propBase + ".name");
            if (name != null) {
              Property property = new Property(name);

              String description = props.getProperty(propBase + ".description");
              if (description != null) {
                property.setDescription(description);
              }
              String defaultValue = props.getProperty(propBase + ".default");
              if (defaultValue != null) {
                property.setDefaultValue(defaultValue);
                property.setValue(defaultValue);
              }
              String allowedValues = props.getProperty(propBase + ".allowed");
              if (allowedValues != null) {
                List<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(allowedValues, ",");
                while (st.hasMoreTokens()) {
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
        if (is != null)
          is.close();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return groups.toArray(new PropertyGroup[groups.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#createConfiguration()
   */
  public IFrameworkConfiguration createConfiguration(String installDir,
                                                     String workDir)
  {
    if (workDir == null)
      return null;

    File dir = new File(workDir);
    if (!dir.exists() || !dir.isDirectory())
      return null;

    return new FrameworkConfiguration(dir);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getExportedPackages(
   * org.knopflerfish.eclipse.core.IOsgiLibrary[])
   */
  public PackageDescription[] getExportedPackages(IOsgiLibrary[] libraries)
  {
    List<PackageDescription> descriptions = new ArrayList<PackageDescription>();

    if (libraries != null) {
      for (int i = 0; i < libraries.length; i++) {
        try {
          OsgiBundle bundle = new OsgiBundle(new File(libraries[i].getPath()));

          PackageDescription[] packages = null;
          if (bundle.getBundleManifest() != null) {
            packages = bundle.getBundleManifest().getExportedPackages();
          }
          if (packages != null) {
            for (int j = 0; j < packages.length; j++) {
              descriptions.add(packages[j]);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return descriptions.toArray(new PackageDescription[descriptions.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkDefinition#getSystemPackages(java
   * .io.File, java.util.Map)
   */
  public PackageDescription[] getSystemPackages(File dir,
                                                Map<String, String> systemProperties)
  {
    // TODO : Cache this
    final StringBuffer sp = new StringBuffer();

    // Read properties
    final String sysPkg = getProperty(systemProperties,
        Constants.FRAMEWORK_SYSTEMPACKAGES);
    final String extraPkgs = getProperty(systemProperties,
        Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA);
    final String kfSysPkgFile = getProperty(systemProperties,
        SYSTEM_PACKAGES_FILE_PROP);
    final String kfSysPkgBase = getProperty(systemProperties,
        SYSTEM_PACKAGES_BASE_PROP);
    String jver = getProperty(systemProperties,
        SYSTEM_PACKAGES_VERSION_PROP);
    if (jver == null) {
      jver = Integer.toString(javaVersionMajor) + "." + javaVersionMinor;
    }

    if (sysPkg != null) {
      sp.append(sysPkg);
    }
    if (sp.length() == 0) {
      // Try the system packages file
      if (kfSysPkgFile != null) {
        addSysPackagesFromFile(sp, dir, kfSysPkgFile);
      }
      if (sp.length() == 0) {
        // Try the system packages base property.
        if (kfSysPkgBase != null) {
          sp.append(kfSysPkgBase);
        }
        if (sp.length() == 0) {
          // use default set of packages.
          if (!addSysPackagesFromFile(sp, dir, "packages" + jver + ".txt")) {
            String message = "No built in list of Java packages to be exported "
                + "by the system bundle for JRE with version '"
                + jver
                + "', using the list for 1.6.";
            IStatus status = new Status(IStatus.ERROR,
                "org.knopflerfish.eclipse.framework", IStatus.OK, message, null);
            KnopflerfishPlugin.getDefault().getLog().log(status);
            addSysPackagesFromFile(sp, dir, "packages1.6.txt");
          }
        }
        // The system packages are exported through the manifest and does not
        // need to be added here
        // addSystemPackages(sp);

        if (sp.length() > 0 && ',' == sp.charAt(sp.length() - 1)) {
          sp.setLength(sp.length() - 1);
        }
      }
    }

    // Add system.packages.extra
    if (extraPkgs != null && extraPkgs.length() > 0) {
      sp.append(",").append(extraPkgs);
    }

    List<PackageDescription> packages = new ArrayList<PackageDescription>();
    StringTokenizer st = new StringTokenizer(sp.toString(), ",");
    while (st.hasMoreTokens()) {
      try {
        packages.add(new PackageDescription(st.nextToken(),
            Version.emptyVersion));
      } catch (Exception e) {
      }
    }

    return packages.toArray(new PackageDescription[packages.size()]);
  }

  // ***************************************************************************
  // Private utility methods
  // ***************************************************************************
  public File getRootDir(File dir)
  {
    if (dir == null || !dir.isDirectory())
      return null;

    File root = null;
    for (int i = 0; i < PATH_FRAMEWORK_LIB.length; i++) {
      File libFile = new File(dir, PATH_FRAMEWORK_LIB[i]);
      if (libFile.exists() && libFile.isFile()) {
        root = libFile.getParentFile();
      }
    }

    return root;
  }
  
  private List<File> getJars(File f)
  {
    List<File> jars = new ArrayList<File>();
    if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
      jars.add(f);
    } else if (f.isDirectory()) {
      File[] list = f.listFiles();
      for (int i = 0; list != null && i < list.length; i++) {
        jars.addAll(getJars(list[i]));
      }
    }
    return jars;
  }

  /**
   * Read a file with package names and add them to a stringbuffer.
   */
  private boolean addSysPackagesFromFile(StringBuffer sp,
                                         File dir,
                                         String sysPkgFile)
  {

    File f = new File(new File(sysPkgFile).getAbsolutePath());

    if (!f.exists() || !f.isFile()) {
      // Try resource directory
      f = new File(dir, "osgi/framework/resources/" + sysPkgFile);
      if (!f.exists() || !f.isFile()) {
        return false;
      }
    }

    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(f));
      String line;
      for (line = in.readLine(); line != null; line = in.readLine()) {
        line = line.trim();
        if (line.length() > 0 && !line.startsWith("#")) {
          sp.append(line);
          sp.append(",");
        }
      }
      return true;
    } catch (IOException e) {
      return false;
    } finally {
      try {
        in.close();
      } catch (IOException e) {
      }
    }
  }

  private String getProperty(final Map<String, String> props, final String key)
  {
    if (props == null) {
      return null;
    } else {
      return props.get(key);
    }
  }
  
  private static String findSourceDir(final String builtFromDir, final File frameworkDir) {
    if (builtFromDir == null) {
      return null;
    }
    
    String d = builtFromDir;
    if (!d.endsWith(File.separator+"src")) {
      d = d + File.separator+"src";
    }
    
    // Check if builtFrom directly points to an directory
    File f = new File(d);
    if (f.exists() && f.isDirectory()) {
      return f.getAbsolutePath();
    }
    
    // Try to merge builtFrom with frameworkDir to find directory
    // Make path relative
    int idx = d.indexOf(File.pathSeparatorChar);
    if (idx != -1) {
      d = d.substring(idx+1);
    }
    while (d.startsWith(File.separator)) {
      d = d.substring(1);
    }
    while(d.length() > 0) {
      f = new File(frameworkDir, d);
      if (f.exists() && f.isDirectory()) {
        return f.getAbsolutePath();
      }
      idx = d.indexOf(File.separator);
      if (idx == -1 || (idx+1) >= d.length()) {
        return null;
      }
      d = d.substring(idx+1);
    } 
    return null;
  }

}
