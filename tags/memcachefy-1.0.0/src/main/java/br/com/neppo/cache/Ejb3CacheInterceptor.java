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
package br.com.neppo.cache;

import br.com.neppo.cache.interceptor.CacheInterceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * EJB3 cache interceptor
 *
 * @author bhlangonijr
 */
public class Ejb3CacheInterceptor extends CacheInterceptor {

	@AroundInvoke
	public Object interceptMethod(InvocationContext ctx) throws Exception {
		return this.intercept(ctx, ctx.getMethod(), ctx.getParameters());
	}

	@Override
	public Object proceed(Object obj, Method method, Object[] args) throws Exception {
		return ((InvocationContext) obj).proceed();
	}


}
