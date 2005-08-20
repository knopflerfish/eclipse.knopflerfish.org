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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.knopflerfish.eclipse.core.project.BundleResource;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FileTreeSelectionDialog extends ElementTreeSelectionDialog {

  private BundleResource bundleResource;
  
  // Widgets
  //private Text wDestinationText;
  
  public FileTreeSelectionDialog(Shell parent, IFile[] files) {
    super(parent, new ResourceTreeLabelProvider(), new FileTreeContentProvider(files));

    setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
    setValidator(new ResourceValidator(new int[] {IResource.FILE}));
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  /*
  protected Control createDialogArea(Composite parent) {
    Control c = super.createDialogArea(parent);
    
    Label wDestinationLabel = new Label((Composite) c, SWT.LEFT);
    wDestinationLabel.setText("Location to store library in bundle's JAR. ");
    wDestinationText = new Text((Composite) c, SWT.SINGLE | SWT.BORDER);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wDestinationText.setLayoutData(gd);

    return c;
  }
  */
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#updateOKStatus()
   */
  /*
  protected void updateOKStatus() {
    super.updateOKStatus();
    
    IStructuredSelection selection = 
      (IStructuredSelection) getTreeViewer().getSelection();
    
    IResource resource = (IResource) selection.getFirstElement();
    if (resource == null || !(resource instanceof IFile)) {
      wDestinationText.setText("");
    } else {
      IFile file = (IFile) resource;
      wDestinationText.setText(file.getFullPath().removeFirstSegments(1).toString());
    }
  }
  */
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    IResource r = (IResource) getResult()[0];
    /*
    String loc = wDestinationText.getText();
    
    if (r == null || loc == null || loc.trim().length()==0) {
      return;
    }
    */
    
    bundleResource = new BundleResource(BundleResource.TYPE_USER, r.getFullPath(), r.getFullPath().toString(), null);
    
    super.okPressed();
  }
  
  public BundleResource getResource() {
    return bundleResource;
  }
}

