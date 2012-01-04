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

package org.knopflerfish.eclipse.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knopflerfish.eclipse.core.internal.OsgiPlugin;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;

public class XArgsFile implements IXArgsFile
{
  // static public final String XARGS_DEFAULT = "default";
  static public final String                JARDIR_PROP    =
                                                             "org.knopflerfish.gosg.jars";
  static public final String                JARDIR_DEFAULT = "file:";

  private final File                        defDir;
  private final Map<String, IXArgsProperty> frameworkProps =
                                                             new HashMap<String, IXArgsProperty>();
  private final Map<String, IXArgsProperty> systemProps    =
                                                             new HashMap<String, IXArgsProperty>();
  private final Map<String, IXArgsBundle>   bundles        =
                                                             new HashMap<String, IXArgsBundle>();

  // Top directory (excluding trailing /)
  String                                    topDir         = "";

  private int                               startLevel;
  private int                               initialBundleStartLevel;
  private boolean                           clearPersistentData;

  public XArgsFile(final File rootDir, final String xargsPath)
    throws IOException
  {
    // Parse xargs file
    defDir = rootDir;

    String[] args = expandArgs(new String[]{
      "-xargs", xargsPath
    });

    for (int i = 0; i < args.length; i++) {

      if (args[i].startsWith(IXArgsProperty.TYPE_SYSTEM)) {
        IXArgsProperty p = new XArgsProperty(args[i]);
        systemProps.put(p.getName(), p);
      } else if (args[i].startsWith(IXArgsProperty.TYPE_FRAMEWORK)) {
        IXArgsProperty p = new XArgsProperty(args[i]);
        frameworkProps.put(p.getName(), p);
      } else if ("-init".equals(args[i])) {
        clearPersistentData = true;
      } else if ("-install".equals(args[i])) {
        if (i + 1 < args.length) {
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle =
            new XArgsBundle(location, initialBundleStartLevel);
          xArgsBundle.setStartMode(BundleLaunchInfo.MODE_INSTALL);
          bundles.put(location, xArgsBundle);
          i++;
        } else {
          String msg = "No URL for install command";
          error(xargsPath, i, msg);
        }
      } else if ("-istart".equals(args[i])) {
        if (i + 1 < args.length) {
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle =
            new XArgsBundle(location, initialBundleStartLevel);
          xArgsBundle.setStartMode(BundleLaunchInfo.MODE_START);
          bundles.put(location, xArgsBundle);
          i++;
        } else {
          String msg = "No URL for istart command";
          error(xargsPath, i, msg);
        }
      } else if ("-start".equals(args[i])) {
        if (i + 1 < args.length) {
          // Only support locations as ID
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle = bundles.get(location);
          if (xArgsBundle == null) {
            StringBuffer msg = new StringBuffer("ID [");
            msg.append(bundle);
            msg.append("] in start command does not match any installed bundles. Note: Only locations are supported as ID.");
            error(xargsPath, i, msg.toString());
          } else {
            xArgsBundle.setStartMode(BundleLaunchInfo.MODE_START);
          }
          i++;
        } else {
          String msg = "No ID for start command";
          error(xargsPath, i, msg);
        }
      } else if ("-start_e".equals(args[i])) {
        if (i + 1 < args.length) {
          // Only support locations as ID
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle = bundles.get(location);
          if (xArgsBundle == null) {
            StringBuffer msg = new StringBuffer("ID [");
            msg.append(bundle);
            msg.append("] in start_e command does not match any installed bundles. Note: Only locations are supported as ID.");
            error(xargsPath, i, msg.toString());
          } else {
            xArgsBundle.setStartMode(BundleLaunchInfo.MODE_START_EAGERLY);
          }
          i++;
        } else {
          String msg = "No ID for start_e command";
          error(xargsPath, i, msg);
        }
      } else if ("-start_et".equals(args[i])) {
        if (i + 1 < args.length) {
          // Only support locations as ID
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle = bundles.get(location);
          if (xArgsBundle == null) {
            StringBuffer msg = new StringBuffer("ID [");
            msg.append(bundle);
            msg.append("] in start_et command does not match any installed bundles. Note: Only locations are supported as ID.");
            error(xargsPath, i, msg.toString());
          } else {
            xArgsBundle.setStartMode(BundleLaunchInfo.MODE_START_EAGERLY_TRANSIENTLY);
          }
          i++;
        } else {
          String msg = "No ID for start_et command";
          error(xargsPath, i, msg);
        }
      } else if ("-start_pt".equals(args[i])) {
        if (i + 1 < args.length) {
          // Only support locations as ID
          final String bundle = args[i + 1];
          String location = completeLocation(bundle);
          IXArgsBundle xArgsBundle = bundles.get(location);
          if (xArgsBundle == null) {
            StringBuffer msg = new StringBuffer("ID [");
            msg.append(bundle);
            msg.append("] in start_pt command does not match any installed bundles. Note: Only locations are supported as ID.");
            error(xargsPath, i, msg.toString());
          } else {
            xArgsBundle.setStartMode(BundleLaunchInfo.MODE_START_TRANSIENTLY);
          }
          i++;
        } else {
          String msg = "No ID for start_pt command";
          error(xargsPath, i, msg);
        }
      } else if ("-initlevel".equals(args[i])) {
        if (i + 1 < args.length) {
          final int n = Integer.parseInt(args[i + 1]);
          setInitialBundleStartLevel(n);
          i++;
        }
      } else if ("-startlevel".equals(args[i])) {
        if (i + 1 < args.length) {
          final int n = Integer.parseInt(args[i + 1]);
          setStartLevel(n);
          i++;
        }
      } else {
        // Ignore unknown options
      }
    }

  }

