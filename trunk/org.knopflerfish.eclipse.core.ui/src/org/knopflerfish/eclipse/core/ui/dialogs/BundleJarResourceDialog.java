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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.knopflerfish.eclipse.core.project.BundleJarResource;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleJarResourceDialog extends Dialog {

  private static final String ERROR     = "error";
  private static final String WARNING   = "warning";

  private static final int NUM_CHARS_WIDTH = 60;

  private BundleJarResource resource;
  private final String title;

  // Widgets
  private Text      wResourceText;
  private Text      wDestinationText;
  private Text      wPatternText;
  private Composite wErrorComposite;
  private Label     wErrorMsgLabel;
  private Label     wErrorImgLabel;

  public BundleJarResourceDialog(Shell parentShell, BundleJarResource resource, String title) {
    super(parentShell);
    
    this.resource = resource;
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
      resource = new BundleJarResource();
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
        ElementTreeSelectionDialog dialog =
          new ElementTreeSelectionDialog(
              Display.getCurrent().getActiveShell(),
              new LabelProvider() {
                public Image getImage(Object element) {
                  IResource resource = (IResource) element;
                  if (resource.getType() == IResource.FILE) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
                  } else if (resource.getType() == IResource.FOLDER) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                  } else if (resource.getType() == IResource.PROJECT) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
                  } else {
                    return null;
                  }
                }

                public String getText(Object element) {
                  IResource resource = (IResource) element;
                  return resource.getName();
                }
              }, 
              new BaseWorkbenchContentProvider() );
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setValidator(new ISelectionStatusValidator() {

          public IStatus validate(Object[] selection) {
            if (selection == null || selection.length != 1) {
              Status status = new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core.ui", 
                  IStatus.OK, "Select file or folder", null);
              return status;
            }
            
            IResource resource = (IResource) selection[0];
            if (resource.getType() != IResource.FOLDER && resource.getType() != IResource.FILE) {
              Status status = new Status(IStatus.ERROR, "org.knopflerfish.eclipse.core.ui", 
                  IStatus.OK, "Select file or folder", null);
              return status;
            } else {
              Status status = new Status(IStatus.INFO, "org.knopflerfish.eclipse.core.ui", 
                  IStatus.OK, "", null);
              return status;
            }
          }
          
        });
        dialog.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
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
  private String verifyResource() {
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
  
  private String verifyDestination() {
    //String destination = wDestinationText.getText();
    
    // TODO: Verify that destination is a valid jar folder or file
    
    return null;
  }
  
  private String verifyPattern() {
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
  private void updateStatus() {
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
 
  
  private void setValues(BundleJarResource res) {
    
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
  
  public BundleJarResource getResource() {
    return resource;
  }
}
