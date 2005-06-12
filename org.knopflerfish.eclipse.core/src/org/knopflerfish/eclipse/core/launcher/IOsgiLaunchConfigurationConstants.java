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

/**
 * @author ar
 */
public interface IOsgiLaunchConfigurationConstants {

  public String PACKAGE = "org.knopflerfish.eclipse.core.launcher.";
  
  // Main Tab
  public String ATTR_OSGI_VENDOR_NAME   = PACKAGE+"vendor_name";
  public String ATTR_OSGI_INSTALL_NAME  = PACKAGE+"install_name";
  public String ATTR_OSGI_INSTANCE_DIR  = PACKAGE+"instance_dir";
  public String ATTR_OSGI_INSTANCE_INIT = PACKAGE+"instance_init";
  public String ATTR_PROPERTIES         = PACKAGE+"instance_properties";

  // Bundle Tab
  public String ATTR_BUNDLES            = PACKAGE+"bundles";
  public String ATTR_BUNDLE_PROJECTS    = PACKAGE+"bundle_projects";

  // Error codes
  public int ERR_UNSPECIFIED_MAIN_CLASS       = 0;
  public int ERR_UNSPECIFIED_VENDOR_NAME      = 1;
  public int ERR_VENDOR_EXTENSION_NOT_FOUND   = 2;
  public int ERR_UNSPECIFIED_INSTALL_NAME     = 3;
  public int ERR_INSTALL_NOT_FOUND            = 4;
  public int ERR_INSTANCE_DIR_INVALID         = 5;
  public int ERR_CREATE_CONFIGURATION         = 6;
  public int ERR_BUNDLE_LIST                  = 7;
  public int ERR_PROJECT_LIST                 = 8;
  public int ERR_PROJECT_NOT_EXIST            = 9;
  public int ERR_PROJECT_WRONG_NATURE         = 10;





}
