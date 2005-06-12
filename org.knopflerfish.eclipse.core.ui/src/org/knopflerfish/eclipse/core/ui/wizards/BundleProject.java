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

package org.knopflerfish.eclipse.core.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.knopflerfish.eclipse.core.IOsgiInstall;

/**
 * @author ar
 */
public class BundleProject {
  // Project Settings
  private final String name;
  private IPath location = null;
  private IPath sourceFolder = null;
  private IPath outputFolder = null;
  private IOsgiInstall osgiInstall = null;
  
  // Bundle Information
  private String bundleSymbolicName;
  private String bundleName;
  private String bundleVersion;
  private String bundleDescription;
  private String bundleVendor;
  private boolean createBundleActivator;
  private String activatorClassName;
  private String activatorPackageName;


  public BundleProject(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public IPath getLocation() {
    return location;
  }
  
  public void setLocation(IPath location) {
    this.location = location;
  }

  public IPath getSourceFolder() {
    return sourceFolder;
  }
  
  public void setSourceFolder(IPath folder) {
    sourceFolder = folder;
  }

  public IPath getOutputFolder() {
    return outputFolder;
  }
  
  public void setOutputFolder(IPath folder) {
    outputFolder = folder;
  }
  
  public IOsgiInstall getOsgiInstall() {
    return osgiInstall;
  }
  
  public void setOsgiInstall(IOsgiInstall osgiInstall) {
    this.osgiInstall = osgiInstall;
  }
  
  /**
   * @return Returns the activatorPackageName.
   */
  public String getActivatorPackageName() {
    return activatorPackageName;
  }
  /**
   * @param activatorPackageName The activatorPackageName to set.
   */
  public void setActivatorPackageName(String activatorPackageName) {
    this.activatorPackageName = activatorPackageName;
  }
  
  /**
   * @return Returns the activatorClassName.
   */
  public String getActivatorClassName() {
    return activatorClassName;
  }
  /**
   * @param activatorClassName The activatorClassName to set.
   */
  public void setActivatorClassName(String activatorClassName) {
    this.activatorClassName = activatorClassName;
  }
  /**
   * @return Returns the bundleDescription.
   */
  public String getBundleDescription() {
    return bundleDescription;
  }
  /**
   * @param bundleDescription The bundleDescription to set.
   */
  public void setBundleDescription(String bundleDescription) {
    this.bundleDescription = bundleDescription;
  }
  /**
   * @return Returns the bundleName.
   */
  public String getBundleName() {
    return bundleName;
  }
  /**
   * @param bundleName The bundleName to set.
   */
  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }
  /**
   * @return Returns the bundleVendor.
   */
  public String getBundleVendor() {
    return bundleVendor;
  }
  /**
   * @param bundleVendor The bundleVendor to set.
   */
  public void setBundleVendor(String bundleVendor) {
    this.bundleVendor = bundleVendor;
  }
  /**
   * @return Returns the bundleVersion.
   */
  public String getBundleVersion() {
    return bundleVersion;
  }
  /**
   * @param bundleVersion The bundleVersion to set.
   */
  public void setBundleVersion(String bundleVersion) {
    this.bundleVersion = bundleVersion;
  }
  /**
   * @return Returns the createBundleActivator.
   */
  public boolean isCreateBundleActivator() {
    return createBundleActivator;
  }
  /**
   * @param createBundleActivator The createBundleActivator to set.
   */
  public void setCreateBundleActivator(boolean createBundleActivator) {
    this.createBundleActivator = createBundleActivator;
  }

  public String getBundleSymbolicName() {
    return bundleSymbolicName;
  }

  public void setBundleSymbolicName(String bundleSymbolicName) {
    this.bundleSymbolicName = bundleSymbolicName;
  }
}
