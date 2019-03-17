package org.mltds.sargeras.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.SagaNotFoundException;
import org.mltds.sargeras.spi.SagaBean;
import org.mltds.sargeras.spi.SagaBeanFactory;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.service.Service;
import org.mltds.sargeras.spi.pollretry.PollRetry;

/**
 * @author sunyi
 */
public class SagaApplication {

    private static final Map<Class, SagaBeanFactory> factories = new ConcurrentHashMap<>();
    private static final Map<String, Saga> sagas = new ConcurrentHashMap<>();

    static void addSaga(Saga saga) {
        String keyName = saga.getKeyName();
        if (sagas.containsKey(keyName)) {
            throw new SagaException("已经存在一个相同的Saga，同名的Saga：" + keyName);
        }
        sagas.put(keyName, saga);
    }

    static void addBeanFactory(Class<? extends SagaBean> beanClass, SagaBeanFactory beanFactory) {
        factories.put(beanClass, beanFactory);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SagaBean> T getBean(Class<T> beanClass) {
        SagaBeanFactory beanFactory = factories.get(beanClass);
        return (T) beanFactory.getObject();
    }

    public static Service getService() {
        return getBean(Service.class);
    }

    public static Manager getRepository() {
        return getBean(Manager.class);
    }

    public static Saga getSaga(String keyName) {
        Saga saga = sagas.get(keyName);
        if (saga == null) {
            throw new SagaNotFoundException(keyName);
        }
        return saga;
    }

    public static Saga getSaga(String appName, String bizName) {
        return getSaga(Saga.getKeyName(appName, bizName));
    }

    public static PollRetry getPollRetry() {
        return getBean(PollRetry.class);
    }
}