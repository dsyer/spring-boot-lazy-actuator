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

package com.example.demo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * @author Dave Syer
 *
 */
@Component
@EnableConfigurationProperties(WebEndpointProperties.class)
public class LazyMvcEndpointHandlerMapping extends AbstractUrlHandlerMapping
		implements Ordered, DisposableBean {

	private int order;
	private ConfigurableApplicationContext parent;
	private ConfigurableApplicationContext context;
	private HandlerMapping delegate;

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
	protected Object lookupHandler(String urlPath, HttpServletRequest request)
			throws Exception {
		Object handler = super.lookupHandler(urlPath, request);
		if (handler == null) {
			return null;
		}
		if (this.context == null) {
			AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
			context.setParent(this.parent);
			context.register(SimpleActuatorConfiguration.class);
			context.refresh();
			this.delegate = context.getBean("webEndpointServletHandlerMapping", HandlerMapping.class);
			this.context = context;
		}
		return this.delegate.getHandler(request);
	}

	@Configuration
	@Import({ EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
			HealthIndicatorAutoConfiguration.class,
			HealthEndpointAutoConfiguration.class,
			WebMvcEndpointManagementContextConfiguration.class})
	static class SimpleActuatorConfiguration {
	}

}
