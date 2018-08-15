/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.play.internal.run;

import org.gradle.internal.reflect.JavaReflectionUtil;
import org.gradle.scala.internal.reflect.ScalaMethod;
import org.gradle.scala.internal.reflect.ScalaReflectionUtil;

import java.net.InetSocketAddress;

public class PlayRunAdapterV26X extends PlayRunAdapterV24X {
    @Override
    public InetSocketAddress runDevHttpServer(ClassLoader classLoader, ClassLoader docsClassLoader, Object buildLink, Object buildDocHandler, int httpPort, int httpsPort) throws ClassNotFoundException {
        Object reloadableServer = getServer(classLoader, buildLink, httpPort, httpsPort);
        return JavaReflectionUtil.method(reloadableServer, InetSocketAddress.class, "mainAddress").invoke(reloadableServer);
    }

    private Object getServer(ClassLoader classLoader, Object buildLink, int httpPort, int httpsPort) throws ClassNotFoundException {
        if (httpPort != -1) {
            if (httpsPort != -1) {
                throw new IllegalStateException("Play does not currently support starting the dev server with both HTTP and HTTPS");
            }
            ScalaMethod runMethod = ScalaReflectionUtil.scalaMethod(classLoader, "play.core.server.DevServerStart", "mainDevHttpMode", getBuildLinkClass(classLoader), int.class, String.class);
            return runMethod.invoke(buildLink, httpPort, "0.0.0.0");
        } else  if (httpsPort != -1) {
            ScalaMethod runMethod = ScalaReflectionUtil.scalaMethod(classLoader, "play.core.server.DevServerStart", "mainDevOnlyHttpsMode", getBuildLinkClass(classLoader), int.class, String.class);
            return runMethod.invoke(buildLink, httpsPort, "0.0.0.0");
        } else {
            throw new IllegalStateException("Either the HTTP or the HTTPS port should be specified");
        }
    }
}
