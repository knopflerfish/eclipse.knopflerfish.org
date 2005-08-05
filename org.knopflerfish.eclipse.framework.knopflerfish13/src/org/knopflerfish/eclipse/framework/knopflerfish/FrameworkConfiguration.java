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

package org.knopflerfish.eclipse.framework.knopflerfish;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.knopflerfish.eclipse.core.Arguments;
import org.knopflerfish.eclipse.core.IFrameworkConfiguration;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkConfiguration implements IFrameworkConfiguration {

  private static final int DEFAULT_STARTLEVEL = 7;
  
  private static String PROPERTY_FRAMEWORK_DIR = "org.osgi.framework.dir";
  
  private File workDir;
  private TreeMap bundles = new TreeMap(); // Startlevel (Integer), List of BundleElement
  private Map systemProperties;
  private boolean clean;
  
  public FrameworkConfiguration(File dir) {
    this.workDir = dir;
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.IFrameworkDefinition methods
   ***************************************************************************/
  public Arguments create() throws IOException {
    Arguments args = new Arguments();
    ArrayList programArgs = new ArrayList();
    
    // Create xargs file
    File initFile = new File(workDir, "init.xargs");
    File restartFile = new File(workDir, "restart.xargs");
    
    // Start empty framework
    if (clean) {
      programArgs.add("-init");
    }
    
    // Set framework dir
    writeProperty(initFile, PROPERTY_FRAMEWORK_DIR, workDir.getAbsolutePath()+"/fwdir", false);
    writeProperty(restartFile, PROPERTY_FRAMEWORK_DIR, workDir.getAbsolutePath()+"/fwdir", false);
    
    // System properties
    if (systemProperties != null) {
      for(Iterator i=systemProperties.entrySet().iterator();i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        writeProperty(initFile, (String) entry.getKey(), (String) entry.getValue(), true);
        writeProperty(restartFile, (String) entry.getKey(), (String) entry.getValue(), true);
      }
    }

    writeCommand(initFile, "-init", "", true);
    
    // Add install entries
    int currentLevel = -1;
    for (Iterator i=bundles.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      Integer initLevel = (Integer) entry.getKey();
      
      // Set initial start level
      if (currentLevel != initLevel.intValue()) {
        writeCommand(initFile, "-initlevel", initLevel.toString(), true);
        currentLevel = initLevel.intValue();
      }
      
      // Add bundle install entries for this start level
      ArrayList l = (ArrayList) entry.getValue();
      for (Iterator j = l.iterator() ; j.hasNext() ;) {
        BundleElement e = (BundleElement) j.next();
        writeCommand(initFile, "-install", "file:"+e.getBundle().getPath(), true);
      }
    }
    // Set start level and launch
    writeCommand(initFile, "-startlevel", Integer.toString(DEFAULT_STARTLEVEL), true);
    writeCommand(initFile, "-launch", "", true);
    
    
    // Add start entries
    for (Iterator i=bundles.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      
      // Add bundle install entries for this start level
      ArrayList l = (ArrayList) entry.getValue();
      for (Iterator j = l.iterator() ; j.hasNext() ;) {
        BundleElement e = (BundleElement) j.next();
        if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START) {
          writeCommand(initFile, "-start", "file:"+e.getBundle().getPath(), true);
        }
      }
    }
    
    args.setProgramArguments((String[]) programArgs.toArray(new String[programArgs.size()]));
    return args;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#addBundle(org.knopflerfish.eclipse.core.IOsgiBundle, org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo)
   */
  public void addBundle(IOsgiBundle bundle, BundleLaunchInfo info) {
    Integer startLevel = new Integer(info.getStartLevel());
    ArrayList l = (ArrayList) bundles.get(startLevel);
    if (l == null) {
      l = new ArrayList();
    }
    l.add(new BundleElement(bundle, info));
    bundles.put(startLevel, l);
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#setSystemProperties(java.util.Map)
   */
  public void setSystemProperties(Map properties) {
    systemProperties = properties;
  }

  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.IFrameworkConfiguration#setStartClean(boolean)
   */
  public void setStartClean(boolean clean) {
    this.clean = clean;
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void writeCommand(File f, String cmd, String value, boolean append) throws IOException {
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
  
  private void writeProperty(File f, String name, String value, boolean append) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      buf.append("-D");
      buf.append(name);
      buf.append("=");
      buf.append(value);
      buf.append("\n");
      
      writer.write(buf.toString());
      
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }
  
  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  class BundleElement {
    private final IOsgiBundle bundle;
    private final BundleLaunchInfo launchInfo;
    
    BundleElement(IOsgiBundle bundle, BundleLaunchInfo info) {
      this.bundle = bundle;
      this.launchInfo = info;
    }
    
    public IOsgiBundle getBundle() {
      return bundle;
    }

    public BundleLaunchInfo getLaunchInfo() {
      return launchInfo;
    }
  }

}
