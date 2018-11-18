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

package com.github.nosan.embedded.cassandra.test.support;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Utility class to check a {@code cause} exception.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class CauseMatcher extends TypeSafeMatcher<Throwable> {

	@Nonnull
	private final Class<? extends Throwable> type;

	@Nonnull
	private final String[] expectedMessages;

	/**
	 * Creates a new {@link CauseMatcher}.
	 *
	 * @param cause caused exception
	 * @param expectedMessages expected message
	 */
	public CauseMatcher(@Nonnull Class<? extends Throwable> cause, @Nonnull String... expectedMessages) {
		this.type = Objects.requireNonNull(cause, "Cause must not be null");
		this.expectedMessages = Objects.requireNonNull(expectedMessages, "Messages must not be null");
	}

	@Override
	protected boolean matchesSafely(@Nonnull Throwable item) {
		return item.getClass().isAssignableFrom(this.type)
				&& containsMessage(item.getMessage(), this.expectedMessages);
	}

	@Override
	public void describeTo(@Nonnull Description description) {
		description.appendText("expects type ")
				.appendValue(this.type)
				.appendText(" and one of the messages ")
				.appendValue(Arrays.toString(this.expectedMessages));
	}


	private boolean containsMessage(String message, String[] messages) {
		for (String expected : messages) {
			if (message.contains(expected)) {
				return true;
			}
		}
		return false;
	}
}
