/*
 * Copyright 2014 Black Duck Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackducksoftware.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An executor service wrapper which additionally allows incoming tasks to be altered before
 * submission to a delegate executor service.
 *
 * @author jgustie
 */
public abstract class ForwardingExecutorService implements ExecutorService {

    /**
     * A simple wrapper of a single executor service.
     */
    public abstract static class SimpleForwardingExecutorService extends ForwardingExecutorService {

        private final ExecutorService delegate;

        protected SimpleForwardingExecutorService(ExecutorService delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        protected final ExecutorService delegate() {
            return delegate;
        }
    }

    protected ForwardingExecutorService() {
    }

    /**
     * Returns the backing delegate executor that is being wrapped.
     */
    protected abstract ExecutorService delegate();

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
        delegate().execute(wrap(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate().submit(wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate().submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate().submit(wrap(task), result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate().invokeAll(wrapAll(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().invokeAll(wrapAll(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate().invokeAny(wrapAll(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().invokeAny(wrapAll(tasks), timeout, unit);
    }

    @Override
    public void shutdown() {
        delegate().shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        // TODO Unwrap?
        return delegate().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().awaitTermination(timeout, unit);
    }
}
