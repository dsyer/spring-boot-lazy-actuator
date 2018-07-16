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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
public class LazyActuatorEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		if (!environment.getProperty("spring.lazy.enabled", Boolean.class, true)) {
			return;
		}
		List<String> exclude = new ArrayList<>();
		List<String> autos = SpringFactoriesLoader
				.loadFactoryNames(EnableAutoConfiguration.class, null);
		for (String auto : autos) {
			if (isActuator(auto)) {
				exclude.add(auto);
			}
		}
		addDefaultProperties(environment, exclude);
	}

	private void addDefaultProperties(ConfigurableEnvironment environment,
			List<String> exclude) {
		if (exclude != null && !exclude.isEmpty()) {
			// TODO: save user's own excludes somewhere and apply them later
			MutablePropertySources sources = environment.getPropertySources();
			Map<String, Object> map;
			if (!sources.contains("lazyProperties")) {
				map = new HashMap<>();
				sources.addLast(new MapPropertySource("lazyProperties", map));
			}
			else {
				@SuppressWarnings("unchecked")
				Map<String, Object> source = (Map<String, Object>) sources
						.get("lazyProperties").getSource();
				map = source;
			}
			map.put("spring.autoconfigure.exclude",
					StringUtils.collectionToCommaDelimitedString(exclude));
		}
	}

	public static boolean isActuator(String auto) {
		if (!auto.startsWith("org.springframework.boot.actuate.autoconfig")) {
			return false;
		}
		if (auto.contains("metrics") || auto.contains("audit")) {
			return auto.contains("Endpoint");
		}
		return  true;
	}

}
