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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knopflerfish.eclipse.core.Arguments;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class FrameworkConfiguration implements IFrameworkConfiguration
{

  private static final int                      DEFAULT_STARTLEVEL     = 7;

  private static String                         PROPERTY_FRAMEWORK_DIR =
                                                                         "org.osgi.framework.dir";

  private File                                  workDir;
  private TreeMap<Integer, List<BundleElement>> bundles                =
                                                                         new TreeMap<Integer, List<BundleElement>>();
  private Map<String, Property>                 systemProperties;
  private boolean                               clean;
  private int                                   startLevel             =
                                                                         DEFAULT_STARTLEVEL;

  public FrameworkConfiguration(File dir)
  {
    this.workDir = dir;
  }

  /****************************************************************************
   * org.knopflerfish.eclipse.core.IFrameworkConfiguration methods
   ***************************************************************************/
  /*
   * (non-Javadoc)
   * 
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#create()
   */
  public Arguments create() throws IOException
  {
    Arguments args = new Arguments();
    ArrayList<String> programArgs = new ArrayList<String>();

    // Create xargs file
    File initFile = new File(workDir, "init.xargs");
    File restartFile = new File(workDir, "restart.xargs");

    // Start empty framework
    if (clean) {
      programArgs.add("-init");
      // Remove fwdir
      File fwDir = new File(workDir, "fwdir");
      if (fwDir.exists() && fwDir.isDirectory()) {
        deleteDir(fwDir);
      }
    }

    // Set framework dir
    Property propFrameworkDir = new Property(PROPERTY_FRAMEWORK_DIR);
    propFrameworkDir.setValue(workDir.getAbsolutePath() + "/fwdir");
    writeProperty(initFile, propFrameworkDir, false);
    writeProperty(restartFile, propFrameworkDir, false);

    // System properties
    if (systemProperties != null) {
      for (Map.Entry<String, Property> element : systemProperties.entrySet()) {
        writeProperty(initFile, element.getValue(), true);
        writeProperty(restartFile, element.getValue(), true);
      }
    }

    Property startLevelProp = new Property("org.osgi.framework.startlevel.beginning");
    startLevelProp.setType(Property.FRAMEWORK_PROPERTY);
    startLevelProp.setValue(Integer.toString(startLevel));
    writeProperty(initFile, startLevelProp, true);
    
    writeCommand(initFile, "-init", "", true);
   
    // Add install entries
    int currentLevel = -1;
    for (Map.Entry<Integer, List<BundleElement>> element : bundles.entrySet()) {
      Integer initLevel = element.getKey();

      // Set initial start level
      if (currentLevel != initLevel.intValue()) {
        writeCommand(initFile, "-initlevel", initLevel.toString(), true);
        currentLevel = initLevel.intValue();
      }

      // Add bundle install entries for this start level
      List<BundleElement> l = element.getValue();
      for (BundleElement e : l) {
        writeCommand(initFile, "-install", "file:" + e.getBundle().getPath(),
                     true);
      }
    }
    // Start level must be set via property
    // Setting it explicitly is not possible since KF 5.2.1, see issue #1
    // writeCommand(initFile, "-startlevel", Integer.toString(startLevel), true);

    // Add start entries
    for (Map.Entry<Integer, List<BundleElement>> element : bundles.entrySet()) {

      // Add bundle install entries for this start level
      List<BundleElement> l = element.getValue();
      for (BundleElement e : l) {
        if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START) {
          writeCommand(initFile, "-start", "file:" + e.getBundle().getPath(),
                       true);
        } else if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START_EAGERLY) {
          writeCommand(initFile, "-start_e", "file:" + e.getBundle().getPath(),
                       true);
        } else if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START_TRANSIENTLY) {
          writeCommand(initFile, "-start_pt",
                       "file:" + e.getBundle().getPath(), true);
        } else if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START_EAGERLY_TRANSIENTLY) {
          writeCommand(initFile, "-start_et",
                       "file:" + e.getBundle().getPath(), true);
        }
      }
    }

    args.setProgramArguments((String[]) programArgs.toArray(new String[programArgs.size()]));
    return args;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkConfiguration#getWorkingDirectory()
   */
  public File getWorkingDirectory()
  {
    return workDir;
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.knopflerfish.eclipse.core.IFrameworkConfiguration#addBundle(org.
   * knopflerfish.eclipse.core.IOsgiBundle,
   * org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo)
   */
  public void addBundle(IOsgiBundle bundle, BundleLaunchInfo info)
  {
    Integer startLevel = new Integer(info.getStartLevel());
    List<BundleElement> l = bundles.get(startLevel);
    if (l == null) {
      l = new ArrayList<BundleElement>();
    }
    l.add(new BundleElement(bundle, info));
    bundles.put(startLevel, l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkConfiguration#setSystemProperties
   * (java.util.Map)
   */
  public void setSystemProperties(Map<String, Property> properties)
  {
    systemProperties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkConfiguration#setStartClean(boolean
   * )
   */
  public void clearBundleCache(boolean clean)
  {
    this.clean = clean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.knopflerfish.eclipse.core.IFrameworkConfiguration#setStartLevel(int)
   */
  public void setStartLevel(int startLevel)
  {
    this.startLevel = startLevel;
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void writeCommand(File f, String cmd, String value, boolean append)
    throws IOException
  {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      buf.append(cmd);
      buf.append(" ");
      buf.append(value);
      buf.append("\n");

      writer.write(buf.toString());

    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  private void writeProperty(File f, Property property, boolean append)
    throws IOException
  {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      if (property.getType().equals(Property.FRAMEWORK_PROPERTY)) {
        buf.append("-F");
      } else {
        buf.append("-D");
      }
      buf.append(property.getName());
      buf.append("=");
      buf.append(property.getValue());
      buf.append("\n");

      writer.write(buf.toString());

    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public static boolean deleteDir(File dir)
  {
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

  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  class BundleElement
  {
    private final IOsgiBundle      bundle;
    private final BundleLaunchInfo launchInfo;

    BundleElement(IOsgiBundle bundle, BundleLaunchInfo info)
    {
      this.bundle = bundle;
      this.launchInfo = info;
    }

    public IOsgiBundle getBundle()
    {
      return bundle;
    }

    public BundleLaunchInfo getLaunchInfo()
    {
      return launchInfo;
    }
  }
}
