/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * Factory that creates a {@link Cluster}.
 *
 * @author Dmytro Nosan
 * @see Cluster
 * @since 1.0.0
 */
public interface ClusterFactory {

	/**
	 * Creates a new configured {@link Cluster}.
	 *
	 * @param settings a settings
	 * @return a Cluster
	 */
	@Nonnull
	Cluster create(@Nonnull Settings settings);
}