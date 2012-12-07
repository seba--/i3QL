/*
 *  License (BSD Style License):
 *   Copyright (c) 2011
 *   Software Technology Group
 *   Department of Computer Science
 *   Technische Universitiät Darmstadt
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the Software Technology Group Group or Technische
 *     Universität Darmstadt nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific
 *     prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.
 */
package de.tud.cs.st.vespucci.interfaces;


/**
 * A source code element is either a whole class, a method or a field
 * Always identified by a PackageIdentifier, a SimpleClassName and a LineNumber
 * We don't distinguish here between classes, methods or just simple fields
 * 
 * @author Patrick Gottschaemmer
 * @author Ralf Mitschke
 * @author Olav Lenz
 */
public interface ICodeElement {

	/**
	 * Returns the package identifier of the codeElement
	 * 
	 * for example:<br>
	 * <code>model</code><br>
	 * <code>tud.cs.st.vespucci.test</code>
	 * <br><br>
	 * 
	 * For default package an empty String will be return
	 * 
	 * @return The identifier of the package
	 */
	public String getPackageIdentifier();

	/**
	 * Returns the SimpleClassName of the codeElement
	 * 
	 * for example:<br>
	 * <code>DataModel</code><br>
	 * <code>DataModel$SubModel</code> (inner classes/nested classes)<br>
	 * <code>DataModel$0</code> (anonymous inner classes)
	 * 
	 * @return The name of the class
	 */
	public String getSimpleClassName();
	
	
}