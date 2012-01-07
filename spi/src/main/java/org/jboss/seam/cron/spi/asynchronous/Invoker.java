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
package org.jboss.seam.cron.spi.asynchronous;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Future;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import org.jboss.seam.cron.api.asynchronous.AsyncResult;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;
import org.jboss.seam.cron.impl.scheduling.exception.InternalException;
import org.jboss.seam.cron.util.CdiUtils;
import org.jboss.solder.logging.Logger;

import static org.jboss.seam.cron.spi.asynchronous.AsynchronousInterceptor.INVOKED_IN_THREAD;

/**
 * This class handles the invocation of the #{@link Asynchronous} method, unwrapping of the
 * results out of a "dummy" #{@link AsyncResult} if necessary, and firing post-execution
 * events with the results if any. It is designed as a managed bean to be instantiated via 
 * #{@literal @Inject Instance<Invoker>}.
 * 
 * @author Peter Royle
 */
public class Invoker implements Serializable {

    @Inject
    BeanManager beanMan;

    private transient InvocationContext ic;

    private Class<?> beanClass;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    private boolean popResultsFromFuture = false;
    private static final Logger log = Logger.getLogger(Invoker.class);

    public Invoker() {
    }

    /**
     * @param ic The #{@link InvocationContext} which will be executed.
     */
    public void setInvocationContext(final InvocationContext ic) {
        this.ic = ic;
        this.beanClass = CdiUtils.getBeanClass(beanMan, ic.getTarget());
        Method method = ic.getMethod();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.parameters = ic.getParameters();
    }

    /**
     * Set to true if the #{@link InvocationContext} returns a "dummy" #{@link Future}.
     * In that case we need to explicitly pop the return value out of it as it will have
     * already been wrapped in an #{@link AsyncResult} by the #{@link AsynchronousInterceptor}.
     * @param popResultsFromFuture 
     */
    protected void setPopResultsFromFuture(final boolean popResultsFromFuture) {
        this.popResultsFromFuture = popResultsFromFuture;
    }

    /**
     * Execute the #{@link InvocationContext}, unwrap the results from their #{@link AsyncResult}
     * if necessary and fire a post-execution event.
     * 
     * @return The result of the method invocation, unwrapped if #{@literal popResultsFromFuture} is true 
     * (ie: the return type of the method is a #{@link Future}).
     * @throws Exception Includes any exception thrown by the invoked method.
     */
    public Object executeInvocationContext() throws Exception {

        // This will be the basic form, with the result available immediately
        Method method = null;
        Object result = null;

        if (ic != null) {
            // housekeeping
            if (ic.getMethod() == null) {
                throw new InternalException("Failed to provide an InvocationContext/method to this " + this.getClass().getName());
            }

            method = ic.getMethod();
            if (log.isTraceEnabled()) {
                log.trace("Running Invocation Context for " + method.getName());
            }

            ic.getContextData().put(INVOKED_IN_THREAD, Boolean.TRUE);
            result = ic.proceed();
        }
        else {
            Object instance = CdiUtils.getInstanceByType(beanMan, beanClass);
            method = beanClass.getMethod(methodName, parameterTypes);

            AsynchronousInterceptor.invokedFromInterceptorInThread.set(true);
            result = method.invoke(instance, parameters);
        }

        if (popResultsFromFuture) {
            // pop the value out of the "dummy" AsynchResult as it will be wrapped
            // in proper AsynchResult by the AsynchronousInterceptor
            result = ((Future) result).get();
        }
        // fire the post execution event if a result was returned.
        if (result != null) {
            if (log.isTraceEnabled()) {
                log.trace("Firing post execution event result: " + result);
            }

            // grab qualifiers from the method to use for the post-execution event
            final ArrayList<Annotation> qualifiers = new ArrayList<Annotation>();
            for (Annotation ant : method.getAnnotations()) {
                if (beanMan.isQualifier(ant.annotationType())) {
                    qualifiers.add(ant);
                }
            }

            beanMan.fireEvent(result, qualifiers.toArray(new Annotation[qualifiers.size()]));
        } else {
            if (log.isTraceEnabled()) {
                if (method.getReturnType().equals(Void.TYPE)) {
                    log.trace("Method invocation on " + method.getName() + ":" + method.getClass().getName() + " returns void, so not firing a post-execution event");
                } else {
                    log.trace("Method invocation on " + method.getName() + ":" + method.getClass().getName() + " returned null, so not firing an event");
                }
            }
        }

        return result;

    }
}
