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

package org.knopflerfish.eclipse.repository.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Plugin;
import org.knopflerfish.eclipse.core.IBundleRepository;
import org.knopflerfish.eclipse.core.manifest.BundleIdentity;
import org.osgi.framework.BundleContext;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class RepositoryPlugin extends Plugin {

  // The shared instance.
  private static RepositoryPlugin plugin;

  static Map<String, IBundleRepository> repositoriesCache = new HashMap<String, IBundleRepository>();

  /**
   * The constructor.
   */
  public RepositoryPlugin()
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static RepositoryPlugin getDefault()
  {
    return plugin;
  }

  public String storeFile(InputStream is, String name, BundleIdentity id)
  {

    // Copy file to state location
    StringBuffer buf = new StringBuffer(id.getSymbolicName().getSymbolicName());
    buf.append("_");
    buf.append(id.getBundleVersion().toString());
    File stateDir = new File(getStateLocation().toFile(), buf.toString());
    File file = new File(stateDir, name);
    File parentDir = file.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }

    // Check that parent directory exist
    if (!file.exists()) {
      copyFile(is, file);
    }
    return file.getAbsolutePath();
  }

  private void copyFile(InputStream is, File dst)
  {
    // Read bundle activator template
    try {
      FileOutputStream fos = new FileOutputStream(dst);
      try {

        byte[] buf = new byte[256];
        int numRead = 0;
        while ((numRead = is.read(buf)) != -1) {
          fos.write(buf, 0, numRead);
        }
      } finally {
        fos.flush();
        fos.close();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
