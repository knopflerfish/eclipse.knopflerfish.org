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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.project.BundleResource;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleResourceDialog extends Dialog {

  private static String TITLE_ADD  = "Add resource";
  private static String TITLE_EDIT = "Edit resource";
  
  private static final String ERROR     = "error";
  private static final String WARNING   = "warning";

  private static final int NUM_CHARS_WIDTH = 60;

  private BundleResource resource;
  IProject project;

  Text      wResourceText;
  private Text      wDestinationText;
  private Text      wPatternText;
  private Composite wErrorComposite;
  private Label     wErrorMsgLabel;
  private Label     wErrorImgLabel;

  public BundleResourceDialog(Shell parentShell, BundleResource resource, IProject project) {
    super(parentShell);
    
    this.resource = resource;
    this.project = project;
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
    if (resource != null) {
      newShell.setText(TITLE_EDIT);
    } else {
      newShell.setText(TITLE_ADD);
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    
    setValues(resource);

    // Verify values
    wResourceText.setData(ERROR, verifyResource());
    wDestinationText.setData(ERROR, verifyDestination());
    wPatternText.setData(ERROR, verifyPattern());
    updateStatus();
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
    if (resource == null) {
      // Create new resource object
      resource = new BundleResource();
    }
    
    resource.setSource(new Path(wResourceText.getText()));
    resource.setDestination(wDestinationText.getText());
    String pattern = wPatternText.getText();
    if (pattern == null || pattern.trim().length()== 0) {
      resource.setPattern(null);
    } else {
      resource.setPattern(Pattern.compile(pattern));
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
    
    // Source
    Label wResourceLabel = new Label(composite, SWT.LEFT);
    wResourceLabel.setText("Resource:");
    wResourceText = new Text(composite, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    wResourceText.setLayoutData(data);
    wResourceText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        // Verify resource
        e.widget.setData(ERROR, verifyResource());
        updateStatus();
      }
    });
    Button wResourceButton = new Button(composite, SWT.NONE);
    wResourceButton.setText("Browse...");
    wResourceButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        ResourceTreeSelectionDialog dialog = 
          new ResourceTreeSelectionDialog(Display.getCurrent().getActiveShell(),
          new int[] {IResource.FILE, IResource.FOLDER});
        if (project != null) {
          dialog.setInput(project);
        } else {
          dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        }
        dialog.setTitle("Select resource");
        dialog.setMessage("Select file or folder.");
        if (dialog.open() == Window.OK && dialog.getResult() != null) {
          IResource resource = (IResource) dialog.getResult()[0];
          wResourceText.setText(resource.getFullPath().toString());
          wResourceText.setData(ERROR, verifyResource());
          updateStatus();
        }
      }
    });
    
    // Destination
    Label wDestinationLabel = new Label(composite, SWT.LEFT);
    wDestinationLabel.setText("Destination:");
    wDestinationLabel.setToolTipText("The folder or file name where the resource is placed in the JAR-file.");
    wDestinationText = new Text(composite, SWT.BORDER);
    wDestinationText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        // Verify pattern
        e.widget.setData(ERROR, verifyDestination());
        updateStatus();
      }
    });
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wDestinationText.setLayoutData(data);

    // Pattern
    Label wPatternLabel = new Label(composite, SWT.LEFT);
    wPatternLabel.setText("Pattern:");
    wPatternLabel.setToolTipText("Regular expression used for matching files to include from the resource location.");
    wPatternText = new Text(composite, SWT.BORDER);
    wPatternText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        // Verify pattern
        e.widget.setData(ERROR, verifyPattern());
        updateStatus();
      }
    });
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wPatternText.setLayoutData(data);
    
    // Error composite
    wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wErrorComposite.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wErrorComposite.setLayoutData(gd);
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(gd);
    
    return composite;
  }
  
  /****************************************************************************
   * Verify UI Input methods
   ***************************************************************************/
  String verifyResource() {
    String resource = wResourceText.getText();
    
    // Empty resource
    if (resource == null || resource.trim().length() == 0) {
      return "Resource must be specified.";
    }
    
    // Verify that resource is a valid folder or file
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
    IResource res = root.findMember(resource);
    if (res == null) {
      return "Resource does not exist.";
    }
    
    if (res.getType() != IResource.FOLDER && res.getType() != IResource.FILE) {
      return "Resource must be a folder or a file.";
    }
    
    return null;
  }
  
  String verifyDestination() {
    //String destination = wDestinationText.getText();
    
    // TODO: Verify that destination is a valid jar folder or file
    
    return null;
  }
  
  String verifyPattern() {
    String pattern = wPatternText.getText();

    // Empty pattern ok
    if (pattern == null || pattern.trim().length() == 0) {
      return null;
    }

    // Try to  compile pattern
    try{
      Pattern.compile(pattern);
    } catch (Throwable t) {
      return "Bad pattern: "+t.getMessage(); 
    }
    
    return null;
  }
  
  /****************************************************************************
   * Private Worker Methods
   ***************************************************************************/
  void updateStatus() {
    Control c = getDialogArea();
    if (c == null) return;
    
    // Loop through widgets checking for errors
    String error = (String) getData(c, ERROR);
    if (error != null) {
      setPageComplete(false);
      setMessage(error, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
      return;
    }
    
    // Loop through widgets checking for warnings
    String warning = (String) getData(c, WARNING);
    if (warning != null) {
      setPageComplete(true);
      setMessage(warning, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
      return;
    }

    // Everything is ok
    setPageComplete(true);
    setMessage(null, null);
   
  }
  
  private Object getData(Control c, String key) {
    if (c == null || key == null) return null;
    
    // Check if this control contains the key
    Object data = c.getData(key);
    if (data != null) return data;
    
    if (c instanceof Composite) {
      // Check if any children contain the key
      Control [] children = ((Composite) c).getChildren();
      if (children != null) {
        for(int i=0; i<children.length; i++) {
          data = getData(children[i], key);
          if (data != null) return data;
        }
      }
    }
    return null;
  }
  

  private void setPageComplete(boolean complete) {
    getButton(IDialogConstants.OK_ID).setEnabled(complete);
  }

  private void setMessage(String msg, Image img) {
    if (msg == null) {
      wErrorComposite.setVisible(false);
    } else {
      wErrorComposite.setVisible(true);
      wErrorImgLabel.setImage(img);
      wErrorMsgLabel.setText(msg);
    }
  }
 
  
  private void setValues(BundleResource res) {
    
    if (res != null) {
      wResourceText.setText(res.getSource().toString());
      if (res.getDestination() != null) {
        wDestinationText.setText(res.getDestination());
      } else {
        wDestinationText.setText("");
      }
      if (res.getPattern() != null) {
        wPatternText.setText(res.getPattern().pattern());
      } else {
        wPatternText.setText("");
      }
    }
  }
  
  public BundleResource getResource() {
    return resource;
  }
}
