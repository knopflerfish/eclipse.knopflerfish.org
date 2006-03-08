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

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.ui.dialogs.BuildPathLabelProvider;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ImportDialogEditor extends DialogCellEditor {

  private BuildPath[] elements;
  
  public ImportDialogEditor(Composite parent) {
    super(parent);
  }

  public void setElements(BuildPath[] elements) {
    this.elements = elements;
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.DialogCellEditor methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
   */
  protected Object openDialogBox(Control cellEditorWindow) {
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        Display.getCurrent().getActiveShell(), new BuildPathLabelProvider());
    dialog.setElements(elements);
    dialog.setMultipleSelection(true);
    dialog.setTitle("Select bundle");
    dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY));
    dialog.setMessage("Select bundle to import package from.");
    if (dialog.open() == Window.OK) {
      return dialog.getFirstResult();
    }
    return null;
  }

  
}
