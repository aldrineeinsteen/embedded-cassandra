/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TopologyFileInitializer}.
 *
 * @author Dmytro Nosan
 */
class TopologyFileInitializerTests {

	@Test
	void customize(@TempDir Path temporaryFolder) throws Exception {
		Path directory = temporaryFolder.resolve("conf");
		Files.createDirectories(directory);
		TopologyFileInitializer initializer = new TopologyFileInitializer(
				getClass().getResource("/cassandra-topology.properties"));
		initializer.initialize(directory.getParent(), new Version(3, 11, 3));
		try (InputStream inputStream = getClass().getResourceAsStream("/cassandra-topology.properties")) {
			assertThat(directory.resolve("cassandra-topology.properties"))
					.hasBinaryContent(IOUtils.toByteArray(inputStream));
		}

	}

	@Test
	void notCustomize(@TempDir Path temporaryFolder) throws Exception {
		Path directory = temporaryFolder.resolve("conf");
		Files.createDirectories(directory);
		TopologyFileInitializer initializer = new TopologyFileInitializer(null);
		initializer.initialize(directory.getParent(), new Version(3, 11, 3));
		assertThat(directory.resolve("cassandra-topology.properties")).doesNotExist();

	}

}
