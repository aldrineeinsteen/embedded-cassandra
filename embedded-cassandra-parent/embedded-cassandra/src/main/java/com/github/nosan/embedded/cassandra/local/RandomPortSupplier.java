/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Random port {@link Supplier}.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
class RandomPortSupplier implements Supplier<Integer> {

	private static final int ATTEMPTS = 1024;

	private static final int SIZE = 50;

	private static final int MIN = 49152;

	private static final int MAX = 65535;

	private final ArrayDeque<Integer> ports = new ArrayDeque<>(50);

	private final Supplier<InetAddress> addressSupplier;

	RandomPortSupplier(Supplier<InetAddress> addressSupplier) {
		this.addressSupplier = addressSupplier;
	}

	@Override
	public Integer get() {
		int size = this.ports.size();
		if (size == SIZE) {
			this.ports.removeFirst();
		}
		return getPort();
	}

	private int getPort() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		InetAddress address = this.addressSupplier.get();
		for (int i = 0; i < ATTEMPTS; i++) {
			int port = MIN + random.nextInt(MAX - MIN + 1);
			if (!this.ports.contains(port)) {
				try (ServerSocket ss = new ServerSocket(port, 1, address)) {
					this.ports.addLast(port);
					return port;
				}
				catch (IOException ex) {
					//ignore
				}
			}
		}
		throw new IllegalStateException(
				String.format("Can not find an available port in the range [%d, %d]", MIN, MAX));
	}

}