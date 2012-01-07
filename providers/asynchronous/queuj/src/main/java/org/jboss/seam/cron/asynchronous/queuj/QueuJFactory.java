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

import com.workplacesystems.queuj.process.ProcessImplServer;
import com.workplacesystems.queuj.process.ProcessPersistence;
import com.workplacesystems.queuj.process.ProcessWrapper;
import com.workplacesystems.queuj.process.QueujFactory;
import com.workplacesystems.queuj.process.QueujTransaction;
import com.workplacesystems.queuj.process.jpa.ProcessDAO;
import com.workplacesystems.queuj.utils.BackgroundProcess;
import com.workplacesystems.utilsj.Callback;
import java.util.List;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.seam.cron.util.CdiUtils;

/**
 * QueuJFactory for Seam Cron.
 *
 * @author Dave Oxley
 */
public class QueuJFactory extends QueujFactory {

    private static BeanManager beanManager;
    private static boolean persistenceContextInstalled = false;

    static void init(BeanManager _beanManager) {
        beanManager = _beanManager;

        final ProcessDAO processDAO = (ProcessImplDAO)getProcessDAO();
        persistenceContextInstalled = processDAO != null;

        if (persistenceContextInstalled) {
            (new BackgroundProcess() {

                @Override
                protected void doRun() {
                    if (processDAO != null) {
                        List<String> queueOwners = processDAO.findQueueOwners();
                        for (String queueOwner : queueOwners) {
                            ((ProcessImplServer)getProcessServer(queueOwner)).init();
                        }
                    }
                }
            }).start();
        }
    }

    public QueuJFactory() {
        super();
    }

    @Override
    protected ProcessDAO getProcessDAO0() {
        return CdiUtils.getInstanceByType(beanManager, ProcessImplDAO.class);
    }

    @Override
    protected QueujTransaction getTransaction0() {
        if (persistenceContextInstalled)
            return CdiUtils.getInstanceByType(beanManager, QueuJTransaction.class);
        else {
            return new QueujTransaction() {

                public <T> T doTransaction(ProcessWrapper process, Callback<T> callback, boolean doStart) {
                    return doTransaction(process.isPersistent(), callback, doStart);
                }

                public <T> T doTransaction(boolean persistent, Callback<T> callback, boolean doStart) {
                    if (persistent)
                        throw new IllegalStateException("PersistenceContext not installed. Persistence in Seam Cron requires a J2EE environment.");

                    T result = callback.action();

                    if (result instanceof ProcessWrapper) {
                        ProcessWrapper process = (ProcessWrapper)result;
                        ((ProcessImplServer)process.getContainingServer()).commit();
                        if (doStart)
                            process.start();
                    }

                    return result;
                }
            };
        }
    }

    @Override
    protected ProcessPersistence getPersistence0() {
        return CdiUtils.getInstanceByType(beanManager, ProcessInstance.class);
    }
}
