/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
package a.b.c;


/**
 * Test supported @noimplement tag on constructors in outer / inner classes
 */
public class test23 {
	/**
	 * Constructor
	 * @noimplement This constructor is not intended to be referenced by clients.
	 */
	public test23() {
		
	}
	
	/**
	 * Constructor
	 * @noimplement This constructor is not intended to be referenced by clients.
	 */
	public test23(int i) {
		
	}
	static class inner {
		/**
		 * Constructor
		 * @noimplement This constructor is not intended to be referenced by clients.
		 */
		public inner() {
			
		}
		
		/**
		 * Constructor
		 * @noimplement This constructor is not intended to be referenced by clients.
		 */
		protected inner(int i) {
			
		}
		static class inner2 {
			/**
			 * Constructor
			 * @noimplement This constructor is not intended to be referenced by clients.
			 */
			public inner2() {
				
			}
			
			/**
			 * Constructor
			 * @noimplement This constructor is not intended to be referenced by clients.
			 */
			protected inner2(int i) {
				
			}
		}
	}
}

class outer {
	/**
	 * Constructor
	 * @noimplement This constructor is not intended to be referenced by clients.
	 */
	public outer() {
		
	}
	
	/**
	 * Constructor
	 * @noimplement This constructor is not intended to be referenced by clients.
	 */
	protected outer(int i) {
		
	}
}
