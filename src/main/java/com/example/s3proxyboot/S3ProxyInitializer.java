package com.example.s3proxyboot;

import com.google.common.base.Strings;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gaul.s3proxy.AuthenticationType;
import org.gaul.s3proxy.S3Proxy;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class S3ProxyInitializer implements CommandLineRunner {

    private final String s3proxy_endpoint;
    private final String s3proxy_virtualhost;
    private final String s3proxy_authorization;
    private final String s3proxy_identity;
    private final String s3proxy_credential;
    private final String s3proxy_cors_allow_all;
    private final String s3proxy_ignore_unknown_headers;
    private final String jclouds_provider;
    private final String jclouds_identity;
    private final String jclouds_credential;

    public S3ProxyInitializer() {

        System.out.println("Initializing S3Proxy ...");

        s3proxy_endpoint = System.getenv("S3PROXY_ENDPOINT");
        s3proxy_virtualhost = System.getenv("S3PROXY_VIRTUALHOST");
        s3proxy_authorization = System.getenv("S3PROXY_AUTHORIZATION");
        s3proxy_identity = System.getenv("S3PROXY_IDENTITY");
        s3proxy_credential = System.getenv("S3PROXY_CREDENTIAL");
        s3proxy_cors_allow_all = System.getenv("S3PROXY_CORS_ALLOW_ALL");
        s3proxy_ignore_unknown_headers = System.getenv("S3PROXY_IGNORE_UNKNOWN_HEADERS");

        jclouds_provider = System.getenv("JCLOUDS_PROVIDER");
        jclouds_identity = System.getenv("JCLOUDS_IDENTITY");
        jclouds_credential = System.getenv("JCLOUDS_CREDENTIAL");

        System.out.println("--------------------------------------------------------------");
        System.out.println("S3 Proxy Settings:");
        System.out.println("  endpoint:       " + s3proxy_endpoint);
        System.out.println("  virtualhost:    " + s3proxy_virtualhost);
        System.out.println("  authorization:  " + s3proxy_authorization);
        System.out.println("  identity:       " + s3proxy_identity);
        System.out.println("  credential:     " + s3proxy_credential);
        System.out.println("  cors-allow:     " + s3proxy_cors_allow_all);
        System.out.println("  ignore-headers: " + s3proxy_ignore_unknown_headers);
        System.out.println("JClouds Settings:");
        System.out.println("  provider:       " + jclouds_provider);
        System.out.println("  identity:       " + jclouds_identity);
        System.out.println("  credential:     " + jclouds_credential);
        System.out.println("--------------------------------------------------------------");

    }

    @Override
    public void run(String... strings) throws Exception {

        System.out.println("Starting S3Proxy ...");

        BlobStoreContext context = ContextBuilder
                .newBuilder(jclouds_provider)
                .credentials(jclouds_identity, jclouds_credential)
                .buildView(BlobStoreContext.class);

        S3Proxy proxy;

        try {

            S3Proxy.Builder s3ProxyBuilder = S3Proxy.builder().blobStore(context.getBlobStore());

            if ( s3proxy_endpoint != null ) {
                s3ProxyBuilder.endpoint(URI.create(s3proxy_endpoint));
            }

            if ( !Strings.isNullOrEmpty(s3proxy_virtualhost)) {
                s3ProxyBuilder.virtualHost(s3proxy_virtualhost);
            }

            if ( !Strings.isNullOrEmpty(s3proxy_cors_allow_all)) {
                s3ProxyBuilder.corsAllowAll(true);
            }

            if ( !Strings.isNullOrEmpty(s3proxy_ignore_unknown_headers) ) {
                s3ProxyBuilder.ignoreUnknownHeaders(true);
            }

            AuthenticationType s3proxy_authentication_type = AuthenticationType.NONE;

            if ( !Strings.isNullOrEmpty(s3proxy_authorization)) {

                if ("aws_v2".equals(s3proxy_authorization.toLowerCase())) {
                    s3proxy_authentication_type = AuthenticationType.AWS_V2;
                } else if ("aws_v4".equals(s3proxy_authorization.toLowerCase())) {
                    s3proxy_authentication_type = AuthenticationType.AWS_V4;
                } else if ("aws_v2_or_v4".equals(s3proxy_authorization.toLowerCase())) {
                    s3proxy_authentication_type = AuthenticationType.AWS_V2_OR_V4;
                }

                if (s3proxy_authentication_type != AuthenticationType.NONE) {
                    s3ProxyBuilder.awsAuthentication(s3proxy_authentication_type, s3proxy_identity, s3proxy_credential);
                }
            }

            proxy = s3ProxyBuilder.build();
            proxy.start();

            while (!proxy.getState().equals(AbstractLifeCycle.STARTED)) {
                Thread.sleep(1);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}