  // ***************************************************************************
  // IXArgsFile implementation
  // ***************************************************************************

  public IXArgsProperty getFrameworkProperty(String name)
  {
    return (IXArgsProperty) frameworkProps.get(name);
  }

  public Set<String> getFrameworkPropertyNames()
  {
    return new HashSet<String>(frameworkProps.keySet());
  }

  public IXArgsProperty getSystemProperty(String name)
  {
    return (IXArgsProperty) systemProps.get(name);
  }

  public Set<String> getSystemPropertyNames()
  {
    return new HashSet<String>(systemProps.keySet());
  }

  public IXArgsProperty getProperty(String name)
  {
    IXArgsProperty p = getFrameworkProperty(name);
    if (p == null) {
      return getSystemProperty(name);
    } else {
      return p;
    }
  }

  public Set<IXArgsBundle> getBundles()
  {
    return new HashSet<IXArgsBundle>(bundles.values());
  }

  public int getStartLevel()
  {
    return startLevel;
  }

  public boolean clearPersistentData()
  {
    return clearPersistentData;
  }

  // ***************************************************************************
  // Private utility methods
  // ***************************************************************************

  private static void error(String path, int cmd, String msg)
  {
    StringBuffer buf = new StringBuffer("Error in xargs file [");
    buf.append(path);
    buf.append(", cmd #");
    buf.append(cmd);
    buf.append("] : ");
    buf.append(msg);
    OsgiPlugin.error(buf.toString(), null);
  }

  /**
   * Expand all occurance of <tt>-xarg &lt;URL&gt;</tt> and <tt>--xarg
   * &lt;URL&gt;</tt> into a new array without any <tt>-xargs</tt>,
   * <tt>--xargs</tt>.
   * 
   * @param argv
   *          array of command line options to expand all
   *          <tt>-xarg &lt;URL&gt;</tt> options in.
   * @return New argv array where all <tt>-xarg &lt;URL&gt;</tt> options have
   *         been expanded.
   */
  String[] expandArgs(String[] argv)
  {
    List<String> v = new ArrayList<String>();
    int i = 0;
    while (i < argv.length) {
      if ("-xargs".equals(argv[i]) || "--xargs".equals(argv[i])) {
        // if "--xargs", ignore any load errors of xargs file
        boolean bIgnoreException = argv[i].equals("--xargs");
        if (i + 1 < argv.length) {
          String xargsPath = argv[i + 1];
          i++;
          try {
            String[] moreArgs = loadArgs(xargsPath, argv);
            String[] r = expandArgs(moreArgs);
            for (int j = 0; j < r.length; j++) {
              v.add(r[j]);
            }
          } catch (RuntimeException e) {
            if (bIgnoreException) {
              // println("Failed to load -xargs " + xargsPath, 1, e);
            } else {
              throw e;
            }
          }
        } else {
          throw new IllegalArgumentException("-xargs without argument");
        }
      } else {
        v.add(argv[i]);
      }
      i++;
    }
    String[] r = new String[v.size()];

    v.toArray(r);
    return r;
  }

