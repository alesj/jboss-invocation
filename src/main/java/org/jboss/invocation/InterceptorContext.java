/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.invocation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationMessages.msg;

/**
 * An interceptor/invocation context object.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class InterceptorContext implements Cloneable {
    private static final List<Interceptor> EMPTY = Collections.<Interceptor>emptyList();
    private static final ListIterator<Interceptor> EMPTY_ITR = EMPTY.listIterator();

    private Object target;
    private Method method;
    private Object[] parameters;
    private Map<String, Object> contextData;
    private Object timer;
    private List<Interceptor> interceptors = EMPTY;
    private ListIterator<Interceptor> interceptorIterator = EMPTY_ITR;
    private final Map<Object, Object> privateData = new IdentityHashMap<Object, Object>();
    private final InvocationContext invocationContext = new Invocation();

    /**
     * Get the invocation target which is reported to the interceptor invocation context.
     *
     * @return the invocation target
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Set the invocation target which is reported to the interceptor invocation context.
     *
     * @param target the invocation target
     */
    public void setTarget(final Object target) {
        this.target = target;
    }

    /**
     * Get the invoked method which is reported to the interceptor invocation context.
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Set the invoked method which is reported to the interceptor invocation context.
     *
     * @param method the method
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * Get the method parameters which are reported to the interceptor invocation context.
     *
     * @return the method parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Set the method parameters which are reported to the interceptor invocation context.
     *
     * @param parameters the method parameters
     */
    public void setParameters(final Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Get the context data which is reported to the interceptor invocation context.
     *
     * @return the context data
     * @throws IllegalStateException if the context data was never initialized
     */
    public Map<String, Object> getContextData() throws IllegalStateException {
        Map<String, Object> contextData = this.contextData;
        if (contextData == null) {
            throw new IllegalStateException("The context data was not set");
        }
        return contextData;
    }

    /**
     * Set the context data which is reported to the interceptor invocation context.
     *
     * @param contextData the context data
     */
    public void setContextData(final Map<String, Object> contextData) {
        this.contextData = contextData;
    }

    /**
     * Get the timer object which is reported to the interceptor invocation context.
     *
     * @return the timer object
     */
    public Object getTimer() {
        return timer;
    }

    /**
     * Set the timer object which is reported to the interceptor invocation context.
     *
     * @param timer the timer object
     */
    public void setTimer(final Object timer) {
        this.timer = timer;
    }

    /**
     * Get the invocation context.
     *
     * @return the invocation context
     */
    public InvocationContext getInvocationContext() {
        return invocationContext;
    }

    /**
     * Get a private data item.
     *
     * @param type the data type class object
     * @param <T> the data type
     * @return the data item or {@code null} if no such item exists
     */
    public <T> T getPrivateData(Class<T> type) {
        return type.cast(privateData.get(type));
    }

    /**
     * Get a private data item.  The key will be looked up by object identity, not by value.
     *
     * @param key the object key
     * @return the private data object
     */
    public Object getPrivateData(Object key) {
        return privateData.get(key);
    }

    /**
     * Insert a private data item.
     *
     * @param type the data type class object
     * @param value the data item value, or {@code null} to remove the mapping
     * @param <T> the data type
     * @return the data item which was previously mapped to this position, or {@code null} if no such item exists
     */
    public <T> T putPrivateData(Class<T> type, T value) {
        if (value == null) {
            return type.cast(privateData.remove(type));
        } else {
            return type.cast(privateData.put(type, type.cast(value)));
        }
    }

    /**
     * Insert a private data item.  The key is used by object identity, not by value; in addition, if the key is
     * a {@code Class} then the value given must be assignable to that class.
     *
     * @param key the data key
     * @param value the data item value, or {@code null} to remove the mapping
     * @return the data item which was previously mapped to this position, or {@code null} if no such item exists
     */
    public Object putPrivateData(Object key, Object value) {
        if (key instanceof Class) {
            final Class<?> type = (Class<?>) key;
            if (value == null) {
                return type.cast(privateData.remove(type));
            } else {
                return type.cast(privateData.put(type, type.cast(value)));
            }
        } else {
            if (value == null) {
                return privateData.remove(key);
            } else {
                return privateData.put(key, value);
            }
        }
    }

    /**
     * Get the current interceptors.
     *
     * @return the interceptors
     */
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * Returns the next interceptor index.
     *
     * @return
     */
    public int getNextInterceptorIndex() {
        return interceptorIterator.nextIndex();
    }

    /**
     * Set the interceptor iterator.
     *
     * @param interceptors the interceptor list
     */
    public void setInterceptors(final List<Interceptor> interceptors) {
        setInterceptors(interceptors, 0);
    }

    /**
     * Set the interceptors.  With a starting index to proceed from.
     *
     * @param interceptors the interceptor list
     * @param nextIndex the next index to proceed
     */
    public void setInterceptors(final List<Interceptor> interceptors, int nextIndex) {
        if (interceptors == null) {
            throw new IllegalArgumentException("interceptors is null");
        }
        this.interceptors = interceptors;
        interceptorIterator = interceptors.listIterator(nextIndex);
    }

    /**
     * Pass the invocation on to the next step in the chain.
     *
     * @return the result
     * @throws Exception if an invocation throws an exception
     */
    public Object proceed() throws Exception {
        final ListIterator<Interceptor> iterator = interceptorIterator;
        if (iterator.hasNext()) {
            Interceptor next = iterator.next();
            try {
                return next.processInvocation(this);
            } finally {
                if (iterator.hasPrevious()) iterator.previous();
            }
        } else {
            throw msg.cannotProceed();
        }
    }

    /**
     * Clone this interceptor context instance.  The cloned context will resume execution at the same point that
     * this context would have at the moment it was cloned.
     *
     * @return the copied context
     */
    public InterceptorContext clone() {
        final InterceptorContext clone = new InterceptorContext();
        final Map<String, Object> contextData = this.contextData;
        if (contextData != null) {
            clone.contextData = new HashMap<String, Object>(contextData);
        }
        clone.privateData.putAll(privateData);
        clone.target = target;
        clone.method = method;
        clone.parameters = parameters;
        clone.timer = timer;
        final int next = interceptorIterator.nextIndex();
        clone.setInterceptors(interceptors.subList(next, interceptors.size()));
        return clone;
    }

    private class Invocation implements InvocationContext {
        public Object getTarget() {
            return target;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getParameters() {
            return parameters;
        }

        public void setParameters(final Object[] params) {
            parameters = params;
        }

        public Map<String, Object> getContextData() {
            return contextData;
        }

        public Object getTimer() {
            return timer;
        }

        public Object proceed() throws Exception {
            return InterceptorContext.this.proceed();
        }
    }
}
