/*
 * Copyright 2015 Black Duck Software, Inc.
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

import static org.junit.Assume.assumeNotNull;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.blackducksoftware.common.concurrent.ForwardingExecutorService.SimpleForwardingExecutorService;
import com.google.common.collect.ImmutableList;

/**
 * Tests for the {@code ForwardingExecutorService}.
 *
 * @author jgustie
 */
public class ForwardingExecutorServiceDelegateTest {

    @Mock
    private ExecutorService mockedExecutorService;

    @Mock
    private Callable<Void> callableTask;

    @Mock
    private Runnable runnableTask;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void allKnownDelegateMethods() throws Exception {
        assumeNotNull(mockedExecutorService, callableTask, runnableTask);
        Collection<Callable<Void>> callableTasks = ImmutableList.of(callableTask);
        ExecutorService forwardingExecutorService = new SimpleForwardingExecutorService(mockedExecutorService) {
        };

        // Call every method we know
        forwardingExecutorService.shutdown();
        forwardingExecutorService.shutdownNow();
        forwardingExecutorService.isShutdown();
        forwardingExecutorService.isTerminated();
        forwardingExecutorService.awaitTermination(0, TimeUnit.MILLISECONDS);
        forwardingExecutorService.submit(callableTask);
        forwardingExecutorService.submit(runnableTask);
        forwardingExecutorService.submit(runnableTask, null);
        forwardingExecutorService.invokeAll(callableTasks);
        forwardingExecutorService.invokeAll(callableTasks, 0, TimeUnit.MILLISECONDS);
        forwardingExecutorService.invokeAny(callableTasks);
        forwardingExecutorService.invokeAny(callableTasks, 0, TimeUnit.MILLISECONDS);

        // Verify said methods were invoked on wrapped executor service
        verify(mockedExecutorService).shutdown();
        verify(mockedExecutorService).shutdownNow();
        verify(mockedExecutorService).isShutdown();
        verify(mockedExecutorService).isTerminated();
        verify(mockedExecutorService).awaitTermination(0, TimeUnit.MILLISECONDS);
        verify(mockedExecutorService).submit(callableTask);
        verify(mockedExecutorService).submit(runnableTask);
        verify(mockedExecutorService).submit(runnableTask, null);
        verify(mockedExecutorService).invokeAll(callableTasks);
        verify(mockedExecutorService).invokeAll(callableTasks, 0, TimeUnit.MILLISECONDS);
        verify(mockedExecutorService).invokeAny(callableTasks);
        verify(mockedExecutorService).invokeAny(callableTasks, 0, TimeUnit.MILLISECONDS);
    }

}
