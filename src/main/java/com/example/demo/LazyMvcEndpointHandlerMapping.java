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
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

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
			this.context = new SpringApplicationBuilder(Object.class).bannerMode(Mode.OFF)
					.initializers(new LazyInitializer()).web(WebApplicationType.NONE)
					.parent(this.parent).run();
			this.delegate = this.context.getBean(HandlerMapping.class);
		}
		return this.delegate.getHandler(request);
	}

	static class LazyInitializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(LazyController.class, () -> new LazyController());
			context.registerBean(SimpleHandlerMapping.class,
					() -> new SimpleHandlerMapping(context));
		}

	}

	static class SimpleHandlerMapping extends AbstractHandlerMapping {

		private ApplicationContext context;

		public SimpleHandlerMapping(ApplicationContext context) {
			this.context = context;
		}

		@Override
		protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
			return this.context.getBean(LazyController.class);
		}

	}

	static class LazyController implements Controller {
		@Override
		public ModelAndView handleRequest(HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			response.setStatus(HttpStatus.OK.value());
			StreamUtils.copy("ok".getBytes(), response.getOutputStream());
			return null;
		}
	}

}
