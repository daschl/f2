package com.couchbase.client.core.io.service.spec;

import com.couchbase.client.core.io.service.design.DesignService;
import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import reactor.core.Environment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Verifies the correct functionality of a {@link ServiceSpec}.
 */
public class ServiceSpecTest {

    private static final Environment ENV = new Environment();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowWhenServiceTypeNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A ServiceType needs to be specified.");
        new ServiceSpec().get();
    }

    @Test
    public void shouldThrowWhenTargetNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A target host needs to be specified.");
        new ServiceSpec().type(ServiceType.DESIGN).get();
    }

    @Test
    public void shouldThrowWhenEnvironmentNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A Environment needs to be specified.");
        new ServiceSpec().type(ServiceType.DESIGN).target("127.0.0.1", 8091).get();
    }

    @Test
    public void shouldCreateADesignService() {
        Service service = new ServiceSpec()
            .target("127.0.0.1", 8092)
            .type(ServiceType.DESIGN)
            .env(ENV)
            .get();
        assertThat(service, instanceOf(DesignService.class));
    }

}
