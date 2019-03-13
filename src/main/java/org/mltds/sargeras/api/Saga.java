package org.mltds.sargeras.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mltds.sargeras.listener.SagaListener;
import org.mltds.sargeras.manager.Manager;

/**
 * Saga 代表着一个长事务（LLT,long live transaction），由多个小事务（Tx）有序组成。<br/>
 * 利用 {@link SagaBuilder} 构建，被构建后不可更改，线程安全。
 *
 * @author sunyi
 */
public class Saga {

    /**
     * 默认每次执行时占有锁的最长时间，100秒。
     */
    private static final int DEFAULT_LOCK_TIMEOUT = 100;
    /**
     * 默认每笔业务的超时时间，1天。
     */
    private static final int DEFAULT_BIZ_TIMEOUT = 60 * 60 * 24;

    private static final int[] DEFAULT_TRIGGER_INTERVAL = new int[] { 1, 2, 4, 8, 16, 32, 64, 128 };

    private final String appName;
    private final String bizName;

    private int lockTimeout = DEFAULT_LOCK_TIMEOUT;
    private int bizTimeout = DEFAULT_BIZ_TIMEOUT;
    private int[] triggerInterval = DEFAULT_TRIGGER_INTERVAL;

    private List<SagaTx> txList = new ArrayList<>();
    private List<SagaListener> listenerList = new ArrayList<>();

    Saga(String appName, String bizName) {
        this.appName = appName;
        this.bizName = bizName;
    }

    public static String getKeyName(String appName, String bizName) {
        return appName + "-" + bizName;
    }

    public String getAppName() {
        return appName;
    }

    public String getBizName() {
        return bizName;
    }

    public String getKeyName() {
        return getKeyName(this.appName, this.bizName);
    }

    void addTx(SagaTx tx) {
        txList.add(tx);
    }

    public List<SagaTx> getTxList() {
        return Collections.unmodifiableList(txList);
    }

    public List<SagaListener> getListenerList() {
        return Collections.unmodifiableList(listenerList);
    }

    void addListener(SagaListener listener) {
        this.listenerList.add(listener);
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int getBizTimeout() {
        return bizTimeout;
    }

    public void setBizTimeout(int bizTimeout) {
        this.bizTimeout = bizTimeout;
    }

    public int[] getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(int[] triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    /**
     * 首次执行
     */
    public SagaResult start(String bizId, Object bizParam) {
        Manager manager = SagaApplication.getManager();
        return manager.start(this, bizId, bizParam);
    }

    public SagaResult restart(String bizId) {
        Manager manager = SagaApplication.getManager();
        return manager.restart(this, bizId);
    }

}