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

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class Osgi {
  public static String NATURE_ID = "org.knopflerfish.eclipse.core.bundlenature";
  public static String BUILDER_ID = "org.knopflerfish.eclipse.core.bundlebuilder";

  // Extension points
  private static String EXTENSION_POINT_FRAMEWORKDEFINITION = "org.knopflerfish.eclipse.core.frameworkDefinition";
  private static String EXTENSION_POINT_BUNDLEREPOSITORYTYPE = "org.knopflerfish.eclipse.core.bundleRepositoryType";
  
  /****************************************************************************
   * Bundle Repository Methods
   ***************************************************************************/
  public static String[] getBundleRepositoryTypeNames()  {
    TreeSet definitions = new TreeSet();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName())) {
            definitions.add( configs[j].getAttribute("name") );
          }
        }
      }
    }
    
    return (String[]) definitions.toArray(new String[definitions.size()]);
  }
  
  public static String getBundleRepositoryTypeImage(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) {
            
            String image = configs[j].getAttribute("image");
            return image;
          }
        }
      }
    }
    
    return null;
  }

  public static String getBundleRepositoryTypeConfigDescription(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) {
            
            String description = configs[j].getAttribute("configDescription");
            return description;
          }
        }
      }
    }
    
    return null;
  }
  
  public static String getBundleRepositoryTypeId(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) {
            return extensions[i].getNamespace();
          }
        }
      }
    }
    
    return null;
  }
  
  public static IBundleRepositoryType getBundleRepositoryType(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) { 
            try {
              return (IBundleRepositoryType) configs[j].createExecutableExtension("class");
            } catch (CoreException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    
    return null;
  }

  public static IBundleRepositoryType[] getBundleRepositoryTypes()  {
    ArrayList repos = new ArrayList();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUNDLEREPOSITORYTYPE);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("bundleRepositoryType".equals(configs[j].getName())) {
            try {
              repos.add( configs[j].createExecutableExtension("class"));
            } catch (CoreException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    
    return (IBundleRepositoryType[]) repos.toArray(new IBundleRepositoryType[repos.size()]);
  }

  /****************************************************************************
   * Framework Definition Methods
   ***************************************************************************/
  public static String[] getFrameworkDefinitionNames()  {
    TreeSet definitions = new TreeSet();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_FRAMEWORKDEFINITION);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("frameworkDefinition".equals(configs[j].getName())) {
            definitions.add( configs[j].getAttribute("name") );
          }
        }
      }
    }
    
    return (String[]) definitions.toArray(new String[definitions.size()]);
  }
  
  public static String getFrameworkDefinitionImage(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_FRAMEWORKDEFINITION);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("frameworkDefinition".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) {
            
            String image = configs[j].getAttribute("image");
            return image;
          }
        }
      }
    }
    
    return null;
  }

  public static String getFrameworkDefinitionId(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_FRAMEWORKDEFINITION);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("frameworkDefinition".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) {
            return extensions[i].getNamespace();
          }
        }
      }
    }
    
    return null;
  }
  
  public static IFrameworkDefinition getFrameworkDefinition(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_FRAMEWORKDEFINITION);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("frameworkDefinition".equals(configs[j].getName()) && 
              name.equals(configs[j].getAttribute("name"))) { 
            try {
              return (IFrameworkDefinition) configs[j].createExecutableExtension("class");
            } catch (CoreException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    
    return null;
  }

  public static IFrameworkDefinition[] getFrameworkDefinitions()  {
    ArrayList definitions = new ArrayList();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_FRAMEWORKDEFINITION);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("frameworkDefinition".equals(configs[j].getName())) {
            try {
              definitions.add( configs[j].createExecutableExtension("class"));
            } catch (CoreException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    
    return (IFrameworkDefinition[]) definitions.toArray(new IFrameworkDefinition[definitions.size()]);
  }
  
  /****************************************************************************
   * Package methods
   ***************************************************************************/
  /*
  public static PackageDescription[] getExportedFrameworkPackages(IFrameworkDefinition framework) {
    if (framework == null) return null;
    
    return null;
  }
  
  public static PackageDescription[] getExportedBundlePackages(IFrameworkDefinition framework) {
    if (framework == null) return null;

    return null;
  }
  
  public static PackageDescription[] findExportedPackage(String packageName, IFrameworkDefinition framework) {
    if (packageName == null || framework == null) return null;

    return null;
  }
  */
  
  
  /****************************************************************************
   * Project methods
   ***************************************************************************/
  public static boolean isBundleProject(IJavaProject project) {
    try {
      return project.getProject().hasNature(Osgi.NATURE_ID);
    } catch (CoreException e) {
      return false;
    }
  }
}
