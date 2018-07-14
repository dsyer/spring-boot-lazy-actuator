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

package com.example.config;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.example.config.ActuatorAutoConfigurations.EnableActuatorAutoConfigurations;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

@Configuration
// @ComponentScan(basePackages = "org.springframework.boot.actuate.autoconfigure",
// excludeFilters = @Filter(type = FilterType.CUSTOM, classes =
// AutoConfigurationFilter.class))
// @Import({ WebMvcEndpointManagementContextConfiguration.class })
@EnableActuatorAutoConfigurations
class ActuatorAutoConfigurations {

	static class AutoConfigurationFilter implements TypeFilter {

		@Override
		public boolean match(MetadataReader metadataReader,
				MetadataReaderFactory metadataReaderFactory) throws IOException {
			return !metadataReader.getClassMetadata().getClassName()
					.endsWith("AutoConfiguration");
		}

	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Inherited
	@AutoConfigurationPackage
	@Import(ActuatorAutoSelector.class)
	static @interface EnableActuatorAutoConfigurations {
		Class<?>[] exclude() default {};

		String[] excludeName() default {};
	}

	static class ActuatorAutoSelector extends AutoConfigurationImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata annotationMetadata) {
			String[] imports = super.selectImports(annotationMetadata);
			List<String> result = new ArrayList<>();
			for (String candidate : imports) {
				if (candidate.startsWith("org.springframework.boot.actuate")) {
					result.add(candidate);
				}
			}
			return result.toArray(new String[0]);
		}

		@Override
		protected Set<String> getExclusions(AnnotationMetadata metadata,
				AnnotationAttributes attributes) {
			// TODO: exclude only actuate autoconfigs
			return Collections.emptySet();
		}

		@Override
		protected Class<?> getAnnotationClass() {
			return EnableActuatorAutoConfigurations.class;
		}
	}
}