package org.knopflerfish.eclipse.framework.oscar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;

public class FrameworkDefinition implements IFrameworkDefinition {
  
  private static String PATH_PROPERTY_FILE = "resources/framework.props";
  
  private static String [] PATH_FRAMEWORK_LIB = new String [] {
    "lib/oscar.jar"
  };

  // Paths relative root directory for definition  
  private static String PATH_MAINLIB     = "lib/oscar.jar";
  private static String PATH_MAINLIB_SRC = "src.jar";

  private static String[] PATH_RUNTIME_LIBRARIES = new String[] {
    "lib/oscar.jar",
    "lib/osgi.jar",
    "lib/moduleloader.jar"
  };
  
  private static String[] PATH_BUILD_LIBRARIES = new String[] {
    "lib/osgi.jar"
  };

  private static String PATH_BUNDLE_DIR = "bundle";
  
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
    
    File root = getRootDir(dir);
    if (root == null) return null;
    
    for (int i=0; i<PATH_RUNTIME_LIBRARIES.length; i++) {
      File libFile = new File(root, PATH_RUNTIME_LIBRARIES[i]);
      if ( libFile.exists() && libFile.isFile()) {
        try {
          OsgiLibrary library = new OsgiLibrary(libFile);
          File src = new File(root, PATH_MAINLIB_SRC);
          if (src.exists()) {
            library.setSource(src.getAbsolutePath());
          }
          libraries.add(library);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return (IOsgiLibrary[]) libraries.toArray(new IOsgiLibrary[libraries.size()]);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkDefinition#getBuildLibraries(java.io.File)
   */
  public IOsgiLibrary[] getBuildLibraries(File dir) {
    ArrayList libraries = new ArrayList();
    
    File root = getRootDir(dir);
    if (root == null) return null;
    
    for (int i=0; i<PATH_BUILD_LIBRARIES.length; i++) {
      File libFile = new File(root, PATH_BUILD_LIBRARIES[i]);
      if ( libFile.exists() && libFile.isFile()) {
        try {
          OsgiLibrary library = new OsgiLibrary(libFile);
          File src = new File(root, PATH_MAINLIB_SRC);
          if (src.exists()) {
            library.setSource(src.getAbsolutePath());
          }
          libraries.add(library);
        } catch (IOException e) {
          e.printStackTrace();
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
  public SystemPropertyGroup[] getSystemPropertyGroups() {
    ArrayList groups = new ArrayList();

    // Load and set properties
    InputStream is = null;
    try {
      try {
        is = OscarPlugin.getDefault().openStream(new Path(PATH_PROPERTY_FILE));
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
              SystemProperty property = new SystemProperty(propertyGroup, name);

              String description = props.getProperty(propBase+".description");
              if (description != null) {
                property.setDescription(description);
              }
              String defaultValue = props.getProperty(propBase+".default");
              if (defaultValue != null) {
                property.setDefaultValue(defaultValue);
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

}