  /**
   * If the last to elements in args "-xargs" or "--xargs" then expand it with
   * arg as argument and replace the last element in args with the expansion.
   * Otherwise just add arg to args.
   * <p>
   * This expansion is necessarry to allow redefinition of a system property
   * after inclusion of xargs-file that sets the same property.
   * 
   * @param args
   *          The list to add elements to.
   * @param arg
   *          The element to add.
   */
  private void addArg(List<String> args, String arg)
  {
    if (0 == args.size()) {
      args.add(arg);
    } else {
      String lastArg = (String) args.get(args.size() - 1);
      if ("-xargs".equals(lastArg) || "--xargs".equals(lastArg)) {
        String[] exArgs = expandArgs(new String[]{
          lastArg, arg
        });
        args.remove(args.size() - 1);
        for (int i = 0; i < exArgs.length; i++) {
          args.add(exArgs[i]);
        }
      } else {
        args.add(arg);
      }
    }
  }

  /**
   * Helper method when OS shell does not allow long command lines. This method
   * has now days become the only reasonable way to start the framework due to
   * the amount of properties.
   * 
   * <p>
   * Loads a specified file or URL and creates a new String array where each
   * entry corresponds to entries in the loaded file.
   * </p>
   * 
   * <p>
   * File format:<br>
   * 
   * <ul>
   * <li>Each line starting with '-D' or '-F' is used dirctly as an entry in the
   * new command line array.
   * <li>Each line of length zero is ignored.
   * <li>Each line starting with '#' is ignored.
   * <li>Lines starting with '-' is used a command with optional argument after
   * space.
   * <li>All other lines is used directly as an entry to the new command line
   * array.
   * </ul>
   * </p>
   * 
   * @param xargsPath
   *          The URL to load the xargs-file from. The URL protcoll defaults to
   *          "file:". File URLs are first search for in the parent directory of
   *          the current FW-dir, then in the current working directory.
   * @param oldArgs
   *          The command line arguments as it looks before the file named in
   *          <tt>xargsPath</tt> have been expanded.
   * @return array with command line options loaded from <tt>xargsPath</tt>
   *         suitable to be merged into <tt>argv</tt> by the caller.
   */
  String[] loadArgs(String xargsPath, String[] oldArgs)
  {
    /*
     * if(XARGS_DEFAULT.equals(xargsPath)) { processProperties(oldArgs);
     * xargsPath = getDefaultXArgs(); }
     */

    // out result
    final List<String> v = new ArrayList<String>();

    BufferedReader in = null;
    try {

      // Check as file first, then as a URL
      // println("Searching for xargs file with '" +xargsPath +"'.", 2);

      // 1) Search in parent dir of the current framework directory
      // final String fwDirStr = Util.getFrameworkDir(fwProps);

      // avoid getAbsoluteFile() since some profiles don't have this
      // final File fwDir = new File(new File(fwDirStr).getAbsolutePath());

      // avoid getParentFile() since some profiles don't have this
      // final String defDirStr = fwDir.getParent();
      // final File defDir = defDirStr != null ? new File(defDirStr) : null;
      if (null != defDir) {
        // Make the file object absolute before calling exists(), see
        // http://forum.java.sun.com/thread.jspa?threadID=428403&messageID=2595075
        // for details.
        final File f = new File(new File(defDir, xargsPath).getAbsolutePath());
        // println(" trying " +f, 5);
        if (f.exists()) {
          // println("Loading xargs file " + f, 1);
          in = new BufferedReader(new FileReader(f));
        }
      }

      // 2) Search in the current working directory
      if (null == in) {
        // Make the file object absolute before calling exists(), see
        // http://forum.java.sun.com/thread.jspa?threadID=428403&messageID=2595075
        // for details.
        final File f = new File(new File(xargsPath).getAbsolutePath());
        // println(" trying " +f, 5);
        if (f.exists()) {
          // println("Loading xargs file " + f, 1);
          in = new BufferedReader(new FileReader(f));
        }
      }

      // 3) Try argument as URL
      if (in == null) {
        try {
          // println(" trying URL " +xargsPath, 5);
          final URL url = new URL(xargsPath);
          // println("Loading xargs url " + url, 0);
          in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (MalformedURLException e) {
          throw new IllegalArgumentException("Bad xargs URL " + xargsPath
                                             + ": " + e);
        }
      }

      StringBuffer contLine = new StringBuffer();
      String line = null;
      String tmpline = null;
      int lineno = 0;
      for (tmpline = in.readLine(); tmpline != null; tmpline = in.readLine()) {
        lineno++;
        tmpline = tmpline.trim();

        // check for line continuation char and
        // build up line until a line without such a mark is found.
        if (tmpline.endsWith("\\")) {
          // found continuation mark, store actual line to
          // buffered continuation line
          tmpline = tmpline.substring(0, tmpline.length() - 1);
          if (contLine == null) {
            contLine = new StringBuffer(tmpline);
          } else {
            contLine.append(tmpline);
          }
          // read next line
          continue;
        } else {
          // No continuation mark, gather stored line + newly read line
          if (contLine != null) {
            contLine.append(tmpline);
            line = contLine.toString();
            contLine = null;
          } else {
            // this is the normal case if no continuation char is found
            // or any buffered line is found
            line = tmpline;
          }
        }

        if (line.startsWith("-D")) {
          // Preserve System property
          addArg(v, line);
        } else if (line.startsWith("-F")) {
          // Preserve framework property
          addArg(v, line);
        } else if (line.startsWith("#")) {
          // Ignore comments
        } else if (line.startsWith("-")) {
          // Split command that contains a ' ' int two args
          int i = line.indexOf(' ');
          if (i != -1) {
            addArg(v, line.substring(0, i));
            line = line.substring(i).trim();
            if (line.length() > 0) {
              addArg(v, line);
            }
          } else {
            addArg(v, line);
          }
        } else if (line.length() > 0) {
          // Add argument
          addArg(v, line);
        }
      }

      // Write to framework properties. This should be the primary
      // source for all code, including the framework itself.
      // framework.props.setProperties(sysProps);

    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new IllegalArgumentException("xargs loading failed: " + e);
    } finally {
      if (null != in) {
        try {
          in.close();
        } catch (IOException ignore) {
        }
      }
    }

    final String[] args2 = new String[v.size()];
    v.toArray(args2);

    return args2;
  }

  /**
   * Complete location relative to topDir.
   * 
   * @param location
   *          The location to be completed.
   */
  private String completeLocation(String location)
  {
    String[] base = getJarBase();
    // Handle file: case where topDir is not ""
    if (location.startsWith("file:jars/") && !topDir.equals("")) {
      location =
        ("file:" + topDir + "/" + location.substring(5)).replace('\\', '/');
      // println("mangled bundle location to " + location, 2);
    }
    int ic = location.indexOf(":");
    if (ic < 2 || ic > location.indexOf("/")) {
      // println("location=" + location, 2);
      // URL without protocol complete it.
      for (int i = 0; i < base.length; i++) {
        // println("base[" + i + "]=" + base[i], 2);
        try {
          URL url = new URL(new URL(base[i]), location);

          // println("check " + url, 2);
          if ("file".equals(url.getProtocol())) {
            File f1 = new File(defDir, url.getFile()).getAbsoluteFile();
            if (!f1.exists() || !f1.canRead()) {
              File f = new File(url.getFile()).getAbsoluteFile();
              if (!f.exists() || !f.canRead()) {
                continue; // Noope; try next.
              }
            } else {
              url = new URL("file", null, f1.getAbsolutePath());
            }
          } else if ("http".equals(url.getProtocol())) {
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.connect();
            int rc = uc.getResponseCode();
            uc.disconnect();
            if (rc != HttpURLConnection.HTTP_OK) {
              // println("Can't access HTTP bundle: " + url + ", response code="
              // + rc, 0);
              continue; // Noope; try next.
            }
          } else {
            // Generic case; Check if we can read data from this URL
            InputStream is = null;
            try {
              is = url.openStream();
            } finally {
              if (is != null) is.close();
            }
          }
          location = url.toString();
          // println("found location=" + location, 5);
          break; // Found.
        } catch (Exception _e) {
        }
      }
    }
    return location;
  }

  String[] getJarBase()
  {
    IXArgsProperty p = getProperty(JARDIR_PROP);
    String jars = null;
    if (p != null) {
      jars = p.getValue();
    }
    if (jars == null) {
      jars = JARDIR_DEFAULT;
    }

    String[] base = Util.splitwords(jars, ";");
    for (int i = 0; i < base.length; i++) {
      try {
        base[i] = new URL(base[i]).toString();
      } catch (Exception ignored) {
      }
      // println("jar base[" + i + "]=" + base[i], 3);
    }

    return base;
  }

  private void setStartLevel(int n)
  {
    startLevel = n;
  }

  private void setInitialBundleStartLevel(int n)
  {
    initialBundleStartLevel = n;
  }

}
