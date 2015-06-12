/*
 * Copyright (C) 2014 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An executor service wrapper which additionally allows incoming tasks to be altered before
 * submission to a delegate executor service.
 *
 * @author jgustie
 */
public abstract class ForwardingExecutorService extends com.google.common.util.concurrent.ForwardingExecutorService {

    protected ForwardingExecutorService() {}

    /**
     * Returns an alternate task to be executed in place of the originally submitted task.
     */
    protected Runnable wrap(Runnable task) {
        return task;
    }

    /**
     * Returns an alternate task to be executed in place of the originally submitted task.
     */
    protected <V> Callable<V> wrap(Callable<V> task) {
        return task;
    }

    /**
     * Copies the supplied collection of tasks, invoking {@code wrap} on each element.
     */
    protected <V> Collection<? extends Callable<V>> wrapAll(Collection<? extends Callable<V>> tasks) {
        List<Callable<V>> wrappedTasks = new ArrayList<>(tasks.size());
        for (Callable<V> task : tasks) {
            wrappedTasks.add(wrap(task));
        }
        return wrappedTasks;
    }

    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(wrap(task), result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return super.invokeAll(wrapAll(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return super.invokeAll(wrapAll(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return super.invokeAny(wrapAll(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(wrapAll(tasks), timeout, unit);
    }
}
