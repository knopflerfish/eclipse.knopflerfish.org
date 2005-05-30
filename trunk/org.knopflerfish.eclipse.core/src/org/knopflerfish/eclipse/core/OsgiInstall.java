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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Knopflerfish implementation of the IOsgiInstall interface. Use preferences
 * to save settings. 
 */
public class OsgiInstall implements IOsgiInstall {

  public static String TYPE_OSGI_R2 = "OSGi R2";
  public static String TYPE_OSGI_R3 = "OSGi R3";
  public static String TYPE_OSGI_R4 = "OSGi R4";
  
  public static String [] TYPES = {TYPE_OSGI_R2, TYPE_OSGI_R3, TYPE_OSGI_R4};

  private static String PREF_LOCATION               = "Location";
  private static String PREF_SPECIFICATION_VERISION = "SpecificationVersion";
  private static String PREF_TYPE                   = "Type";
  private static String PREF_MAINCLASS              = "MainClass";
  private static String PREF_LIBRARIES              = "Libraries";
  private static String PREF_BUNDLES                = "Bundles";
  private static String PREF_JAR                    = "Jar";
  private static String PREF_SOURCE                 = "Source";
  private static String PREF_DEFAULT_SETTINGS       = "DefaultSettings";
  private static String PREF_DEFAULT_DEFINITION     = "DefaultDefinition";
  private static String PREF_PROPERTIES             = "Properties";
  private static String PREF_PROPERTY_NAME          = "Name";
  private static String PREF_PROPERTY_DEFAULT       = "Default";
  private static String PREF_PROPERTY_DESCRIPTION   = "Description";
  private static String PREF_PROPERTY_GROUP         = "Group";
  private static String PREF_PROPERTY_ALLOWED       = "Allowed";
  
  private String name;
  private String specificationVersion;
  private String location;
  private String type = TYPE_OSGI_R3;
  private boolean defaultDefinition;
  private boolean defaultSettings;
  private String mainClass;
  private ArrayList libraries = new ArrayList();  
  private ArrayList bundles = new ArrayList();
  private ArrayList properties = new ArrayList();
  
  public OsgiInstall() {
  }
  
  public OsgiInstall(Preferences node) throws BackingStoreException {
    load(node);
  }
  
  private void load(Preferences node) throws BackingStoreException {
    // Load preferences
    name = node.name();
    
    // Type
    type = node.get(PREF_TYPE, "");
    
    // Specification Version 
    specificationVersion = node.get(PREF_SPECIFICATION_VERISION, null);
    
    // Location
    location = node.get(PREF_LOCATION, "");
    // Default Settings 
    defaultSettings = "true".equalsIgnoreCase(node.get(PREF_DEFAULT_SETTINGS, "false"));
    // Main class
    mainClass = node.get(PREF_MAINCLASS, "");
    
    // Libraries
    Preferences librariesNode = node.node(PREF_LIBRARIES);
    String [] libraryNames = librariesNode.childrenNames();
    libraries.clear();
    if (libraryNames != null) {
      for (int i=0; i<libraryNames.length; i++) {
        Preferences libraryNode = librariesNode.node(libraryNames[i]);
        try {
          OsgiLibrary library = new OsgiLibrary(new File(libraryNode.get(PREF_JAR, "")));
          library.setSourceDirectory(libraryNode.get(PREF_SOURCE, null));
          libraries.add(library);
        } catch (Exception e) {}
      }
    }

    // Default definition
    defaultDefinition = "true".equalsIgnoreCase(node.get(PREF_DEFAULT_DEFINITION, "false"));

    // Bundles
    Preferences bundlesNode = node.node(PREF_BUNDLES);
    String [] bundleNames = bundlesNode.childrenNames();
    bundles.clear();
    if (bundleNames != null) {
      for (int i=0; i<bundleNames.length; i++) {
        Preferences bundleNode = bundlesNode.node(bundleNames[i]);
        try {
          OsgiBundle bundle = new OsgiBundle(new File(bundleNode.get(PREF_JAR, "")));
          bundle.setSourceDirectory(bundleNode.get(PREF_SOURCE, null));
          bundles.add(bundle);
        } catch (Exception e) {}
      }
    }

    // Properties
    Preferences propertiesNode = node.node(PREF_PROPERTIES);
    properties.clear();
    int idx = 0;
    while (propertiesNode.nodeExists("Property "+idx)) {
      Preferences propertyNode = propertiesNode.node("Property "+idx);
      idx++;
      // Name
      String value = propertyNode.get(PREF_PROPERTY_NAME, null);
      if (value == null) continue;
      
      SystemProperty property = new SystemProperty(value);
      
      // Default value
      value = propertyNode.get(PREF_PROPERTY_DEFAULT, null);
      if (value != null && value.trim().length() > 0) {
        property.setDefaultValue(value);
      }
      // Description
      value = propertyNode.get(PREF_PROPERTY_DESCRIPTION, null);
      if (value != null && value.trim().length() > 0) {
        property.setDescription(value);
      }
      // Group
      value = propertyNode.get(PREF_PROPERTY_GROUP, null);
      if (value != null && value.trim().length() > 0) {
        property.setGroup(value);
      }
      
      // Allowed values
      value = propertyNode.get(PREF_PROPERTY_ALLOWED, null);
      if (value != null && value.trim().length() > 0) {
        ArrayList values = new ArrayList();
        StringTokenizer st = new StringTokenizer(value, ",");
        while(st.hasMoreTokens()) {
          String token = st.nextToken();
          values.add(token);
        }
        property.setAllowedValues(values);
      }
      
      properties.add(property);
    }
  }

