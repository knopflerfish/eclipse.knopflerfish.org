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

package org.knopflerfish.eclipse.core.ui.dialogs;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ResourceValidator implements ISelectionStatusValidator {
  
  private int[] types;
  private String error;
  
  protected ResourceValidator(int[] types)  {
    if (types != null) {
      this.types = new int[types.length];
      System.arraycopy(types, 0, this.types, 0, types.length);
      Arrays.sort(this.types);
    } else {
      this.types = new int[0];
    }
    if (types == null || types.length == 0) {
      error = "Select resource";
    } else {
      StringBuffer buf = new StringBuffer("Select");
      for (int i=0; i<types.length; i++) {
        if(i > 0 && i == types.length-1) {
          buf.append(" or");
        } else if (i>0) {
          buf.append(",");
        }
        switch (types[i]) {
        case IResource.FILE:
          buf.append(" file");
          break;
        case IResource.FOLDER:
          buf.append(" folder");
          break;
        case IResource.PROJECT:
          buf.append(" project");
          break;
        }
      }
      error = buf.toString();
    }
  }
  
  public IStatus validate(Object[] selection) {
    if (selection == null || selection.length != 1) {
      Status status = new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core.ui", 
          IStatus.OK, error, null);
      return status;
    }
    
    IResource resource = (IResource) selection[0];
    int type = resource.getType();
    if (Arrays.binarySearch(types, type) < 0) {
      Status status = new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core.ui", 
          IStatus.OK, error, null);
      return status;
    }
    Status status = new Status(IStatus.INFO, "org.knopflerfish.eclipse.core.ui", 
        IStatus.OK, "", null);
    return status;
  }
  
}
