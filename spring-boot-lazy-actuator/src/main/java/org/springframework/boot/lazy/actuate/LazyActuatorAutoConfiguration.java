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

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dave Syer
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.lazy", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(Endpoint.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
public class LazyActuatorAutoConfiguration {
	
	@Configuration
	@ConditionalOnClass(org.springframework.web.servlet.HandlerMapping.class)
	@ConditionalOnWebApplication(type=Type.SERVLET)
	protected static class MvcAutoConfiguration {
		@Bean
		public LazyMvcEndpointHandlerMapping endpointHandlerMapping(ConfigurableApplicationContext context, WebEndpointProperties properties) {
			return new LazyMvcEndpointHandlerMapping(context, properties);
		}
	}

	@Configuration
	@ConditionalOnClass(org.springframework.web.reactive.HandlerMapping.class)
	@ConditionalOnWebApplication(type=Type.REACTIVE)
	protected static class WebfluxAutoConfiguration {
		@Bean
		public LazyReactiveEndpointHandlerMapping endpointHandlerMapping(ConfigurableApplicationContext context, WebEndpointProperties properties) {
			return new LazyReactiveEndpointHandlerMapping(context, properties);
		}
	}

}
