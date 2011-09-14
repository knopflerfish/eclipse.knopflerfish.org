/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
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

package org.knopflerfish.eclipse.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkPreference {

  final private static String PREF_LOCATION = "Location";
  final private static String PREF_SPECIFICATION_VERISION = "SpecificationVersion";
  final private static String PREF_TYPE = "Type";
  final private static String PREF_MAINCLASS = "MainClass";
  final private static String PREF_RUNTIME_LIBRARIES = "Libraries";
  final private static String PREF_BUNDLES = "Bundles";
  final private static String PREF_BUNDLE_DIRS = "BundleDirs";
  final private static String PREF_JAR = "Jar";
  final private static String PREF_SOURCE = "Source";
  final private static String PREF_PATH = "Path";
  final private static String PREF_USER_DEFINED = "UserDefined";
  final private static String PREF_DEFAULT_SETTINGS = "DefaultSettings";
  final private static String PREF_DEFAULT_DEFINITION = "DefaultDefinition";
  final private static String PREF_PROPERTY_GROUPS = "PropertyGroups";
  final private static String PREF_PROPERTY_DEFAULT = "Default";
  final private static String PREF_PROPERTY_DESCRIPTION = "Description";
  final private static String PREF_PROPERTY_ALLOWED = "Allowed";
  final private static String PREF_PROPERTY_TYPE = "Type";
  
  public static final String BUNDLE_ROOT_DIR = "--root--";
  
  private String name;
  private String specificationVersion;
  private String location;
  private String type;
  private boolean defaultDefinition;
  private boolean defaultSettings;
  private String mainClass;
  private final ArrayList<IOsgiLibrary> runtimeLibs = new ArrayList<IOsgiLibrary>();
  private final Map<String, List<IOsgiBundle>> bundles = new HashMap<String, List<IOsgiBundle>>();
  private final ArrayList<PropertyGroup> propertyGroups = new ArrayList<PropertyGroup>();

  public FrameworkPreference()
  {
  }

  public FrameworkPreference(Preferences node) throws BackingStoreException
  {
    load(node);
  }

  private void load(Preferences node) throws BackingStoreException
  {
    // Load preferences
    name = node.name();

    // Type
    type = node.get(PREF_TYPE, "");

    // Specification Version
    specificationVersion = node.get(PREF_SPECIFICATION_VERISION, null);

    // Location
    location = node.get(PREF_LOCATION, "");
    // Default Settings
    defaultSettings = "true".equalsIgnoreCase(node.get(PREF_DEFAULT_SETTINGS,
        "false"));
    // Main class
    mainClass = node.get(PREF_MAINCLASS, "");

    // Runtime Libraries
    Preferences librariesNode = node.node(PREF_RUNTIME_LIBRARIES);
    runtimeLibs.clear();
    int idx = 0;
    while (librariesNode.nodeExists("Library " + idx)) {
      Preferences libraryNode = librariesNode.node("Library " + idx);
      try {
        OsgiLibrary library = new OsgiLibrary(new File(libraryNode.get(
            PREF_JAR, "")));
        library.setSource(libraryNode.get(PREF_SOURCE, null));
        library.setUserDefined("true".equalsIgnoreCase(libraryNode.get(
            PREF_USER_DEFINED, "false")));
        runtimeLibs.add(library);
      } catch (Exception e) {
      }
      idx++;
    }

    // Default definition
    defaultDefinition = "true".equalsIgnoreCase(node.get(
        PREF_DEFAULT_DEFINITION, "false"));

    // Bundle directories
    Preferences bundleDirsNode = node.node(PREF_BUNDLE_DIRS);
    String[] bundleDirs = bundleDirsNode.childrenNames();
    bundles.clear();
    for (int i = 0; i < bundleDirs.length; i++) {
      Preferences bundleDirNode = bundleDirsNode.node(bundleDirs[i]);
      // Bundles in directory
      String path = bundleDirNode.get(PREF_PATH, null);
      if (path == null) {
        continue;
      }
      List<IOsgiBundle> l = new ArrayList<IOsgiBundle>();
      bundles.put(path, l);
      String[] bundleNames = bundleDirNode.childrenNames();
      for (int j = 0; j < bundleNames.length; j++) {
        Preferences bundleNode = bundleDirNode.node(bundleNames[j]);
        try {
          OsgiBundle bundle = new OsgiBundle(new File(bundleNode.get(PREF_JAR,
              "")));
          bundle.setSource(bundleNode.get(PREF_SOURCE, null));
          bundle.setUserDefined("true".equalsIgnoreCase(bundleNode.get(
              PREF_USER_DEFINED, "false")));
          l.add(bundle);
        } catch (Exception e) {
        }
      }
    }
    
    // Older preferences stored bundles in PREF_BUNDLES, keep in order to be backward compatible
    Preferences bundlesNode = node.node(PREF_BUNDLES);
    String[] bundleNames = bundlesNode.childrenNames();
    List<IOsgiBundle> l = new ArrayList<IOsgiBundle>();
    bundles.put(BUNDLE_ROOT_DIR, l);
    for (int i = 0; i < bundleNames.length; i++) {
      Preferences bundleNode = bundlesNode.node(bundleNames[i]);
      try {
        OsgiBundle bundle = new OsgiBundle(new File(bundleNode.get(PREF_JAR,
            "")));
        bundle.setSource(bundleNode.get(PREF_SOURCE, null));
        bundle.setUserDefined("true".equalsIgnoreCase(bundleNode.get(
            PREF_USER_DEFINED, "false")));
        l.add(bundle);
      } catch (Exception e) {
      }
    }
    
    // System Property Groups
    Preferences propertyGroupsNode = node.node(PREF_PROPERTY_GROUPS);
    propertyGroups.clear();
    String[] propertyGroupNodeNames = propertyGroupsNode.childrenNames();
    for (int i = 0; i < propertyGroupNodeNames.length; i++) {
      PropertyGroup propertyGroup = new PropertyGroup(propertyGroupNodeNames[i]);
      Preferences propertyGroupNode = propertyGroupsNode.node(propertyGroup
          .getName());

      // Properties
      String[] propertyNodeNames = propertyGroupNode.childrenNames();
      for (int j = 0; j < propertyNodeNames.length; j++) {
        Property property = new Property(propertyNodeNames[j]);
        Preferences propertyNode = propertyGroupNode.node(property.getName());

        // Default value
        String value = propertyNode.get(PREF_PROPERTY_DEFAULT, null);
        if (value != null && value.trim().length() > 0) {
          property.setDefaultValue(value);
          property.setValue(value);
        }
        // Type
        value = propertyNode.get(PREF_PROPERTY_TYPE, null);
        if (value != null && value.trim().length() > 0) {
          if (Property.FRAMEWORK_PROPERTY.equals(value)) {
            property.setType(value);
          } else {
            property.setType(Property.SYSTEM_PROPERTY);
          }
        }
        // Description
        value = propertyNode.get(PREF_PROPERTY_DESCRIPTION, null);
        if (value != null && value.trim().length() > 0) {
          property.setDescription(value);
        }

        // Allowed values
        value = propertyNode.get(PREF_PROPERTY_ALLOWED, null);
        if (value != null && value.trim().length() > 0) {
          ArrayList<String> values = new ArrayList<String>();
          StringTokenizer st = new StringTokenizer(value, ",");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            values.add(token);
          }
          property.setAllowedValues(values);
        }

        propertyGroup.addSystemProperty(property);
      }

      addSystemPropertyGroup(propertyGroup);
    }
  }

  protected void save(Preferences node) throws BackingStoreException
  {
    // Save preferences in node
    Preferences subNode = node.node(name);

    // Type
    if (type != null) {
      subNode.put(PREF_TYPE, type);
    } else {
      subNode.remove(PREF_TYPE);
    }

    // Specification Version
    if (specificationVersion != null) {
      subNode.put(PREF_SPECIFICATION_VERISION, specificationVersion);
    } else {
      subNode.remove(PREF_SPECIFICATION_VERISION);
    }

    // Location
    if (location != null) {
      subNode.put(PREF_LOCATION, location);
    } else {
      subNode.remove(PREF_LOCATION);
    }
    // Default Settings
    subNode.putBoolean(PREF_DEFAULT_SETTINGS, defaultSettings);
    // Main class
    if (mainClass != null) {
      subNode.put(PREF_MAINCLASS, mainClass);
    } else {
      subNode.remove(PREF_MAINCLASS);
    }
    // Runtime Libraries
    subNode.node(PREF_RUNTIME_LIBRARIES).removeNode();
    Preferences librariesNode = subNode.node(PREF_RUNTIME_LIBRARIES);
    for (int i = 0; i < runtimeLibs.size(); i++) {
      IOsgiLibrary library = runtimeLibs.get(i);
      Preferences libraryNode = librariesNode.node("Library " + i);
      libraryNode.put(PREF_JAR, library.getPath());
      if (library.getSource() != null) {
        libraryNode.put(PREF_SOURCE, library.getSource());
      }
      libraryNode.putBoolean(PREF_USER_DEFINED, library.isUserDefined());
    }

    // Default definition
    subNode.putBoolean(PREF_DEFAULT_DEFINITION, defaultDefinition);

    // Bundle directories
    subNode.node(PREF_BUNDLE_DIRS).removeNode();
    Preferences bundleDirsNode = subNode.node(PREF_BUNDLE_DIRS);
    for (Iterator<Entry<String, List<IOsgiBundle>>> i=bundles.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry<String, List<IOsgiBundle>> entry = i.next();
      String bundleDir = entry.getKey();
      if (BUNDLE_ROOT_DIR.equals(bundleDir)) {
        continue;
      }
      List<IOsgiBundle> l = entry.getValue();
      Preferences bundleDirNode = bundleDirsNode.node("Dir "+bundleDir.hashCode());
      bundleDirNode.put(PREF_PATH, bundleDir);
      for (int j = 0; j < l.size(); j++) {
        IOsgiBundle bundle = l.get(j);
        Preferences bundleNode = bundleDirNode.node("Bundle " + j);
        bundleNode.put(PREF_JAR, bundle.getPath());
        if (bundle.getSource() != null) {
          bundleNode.put(PREF_SOURCE, bundle.getSource());
        }
        bundleNode.putBoolean(PREF_USER_DEFINED, bundle.isUserDefined());
      }
      
    }
    
    // Older preferences stored bundles in PREF_BUNDLES, keep in order to be backward compatible
    subNode.node(PREF_BUNDLES).removeNode();
    Preferences bundlesNode = subNode.node(PREF_BUNDLES);
    List<IOsgiBundle> l = (List<IOsgiBundle>)bundles.get(BUNDLE_ROOT_DIR);
    if (l != null) {
      for (int i = 0; i < l.size(); i++) {
        IOsgiBundle bundle = l.get(i);
        Preferences bundleNode = bundlesNode.node("Bundle " + i);
        bundleNode.put(PREF_JAR, bundle.getPath());
        if (bundle.getSource() != null) {
          bundleNode.put(PREF_SOURCE, bundle.getSource());
        }
        bundleNode.putBoolean(PREF_USER_DEFINED, bundle.isUserDefined());
      }
    }

    // Property Groups
    subNode.node(PREF_PROPERTY_GROUPS).removeNode();
    Preferences propertyGroupsNode = subNode.node(PREF_PROPERTY_GROUPS);
    for (int i = 0; i < propertyGroups.size(); i++) {
      PropertyGroup propertyGroup = (PropertyGroup) propertyGroups.get(i);
      Preferences propertyGroupNode = propertyGroupsNode.node(propertyGroup
          .getName());

      // Properties
      Property[] properties = propertyGroup.getProperties();
      for (int j = 0; j < properties.length; j++) {
        Property property = properties[j];
        Preferences propertyNode = propertyGroupNode.node(property.getName());

        // Default value
        String value = property.getDefaultValue();
        if (value != null && value.trim().length() > 0) {
          propertyNode.put(PREF_PROPERTY_DEFAULT, value);
        }
        // Type
        value = property.getType();
        if (value != null && value.trim().length() > 0) {
          propertyNode.put(PREF_PROPERTY_TYPE, value);
        }
        // Description
        value = property.getDescription();
        if (value != null && value.trim().length() > 0) {
          propertyNode.put(PREF_PROPERTY_DESCRIPTION, value);
        }
        // Allowed values
        List<String> values = property.getAllowedValues();
        if (values != null && values.size() > 0) {
          StringBuffer buf = new StringBuffer();
          for (String s : values) {
            buf.append(s);
            buf.append(",");
          }
          // Remove last ','
          buf.setLength(buf.length() - 1);
          propertyNode.put(PREF_PROPERTY_ALLOWED, buf.toString());
        }
      }
    }
  }

  //***************************************************************************
  // Getters and Setters
  //***************************************************************************

  public boolean isDefaultDefinition()
  {
    return defaultDefinition;
  }

  /**
   * @param defaultDefinition
   *          The defaultDefinition to set.
   */
  public void setDefaultDefinition(boolean defaultDefinition)
  {
    this.defaultDefinition = defaultDefinition;
  }

  /**
   * @return Returns the type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType(String type)
  {
    this.type = type;
  }

  public String getSpecificationVersion()
  {
    return specificationVersion;
  }

  public void setSpecificationVersion(String specificationVersion)
  {
    this.specificationVersion = specificationVersion;
  }

  public String getName()
  {
    return name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @param location
   *          The location to set.
   */
  public void setLocation(String location)
  {
    this.location = location;
  }

  /**
   * @return Returns the location.
   */
  public String getLocation()
  {
    return location;
  }

  public boolean isDefaultSettings()
  {
    return defaultSettings;
  }

  public void setDefaultSettings(boolean defaultSettings)
  {
    this.defaultSettings = defaultSettings;
  }

  public String getMainClass()
  {
    return mainClass;
  }

  /**
   * @param mainClass
   *          The mainClass to set.
   */
  public void setMainClass(String mainClass)
  {
    this.mainClass = mainClass;
  }

  public IOsgiLibrary[] getRuntimeLibraries()
  {
    return runtimeLibs.toArray(new OsgiLibrary[runtimeLibs.size()]);
  }

  public void setRuntimeLibraries(IOsgiLibrary[] libraries)
  {
    runtimeLibs.clear();
    if (libraries == null)
      return;
    for (int i = 0; i < libraries.length; i++) {
      runtimeLibs.add(libraries[i]);
    }
  }

  public IOsgiBundle[] getBundles()
  {
    List<IOsgiBundle> l = new ArrayList<IOsgiBundle>();
    String[] dirs = getBundleDirectories();
    for(int i=0; i<dirs.length; i++) {
      l.addAll(Arrays.asList(getBundles(dirs[i])));
    }
    return l.toArray(new OsgiBundle[l.size()]);
  }
  
  public String[] getBundleDirectories()
  {
    return bundles.keySet().toArray(new String[bundles.size()]);
  }

  public IOsgiBundle[] getBundles(String dir)
  {
    List<IOsgiBundle> l = bundles.get(dir);
    if (l == null) {
      l = new ArrayList<IOsgiBundle>();
    }
    return (OsgiBundle[]) l.toArray(new OsgiBundle[l.size()]);
  }

  public void setBundles(String dir, IOsgiBundle[] libraries)
  {
    bundles.remove(dir);
    if (libraries == null)
      return;
    List<IOsgiBundle> l = new ArrayList<IOsgiBundle>(Arrays.asList(libraries));
    bundles.put(dir, l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IOsgiInstall#getSystemProperties()
   */
  public PropertyGroup[] getSystemPropertyGroups()
  {
    return (PropertyGroup[]) propertyGroups
        .toArray(new PropertyGroup[propertyGroups.size()]);
  }

  public void setSystemPropertyGroups(Collection<PropertyGroup> groups)
  {
    propertyGroups.clear();
    propertyGroups.addAll(groups);
  }

  public void addSystemPropertyGroup(PropertyGroup group)
  {
    if (group != null && !propertyGroups.contains(group)) {
      propertyGroups.add(group);
    }
  }

  public void clearSystemPropertyGroups()
  {
    propertyGroups.clear();
  }

  public Property findSystemProperty(String name)
  {
    for (PropertyGroup group : propertyGroups) {
      Property property = group.findSystemProperty(name);
      if (property != null)
        return property;
    }
    return null;
  }

}
