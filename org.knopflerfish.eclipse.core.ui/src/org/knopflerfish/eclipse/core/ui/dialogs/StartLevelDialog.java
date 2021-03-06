/*
 * Copyright (c) 2012-2012, KNOPFLERFISH project
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Anders Rim�n, Makewave
 * @see http://www.makewave.com/
 */
public class StartLevelDialog extends Dialog
{

  private static final String ERROR   = "error";
  private static final String WARNING = "warning";

  private static final int NUM_CHARS_WIDTH = 5;
  
  private final String        title;
  private int                startLevel;

  // Widgets
  private Text                wStartLevel;
  private Composite           wErrorComposite;
  private Label               wErrorMsgLabel;
  private Label               wErrorImgLabel;

  public StartLevelDialog(Shell parentShell, String title)
  {
    super(parentShell);

    this.title = title;
  }

  // ***************************************************************************
  // org.eclipse.jface.window.Window Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.
   * Shell)
   */
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
    newShell.setText(title);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.
   * Composite)
   */
  protected Control createContents(Composite parent)
  {
    Control c = super.createContents(parent);
    
    // Verify values
    wStartLevel.setData(ERROR, verifyStartLevel());
    updateStatus();
    return c;
  }

  // ***************************************************************************
  // org.eclipse.jface.dialogs.Dialog Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed()
  {

    // Set return code and close window
    setReturnCode(Window.OK);
    close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
   * .Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    composite.setLayout(layout);

    // Start Level
    Label wStartLevelLabel = new Label(composite, SWT.LEFT);
    wStartLevelLabel.setText("StartLevel:");
    wStartLevel = new Text(composite, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wStartLevel.setLayoutData(data);
    wStartLevel.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        // Verify path
        e.widget.setData(ERROR, verifyStartLevel());
        updateStatus();
      }
    });

    // Error composite
    wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 3;
    wErrorComposite.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wErrorComposite.setLayoutData(gd);
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench()
                                      .getSharedImages()
                                      .getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(gd);

    return composite;
  }

  // ***************************************************************************
  // Private Worker Methods
  // ***************************************************************************

  void updateStatus()
  {
    Control c = getDialogArea();
    if (c == null) return;

    // Loop through widgets checking for errors
    String error = (String) getData(c, ERROR);
    if (error != null) {
      setPageComplete(false);
      setMessage(error,
                 PlatformUI.getWorkbench().getSharedImages()
                           .getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
      return;
    }

    // Loop through widgets checking for warnings
    String warning = (String) getData(c, WARNING);
    if (warning != null) {
      setPageComplete(true);
      setMessage(warning,
                 PlatformUI.getWorkbench().getSharedImages()
                           .getImage(ISharedImages.IMG_OBJS_WARN_TSK));
      return;
    }

    // Everything is ok
    setPageComplete(true);
    setMessage(null, null);

  }

  private Object getData(Control c, String key)
  {
    if (c == null || key == null) return null;

    // Check if this control contains the key
    Object data = c.getData(key);
    if (data != null) return data;

    if (c instanceof Composite) {
      // Check if any children contain the key
      Control[] children = ((Composite) c).getChildren();
      if (children != null) {
        for (int i = 0; i < children.length; i++) {
          data = getData(children[i], key);
          if (data != null) return data;
        }
      }
    }
    return null;
  }

  private void setPageComplete(boolean complete)
  {
    getButton(IDialogConstants.OK_ID).setEnabled(complete);
  }

  private void setMessage(String msg, Image img)
  {
    if (msg == null) {
      wErrorComposite.setVisible(false);
    } else {
      wErrorComposite.setVisible(true);
      wErrorImgLabel.setImage(img);
      wErrorMsgLabel.setText(msg);
    }
  }

  // ***************************************************************************
  // Verify dialog methods
  // ***************************************************************************

  private String verifyStartLevel()
  {
    String level = wStartLevel.getText();

    try {
      startLevel = Integer.parseInt(level);
    } catch(Throwable t) {
      return "Start level must be an integer.";
    }
    if (startLevel < 0) {
       return "Start level must be positive.";
    }

    return null;
  }

  // ***************************************************************************
  // Public getters
  // ***************************************************************************

  public int getStartLevel()
  {
    return startLevel;
  }
}
