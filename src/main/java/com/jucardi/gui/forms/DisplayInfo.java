/**
 * @file						DisplayInfo.java
 * @author					juan.diaz
 * @date						08/03/2012
 * @copyright		  	Jucardi. All Rights Reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.jucardi.gui.forms;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DisplayInfo
{
	public String displayName();
	public String description() default "";
	public String category() default "Misc";
}
