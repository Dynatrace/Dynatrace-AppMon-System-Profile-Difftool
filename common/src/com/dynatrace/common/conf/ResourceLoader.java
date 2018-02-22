package com.dynatrace.common.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ResourceLoader {
	
	private static final ResourceLoader instanceDefault = new ResourceLoader();
	private static final ResourceLoader instanceFileSystemOverride = new FileSystemOverride();
	
	public static ResourceLoader getDefault() {
		return instanceDefault;
	}
	
	public static ResourceLoader getFileSystemOverride() {
		return instanceFileSystemOverride;
	}

	private ResourceLoader() {
	}
	
	public URL loadResource(Class<?> resourceClass, String resourceName) {
		return resourceClass.getResource(resourceName);
	}
	
	public URL loadResourceForce(Class<?> resourceClass, String resourceName) {
		URL resource = loadResource(resourceClass, resourceName);
		if (resource == null) {
			throw new IllegalStateException(String.format("Resource not found (class='%s', name='%s')", resourceClass.getName(), resourceName));
		}
		return resource;
	}
	
	private static class FileSystemOverride extends ResourceLoader {
		
		private FileSystemOverride() {
		}

		@Override
		public URL loadResource(Class<?> resourceClass, String resourceName) {
			File file = new File(resourceName);
			if (file.isFile()) {
				System.out.printf("Overriding resource (class='%s', name='%s') with file '%s'%n", resourceClass.getName(), resourceName, file.getAbsolutePath());
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					throw new IllegalStateException(e);
				}
			}
			return super.loadResource(resourceClass, resourceName);
		}
	}
	
	public static Properties loadProperties(URL resource) {
		Properties properties = new Properties();
		
		if (resource != null) {
			try (InputStream in = resource.openStream()) {
				properties.load(in);
			} catch (IOException e) {
				throw new IllegalStateException(String.format("Cannot load properties from URL '%s'", resource), e);
			}
		}
		
		return properties;
	}
}
