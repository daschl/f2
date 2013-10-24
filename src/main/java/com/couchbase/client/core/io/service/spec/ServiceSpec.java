package com.couchbase.client.core.io.service.spec;

import com.couchbase.client.core.io.service.design.DesignService;
import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import reactor.core.Environment;
import reactor.function.Supplier;
import reactor.util.Assert;

import java.net.InetSocketAddress;

/**
 * A helper class for configuring a {@link Service}.
 */
public class ServiceSpec implements Supplier<Service> {

    private ServiceType serviceType;
    private String host;
    private int port;
    private Environment env;

    /**
     * The {@link Environment} to use.
     *
     * @param env the environment to use.
     * @return the {@link ServiceSpec} to allow for chaining.
     */
    public ServiceSpec env(Environment env) {
        this.env = env;
        return this;
    }

    /**
     * The {@link ServiceType} to use for the service.
     *
     * @param type defines the implementation type.
     * @return the {@link ServiceSpec} to allow for chaining.
     */
    public ServiceSpec type(ServiceType type) {
        serviceType = type;
        return this;
    }

    /**
     * The host and port to which this service should connect to.
     *
     * @param host the hostname of the server.
     * @param port the port of the server.
     * @return the {@link ServiceSpec} to allow for chaining.
     */
    public ServiceSpec target(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    /**
     * The host to which this service should connect to.
     *
     * If no port is set, the default one for the service is used.
     *
     * @param host the hostname of the server.
     * @return the {@link ServiceSpec} to allow for chaining.
     */
    public ServiceSpec target(String host) {
        this.host = host;
        return this;
    }

    @Override
    public Service get() {
        Assert.notNull(serviceType, "A ServiceType needs to be specified.");
        Assert.notNull(host, "A target host needs to be specified.");
        Assert.notNull(env, "A Environment needs to be specified.");

        switch(serviceType) {
            case DESIGN:
                port = port < 0 ? port : DesignService.DEFAULT_PORT;
                return new DesignService(new InetSocketAddress(host, port), env);
            default:
                throw new IllegalArgumentException("Unknown service provided: " + serviceType);
        }
    }

}
