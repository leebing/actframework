package act.job;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.app.ActionContext;
import act.app.App;
import act.util.ActContext;
import org.osgl.http.H;
import org.osgl.util.E;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Communicate context data across threads
 */
public class JobContext extends ActContext.Base<JobContext> {

    private static ThreadLocal<JobContext> current_ = new ThreadLocal<JobContext>();

    private JobContext() {
        super(App.instance());
        ActContext<?> actContext = ActContext.Base.currentContext();
        if (null != actContext) {
            bag_.put("locale", actContext.locale());
            if (actContext instanceof ActionContext) {
                H.Session session = ((ActionContext) actContext).session();
                if (null != session) {
                    bag_.put("session", session);
                }
            }
        }
    }

    private static Map<String, Object> m() {
        return current_.get().bag_;
    }

    /**
     * Whether JobContext of current thread initialized
     * @return `true` if current job context is not null
     */
    public static boolean initialized() {
        return null != current_.get();
    }

    public static JobContext current() {
        return current_.get();
    }

    @Override
    public JobContext accept(H.Format fmt) {
        return this;
    }

    @Override
    public H.Format accept() {
        throw E.unsupport();
    }

    @Override
    public String methodPath() {
        throw E.unsupport();
    }

    @Override
    public Set<String> paramKeys() {
        throw E.unsupport();
    }

    @Override
    public String paramVal(String s) {
        throw E.unsupport();
    }

    @Override
    public String[] paramVals(String s) {
        throw E.unsupport();
    }

    /**
     * Init JobContext of current thread
     */
    static void init() {
        clear();
        current_.set(new JobContext());
    }

    /**
     * Clear JobContext of current thread
     */
    static void clear() {
        JobContext ctxt = current_.get();
        if (null != ctxt) {
            ctxt.bag_.clear();
            current_.remove();
        }
    }

    /**
     * Get value by key from the JobContext of current thread
     * @param key the key
     * @param <T> the val type
     * @return
     */
    public static <T> T get(String key) {
        return (T) m().get(key);
    }

    /**
     * Generic version of getting value by key from the JobContext of current thread
     * @param key the key
     * @param clz the val class
     * @param <T> the val type
     * @return the value
     */
    public static <T> T get(String key, Class<T> clz) {
        return (T)m().get(key);
    }

    /**
     * Set value by key to the JobContext of current thread
     * @param key the key
     * @param val the value
     */
    public static void put(String key, Object val) {
        m().put(key, val);
    }

    /**
     * Remove value by key from the JobContext of current thread
     * @param key the key
     */
    public static void remove(String key) {
        m().remove(key);
    }

    /**
     * Make a copy of JobContext of current thread
     * @return the copy of current job context or an empty job context
     */
    static JobContext copy() {
        JobContext ctxt = new JobContext();
        JobContext current = current_.get();
        if (null != current) {
            ctxt.bag_.putAll(current.bag_);
        }
        return ctxt;
    }

    /**
     * Initialize current thread's JobContext using specified copy
     * @param origin the original job context
     */
    static void init(JobContext origin) {
        current_.set(origin);
        if (origin.bag_.isEmpty()) {
            return;
        }
        ActContext<?> actContext = ActContext.Base.currentContext();
        if (null != actContext) {
            Locale locale = (Locale) origin.bag_.get("locale");
            if (null != locale) {
                actContext.locale(locale);
            }
            H.Session session = (H.Session) origin.bag_.get("session");
            if (null != session) {
                actContext.attribute("__session", session);
            }
        }
    }

    private Map<String, Object> bag_ = new HashMap<>();

}
