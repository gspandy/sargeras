package org.mltds.sargeras.serialize;

import org.mltds.sargeras.api.spi.SagaBean;

public interface Serialize extends SagaBean {
    <T> T read(String data, Class<T> c);

    String write(Object object);

}