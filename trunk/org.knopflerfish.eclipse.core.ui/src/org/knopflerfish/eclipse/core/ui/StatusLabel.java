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

package org.knopflerfish.eclipse.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class StatusLabel extends Canvas implements PaintListener, ControlListener, DisposeListener {

  // Positions
  public static int LEFT   = 0;
  public static int RIGHT  = 1;
  public static int TOP    = 2;
  public static int BOTTOM = 3;
  
  private String text;
  private Image imgStatus;
  private int posX;
  private int posY;
  
  // Offscreen resources
  private Image offScreenImage = null;
  private GC offScreenImageGC = null;

  
  public StatusLabel(Composite parent, int style) {
    super(parent, style | SWT.NO_BACKGROUND);
    addPaintListener(this);
    addControlListener(this);
    addDisposeListener(this);
  }

  public void setStatusImage(Image imgStatus, int posX, int posY) {
    this.imgStatus = imgStatus;
    this.posX = posX;
    this.posY = posY;
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  /****************************************************************************
   * org.eclipse.swt.widgets.Widget overrides
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  public void dispose() {
    // Free offscreen resources
    if (offScreenImageGC != null && !offScreenImageGC.isDisposed()) {
      offScreenImageGC.dispose();
      offScreenImageGC = null;
    }
    if (offScreenImage != null && !offScreenImage.isDisposed()) {
      offScreenImage.dispose();
      offScreenImage = null;
    }
    //super.dispose();
  }
  
  /****************************************************************************
   * org.eclipse.swt.widgets.Control overrides
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
   */
  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (offScreenImageGC != null && text != null) {
      return offScreenImageGC.textExtent(text);
    }
    return super.computeSize(wHint, hHint, changed);
  }

  /****************************************************************************
   * org.eclipse.swt.events.PaintListener methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent event) {
    Point size = ((Canvas) event.widget).getSize();
    
    if (offScreenImageGC == null || offScreenImageGC.isDisposed()) return;

    // Background
    offScreenImageGC.setBackground(getBackground());
    offScreenImageGC.setForeground(getForeground());
    offScreenImageGC.fillRectangle(0,0,size.x, size.y);
    offScreenImageGC.setFont(getFont());
    
    // Draw text
    if (text != null) {
      offScreenImageGC.drawText(text, 0, 0, true);
    }
      
    // Draw image
    if (imgStatus != null) {
      Rectangle imageSize = imgStatus.getBounds();
      int x = 0;
      if (posX == RIGHT) {
        x = size.x-imageSize.width;
      }
      int y = 0; 
      if (posY == BOTTOM) {
        y = size.y-imageSize.height;
      }
      offScreenImageGC.drawImage(imgStatus, x, y);
    }

    event.gc.drawImage(offScreenImage, 0, 0);
  }

  /****************************************************************************
   * org.eclipse.swt.events.ControlListener methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
   */
  public void controlMoved(ControlEvent e) {
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  public void controlResized(ControlEvent e) {
    // Free offscreen resources
    dispose();
    
    // Create new offscreen resources
    Point p = ((Canvas) e.widget).getSize();
    offScreenImage = new Image(getDisplay(), p.x, p.y);
    offScreenImageGC = new GC(offScreenImage);
  }

  /****************************************************************************
   * org.eclipse.swt.events.DisposeListener methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
   */
  public void widgetDisposed(DisposeEvent e) {
    // Free offscreen resources
    dispose();
  }
}
