/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 * Tests all tags are invalid when parent class is private or package default
 */
public class test12 {

	static class inner1 {

		/**
		 * @noextend
		 * @noinstantiate
		 * @noreference
		 * 
		 */
		public static class Clazz {

		}

		/**
		 * @noextend
		 * @noimplement
		 * @noreference
		 */
		public interface inter {

		}

		/**
		 * @noreference
		 * @noextend
		 */
		public int field = 0;

		/**
		 * @noreference
		 */
		public @interface annot {

		}

		/**
		 * @noreference
		 */
		enum enu {

		}
		
		/**
		 * @nooverride
		 * @noreference
		 * @noextend
		 */
		public void method(){
			
		}

	}

}
