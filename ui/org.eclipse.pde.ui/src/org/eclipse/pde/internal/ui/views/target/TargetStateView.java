/*******************************************************************************
 * Copyright (c) 2009, 2013 EclipseSource Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.views.target;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;

public class TargetStateView extends PageBookView {

	public static final String VIEW_ID = "org.eclipse.pde.ui.TargetPlatformState"; //$NON-NLS-1$
	private Map<IPageBookViewPage, IWorkbenchPart> fPagesToParts;
	private Map<IWorkbenchPart, IPageBookViewPage> fPartsToPages;
	private IWorkbenchPart fPartState;

	static class DummyPart implements IWorkbenchPart {
		private IWorkbenchPartSite fSite;

		public DummyPart(IWorkbenchPartSite site) {
			fSite = site;
		}

		public void addPropertyListener(IPropertyListener listener) {/* dummy */
		}

		public void createPartControl(Composite parent) {/* dummy */
		}

		public void dispose() {
			fSite = null;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public IWorkbenchPartSite getSite() {
			return fSite;
		}

		public String getTitle() {
			return null;
		}

		public Image getTitleImage() {
			return null;
		}

		public String getTitleToolTip() {
			return null;
		}

		public void removePropertyListener(IPropertyListener listener) {/* dummy */
		}

		public void setFocus() {/* dummy */
		}
	}

	public TargetStateView() {
		fPartsToPages = new HashMap<IWorkbenchPart, IPageBookViewPage>(4);
		fPagesToParts = new HashMap<IPageBookViewPage, IWorkbenchPart>(4);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		fPartState = new DummyPart(site);
	}

	@Override
	public void dispose() {
		super.dispose();
		fPartState.dispose();
		fPartState = null;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IHelpContextIds.TARGET_STATE_VIEW);
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		return createPage(getDefaultPart());
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		IPageBookViewPage page = fPartsToPages.get(part);
		if (page == null && !fPartsToPages.containsKey(part)) {
			page = createPage(part);
		}
		if (page != null) {
			return new PageRec(part, page);
		}
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();

		// empty cross-reference cache
		fPartsToPages.remove(part);
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return getDefaultPart();
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof DummyPart;
	}

	/**
	 * part of the part constants
	 */
	private IPageBookViewPage createPage(IWorkbenchPart part) {
		IPageBookViewPage page = new StateViewPage(this);
		initPage(page);
		page.createControl(getPageBook());
		fPartsToPages.put(part, page);
		fPagesToParts.put(page, part);
		return page;
	}

	private IWorkbenchPart getDefaultPart() {
		return fPartState;
	}

}
