package com.example.s3proxyboot;

import com.google.common.base.Strings;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gaul.s3proxy.AuthenticationType;
import org.gaul.s3proxy.S3Proxy;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.net.URI;

@SpringBootApplication
public class S3ProxyBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3ProxyBootApplication.class, args);
	}

}
