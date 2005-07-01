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

import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Anders Rimén
 */
public class UiUtils {

  private static final int MIN_COL_WIDTH    = 15;
  private static final int COL_MARGIN       = 15;


  public static void packTableColumns(Table table) {
    if(table == null) return;
    TableColumn [] columns = table.getColumns();
    if (columns == null) return;
    
    GC gc = null;
    try {
      gc = new GC(table);
      TableItem[] items = table.getItems();
      for(int i=0;i<columns.length;i++) {
        int width = MIN_COL_WIDTH;
        // Header
        String header = columns[i].getText();
        if (header != null) {
          int textWidth = gc.textExtent(header).x;
          if (textWidth > width) width = textWidth;
        }
        
        // Items 
        for (int j=0; j<items.length;j++) {
          String text = items[j].getText(i);
          int textWidth = gc.textExtent(text).x;
          Image img = items[j].getImage();
          if (img != null) {
            textWidth += img.getBounds().width;
          }
          if (textWidth > width) width = textWidth;
        }
        
        
        columns[i].setWidth(width+COL_MARGIN);
      }
    } finally {
      if (gc != null) gc.dispose();
    }
  }
 
  
  public static Point textExtent(Drawable d, String s) {
    GC gc = null;
    try {
      gc = new GC(d);
      return gc.textExtent(s);
    } finally {
      if (gc != null) gc.dispose();
    }
  }
  
  public static int convertWidthInCharsToPixels(Drawable d, int chars) {
    GC gc = null;
    try {
      gc = new GC(d);
      FontMetrics fm = gc.getFontMetrics();
      return fm.getAverageCharWidth()*chars;
    } finally {
      if (gc != null) gc.dispose();
    }
  }
  
  
  public static int convertHeightInCharsToPixels(Drawable d, int chars) {
    GC gc = null;
    try {
      gc = new GC(d);
      FontMetrics fm = gc.getFontMetrics();
      return fm.getHeight()*chars;
    } finally {
      if (gc != null) gc.dispose();
    }
  }
}
