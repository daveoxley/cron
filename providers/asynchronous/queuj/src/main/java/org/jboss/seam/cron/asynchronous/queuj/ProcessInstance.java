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

import com.workplacesystems.queuj.process.ProcessPersistence;
import com.workplacesystems.queuj.process.jpa.ProcessImpl;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author dave
 */
public class ProcessInstance implements ProcessPersistence<ProcessImpl> {

    private ProcessImpl instance;

    private Object id;

    @PersistenceContext
    EntityManager em;

    public void setId(Object o) {
        id = o;
    }

    public void clearInstance() {
        instance = null;
    }

    public ProcessImpl getInstance() {
        if (instance == null) {
            if (id == null)
                instance = new ProcessImpl();
            else
                load();
        }
        return instance;
    }

    private void load() {
        instance = em.find(ProcessImpl.class, id);
        if (instance == null) {
            throw new EntityNotFoundException("No ProcessImpl with id " + id);
        }
    }

    @TransactionAttribute
    public String persist() {
        em.persist(instance);
        em.flush();
        return "persisted";
    }

    @TransactionAttribute
    public String update() {
        em.flush();
        return "updated";
    }

    @TransactionAttribute
    public String remove() {
        em.remove(instance);
        return "removed";
    }
}
