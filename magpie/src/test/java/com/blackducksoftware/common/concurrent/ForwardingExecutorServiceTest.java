/*
 * Copyright (C) 2015 Black Duck Software Inc.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.blackducksoftware.common.concurrent.ForwardingExecutorService.SimpleForwardingExecutorService;
import com.google.common.collect.ImmutableList;

/**
 * Tests for the {@code ForwardingExecutorService}.
 *
 * @author jgustie
 */
public class ForwardingExecutorServiceTest {

    @Test
    public void testKnownDelegateMethods() throws Exception {
        ExecutorService mockedExecutorService = mock(ExecutorService.class);
        Callable<Void> callableTask = mock(Callable.class);
        Runnable runnableTask = mock(Runnable.class);
        Collection<Callable<Void>> callableTasks = ImmutableList.of(callableTask);
        ExecutorService forwardingExecutorService = new SimpleForwardingExecutorService(mockedExecutorService) {};

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

    // TODO Verify wrapping

}
