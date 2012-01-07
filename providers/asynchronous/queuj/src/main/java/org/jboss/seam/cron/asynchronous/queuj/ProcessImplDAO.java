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

import com.workplacesystems.queuj.process.jpa.ProcessDAO;
import com.workplacesystems.queuj.process.jpa.ProcessImpl;
import com.workplacesystems.queuj.process.jpa.ProcessImpl_;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Dave Oxley
 */
public class ProcessImplDAO implements ProcessDAO {

    @PersistenceContext
    EntityManager em;

    boolean hasPersistenceContext() {
        return em != null;
    }

    public List<String> findQueueOwners() {
        if (em == null)
            return new ArrayList<String>();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<String> cquery = builder.createQuery(String.class);
        Root<ProcessImpl> process = cquery.from(ProcessImpl.class);

        cquery.select(process.get(ProcessImpl_.queueOwnerId)).distinct(true);

        return em.createQuery(cquery).getResultList();
    }

    public List<ProcessImpl> findProcesses(String queueOwner) {
        if (em == null)
            return new ArrayList<ProcessImpl>();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ProcessImpl> cquery = builder.createQuery(ProcessImpl.class);
        Root<ProcessImpl> process = cquery.from(ProcessImpl.class);

        if (queueOwner == null) {
            cquery.select(process).
                    where(
                        builder.isNull(process.get(ProcessImpl_.queueOwnerId))).
                    orderBy(
                        builder.asc(process.get(ProcessImpl_.creationTimestamp)));
        }
        else {
            cquery.select(process).
                    where(
                        builder.equal(process.get(ProcessImpl_.queueOwnerId), queueOwner)).
                    orderBy(
                        builder.asc(process.get(ProcessImpl_.creationTimestamp)));
        }

        return em.createQuery(cquery).getResultList();
    }
}
