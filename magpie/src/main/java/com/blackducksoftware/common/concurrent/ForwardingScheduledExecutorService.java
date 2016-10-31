/*
 * Copyright 2013 Black Duck Software, Inc.
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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A scheduled executor service wrapper which additionally allows incoming tasks to be altered
 * before submission to a delegate executor service.
 *
 * @author jgustie
 */
public abstract class ForwardingScheduledExecutorService extends ForwardingExecutorService implements
        ScheduledExecutorService {

    /**
     * A simple wrapper of a single scheduled executor service.
     */
    public static abstract class SimpleScheduledForwardingExecutorService extends ForwardingScheduledExecutorService {

        private final ScheduledExecutorService delegate;

        protected SimpleScheduledForwardingExecutorService(ScheduledExecutorService delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        protected final ScheduledExecutorService delegate() {
            return delegate;
        }
    }

    protected ForwardingScheduledExecutorService() {
    }

    @Override
    protected abstract ScheduledExecutorService delegate();

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return delegate().schedule(wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return delegate().schedule(wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return delegate().scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return delegate().scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }
}
