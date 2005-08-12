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

package org.knopflerfish.eclipse.core.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.knopflerfish.eclipse.core.Arguments;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;

/**
 * Implementation of OSGi launch configuration delegate.
 * 
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

  /* (non-Javadoc)
   * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
   */
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException {

    // Verify OSGi installation
    IOsgiInstall osgiInstall = verifyOsgiInstall(configuration);
    
    // Verify directory used for this OSGi configuration
    File instanceDir =  verifyInstanceDirectory(configuration);
    
    // Bundles
    Map bundleMap = verifyBundles(configuration);
    Map projectMap = verifyProjects(configuration);
    
    // Create configuration
    IOsgiLibrary[] osgiLibraries = osgiInstall.getRuntimeLibraries();
    ArrayList libraries = new ArrayList();
    if (osgiLibraries != null) {
      for (int i=0; i<osgiLibraries.length; i++) {
        libraries.add(osgiLibraries[i].getPath());
      }
    }
    VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
        osgiInstall.getMainClass(), 
        (String []) libraries.toArray(new String[libraries.size()]));
    runConfig.setWorkingDirectory(instanceDir.getAbsolutePath());
    
    // Create framework configuration
    IFrameworkDefinition framework = Osgi.getFrameworkDefinition(osgiInstall.getType());
    IFrameworkConfiguration conf = framework.createConfiguration(instanceDir.getAbsolutePath());
    
    // Set system properties
    conf.setSystemProperties(getSystemProperties(configuration));
    
    // Check if bundle cache shall be cleared
    conf.clearBundleCache(getStartClean(configuration));

    // Set initial start level
    conf.setStartLevel(getStartLevel(configuration));
    
    // Add bundles to be launched
    if (bundleMap != null) {
      for (Iterator i=bundleMap.entrySet().iterator();i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        conf.addBundle((IOsgiBundle) entry.getKey(), (BundleLaunchInfo) entry.getValue());
      }
    }
    
    // Add projects to be launched
    if (projectMap != null) {
      File jarDirectory = new File(instanceDir, "jars");
      if (!jarDirectory.exists()) {
        jarDirectory.mkdir();
      }
      for (Iterator i=projectMap.entrySet().iterator();i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        IJavaProject project = (IJavaProject) entry.getKey();
        // Build bundle jar
        try {
          // TODO: This shall be changed, takes to long time if many projects exist
          BundleProject bundleProject = new BundleProject(project);
          BundlePackDescription bundlePack = bundleProject.getBundlePackDescription();
          String name = project.getProject().getName()+ ".jar";
          File path = new File(jarDirectory, name);
          File jarFile = bundlePack.export(bundleProject, path.getAbsolutePath());
          IOsgiBundle bundle = new OsgiBundle(jarFile);
          conf.addBundle(bundle, (BundleLaunchInfo) entry.getValue());
        } catch (IOException e) {
          // Failed to create jar file
          e.printStackTrace();
        }
      }
    }
    
    Arguments args = null;
    try {
      args = conf.create();
    } catch (IOException e) {
      abort("Failed to create framework configuration.", e,
        IOsgiLaunchConfigurationConstants.ERR_CREATE_CONFIGURATION);
    }
    if (args.getProgramArguments() != null) {
      runConfig.setProgramArguments(args.getProgramArguments());
    }
    if (args.getVMArguments() != null) {
      runConfig.setVMArguments(args.getVMArguments());
    }
    
    // Verify JRE installation
    IVMInstall vm = verifyVMInstall(configuration);

    ISourceLookupDirector sourceLocator = new BundleLookupDirector();
    sourceLocator
        .setSourcePathComputer(getLaunchManager()
            .getSourcePathComputer(
                SourcePathComputer.ID)); //$NON-NLS-1$
    sourceLocator.initializeDefaults(configuration);
    launch.setSourceLocator(sourceLocator);
    
    // Launch the configuration
    IVMRunner runner = vm.getVMRunner(mode);
    runner.run(runConfig, launch, monitor);
  }

  /**
   * Verifies a OSGi vendor is specified by the given launch configuration,
   * and returns the OSGi vendor.
   * 
   * @param configuration launch configuration
   * @return the OSGi vendor specified by the given launch configuration
   * @exception CoreException if unable to retrieve the attribute or the 
   * attribute is unspecified
   */
  /*
  public IOsgiVendor verifyOsgiVendor(ILaunchConfiguration configuration) throws CoreException {
    String name = getOsgiVendorName(configuration);
    if (name == null) {
      abort("OSGi vendor name not specified.", null,
          IOsgiLaunchConfigurationConstants.ERR_UNSPECIFIED_VENDOR_NAME);
    }
    
    IOsgiVendor vendor = Osgi.getVendor(name);
    if (name == null) {
      abort("Could not find extension for OSGi vendor '"+name+"'", null,
          IOsgiLaunchConfigurationConstants.ERR_VENDOR_EXTENSION_NOT_FOUND);
    }
    return vendor;
  }
  */

  /**
   * Verifies a OSGi install is specified by the given launch configuration,
   * and returns the OSGi install.
   * 
   * @param configuration launch configuration
   * 
   * @return the OSGi install specified by the given launch configuration and vendor
   * @exception CoreException if unable to retrieve the attribute or the 
   * attribute is unspecified
   */
  public IOsgiInstall verifyOsgiInstall(ILaunchConfiguration configuration) throws CoreException {
    String name = getOsgiInstallName(configuration);
    if (name == null) {
      abort("OSGi installation name not specified.", null,
          IOsgiLaunchConfigurationConstants.ERR_UNSPECIFIED_INSTALL_NAME);
    }
    
    IOsgiInstall osgiInstall = Osgi.getOsgiInstall(name);
    if (osgiInstall == null) {
      abort("Could not find OSGi installation '"+name+"'", null,
          IOsgiLaunchConfigurationConstants.ERR_INSTALL_NOT_FOUND);
    }
    return osgiInstall;
  }
  
  /**
   * Verifies the instance directory specified by the given launch configuration,
   * and returns the directory.
   * 
   * @param configuration launch configuration
   * @return the instance directory specified by the given launch configuration
   * @exception CoreException if unable to retrieve the attribute or the 
   * attribute is unspecified
   */
  public File verifyInstanceDirectory(ILaunchConfiguration configuration) throws CoreException {
    String location = getInstanceDirectory(configuration);
    if (location == null || location.length() == 0) {
      abort("Instance directory not specified.", null,
          IOsgiLaunchConfigurationConstants.ERR_INSTANCE_DIR_INVALID);
    }
    
    File instanceDir = new File(location);
    if (!instanceDir.exists()) {
      if (!instanceDir.mkdirs()) {
        abort("Failed to create instance directory.", null,
            IOsgiLaunchConfigurationConstants.ERR_INSTANCE_DIR_INVALID);
      }
    }
    
    if (!instanceDir.isDirectory()) {
      abort("Instance directory is not a directory.", null,
          IOsgiLaunchConfigurationConstants.ERR_INSTANCE_DIR_INVALID);
    }
    return instanceDir;
  }

  /**
   * Verifies the bundles specified by the given launch configuration,
   * and returns a Map with bundles and start level.
   * 
   * @param configuration launch configuration
   * @return a map with bundles and start level
   * @exception CoreException if unable to retrieve the attribute or the 
   * attribute is unspecified
   */
  public Map verifyBundles(ILaunchConfiguration configuration) throws CoreException {
    Map map = getBundles(configuration);
    HashMap bundles  = new HashMap();
    for (Iterator i = map.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry entry = (Map.Entry) i.next();
        OsgiBundle bundle = new OsgiBundle(new File((String) entry.getKey()));
        BundleLaunchInfo info = new BundleLaunchInfo((String) entry.getValue());
        bundles.put(bundle, info);
      } catch (Exception e) {
        abort("Error in selected bundles.", e,
            IOsgiLaunchConfigurationConstants.ERR_BUNDLE_LIST);
      }
    }
    return bundles;
  }

  /**
   * Verifies the bundle projects specified by the given launch configuration,
   * and returns a Map with bundle projects and start level.
   * 
   * @param configuration launch configuration
   * @return a map with bundles and start level
   * @exception CoreException if unable to retrieve the attribute or the 
   * attribute is unspecified
   */
  public Map verifyProjects(ILaunchConfiguration configuration) throws CoreException {
    Map map = getProjects(configuration);
    HashMap projects  = new HashMap();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (Iterator i = map.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry entry = (Map.Entry) i.next();
        String name = (String) entry.getKey();
        
        IProject project = root.getProject(name);
        if (!project.exists()) {
          continue;
          /*
          abort("Bundle project '"+name+"' does not exist.", null,
              IOsgiLaunchConfigurationConstants.ERR_PROJECT_NOT_EXIST);
              */
        }
        
        if (!project.hasNature(Osgi.NATURE_ID)) {
          abort("Project '"+name+"' is not a bundle project.", null,
              IOsgiLaunchConfigurationConstants.ERR_PROJECT_WRONG_NATURE);
        }
        
        IJavaProject javaProject = JavaCore.create(project);
        BundleLaunchInfo info = new BundleLaunchInfo((String) entry.getValue());
        projects.put(javaProject, info);
      } catch (Exception e) {
        abort("Error in selected bundle projects.", e,
            IOsgiLaunchConfigurationConstants.ERR_PROJECT_LIST);
      }
    }
    return projects;
  }
  
  /**
   * Returns the OSGi vendor name specified by the given launch configuration,
   * or <code>null</code> if none.
   * 
   * @param configuration launch configuration
   * @return the OSGi vendor name specified by the given launch configuration, 
   * or <code>null</code> if none
   * @exception CoreException if unable to retrieve the attribute
   */
  /*
  public static String getOsgiVendorName(ILaunchConfiguration configuration) throws CoreException {
    String vendorName = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_VENDOR_NAME, (String) null);
    
    return vendorName;
  }
  */
  
  /**
   * Returns the OSGi install name specified by the given launch configuration,
   * or <code>null</code> if none.
   * 
   * @param configuration launch configuration
   * @return the OSGi installr name specified by the given launch configuration, 
   * or <code>null</code> if none
   * @exception CoreException if unable to retrieve the attribute
   */
  public static String getOsgiInstallName(ILaunchConfiguration configuration) throws CoreException {
    String installName = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, (String) null);
    
    return installName;
  }
  
  /**
   * Returns the instance direcory specified by the given launch configuration.
   * 
   * @param configuration launch configuration
   * @return the instance directory specified by the given launch configuration. 
   * @exception CoreException if unable to retrieve the attribute
   */
  public static String getInstanceDirectory(ILaunchConfiguration configuration) throws CoreException {
    String location = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR, (String) null);
    
    return location;
  }

  /**
   * Returns the bundles specified by the given launch configuration.
   * 
   * @param configuration launch configuration
   * @return the bundles specified by the given launch configuration. 
   * @exception CoreException if unable to retrieve the attribute
   */
  public static Map getBundles(ILaunchConfiguration configuration) throws CoreException {
    Map bundles = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, (Map) null);
    
    return bundles;
  }

  /**
   * Returns the bundle projects specified by the given launch configuration.
   * 
   * @param configuration launch configuration
   * @return the bundle projects specified by the given launch configuration. 
   * @exception CoreException if unable to retrieve the attribute
   */
  public static Map getProjects(ILaunchConfiguration configuration) throws CoreException {
    Map projects = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, (Map) null);
    
    return projects;
  }

  public static Map getSystemProperties(ILaunchConfiguration configuration) throws CoreException {
    Map properties = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, (Map) null);
    
    return properties;
  }

  public static boolean getStartClean(ILaunchConfiguration configuration) throws CoreException {
    if (configuration.getAttributes().containsKey(IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE) && 
        ((Boolean) configuration.getAttributes().get(IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE)).booleanValue()) {
      return true;
    } else {
      return false;
    }
  }

  public static int getStartLevel(ILaunchConfiguration configuration) throws CoreException {
    if (configuration.getAttributes().containsKey(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL) ) {
      return  ((Integer) configuration.getAttributes().get(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL)).intValue();
    } else {
      return 1;
    }
  }
}
