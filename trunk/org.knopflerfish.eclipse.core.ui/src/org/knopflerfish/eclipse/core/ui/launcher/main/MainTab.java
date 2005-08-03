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

package org.knopflerfish.eclipse.core.ui.launcher.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiVendor;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiVendor;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;
import org.knopflerfish.eclipse.core.launcher.SourcePathComputer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.launcher.bundle.IAvailableTreeElement;

/**
 * @author Anders Rimén
 */
public class MainTab extends AbstractLaunchConfigurationTab {
  private static String IMAGE = "icons/obj16/knopflerfish_obj.gif";
  private static final String DEFAULT_KF_RUNTIME_PATH = "runtime-knopflerfish";
  
  private static final String TOOLTIP_LOCATION = 
    "The location where fwdir and xargs files for this configuration will be stored.";
  private static final String TOOLTIP_INIT = 
    "If checked the framework will be started with '-init' flag.";
  
  private static String TITLE_ADD_PROPERTY = "Add Property";
  private static String TITLE_EDIT_PROPERTY = "Edit Property";
  
  private static final int NUM_ROWS_DESCRIPTION = 5;
  private int MARGIN = 5;

  private static final String ITEM_TYPE           = "item_type";
  private static final String GROUP_TYPE          = "group";
  private static final String PROPERTY_TYPE       = "property";

  private static final String ITEM_PROPERTY       = "item_property";
  private static final String ITEM_DEFAULT        = "default";
  
  protected static final String USER_GROUP        = "User Defined";
  
  // Widgets
  private Composite wPageComposite;
  private Combo     wOsgiInstallCombo;
  private Text      wInstanceDirText;
  private Button    wInitButton;
  private Button    wAddPropertyButton;
  private Button    wEditPropertyButton;
  private Button    wRemovePropertyButton;
  private Label     wDescriptionLabel;
  private Label     wDescriptionText;
  private Label     wDefaultLabel;
  private Label     wDefaultText;
  
  // jFace Widgets 
  private TreeViewer    wPropertyTreeViewer;
  
  private Image image = null;
  private Font  fontChanged = null;
  
  HashMap propertyGroups = new HashMap();
  HashMap propertyValues = new HashMap();
  
