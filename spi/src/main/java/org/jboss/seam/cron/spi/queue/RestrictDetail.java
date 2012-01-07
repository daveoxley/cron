/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.cron.spi.queue;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;

/**
 * Contains the details necessary for the queue provider to call a concurrency
 * restriction method using the BeanManager.
 * 
 * @author Dave Oxley
 */
public class RestrictDetail implements Serializable {

    private final Class<?> beanClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final String queueId;

    public RestrictDetail(final Class<?> beanClass, final Method method, final String queueId) {
        this.beanClass = beanClass;
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.queueId = queueId;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Method getMethod() {
        try {
            return beanClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new CronProviderInitialisationException("", ex);
        } catch (SecurityException ex) {
            throw new CronProviderInitialisationException("", ex);
        }
    }

    public String getQueueId() {
        return queueId;
    }
}
