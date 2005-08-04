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

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiLibrary;

public class LibraryDialog extends Dialog {

  private static final int NUM_CHARS_WIDTH = 60;
  
  private IOsgiLibrary library;
  private final String title;
  
  // Widgets
  private Text    wLibraryText;
  private Button  wLibraryButton;
  private Text    wSourceText;
  
  public LibraryDialog(Shell parentShell, IOsgiLibrary library, String title) {
    super(parentShell);

    if (library != null) {
      try {
        this.library = new OsgiLibrary(new File(library.getPath()));
        this.library.setSource(library.getSource());
        this.library.setUserDefined(library.isUserDefined());
      } catch (IOException e) {
      }
    }
    this.title = title;
  }

  /****************************************************************************
   * org.eclipse.jface.window.Window Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    
    setValues(library);
    
    return c;
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.Dialog Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    if (library == null) {
      try {
        library = new OsgiLibrary(new File(wLibraryText.getText()));
        library.setUserDefined(true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String src = wSourceText.getText();
    if (src == null || src.trim().length() == 0) {
      library.setSource(null);
    } else {
      library.setSource(src.trim());
    }
    
    // Set return code and close window
    setReturnCode(Window.OK);
    close();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite)super.createDialogArea(parent);
    
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    composite.setLayout(layout);
    
    // Library path
    Label wLibraryLabel = new Label(composite, SWT.LEFT);
    wLibraryLabel.setText("Library:");
    wLibraryText = new Text(composite, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    wLibraryText.setLayoutData(data);
    wLibraryButton = new Button(composite, SWT.NONE);
    wLibraryButton.setText("Browse...");
    wLibraryButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(((Button) e.widget).getShell());
        String path = dialog.open();
        if (path != null) {
          wLibraryText.setText(path);
        }
      }
    });
    
    // Source
    Group wSourceGroup = new Group(composite, SWT.NULL);
    layout = new GridLayout();
    layout.numColumns = 3;
    wSourceGroup.setLayout(layout);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    wSourceGroup.setLayoutData(data);
    wSourceGroup.setText("Source");
    
    // Source path
    Label wSourceDescriptionLabel = new Label(wSourceGroup, SWT.LEFT);
    wSourceDescriptionLabel.setText("Select the location (folder, jar or zip) containing the source.");
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    wSourceDescriptionLabel.setLayoutData(data);
    Button wSourceFileButton = new Button(wSourceGroup, SWT.NONE);
    wSourceFileButton.setText("Archive...");
    wSourceFileButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(((Button) e.widget).getShell());
        dialog.setFilterPath(wSourceText.getText());
        String path = dialog.open();
        if (path != null) {
          wSourceText.setText(path);
        }
      }
    });
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalAlignment = SWT.FILL;
    wSourceFileButton.setLayoutData(data);
    
    
    Label wSourceLabel = new Label(wSourceGroup, SWT.LEFT);
    wSourceLabel.setText("Source Path:");
    wSourceText = new Text(wSourceGroup, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wSourceText.setLayoutData(data);
    Button wSourceButton = new Button(wSourceGroup, SWT.NONE);
    wSourceButton.setText("Folder...");
    wSourceButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(((Button) e.widget).getShell());
        dialog.setFilterPath(wSourceText.getText());
        String path = dialog.open();
        if (path != null) {
          wSourceText.setText(path);
        }
      }
    });
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalAlignment = SWT.FILL;
    wSourceButton.setLayoutData(data);

    return composite;
  }
    
  /****************************************************************************
   * Private Worker Methods
   ***************************************************************************/
  
  private void setValues(IOsgiLibrary lib) {
    
    if (lib == null) {
      wLibraryText.setText("");
      wLibraryText.setEditable(true);
      wLibraryButton.setEnabled(true);
      wSourceText.setText("");
    } else {
      wLibraryText.setText(lib.getPath());
      wLibraryText.setEditable(false);
      wLibraryButton.setEnabled(false);
      if(lib.getSource() != null) {
        wSourceText.setText(lib.getSource());
      } else {
        wSourceText.setText("");
      }
    }
  }
  
  public IOsgiLibrary getLibrary() {
    return library;
  }
}
