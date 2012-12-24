/*
 * Copyright 2012 neppo.com.br. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.memcachefy.hashkey;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.reflect.Method;

/**
 * Hash key generator
 *
 * @author bhlangonijr
 */
public class HashKeyGenerator {

	private static final int NULL_PARAM_KEY = 53;

	/**
	 *  Generates a hashkey using reflection
	 *
	 * @param method
	 * @param parameters
	 * @return
	 */
	public static Integer reflectionHashKey(Method method, Object[] parameters) {

		int hashCode = method.getName().hashCode();

		if (parameters == null || parameters.length == 0) {
			return hashCode;
		}

		if (parameters.length == 1) {
			return 17 * hashCode + (parameters[0] == null ?
					method.getName().hashCode() :
					HashCodeBuilder.reflectionHashCode(parameters[0]));
		}

		for (Object object : parameters) {
			hashCode = 31 * hashCode + (object == null ?
					NULL_PARAM_KEY :
					HashCodeBuilder.reflectionHashCode(object));
		}

		return hashCode;
	}

	/**
	 * Generates a hashkey using the object's own hashCode function
	 *
	 * @param method
	 * @param parameters
	 * @return
	 */
	public static Integer defaultHashKey(Method method, Object[] parameters) {

		int hashCode = method.getName().hashCode();

		if (parameters == null || parameters.length == 0) {
			return hashCode;
		}

		if (parameters.length == 1) {
			return 17 * hashCode + (parameters[0] == null ?
					method.getName().hashCode() :
					parameters[0].hashCode());
		}

		for (Object object : parameters) {
			hashCode = 31 * hashCode + (object == null ?
					NULL_PARAM_KEY : object.hashCode());
		}

		return hashCode;
	}
}
