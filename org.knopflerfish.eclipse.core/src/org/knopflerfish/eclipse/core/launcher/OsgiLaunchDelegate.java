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

import org.eclipse.core.resources.IFolder;
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
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.ProjectUtil;

/**
 * Implementation of OSGi launch configuration delegate.
 * 
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse
   * .debug.core.ILaunchConfiguration, java.lang.String,
   * org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
   */
  public void launch(ILaunchConfiguration configuration,
                     String mode,
                     ILaunch launch,
                     IProgressMonitor monitor) throws CoreException
  {

    // Verify framework distribution
    FrameworkPreference distribution = verifyFrameworkDistribution(configuration);

    // Verify directory used for this OSGi configuration
    File instanceDir = verifyInstanceDirectory(configuration);

    // Bundles
    Map bundleMap = verifyBundles(configuration);
    Map projectMap = verifyProjects(configuration);

    // Create configuration
    IOsgiLibrary[] osgiLibraries = distribution.getRuntimeLibraries();
    ArrayList libraries = new ArrayList();
    if (osgiLibraries != null) {
      for (int i = 0; i < osgiLibraries.length; i++) {
        libraries.add(osgiLibraries[i].getPath());
      }
    }
    VMRunnerConfiguration runConfig = new VMRunnerConfiguration(distribution
        .getMainClass(), (String[]) libraries.toArray(new String[libraries
        .size()]));

    // Create framework configuration
    IFrameworkDefinition framework = Osgi.getFrameworkDefinition(distribution
        .getType());
    IFrameworkConfiguration conf = framework.createConfiguration(distribution
        .getLocation(), instanceDir.getAbsolutePath());

    // Set system properties
    conf.setSystemProperties(getSystemProperties(configuration));

    // Check if bundle cache shall be cleared
    conf.clearBundleCache(getStartClean(configuration));

    // Set initial start level
    conf.setStartLevel(getStartLevel(configuration));

    // Add bundles to be launched
    if (bundleMap != null) {
      for (Iterator i = bundleMap.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        conf.addBundle((IOsgiBundle) entry.getKey(), (BundleLaunchInfo) entry
            .getValue());
      }
    }

    // Add projects to be launched
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    if (projectMap != null) {
      for (Iterator i = projectMap.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        IJavaProject project = (IJavaProject) entry.getKey();
        try {
          BundleProject bundleProject = new BundleProject(project);
          IFolder folder = root.getFolder(project.getOutputLocation());
          File jarFile = new File(folder.getLocation().toString(), ProjectUtil
              .createFileName(bundleProject));
          IOsgiBundle bundle = new OsgiBundle(jarFile);
          conf.addBundle(bundle, (BundleLaunchInfo) entry.getValue());
        } catch (IOException e) {
          abort("Error reading JAR file for bundle project ["
              + project.getProject().getName() + "].", e,
              IOsgiLaunchConfigurationConstants.ERR_BUNDLE_LIST);
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
    runConfig.setWorkingDirectory(conf.getWorkingDirectory().getAbsolutePath());

    if (args.getProgramArguments() != null) {
      runConfig.setProgramArguments(args.getProgramArguments());
    }
    if (args.getVMArguments() != null) {
      runConfig.setVMArguments(args.getVMArguments());
    }

    // Verify JRE installation
    IVMInstall vm = verifyVMInstall(configuration);

    ISourceLookupDirector sourceLocator = new BundleLookupDirector();
    sourceLocator.setSourcePathComputer(getLaunchManager()
        .getSourcePathComputer(SourcePathComputer.ID)); //$NON-NLS-1$
    sourceLocator.initializeDefaults(configuration);
    launch.setSourceLocator(sourceLocator);

    // Launch the configuration
    IVMRunner runner = vm.getVMRunner(mode);
    runner.run(runConfig, launch, monitor);
  }

  public FrameworkPreference verifyFrameworkDistribution(ILaunchConfiguration configuration)
    throws CoreException
  {
    String name = getFrameworkDistributionName(configuration);
    if (name == null) {
      abort("Framework name not specified.", null,
          IOsgiLaunchConfigurationConstants.ERR_UNSPECIFIED_INSTALL_NAME);
    }

    FrameworkPreference distribution = OsgiPreferences.getFramework(name);
    if (distribution == null) {
      abort("Could not find framework '" + name + "'", null,
          IOsgiLaunchConfigurationConstants.ERR_INSTALL_NOT_FOUND);
    }
    return distribution;
  }

  /**
   * Verifies the instance directory specified by the given launch
   * configuration, and returns the directory.
   * 
   * @param configuration
   *          launch configuration
   * @return the instance directory specified by the given launch configuration
   * @exception CoreException
   *              if unable to retrieve the attribute or the attribute is
   *              unspecified
   */
  public File verifyInstanceDirectory(ILaunchConfiguration configuration)
    throws CoreException
  {
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
   * Verifies the bundles specified by the given launch configuration, and
   * returns a Map with bundles and start level.
   * 
   * @param configuration
   *          launch configuration
   * @return a map with bundles and start level
   * @exception CoreException
   *              if unable to retrieve the attribute or the attribute is
   *              unspecified
   */
  public Map verifyBundles(ILaunchConfiguration configuration)
    throws CoreException
  {
    Map map = getBundles(configuration);
    HashMap bundles = new HashMap();
    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
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
   * @param configuration
   *          launch configuration
   * @return a map with bundles and start level
   * @exception CoreException
   *              if unable to retrieve the attribute or the attribute is
   *              unspecified
   */
  public Map verifyProjects(ILaunchConfiguration configuration)
    throws CoreException
  {
    Map map = getProjects(configuration);
    HashMap projects = new HashMap();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
      try {
        Map.Entry entry = (Map.Entry) i.next();
        String name = (String) entry.getKey();

        IProject project = root.getProject(name);
        if (!project.exists()) {
          continue;
        }

        if (!project.hasNature(Osgi.NATURE_ID)) {
          abort("Project '" + name + "' is not a bundle project.", null,
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

  public static String getFrameworkDistributionName(ILaunchConfiguration configuration)
    throws CoreException
  {
    String installName = configuration.getAttribute(
        IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, (String) null);

    return installName;
  }

  public static String getInstanceDirectory(ILaunchConfiguration configuration)
    throws CoreException
  {
    String location = configuration.getAttribute(
        IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR, (String) null);

    return location;
  }

  public static Map getBundles(ILaunchConfiguration configuration)
    throws CoreException
  {
    Map bundles = configuration.getAttribute(
        IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, (Map) null);

    return bundles;
  }

  public static Map getProjects(ILaunchConfiguration configuration)
    throws CoreException
  {
    Map projects = configuration.getAttribute(
        IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, (Map) null);

    return projects;
  }

  public static Map<String, Property> getSystemProperties(ILaunchConfiguration configuration)
    throws CoreException
  {
    Map<String, String> p = configuration.getAttribute(
        IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES,
        (Map<String, String>) null);
    if (p == null) {
      return null;
    }
    Map<String, Property> properties = new HashMap<String, Property>();
    for (Map.Entry<String, String> element : p.entrySet()) {
      String name = element.getKey();
      String value = element.getValue();
      String type = Property.SYSTEM_PROPERTY;
      if (value.startsWith(Property.SYSTEM_PROPERTY + ":")) {
        type = Property.SYSTEM_PROPERTY;
        value = value.substring(Property.SYSTEM_PROPERTY.length() + 1);
      } else if (value.startsWith(Property.FRAMEWORK_PROPERTY + ":")) {
        type = Property.FRAMEWORK_PROPERTY;
        value = value.substring(Property.FRAMEWORK_PROPERTY.length() + 1);
      }
      Property property = new Property(name);
      property.setValue(value);
      property.setType(type);
      properties.put(name, property);
    }

    return properties;
  }

  public static boolean getStartClean(ILaunchConfiguration configuration)
    throws CoreException
  {
    if (configuration.getAttributes().containsKey(
        IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE)
        && ((Boolean) configuration.getAttributes().get(
            IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE)).booleanValue()) {
      return true;
    }
    return false;
  }

  public static int getStartLevel(ILaunchConfiguration configuration)
    throws CoreException
  {
    if (configuration.getAttributes().containsKey(
        IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL)) {
      return ((Integer) configuration.getAttributes().get(
          IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL)).intValue();
    }
    return 1;
  }
}
