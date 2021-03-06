/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.lazy.actuate;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * @author Dave Syer
 *
 */
public class LazyMvcEndpointHandlerMapping extends AbstractUrlHandlerMapping
		implements Ordered, DisposableBean {

	private int order;
	private ConfigurableApplicationContext parent;
	private ConfigurableApplicationContext context;
	private List<HandlerMapping> delegates = new ArrayList<>();

	public LazyMvcEndpointHandlerMapping(ConfigurableApplicationContext parent,
			WebEndpointProperties properties) {
		this.parent = parent;
		registerHandler(properties.getBasePath() + "/**", this);
		setOrder(HIGHEST_PRECEDENCE);
	}

	@Override
	public void destroy() throws Exception {
		if (this.context != null) {
			this.context.close();
		}
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public final HandlerExecutionChain getHandlerInternal(HttpServletRequest request)
			throws Exception {
		if (this.context == null) {
			AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
			context.setParent(this.parent);
			context.register(ActuatorAutoConfigurations.class);
			context.refresh();
			this.delegates.addAll(context.getBeansOfType(HandlerMapping.class).values());
			OrderComparator.sort(this.delegates);
			for (HandlerMapping mapping : new ArrayList<>(this.delegates)) {
				if (mapping instanceof LazyMvcEndpointHandlerMapping) {
					this.delegates.remove(mapping);
				}
			}
			this.context = context;
		}
		for (HandlerMapping delegate : this.delegates) {
			HandlerExecutionChain result = delegate.getHandler(request);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
