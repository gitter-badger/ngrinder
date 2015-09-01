/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.common.util;

/**
 * String utility.
 * 
 * @author Gisoo Gwon
 */
public class StringUtils {

	/**
	 * If last char is ',' then delete char comma
	 * @param builder
	 */
	public static void deleteLastComma(StringBuilder builder) {
		if (builder == null || builder.length() == 0) {
			return;
		}
		if (builder.charAt(builder.length() - 1) == ',') {
			builder.deleteCharAt(builder.length() - 1);
		}
	}
	
}