  protected void save(Preferences node) throws BackingStoreException {
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
    // Libraries
    subNode.node(PREF_LIBRARIES).removeNode();
    Preferences librariesNode = subNode.node(PREF_LIBRARIES);
    for (int i=0; i<libraries.size(); i++) {
      OsgiLibrary library = (OsgiLibrary) libraries.get(i);
      Preferences libraryNode = librariesNode.node("Library "+i);
      libraryNode.put(PREF_JAR, library.getPath());
      if (library.getSourceDirectory() != null) {
        libraryNode.put(PREF_SOURCE, library.getSourceDirectory());
      }
    }
    
    // Default definition
    subNode.putBoolean(PREF_DEFAULT_DEFINITION, defaultDefinition);
    
    // Bundles
    subNode.node(PREF_BUNDLES).removeNode();
    Preferences bundlesNode = subNode.node(PREF_BUNDLES);
    for (int i=0; i<bundles.size(); i++) {
      OsgiBundle bundle = (OsgiBundle) bundles.get(i);
      Preferences bundleNode = bundlesNode.node("Bundle "+i);
      bundleNode.put(PREF_JAR, bundle.getPath());
      if (bundle.getSourceDirectory() != null) {
        bundleNode.put(PREF_SOURCE, bundle.getSourceDirectory());
      }
    }

    // Properties
    subNode.node(PREF_PROPERTIES).removeNode();
    Preferences propertiesNode = subNode.node(PREF_PROPERTIES);
    for (int i=0; i<properties.size(); i++) {
      SystemProperty property = (SystemProperty) properties.get(i);
      Preferences propertyNode = propertiesNode.node("Property "+i);
      // Name
      String value = property.getName();
      if (value != null && value.trim().length() > 0) {
        propertyNode.put(PREF_PROPERTY_NAME, value);
      }
      // Default value
      value = property.getDefaultValue();
      if (value != null && value.trim().length() > 0) {
        propertyNode.put(PREF_PROPERTY_DEFAULT, value);
      }
      // Description
      value = property.getDescription();
      if (value != null && value.trim().length() > 0) {
        propertyNode.put(PREF_PROPERTY_DESCRIPTION, value);
      }
      // Group
      value = property.getGroup();
      if (value != null && value.trim().length() > 0) {
        propertyNode.put(PREF_PROPERTY_GROUP, value);
      }
      // Allowed values
      List values = property.getAllowedValues();
      if (values != null && values.size() >0) {
        StringBuffer buf = new StringBuffer();
        for (int j=0; j<values.size();j++) {
          buf.append(values.get(j));
          buf.append(",");
        }
        // Remove last ','
        buf.setLength(buf.length()-1);
        propertyNode.put(PREF_PROPERTY_ALLOWED, buf.toString());
      }
    }
  }
  
  /****************************************************************************
   * Getters and Setters
   ***************************************************************************/
  
  public boolean isDefaultDefinition() {
    return defaultDefinition;
  }
  
  /**
   * @param defaultDefinition The defaultDefinition to set.
   */
  public void setDefaultDefinition(boolean defaultDefinition) {
    this.defaultDefinition = defaultDefinition;
  }

  /**
   * @return Returns the type.
   */
  public String getType() {
    return type;
  }
  
  /**
   * @param type The type to set.
   */
  public void setType(String type) {
    this.type = type;
  }

  public String getSpecificationVersion() {
    return specificationVersion;
  }
  
  public void setSpecificationVersion(String specificationVersion) {
    this.specificationVersion = specificationVersion;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiInstall#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param location The location to set.
   */
  public void setLocation(String location) {
    this.location = location;
  }
  /**
   * @return Returns the location.
   */
  public String getLocation() {
    return location;
  }

  public boolean isDefaultSettings() {
    return defaultSettings;
  }
  
  public void setDefaultSettings(boolean defaultSettings) {
    this.defaultSettings = defaultSettings;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiInstall#getMainClass()
   */
  public String getMainClass() {
    return mainClass;
  }

  /**
   * @param mainClass The mainClass to set.
   */
  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiInstall#getLibraries()
   */
  public IOsgiLibrary[] getLibraries() {
    return (OsgiLibrary[]) libraries.toArray(new OsgiLibrary[libraries.size()]);
  }

  public void setLibraries(ArrayList libraries) {
    this.libraries = libraries;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiInstall#getBundles()
   */
  public IOsgiBundle[] getBundles() {
    return (OsgiBundle[]) bundles.toArray(new OsgiBundle[bundles.size()]);
  }

  public void setBundles(ArrayList bundles) {
    this.bundles = bundles;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IOsgiInstall#getSystemProperties()
   */
  public SystemProperty[] getSystemProperties() {
    return (SystemProperty[]) properties.toArray(new SystemProperty[properties.size()]);
  }

  public void setSystemProperties(ArrayList properties) {
    this.properties = properties;
  }
}
