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

import java.util.ArrayList;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class TypeSelectionDialog extends ListDialog {

  public TypeSelectionDialog(Shell parentShell, IType[] input, String type) {
    super(parentShell);
    
    setContentProvider(new TypeContentProvider());
    setLabelProvider(new TypeLabelProvider(type));
    ArrayList elements = new ArrayList();
    elements.add(new Selection(null));
    if (input != null) {
      for(int i=0; i<input.length; i++) {
        elements.add(new Selection(input[i]));
      }
    }
    setInput((Selection[]) elements.toArray(new Selection[elements.size()]));
    
    setTitle("Select "+type);
    setMessage("Select "+type+" implementation from the list below.");
  }
  
  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  public class Selection {
    private IType type;
    
    Selection(IType type) {
      this.type = type;
    }
    
    public IType getType() {
      return type;
    }
  }
  
  class TypeContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
      
      return (Object []) inputElement;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
  }
  
  class TypeLabelProvider implements ILabelProvider {
    private String noTypeText;
    
    public TypeLabelProvider(String type) {
      StringBuffer buf = new StringBuffer("[No ");
      buf.append(type);
      buf.append("]");
      noTypeText = buf.toString();
    }
    
    public Image getImage(Object element) {
      return null;
    }

    public String getText(Object element) {
      Selection s = (Selection) element;
      if (s.getType() != null) {
        return s.getType().getFullyQualifiedName();
      } else {
        return noTypeText;
      }
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
  }
}
