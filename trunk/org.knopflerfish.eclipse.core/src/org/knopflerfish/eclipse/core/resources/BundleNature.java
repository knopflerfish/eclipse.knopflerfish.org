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

package org.knopflerfish.eclipse.core.resources;

import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.knopflerfish.eclipse.core.Osgi;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleNature implements IProjectNature {

  private IProject project;
  
  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  public void configure() throws CoreException {
    // Add bundle builder
    addBuilder(Osgi.BUILDER_ID);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  public void deconfigure() throws CoreException {
    // Remove bundle builder
    removeBuilder(Osgi.BUILDER_ID);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  public IProject getProject() {
    return project;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
   */
  public void setProject(IProject project) {
    this.project = project;
  }

  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  private void addBuilder(String builderId) throws CoreException {
    IProjectDescription desc = project.getDescription();
    ICommand[] commands = desc.getBuildSpec();
    boolean found = false;

    for (int i = 0; i < commands.length; ++i) {
       if (commands[i].getBuilderName().equals(builderId)) {
          found = true;
          break;
       }
    }
    if (!found) { 
       //add builder to project
       ICommand command = desc.newCommand();
       command.setBuilderName(builderId);
       ICommand[] newCommands = new ICommand[commands.length + 1];

       // Add it after other builders.
       System.arraycopy(commands, 0, newCommands, 0, commands.length);
       newCommands[commands.length] = command;
       desc.setBuildSpec(newCommands);
       project.setDescription(desc, null);
    }
  }

  private void removeBuilder(String builderId) throws CoreException {
    IProjectDescription desc = project.getDescription();
    ICommand[] commands = desc.getBuildSpec();
    ArrayList list  = new ArrayList();
    boolean found = false;

    for (int i = 0; i < commands.length; ++i) {
       if (commands[i].getBuilderName().equals(builderId)) {
          found = true;
       } else {
         list.add(commands[i]);
       }
    }
    if (found) { 
       desc.setBuildSpec((ICommand []) list.toArray(new ICommand[list.size()]));
       project.setDescription(desc, null);
    }
    
  }
}
