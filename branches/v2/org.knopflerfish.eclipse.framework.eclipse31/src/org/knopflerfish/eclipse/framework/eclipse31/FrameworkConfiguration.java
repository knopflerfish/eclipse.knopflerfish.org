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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.knopflerfish.eclipse.core.Arguments;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;

public class FrameworkConfiguration implements IFrameworkConfiguration {

  public static final String PROP_BUNDLES = "osgi.bundles"; //$NON-NLS-1$

  public static final String PROP_BUNDLES_STARTLEVEL = "osgi.bundles.defaultStartLevel"; //$NON-NLS-1$ //The start level used to install the bundles

  public static final String PROP_INITIAL_STARTLEVEL = "osgi.startLevel"; //$NON-NLS-1$ //The start level when the fwl start

  public static final String PROP_CLEAN = "osgi.clean"; //$NON-NLS-1$

  public static final String PROP_INSTALL_AREA = "osgi.install.area"; //$NON-NLS-1$

  public static final String PROP_IGNOREAPP = "eclipse.ignoreApp"; //$NON-NLS-1$

  private static final int DEFAULT_STARTLEVEL = 7;

  private File installationDir;
  
  private File workDir;

  private Map perBundlesLaunchInfo = new HashMap();

  private Map systemProperties;

  private boolean clean;

  private int startLevel = DEFAULT_STARTLEVEL;

  public FrameworkConfiguration(File installationDir, File dir) {
    this.installationDir = installationDir;
    this.workDir = dir;
  }

  /*****************************************************************************
   * org.knopflerfish.eclipse.core.IFrameworkDefinition methods
   ****************************************************************************/
  public Arguments create() throws IOException {
    Arguments args = new Arguments();
    ArrayList vmArgs = new ArrayList();
    ArrayList appArgs = new ArrayList();

    // Create system properties file
    File systemPropertiesFile = new File(workDir, "config.ini");
    Path path = new Path(workDir.getAbsolutePath());

    appArgs.add("-data");
    appArgs.add(path.toString());
    appArgs.add("-configuration");
    appArgs.add(path.toString());
    appArgs.add("-clean");
    appArgs.add("-consoleLog");

    // Set start level
    writeProperty(systemPropertiesFile, PROP_INITIAL_STARTLEVEL, Integer
        .toString(startLevel), true);

    // Set start level
    writeProperty(systemPropertiesFile, PROP_IGNOREAPP, "true", true);
    
    // Start empty framework
    if (clean) {
      // Remove bundle directories
      File[] children = workDir.listFiles();
      for (int i = 0; i < children.length; i++) {
        if (children[i].isDirectory()
            && children[i].getName().startsWith("bundle")) {
          deleteDir(children[i]);
        }
      }

      writeProperty(systemPropertiesFile, PROP_CLEAN, "true", true);
    }

    // System properties
    if (systemProperties != null) {
      for (Iterator i = systemProperties.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        writeProperty(systemPropertiesFile, (String) entry.getKey(),
            (String) entry.getValue(), true);
      }
    }

    // Add bundle
    StringBuffer bundles = new StringBuffer("");
    for (Iterator i = perBundlesLaunchInfo.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();

      IOsgiBundle bundle = (IOsgiBundle) entry.getKey();
      BundleLaunchInfo launchInfo = (BundleLaunchInfo) entry.getValue();

      if (bundles.length() > 0) {
        bundles.append(", ");
      }

      // bundles.append("file:");
      bundles.append(new File(bundle.getPath()).toURL().toString());

      bundles.append("@");
      if (launchInfo.getMode() == BundleLaunchInfo.MODE_START) {
        bundles.append("start");
        bundles.append(":");
      }
      bundles.append(launchInfo.getStartLevel());
    }
    if (bundles.length() > 0) {
      writeProperty(systemPropertiesFile, PROP_BUNDLES, bundles.toString(),
          true);
    }

    args.setVMArguments((String[]) vmArgs.toArray(new String[vmArgs.size()]));
    args.setProgramArguments((String[]) appArgs.toArray(new String[appArgs
                                                                   .size()]));
    return args;
  }
  
  public File getWorkingDirectory()
  {
    return installationDir;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#addBundle(org.knopflerfish.eclipse.core.IOsgiBundle,
   *      org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo)
   */
  public void addBundle(IOsgiBundle bundle, BundleLaunchInfo info) {
    perBundlesLaunchInfo.put(bundle, info);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#setSystemProperties(java.util.Map)
   */
  public void setSystemProperties(Map properties) {
    systemProperties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#setStartClean(boolean)
   */
  public void clearBundleCache(boolean clean) {
    this.clean = clean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#setStartLevel(int)
   */
  public void setStartLevel(int startLevel) {
    this.startLevel = startLevel;
  }

  /*****************************************************************************
   * Private utility methods
   ****************************************************************************/
  private void writeProperty(File f, String name, String value, boolean append)
      throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      buf.append(name);
      buf.append("=");
      buf.append(value);
      buf.append("\r\n");

      writer.write(buf.toString());

    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }
}
