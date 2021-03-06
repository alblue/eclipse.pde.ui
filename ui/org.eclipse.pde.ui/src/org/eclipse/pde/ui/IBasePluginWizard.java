/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui;

import org.eclipse.jface.wizard.IWizard;

/**
 * This is a tagging interface that should be implemented by all wizards that
 * are plugged into one of PDE extension points. This interface is not intended
 * to be implemented directly - clients implement other interfaces that
 * extend this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @since 1.0
 */
public interface IBasePluginWizard extends IWizard {
}
