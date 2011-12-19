/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.trace.internal.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.ui.trace.internal.TracingUIActivator;
import org.eclipse.ui.trace.internal.datamodel.*;
import org.eclipse.ui.trace.internal.utils.TracingConstants;

/**
 * A label provider created specifically for the view filter. This label provider is not used to populate the labels on
 * the trace view. See the class {@link TracingComponentColumnLabelProvider} for the logic for populating the labels of
 * the trace viewer.
 */
public class TracingComponentLabelProvider extends LabelProvider {

	/** Trace object for this bundle */
	protected final static DebugTrace TRACE = TracingUIActivator.getDefault().getTrace();

	@Override
	public String getText(final Object element) {

		TRACE.traceEntry(TracingConstants.TRACE_UI_PROVIDERS_STRING, element);

		String label = TracingComponentLabelProvider.getLabel(TracingConstants.LABEL_COLUMN_INDEX, element);
		TRACE.traceExit(TracingConstants.TRACE_UI_PROVIDERS_STRING, label);
		return label;
	}

	/**
	 * Access the label text for the specified element at the specified column index in the tree viewer
	 * 
	 * @param columnIndex
	 *            The column index. One of either {@link TracingConstants#LABEL_COLUMN_INDEX} or
	 *            {@link TracingConstants#VALUE_COLUMN_INDEX}
	 * @param element
	 *            The element in the tree viewer.
	 * @return If the element is of type {@link TracingComponent} and the index is
	 *         {@link TracingConstants#LABEL_COLUMN_INDEX} then label of the {@link TracingComponent} will be returned.<br/>
	 *         If the element is of type {@link TracingComponent} and the index is
	 *         {@link TracingConstants#VALUE_COLUMN_INDEX} then null is returned.<br/>
	 *         If the element is of type {@link TracingComponentDebugOption} and the index is
	 *         {@link TracingConstants#LABEL_COLUMN_INDEX} then the option-path of the
	 *         {@link TracingComponentDebugOption} will be returned.<br/>
	 *         If the element is of type {@link TracingComponentDebugOption} and the index is
	 *         {@link TracingConstants#VALUE_COLUMN_INDEX} then the option-path value of the
	 *         {@link TracingComponentDebugOption} will be returned.<br/>
	 */
	public final static String getLabel(final int columnIndex, final Object element) {

		TRACE.traceEntry(TracingConstants.TRACE_UI_PROVIDERS_STRING, new Object[] {new Integer(columnIndex), element});

		String result = null;
		switch (columnIndex) {
			case TracingConstants.LABEL_COLUMN_INDEX :
				if (element instanceof TracingNode) {
					result = ((TracingNode) element).getLabel();
				} else if (element instanceof String) {
					result = (String) element;
				}
				break;
			case TracingConstants.VALUE_COLUMN_INDEX :
				// if the element does not have a boolean value then it is modifiable - the value is the option-path
				// value.
				if (element instanceof TracingComponentDebugOption) {
					result = ((TracingComponentDebugOption) element).getOptionPathValue();
				} else if (element instanceof String) {
					result = (String) element;
				}
				break;
			default : // do nothing
				break;
		}

		TRACE.traceExit(TracingConstants.TRACE_UI_PROVIDERS_STRING, result);
		return result;
	}
}