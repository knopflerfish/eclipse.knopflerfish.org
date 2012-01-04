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

package org.knopflerfish.eclipse.core.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.classpath.ClasspathUtil;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiPlugin extends Plugin implements IResourceChangeListener, IVMInstallChangedListener {
  //The shared instance.
  private static OsgiPlugin plugin;
  //Resource bundle.
  private ResourceBundle resourceBundle;
  
  private final static String JRE_ENVIRONMENT            = "Default JRE";
  private final static String CDC_FOUNDATION_ENVIRONMENT = "CDC-1.0/Foundation-1.0";
  private final static String OSGI_MINIMUM_ENVIRONMENT   = "OSGi/Minimum-1.0";
  
  /**
   * The constructor.
   */
  public OsgiPlugin() {
    super();
    plugin = this;
    try {
      resourceBundle = ResourceBundle.getBundle("org.knopflerfish.eclipse.core.OsgiPluginResources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }
  
  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    
    // Check if execution environments shall be added
    EnvironmentPreference defaultEnvironment = OsgiPreferences.getDefaultExecutionEnvironment();
    ArrayList environments = new ArrayList(Arrays.asList(OsgiPreferences.getExecutionEnvironments()));
    boolean changed = false;
    // Default JRE environment
    if (OsgiPreferences.getExecutionEnvironment(JRE_ENVIRONMENT) == null) {
      EnvironmentPreference environment = new EnvironmentPreference();
      environment.setName(JRE_ENVIRONMENT);
      environment.setType(EnvironmentPreference.TYPE_JRE);
      if (defaultEnvironment == null) {
        environment.setDefaultEnvironment(true);
        defaultEnvironment = environment;
      }
      environments.add(environment);
      changed = true;
    }
    // CDC Foundation environment
    if (OsgiPreferences.getExecutionEnvironment(CDC_FOUNDATION_ENVIRONMENT) == null) {
      EnvironmentPreference environment = new EnvironmentPreference();
      environment.setName(CDC_FOUNDATION_ENVIRONMENT);
      environment.setType(EnvironmentPreference.TYPE_OSGI);
      // Copy libraries to state location
      File file = new File(getStateLocation().toFile(), "ee.foundation.jar");
      if (!file.exists()) {
        copyFile(new Path("resources/ee.foundation.jar"), file);
      }
      OsgiLibrary lib = new OsgiLibrary(file);
      lib.setSource(file.getAbsolutePath());
      environment.setLibraries(new OsgiLibrary[] {lib});
      if (defaultEnvironment == null) {
        environment.setDefaultEnvironment(true);
        defaultEnvironment = environment;
      }
      environments.add(environment);
      changed = true;
    }
    // OSGi Minimum environment
    if (OsgiPreferences.getExecutionEnvironment(OSGI_MINIMUM_ENVIRONMENT) == null) {
      EnvironmentPreference environment = new EnvironmentPreference();
      environment.setName(OSGI_MINIMUM_ENVIRONMENT);
      environment.setType(EnvironmentPreference.TYPE_OSGI);
      // Copy libraries to state location
      File file = new File(getStateLocation().toFile(), "ee.minimum.jar");
      if (!file.exists()) {
        copyFile(new Path("resources/ee.minimum.jar"), file);
      }
      OsgiLibrary lib = new OsgiLibrary(file);
      lib.setSource(file.getAbsolutePath());
      environment.setLibraries(new OsgiLibrary[] {lib});
      if (defaultEnvironment == null) {
        environment.setDefaultEnvironment(true);
        defaultEnvironment = environment;
      }
      environments.add(environment);
      changed = true;
    }
    
    if (changed) {
      OsgiPreferences.setExecutionEnvironment((EnvironmentPreference[]) environments.toArray(new EnvironmentPreference[environments.size()]));
    }

    // Register vm change listener
    ClasspathUtil.updateEnvironmentContainers();
    JavaRuntime.addVMInstallChangedListener(this);
    
    // Register resoure listener
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
  }
  
  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    
    // Unregister vm change listener
    JavaRuntime.removeVMInstallChangedListener(this);
    
    // Unregister resoure listener
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.removeResourceChangeListener(this);
  }
  
  /**
   * Returns the shared instance.
   */
  public static OsgiPlugin getDefault() {
    return plugin;
  }
  
  /**
   * Returns the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = OsgiPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }
  
  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }
  
  /****************************************************************************
   * org.eclipse.core.resources.IResourceChangeListener methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   */
  public void resourceChanged(IResourceChangeEvent event) {
    try {
      ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
      event.getDelta().accept(visitor);
    } catch (CoreException e) {
      OsgiPlugin.log(e.getStatus());
    }
  }

  /****************************************************************************
   * org.eclipse.jdt.launching.IVMInstallChangedListener methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
   */
  public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
    // Update execution environment container
    ClasspathUtil.updateEnvironmentContainers();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
   */
  public void vmChanged(PropertyChangeEvent event) {
    // Update execution environment container
    ClasspathUtil.updateEnvironmentContainers();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
   */
  public void vmAdded(IVMInstall vm) {
    ClasspathUtil.updateEnvironmentContainers();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
   */
  public void vmRemoved(IVMInstall vm) {
    // Update execution environment container
    ClasspathUtil.updateEnvironmentContainers();
  }
  
  /****************************************************************************
   * Public utility methods
   ***************************************************************************/
  
  public static void throwCoreException(String message, Throwable t) throws CoreException {
    IStatus status =
      new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core", IStatus.OK, message, t);
    throw new CoreException(status);
  }

  public static void error(String message, Throwable t) {
    log(new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core", IStatus.OK, message, t));
  }
  
  public static void log(IStatus status) {
    getDefault().getLog().log(status);
  }
  
  private void copyFile(IPath src, File dst) {
    // Read bundle activator template
    try {
      InputStream is = null;
      FileOutputStream fos = new FileOutputStream(dst);
      try {
        is = getDefault().openStream(src);
        
        byte [] buf = new byte[256];
        int numRead = 0;
        while( (numRead = is.read(buf)) != -1) {
          fos.write(buf, 0, numRead);
        }
      } finally {
        if (is != null) is.close();
        fos.flush();
        fos.close();
      }
    } catch (Throwable t) {
      IStatus status =
        new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core", IStatus.OK, 
            "Failure copying file", t);
      OsgiPlugin.log(status);
    }
  }
}
