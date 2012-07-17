/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.text.bundle;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.jface.text.*;
import org.eclipse.pde.internal.core.ibundle.*;
import org.eclipse.pde.internal.core.text.IDocumentKey;
import org.eclipse.pde.internal.core.util.HeaderMap;
import org.osgi.framework.Constants;

public class Bundle implements IBundle {

	private BundleModel fModel;
	private Map fDocumentHeaders = new HeaderMap();

	public Bundle(BundleModel model) {
		fModel = model;
	}

	/**
	 * Loads the given map as the set of headers this model represents. Any previously set
	 * headers will be cleared.  The provided map must be string header keys to string
	 * values.
	 * 
	 * @param headers the headers to load in this model
	 */
	public void load(Map headers) {
		fDocumentHeaders.clear();
		Iterator iter = headers.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next().toString();
			if (key.equals(Constants.BUNDLE_MANIFESTVERSION)) {
				String value = headers.get(key).toString();
				IManifestHeader header = fModel.getFactory().createHeader(key.toString(), value);
				fDocumentHeaders.put(key.toString(), header);
				break;
			}
		}

		iter = headers.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next().toString();
			if (key.equals(Constants.BUNDLE_MANIFESTVERSION))
				continue;
			String value = headers.get(key).toString();
			IManifestHeader header = fModel.getFactory().createHeader(key.toString(), value);
			fDocumentHeaders.put(key.toString(), header);
		}
		adjustOffsets(fModel.getDocument());
	}

	public void clearOffsets() {
		Iterator iter = fDocumentHeaders.values().iterator();
		while (iter.hasNext()) {
			ManifestHeader header = (ManifestHeader) iter.next();
			header.setOffset(-1);
			header.setLength(-1);
		}
	}

	protected void adjustOffsets(IDocument document) {
		int lines = document.getNumberOfLines();
		try {
			IDocumentKey currentKey = null;
			for (int i = 0; i < lines; i++) {
				int offset = document.getLineOffset(i);
				int length = document.getLineLength(i);
				String line = document.get(offset, length);

				if (currentKey != null) {
					int lineNumber = line.startsWith(" ") ? i : i - 1; //$NON-NLS-1$
					IRegion region = document.getLineInformation(lineNumber);
					String delimiter = document.getLineDelimiter(lineNumber);
					int keyLength = region.getOffset() + region.getLength() - currentKey.getOffset();
					currentKey.setLength(delimiter != null ? keyLength + delimiter.length() : keyLength);
					if (!line.startsWith(" ")) { //$NON-NLS-1$
						currentKey = null;
					}
				}

				if (currentKey == null) {
					int index = line.indexOf(':');
					String name = (index != -1) ? line.substring(0, index) : line;
					currentKey = (IDocumentKey) fDocumentHeaders.get(name);
					if (currentKey != null) {
						IRegion region = document.getLineInformation(i);
						currentKey.setOffset(region.getOffset());
						String delimiter = document.getLineDelimiter(i);
						currentKey.setLength(delimiter != null ? region.getLength() + delimiter.length() : region.getLength());
					}
				}
			}
		} catch (BadLocationException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.ibundle.IBundle#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String key, String value) {
		if (value == null) {
			// Do a remove
			IManifestHeader header = (IManifestHeader) fDocumentHeaders.remove(key);
			if (header != null) {
				fModel.fireModelObjectChanged(header, key, header.getValue(), null);
			}
		} else {
			// Edit an existing header value or create a new header object
			IManifestHeader header = (ManifestHeader) fDocumentHeaders.get(key);
			if (header == null) {
				header = getModel().getFactory().createHeader(key, value);
				fDocumentHeaders.put(key, header);
				fModel.fireModelObjectChanged(header, key, null, value);
			} else {
				String old = header.getValue();
				header.setValue(value);
				fModel.fireModelObjectChanged(header, key, old, value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.ibundle.IBundle#getHeader(java.lang.String)
	 */
	public String getHeader(String key) {
		ManifestHeader header = (ManifestHeader) fDocumentHeaders.get(key);
		return (header != null) ? header.getValue() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.ibundle.IBundle#getManifestHeader(java.lang.String)
	 */
	public IManifestHeader getManifestHeader(String key) {
		return (ManifestHeader) fDocumentHeaders.get(key);
	}

	public Map getHeaders() {
		return fDocumentHeaders;
	}

	public IBundleModel getModel() {
		return fModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.ibundle.IBundle#getLocalization()
	 */
	public String getLocalization() {
		String localization = getHeader(Constants.BUNDLE_LOCALIZATION);
		return localization != null ? localization : Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.ibundle.IBundle#renameHeader(java.lang.String, java.lang.String)
	 */
	public void renameHeader(String key, String newKey) {
		ManifestHeader header = (ManifestHeader) getManifestHeader(key);
		if (header != null) {
			header.setName(newKey);
			fDocumentHeaders.put(newKey, fDocumentHeaders.remove(key));
		}
		fModel.fireModelObjectChanged(header, newKey, key, newKey);
	}
}
