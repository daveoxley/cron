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
package com.workplacesystems.queuj.process.jpa;

import com.workplacesystems.queuj.Access;
import com.workplacesystems.queuj.Occurrence;
import com.workplacesystems.queuj.Output;
import com.workplacesystems.queuj.Queue;
import com.workplacesystems.queuj.Resilience;
import com.workplacesystems.queuj.Visibility;
import com.workplacesystems.queuj.process.ProcessParameters;
import com.workplacesystems.queuj.process.jpa.ProcessImpl;
import com.workplacesystems.queuj.process.jpa.ProcessImpl.Status;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

/**
 *
 * @author Dave Oxley
 */
@StaticMetamodel(ProcessImpl.class)
public abstract class ProcessImpl_ {

    public static volatile SingularAttribute<ProcessImpl,Integer> processId;
    public static volatile SingularAttribute<ProcessImpl,Integer> OPTLOCK;
    public static volatile SingularAttribute<ProcessImpl,String> queueOwnerId;
    public static volatile SingularAttribute<ProcessImpl,UUID> uuid;
    public static volatile SingularAttribute<ProcessImpl,String> processName;
    public static volatile SingularAttribute<ProcessImpl,Date> creationTimestamp;
    public static volatile SingularAttribute<ProcessImpl,String> description;
    public static volatile SingularAttribute<ProcessImpl,Date> scheduledTimestamp;
    public static volatile SingularAttribute<ProcessImpl,Date> startedTimestamp;
    public static volatile SingularAttribute<ProcessImpl,ProcessParameters> parameters;
    public static volatile SingularAttribute<ProcessImpl,Queue> queue;
    public static volatile SingularAttribute<ProcessImpl,String> userId;
    public static volatile SingularAttribute<ProcessImpl,Locale> locale;
    public static volatile SingularAttribute<ProcessImpl,Status> status;
    public static volatile SingularAttribute<ProcessImpl,Occurrence> occurrence;
    public static volatile SingularAttribute<ProcessImpl,Access> access;
    public static volatile SingularAttribute<ProcessImpl,Visibility> visibility;
    public static volatile SingularAttribute<ProcessImpl,Resilience> resilience;
    public static volatile SingularAttribute<ProcessImpl,Output> output;
    public static volatile SingularAttribute<ProcessImpl,Integer> attempt;
    public static volatile SingularAttribute<ProcessImpl,Integer> runCount;
    public static volatile SingularAttribute<ProcessImpl,Integer> resultCode;
    public static volatile SingularAttribute<ProcessImpl,Boolean> associatedReport;
    public static volatile SingularAttribute<ProcessImpl,Boolean> keepCompleted;

}
