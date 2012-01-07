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
package org.jboss.seam.cron.asynchronous.queuj;

import com.workplacesystems.queuj.ProcessServer;
import com.workplacesystems.queuj.process.ProcessImplServer;
import com.workplacesystems.queuj.process.ProcessWrapper;
import com.workplacesystems.queuj.process.QueujTransaction;
import com.workplacesystems.utilsj.Callback;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import org.jboss.solder.logging.Logger;

/**
 * Note: Cannot support this in non J2EE environments due to no transactional
 * events: http://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html/environments.html#d0e5221
 * 
 * @author Dave Oxley
 */
@Stateless
@LocalBean
public class QueuJTransaction implements QueujTransaction {

    private final Logger log = Logger.getLogger(QueuJTransaction.class);

    @Inject
    @Any
    Event<ProcessWrapper> processEvent;

    @Inject
    @Any
    Event<ProcessServer> processServerEvent;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public <T> T doTransaction(ProcessWrapper process, Callback<T> callback, boolean doStart) {
        return doTransaction(process.isPersistent(), callback, doStart);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public <T> T doTransaction(boolean persistent, Callback<T> callback, boolean doStart) {
        T result = callback.action();
        if (result instanceof ProcessWrapper) {
            log.debug("Adding process " + ((ProcessWrapper)result).getProcessKey() + "(" +
                    ((ProcessWrapper)result).getProcessVersion() + ")");

            processServerEvent.fire(((ProcessWrapper)result).getContainingServer());
            if  (doStart)
                processEvent.fire((ProcessWrapper)result);
        }
        return result;
    }

    private static void commitProcessServer(@Observes(during=TransactionPhase.AFTER_SUCCESS) ProcessServer processServer) {
        ((ProcessImplServer)processServer).commit();
    }

    private static void rollbackProcessServer(@Observes(during=TransactionPhase.AFTER_FAILURE) ProcessServer processServer) {
        ((ProcessImplServer)processServer).rollback();
    }

    private static void startProcess(@Observes(during=TransactionPhase.AFTER_SUCCESS) ProcessWrapper process) {
        // This observer can get called before the commitProcessServer observer so make sure it's own server is committed.
        ((ProcessImplServer)process.getContainingServer()).commit();
        process.start();
    }
}