  public MainTab() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE);
    if (id != null) {
      image = id.createImage();
    }
  }
  
  /****************************************************************************
   * org.eclipse.debug.ui.ILaunchConfigurationTab Methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Main";
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  public Image getImage() {
    return image;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  public void dispose() {
    if (image != null) {
      image.dispose();
      image = null;
    }
    if (fontChanged != null) {
     fontChanged.dispose();
     fontChanged = null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    
    wPageComposite = new Composite(parent, 0);
    setControl(wPageComposite);
    GridLayout layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 1;
    wPageComposite.setLayout(layout);
    
    // Location Group
    Group wLocationGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wLocationGroup.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wLocationGroup.setLayoutData(gd);
    wLocationGroup.setText("Instance Data");
    Label wLocationLabel = new Label(wLocationGroup, SWT.LEFT | SWT.WRAP);
    wLocationLabel.setText("Location:");
    wLocationLabel.setToolTipText(TOOLTIP_LOCATION);
    wInstanceDirText = new Text(wLocationGroup, SWT.SINGLE | SWT.BORDER);
    wInstanceDirText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wInstanceDirText.setLayoutData(gd);
    Button wBrowseLocationButton = new Button(wLocationGroup, SWT.CENTER);
    wBrowseLocationButton.setText("Browse...");

    
    // Framework Group
    Group wFrameworkGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wFrameworkGroup.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wFrameworkGroup.setLayoutData(gd);
    wFrameworkGroup.setText("Framework to Run");

    // OSGi framework
    Label wFrameworkLabel = new Label(wFrameworkGroup, SWT.LEFT | SWT.WRAP);
    wFrameworkLabel.setText("OSGi Install:");
    wOsgiInstallCombo = new Combo(wFrameworkGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    wOsgiInstallCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
        // Update System Properties accepted by this framework
        initializeSystemProperties();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wOsgiInstallCombo.setLayoutData(gd);
    Button wInstallFrameworkButton = new Button(wFrameworkGroup, SWT.CENTER);
    wInstallFrameworkButton.setText("Install...");
    gd = new GridData();
    wInstallFrameworkButton.setLayoutData(gd);

    wInitButton = new Button(wFrameworkGroup, SWT.CHECK);
    wInitButton.setText("Start empty platform (-init)");
    wInitButton.setToolTipText(TOOLTIP_INIT);
    wInitButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=3;
    wInitButton.setLayoutData(gd);
    
    // Properties Group
    Group wPropertyGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    formLayout.marginWidth = MARGIN;
    wPropertyGroup.setLayout(formLayout);
    gd = new GridData(GridData.FILL_BOTH);
    wPropertyGroup.setLayoutData(gd);
    wPropertyGroup.setText("System Properties");
    
    Tree wPropertyTree = new Tree(wPropertyGroup, SWT.BORDER | SWT.FULL_SELECTION);
    wPropertyTreeViewer = new TreeViewer(wPropertyTree);
    wPropertyTreeViewer.setContentProvider(new SystemPropertyContentProvider());
    wPropertyTreeViewer.setLabelProvider(new SystemPropertyLabelProvider());
    wPropertyTree.setHeaderVisible(true);
    wPropertyTree.setLinesVisible(true);
    // Tree columns
    TreeColumn wPropertyTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wPropertyTreeColumn.setText("Property");
    TreeColumn wValueTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wValueTreeColumn.setText("Value");
    wPropertyTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        showPropertyInfo();
      }
    });
    wPropertyTreeViewer.addOpenListener(new IOpenListener() {
      public void open(OpenEvent event) {
        event.getSelection();
        /*
        int count = wPropertyTableTree.getSelectionCount();
        if (count != -1) {
          TableTreeItem item = wPropertyTableTree.getSelection()[0];
          if (PROPERTY_TYPE.equals(item.getData(ITEM_TYPE))) {
            PropertyDialog dialog = 
              new PropertyDialog(((TableTree) e.widget).getShell(),
                  (SystemProperty) item.getData(ITEM_PROPERTY),
                  TITLE_EDIT_PROPERTY); 
            if (dialog.open() == Window.OK) {
              SystemProperty property = dialog.getSystemProperty();
              // Update property
              updateSystemProperty(property);
              updateLaunchConfigurationDialog();
            }
          }
        }
        */
      }
    });
    
    wAddPropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wAddPropertyButton.setText("Add...");
    wAddPropertyButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        /*
        PropertyDialog dialog = 
          new PropertyDialog(((Button) e.widget).getShell(), 
              null,
              TITLE_ADD_PROPERTY); 
        if (dialog.open() == Window.OK) {
          SystemProperty property = dialog.getSystemProperty();
          // Add user property
          TableTreeItem item = addSystemProperty(property);
          if (item != null) {
            wPropertyTableTree.setSelection(new TableTreeItem[] {item});
          }
          updateLaunchConfigurationDialog();
        }
        */
      }
    });
    wRemovePropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wRemovePropertyButton.setText("Remove");
    wRemovePropertyButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        // TODO :Remove user property
        // How is a item removed from a tree?
        updateLaunchConfigurationDialog();
      }
    });
    wEditPropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wEditPropertyButton.setText("Edit..");
    wEditPropertyButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        /*
        int count = wPropertyTableTree.getSelectionCount();
        if (count != -1) {
          TableTreeItem item = wPropertyTableTree.getSelection()[0];
          if (PROPERTY_TYPE.equals(item.getData(ITEM_TYPE))) {
            PropertyDialog dialog = 
              new PropertyDialog(((Button) e.widget).getShell(),
                  (SystemProperty) item.getData(ITEM_PROPERTY),
                  TITLE_EDIT_PROPERTY); 
            if (dialog.open() == Window.OK) {
              SystemProperty property = dialog.getSystemProperty();
              // Update property
              updateSystemProperty(property);
              updateLaunchConfigurationDialog();
            }
          }
        }
        */
      }
    });
    
    //Description
    wDescriptionLabel = new Label(wPropertyGroup, SWT.LEFT);
    wDescriptionLabel.setText("Description:");
    wDescriptionText = new Label(wPropertyGroup, SWT.LEFT| SWT.WRAP);
    wDefaultLabel = new Label(wPropertyGroup, SWT.LEFT);
    wDefaultLabel.setText("Default Value:");
    wDefaultText = new Label(wPropertyGroup, SWT.LEFT);

    // Layout
    FormData fd = new FormData();
    fd.left = new FormAttachment(0,0);
    fd.top = new FormAttachment(0,0);
    fd.right = new FormAttachment(wRemovePropertyButton,-5,SWT.LEFT);
    fd.bottom = new FormAttachment(wDescriptionLabel, -5, SWT.TOP);
    wPropertyTree.setLayoutData(fd);
    
    fd = new FormData();
    //data.left = new FormAttachment(wBundleAttachButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wPropertyTree,0,SWT.TOP);
    wRemovePropertyButton.setLayoutData(fd);
    
    fd = new FormData();
    fd.left = new FormAttachment(wRemovePropertyButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wRemovePropertyButton,5,SWT.BOTTOM);
    wAddPropertyButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wRemovePropertyButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wAddPropertyButton,5,SWT.BOTTOM);
    wEditPropertyButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(wDescriptionText, -5,SWT.TOP);
    wDescriptionLabel.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(wDefaultText, -5,SWT.TOP);
    fd.height = UiUtils.convertHeightInCharsToPixels(wDescriptionText, NUM_ROWS_DESCRIPTION);
    wDescriptionText.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wDefaultText, 0, SWT.CENTER);
    wDefaultLabel.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wDefaultLabel, 5, SWT.RIGHT);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(100, 0);
    wDefaultText.setLayoutData(fd);

    // Create font used in property tree for non-default values
    if (fontChanged == null) {
      Font font = wPropertyTree.getFont();
      FontData fontData = font.getFontData()[0];
      fontData.setStyle(SWT.BOLD);
      fontChanged = new Font(wPropertyTree.getDisplay(), fontData);
    }
    
    UiUtils.packTreeColumns(wPropertyTree);
    
    showPropertyInfo();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    // New configuration created, set default values
    // Set default OSGi vendor
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_VENDOR_NAME, OsgiVendor.VENDOR_NAME);
    IOsgiVendor osgiVendor = Osgi.getVendor(OsgiVendor.VENDOR_NAME);

    // Set default OSGi install
    IOsgiInstall osgiInstall = osgiVendor == null ? null: osgiVendor.getDefaultOsgiInstall();
    if (osgiInstall != null) {
      configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTALL_NAME, osgiInstall.getName());
    }
    
    // Set default instance directory 
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IPath path = root.getLocation();
    path = path.append(DEFAULT_KF_RUNTIME_PATH);
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_DIR, path.toString());
    
    // Set default instance settings
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_INIT, true);
    
    // Set default properties 
    HashMap properties = new HashMap();
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, properties);   
    
    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, SourcePathComputer.ID);
    
     
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    // Set values to GUI widgets

    // OSGi vendor
    /*
    String vendorName = null;
    try {
      vendorName = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_VENDOR_NAME, (String) null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    int idx = 0;
    if (vendorName != null) {
      idx = wOsgiVendorCombo.indexOf(vendorName);
      if (idx < 0) idx = 0;
    }
    wOsgiVendorCombo.select(idx);
    */
    
    // OSGi install
    updateOsgiInstalls();
    String installName = null;
    try {
      installName = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTALL_NAME, (String) null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    int idx = 0;
    if (installName != null) {
      idx = wOsgiInstallCombo.indexOf(installName);
      if (idx < 0) idx = 0;
    }
    wOsgiInstallCombo.select(idx);
    
    // Instance directory 
    String instanceDir = null;
    try {
      instanceDir = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_DIR, "");
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wInstanceDirText.setText(instanceDir);
    
    boolean instanceInit = false;
    try {
      // Instance settings
      instanceInit = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_INIT, false);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wInitButton.setSelection(instanceInit);
    
    // Initialize default system properties list
    initializeSystemProperties();
    try {
      // Update system property values
      Map properties = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, (Map) null);
      if (properties != null) {
        for(Iterator i=properties.keySet().iterator(); i.hasNext();) {
          String name = (String) i.next();
          /*
          TableTreeItem item = (TableTreeItem) propertyValues.get(name);
          if (item !=null) {
            SystemProperty property = (SystemProperty) item.getData(ITEM_PROPERTY);
            if (property != null) {
              property.setValue((String) properties.get(name));
              updateSystemProperty(property);
            }
          } else {
            // Add user property
            SystemProperty property = new SystemProperty(name);
            property.setGroup(USER_GROUP);
            property.setValue((String) properties.get(name));
            addSystemProperty(property);
          }
          */
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {

    // Read values from GUI widgets
    /*
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_VENDOR_NAME, 
        wOsgiVendorCombo.getText());
    */
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_VENDOR_NAME, 
        OsgiVendor.VENDOR_NAME);
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTALL_NAME, 
        wOsgiInstallCombo.getText());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_DIR, 
        wInstanceDirText.getText());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_INIT, 
        wInitButton.getSelection());
    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, SourcePathComputer.ID);
    
    // System Properties
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, getSystemProperties());
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public boolean isValid(ILaunchConfiguration configuration) {
    // Verify Osgi vendor
    
    // Verify Osgi install
    
    // Verify instance directory
    
    setErrorMessage(null);
    return true;
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void updateOsgiInstalls() {
    //String name = wOsgiVendorCombo.getText();
    String name = OsgiVendor.VENDOR_NAME;
    IOsgiVendor v = Osgi.getVendor(name);
    wOsgiInstallCombo.removeAll();
    List l = null;
    if (v != null && (l = v.getOsgiInstalls()) != null) {
      for(int i=0; i<l.size();i++) {
        wOsgiInstallCombo.add( ((IOsgiInstall) l.get(i)).getName());
      }
    }
  }
  
  private void initializeSystemProperties() {
    //String name = wOsgiVendorCombo.getText();
    String name = OsgiVendor.VENDOR_NAME;
    IOsgiVendor vendor = Osgi.getVendor(name);
    IOsgiInstall osgiInstall = vendor.getOsgiInstall(wOsgiInstallCombo.getText());
    wPropertyTreeViewer.setInput(osgiInstall);
    wPropertyTreeViewer.expandAll();
    UiUtils.packTreeColumns(wPropertyTreeViewer.getTree());
    /*
    wPropertyTableTree.removeAll();
    propertyGroups.clear();
    propertyValues.clear();
    SystemProperty [] properties = osgiInstall.getSystemProperties();
    for(int i=0; i<properties.length; i++) {
      addSystemProperty(properties[i]);
    }
    
    // Expand all groups
    for (Iterator i = propertyGroups.values().iterator(); i.hasNext() ;) {
      TableTreeItem wGroupItem = (TableTreeItem) i.next();
      wGroupItem.setExpanded(true);
    }
    
    // Pack columns
    UiUtils.packTableColumns(wPropertyTableTree.getTable());
    */
  }
  
  /*
  private TableTreeItem addSystemProperty(SystemProperty prop) {
    if (prop == null) return null;
    
    String group = prop.getGroup();
    if (group != null) {
      // Add group
      TableTreeItem wGroupItem = (TableTreeItem) propertyGroups.get(group);
      if (wGroupItem == null) {
        wGroupItem = new TableTreeItem(wPropertyTableTree, SWT.NONE);
        wGroupItem.setText(group);
        //wGroupItem.setExpanded(true);
        wGroupItem.setData(ITEM_TYPE, GROUP_TYPE);
        propertyGroups.put(group, wGroupItem);
      }
      // Add property
      TableTreeItem wPropertyItem = new TableTreeItem(wGroupItem, SWT.NONE);
      wPropertyItem.setText(0, prop.getName());
      String value = prop.getValue();
      if (value == null) value = prop.getDefaultValue();
      if (value != null) {
        wPropertyItem.setText(1, value);
      } else {
        wPropertyItem.setText(1, "");
      }
      wPropertyItem.setData(ITEM_TYPE, PROPERTY_TYPE);
      wPropertyItem.setData(ITEM_PROPERTY, prop);
      if(USER_GROUP.equals(prop.getGroup())) {
        wPropertyItem.setFont(fontChanged);
        wPropertyItem.setData(ITEM_DEFAULT, new Boolean(false));
      } else {
        wPropertyItem.setData(ITEM_DEFAULT, new Boolean(true));
      }
      propertyValues.put(prop.getName(), wPropertyItem);
      
      return wPropertyItem;
    } else {
      // Add property
      TableTreeItem wPropertyItem = new TableTreeItem(wPropertyTableTree, SWT.NONE);
      wPropertyItem.setText(0, prop.getName());
      String value = prop.getValue();
      if (value == null) value = prop.getDefaultValue();
      if (value != null) {
        wPropertyItem.setText(1, value);
      } else {
        wPropertyItem.setText(1, "");
      }
      wPropertyItem.setData(ITEM_TYPE, PROPERTY_TYPE);
      wPropertyItem.setData(ITEM_PROPERTY, prop);
      propertyValues.put(prop.getName(), wPropertyItem);

      return wPropertyItem;
    }
  }
  
  private void updateSystemProperty(SystemProperty prop) {
    if (prop == null) return;
    
    TableTreeItem item = (TableTreeItem) propertyValues.get(prop.getName());
    if (item == null) return;
    
    String value = prop.getValue();
    if (value == null) value = prop.getDefaultValue();
    if (value != null) {
      item.setText(1, value);
    } else {
      item.setText(1, "");
    }

    if ((prop.getValue() != null && !prop.getValue().equals(prop.getDefaultValue()==null ? "":prop.getDefaultValue())) ||
        (prop.getValue() == null  && prop.getDefaultValue() != null) ||
        USER_GROUP.equals(prop.getGroup())) {
      item.setFont(fontChanged);
      item.setData(ITEM_DEFAULT, new Boolean(false));
    } else {
      item.setFont(wPropertyTableTree.getFont());
      item.setData(ITEM_DEFAULT, new Boolean(true));
    }
    
    item.setData(ITEM_PROPERTY, prop);
  }
  */

  private Map getSystemProperties() {
    HashMap properties = new HashMap();
    /*
    TableTreeItem [] items = wPropertyTableTree.getItems();
    if (items != null) {
      for(int i=0; i<items.length; i++) {
        getSystemProperties(items[i], properties);
      }
    }
    */
    return properties;
  }

  private void getSystemProperties(TableTreeItem item, Map props) {
    
    // Add this
    Boolean isDefault = (Boolean) item.getData(ITEM_DEFAULT);
    if (isDefault != null && !isDefault.booleanValue()) {
      SystemProperty property = (SystemProperty) item.getData(ITEM_PROPERTY);
      if (property != null) {
        String value = property.getValue();
        if (value == null) value = "";
        props.put(property.getName(), value);
      }
    }

    // Add children
    TableTreeItem [] items = item.getItems();
    if (items != null) {
      for(int i=0; i<items.length; i++) {
        getSystemProperties(items[i], props);
      }
    }
  }
  
  private void showPropertyInfo() {
    IStructuredSelection selection = 
      (IStructuredSelection) wPropertyTreeViewer.getSelection();

    SystemProperty property = null;
    if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof SystemProperty) {
      property = (SystemProperty) selection.getFirstElement();
    }
    
    if (property == null) {
      wDescriptionLabel.setEnabled(false);
      wDescriptionText.setText("");
      wDefaultLabel.setEnabled(false);
      wDefaultText.setText("");
      
      wEditPropertyButton.setEnabled(false);
      wRemovePropertyButton.setEnabled(false);
    } else {
      // Description
      wDescriptionLabel.setEnabled(true);
      if (property.getDescription() != null) {
        wDescriptionText.setText(property.getDescription());
      } else {
        wDescriptionText.setText("");
      }
      
      // Default value
      wDefaultLabel.setEnabled(true);
      if (property.getDefaultValue() != null) {
        wDefaultText.setText(property.getDefaultValue());
      } else {
        wDefaultText.setText("");
      }
      
      // Buttons
      wEditPropertyButton.setEnabled(true);
      /* TODO
      if (USER_GROUP.equals(property.getGroup())) {
        wRemovePropertyButton.setEnabled(true);
      } else {
        wRemovePropertyButton.setEnabled(false);
      }
      */
    }
  }
}
