/*
 * #%L
 * Camel EJB :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
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
 * #L%
 */

package org.wildfly.swarm.camel.test.ejb;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.camel.core.CamelCoreFraction;
import org.wildfly.swarm.camel.test.ejb.subA.HelloBean;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.JARArchive;

@CamelAware
@RunWith(Arquillian.class)
public class EjbIntegrationTest implements ContainerFactory {

    @Deployment
    public static JARArchive deployment() {
        final JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addClasses(HelloBean.class);
        return archive;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(new CamelCoreFraction());
    }

    @Test
    public void testStatelessSessionBean() throws Exception {

        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("ejb:java:module/HelloBean");
            }
        });

        camelctx.start();
        try {
            ProducerTemplate producer = camelctx.createProducerTemplate();
            String result = producer.requestBody("direct:start", "Kermit", String.class);
            Assert.assertEquals("Hello Kermit", result);
        } finally {
            camelctx.stop();
        }
    }
}
