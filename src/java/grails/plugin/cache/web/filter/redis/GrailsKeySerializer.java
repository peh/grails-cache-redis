package grails.plugin.cache.web.filter.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class GrailsKeySerializer implements RedisSerializer {
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        return o.toString().getBytes();
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return new String(bytes);
    }
}
