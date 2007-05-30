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

package org.knopflerfish.eclipse.core.ui.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ImportPackageModel;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleDocument {

  private final IDocument manifestDocument;
  private final IDocument packDocument;
  private ImportPackageModel importPackageModel;
  
  public BundleDocument(IDocument manifestDocument, IDocument packDocument, ImportPackageModel importPackageModel) {
    this.manifestDocument = manifestDocument;
    this.packDocument = packDocument;
    this.importPackageModel = importPackageModel;
  }
  
  public void addDocumentListener(IDocumentListener listener) {
    if (manifestDocument != null) {
      manifestDocument.addDocumentListener(listener);
    }
  }
  
  public void removeDocumentListener(IDocumentListener listener) {
    if (manifestDocument != null) {
      manifestDocument.removeDocumentListener(listener);
    }
  }

  public IDocument getManifestDocument() {
    return manifestDocument;
  }

  public IDocument getPackDocument() {
    return packDocument;
  }

  public ImportPackageModel getImportPackageModel() {
    return importPackageModel;
  }

  public void setImportPackageModel(ImportPackageModel importPackageModel) {
    this.importPackageModel = importPackageModel;
  }
}
