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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.util.ClassUtils;

/**
 * Glob {@link CqlScript} implementation for {@link ClassLoader#getResources(String)}.
 * <p>
 * All resources will be interpreted as {@link URL} and <b>sorted</b> by {@code URL.toString()}.
 * <blockquote>
 * <table border="0" summary="Pattern Language">
 * <tr>
 * <td>{@code *.cql}</td>
 * <td>Matches a path that represents a file name ending in {@code .cql}</td>
 * </tr>
 * <tr>
 * <td>{@code **.cql}</td>
 * <td>Matches all path that represents a file name ending in {@code .cql}</td>
 * </tr>
 * <tr>
 * <td>{@code rol?s.cql}</td>
 * <td>Matches file names starting with {@code rol<any>s.cql}</td>
 * </tr>
 * <tr>
 * <td>{@code {roles,keyspace}.cql}</td>
 * <td>Matches file names starting with {@code roles.cql or keyspace.cql}</td>
 * </tr>
 * <tr>
 * <td><tt>home&#47;*&#47;*&#47;roles.cql</tt>
 * <td>Matches <tt>home&#47;any&#47;any&#47;roles.cql</tt></td>
 * </tr>
 * <tr>
 * <td><tt>home&#47;**&#47;roles.cql</tt>
 * <td>Matches <tt>home&#47;...&#47;roles.cql</tt></td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @author Dmytro Nosan
 * @see CqlScript#classpathGlobs(String...)
 * @since 1.2.6
 */
@API(since = "1.2.6", status = API.Status.STABLE)
public final class ClassPathGlobCqlScript implements CqlScript {

	private static final Logger log = LoggerFactory.getLogger(ClassPathGlobCqlScript.class);

	private static final String WINDOWS = "\\\\";

	@Nonnull
	private final String pattern;

	@Nullable
	private final Charset encoding;

	@Nullable
	private final ClassLoader classLoader;

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param pattern the glob pattern within the class path
	 */
	public ClassPathGlobCqlScript(@Nonnull String pattern) {
		this(pattern, null, null);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param pattern the glob pattern within the class path
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathGlobCqlScript(@Nonnull String pattern, @Nullable Charset encoding) {
		this(pattern, null, encoding);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param pattern the glob pattern within the class path
	 * @param classLoader the class loader to load the resource with.
	 */
	public ClassPathGlobCqlScript(@Nonnull String pattern, @Nullable ClassLoader classLoader) {
		this(pattern, classLoader, null);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param pattern the glob pattern within the class path
	 * @param classLoader the class loader to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathGlobCqlScript(@Nonnull String pattern, @Nullable ClassLoader classLoader,
			@Nullable Charset encoding) {
		Objects.requireNonNull(pattern, "Pattern must not be null");
		this.pattern = cleanPattern(pattern);
		this.encoding = encoding;
		this.classLoader = (classLoader != null) ? classLoader : ClassUtils.getClassLoader();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	@Nonnull
	@Override
	public Collection<String> getStatements() {
		String pattern = this.pattern;
		Charset encoding = this.encoding;
		ClassLoader classLoader = this.classLoader;
		List<UrlCqlScript> scripts = getResourcesByPattern(classLoader, pattern)
				.stream()
				.sorted(Comparator.comparing(URL::toString))
				.map(url -> new UrlCqlScript(url, encoding))
				.collect(Collectors.toList());
		return new CqlScripts(scripts).getStatements();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ClassPathGlobCqlScript that = (ClassPathGlobCqlScript) other;
		return Objects.equals(this.pattern, that.pattern) &&
				Objects.equals(this.encoding, that.encoding) &&
				Objects.equals(this.classLoader, that.classLoader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.pattern, this.encoding, this.classLoader);
	}

	@Override
	@Nonnull
	public String toString() {
		return this.pattern;
	}

	private static Set<URL> getResourcesByPattern(ClassLoader cl, String pattern) {
		if (!hasPattern(pattern)) {
			return getResources(cl, pattern);
		}
		String directory = getRootDirectory(pattern);
		String subPattern = pattern.substring(directory.length());
		return getResources(cl, directory)
				.stream()
				.map(url -> getResourcesByPattern(url, cl, subPattern))
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private static Set<URL> getResourcesByPattern(URL url, ClassLoader cl, String pattern) {
		try {
			if ("jar".equals(url.getProtocol())) {
				int index = url.toString().indexOf("!/");
				if (index != -1) {
					String jarUri = url.toString().substring(0, index);
					String jarEntry = url.toString().substring(index + 1);
					try (FileSystem fileSystem = FileSystems
							.newFileSystem(URI.create(jarUri), Collections.emptyMap(), cl)) {
						return getResourcesByPattern(fileSystem.getPath(jarEntry), pattern);
					}
				}
			}
			return getResourcesByPattern(Paths.get(url.toURI()), pattern);
		}
		catch (IOException | URISyntaxException ex) {
			if (log.isDebugEnabled()) {
				log.error(String.format("Could not find resources for URL (%s) and glob pattern (%s)", url, pattern),
						ex);
			}
			return Collections.emptySet();
		}
	}

	private static Set<URL> getResourcesByPattern(Path path, String pattern) throws IOException {
		if (!Files.exists(path)) {
			return Collections.emptySet();
		}
		if (!Files.isReadable(path)) {
			return Collections.emptySet();
		}
		if (!Files.isDirectory(path)) {
			return Collections.emptySet();
		}
		Set<URL> urls = new LinkedHashSet<>();
		String globSyntax = cleanLocation(String.format("glob:%s/%s", path.toAbsolutePath(), pattern));
		PathMatcher matcher = path.getFileSystem().getPathMatcher(globSyntax);
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file.toAbsolutePath()) && Files.isReadable(file)) {
					urls.add(file.toUri().toURL());
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return urls;
	}

	private static Set<URL> getResources(ClassLoader cl, String name) {
		try {
			Enumeration<URL> enumeration = (cl != null) ? cl.getResources(name) : ClassLoader.getSystemResources(name);
			return new LinkedHashSet<>(Collections.list(enumeration));
		}
		catch (IOException ex) {
			log.error(String.format("Could not get URLs for location (%s)", name), ex);
			return Collections.emptySet();
		}
	}

	private static String getRootDirectory(String pattern) {
		int endIndex = pattern.length();
		while (endIndex > 0 && hasPattern(pattern.substring(0, endIndex))) {
			endIndex = pattern.lastIndexOf('/', endIndex - 2) + 1;
		}
		return pattern.substring(0, endIndex);
	}

	private static boolean hasPattern(String pattern) {
		return pattern.contains("*") || pattern.contains("?") || pattern.contains("[") || pattern.contains("{");
	}

	private static String cleanLocation(String path) {
		return path.replaceAll(WINDOWS, "/").replaceAll("/+", "/").trim();
	}

	private static String cleanPattern(String pattern) {
		pattern = cleanLocation(pattern);
		if (pattern.startsWith("/")) {
			pattern = pattern.substring(1);
		}
		return pattern;
	}
}
