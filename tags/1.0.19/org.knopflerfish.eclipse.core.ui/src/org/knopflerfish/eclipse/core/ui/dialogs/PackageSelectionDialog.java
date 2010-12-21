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

package org.knopflerfish.eclipse.core.ui.dialogs;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class PackageSelectionDialog extends ElementListSelectionDialog {

  //private Map<String, List<PackageDescription>> packages;

  public PackageSelectionDialog(Shell activeShell, PackageLabelProvider provider)
  {
    super(activeShell, provider);
  }

  public void setPackages(Map<String, List<PackageDescription>> map)
  {
    //packages = map;
    setElements(map.keySet().toArray(new String[map.size()]));
  }

  // ***************************************************************************
  // org.eclipse.jface.dialogs.Dialog methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
   * .Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite wDialogComposite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    wDialogComposite.setLayout(layout);

    // Add default dialog area
    super.createDialogArea(wDialogComposite);

    return wDialogComposite;
  }
}
