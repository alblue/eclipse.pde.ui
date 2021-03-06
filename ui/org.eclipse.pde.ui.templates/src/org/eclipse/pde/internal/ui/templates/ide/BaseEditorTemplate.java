/*******************************************************************************
 *  Copyright (c) 2003, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.ide;

import org.eclipse.pde.internal.ui.templates.PDETemplateSection;

public abstract class BaseEditorTemplate extends PDETemplateSection {

	@Override
	public String getUsedExtensionPoint() {
		return "org.eclipse.ui.editors"; //$NON-NLS-1$
	}

	@Override
	public String[] getNewFiles() {
		return new String[] {"icons/"}; //$NON-NLS-1$
	}
}
