package packseckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packseckill.entity.Seckill;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by xuweijie on 2017/2/23.
 */
public class RedisDao {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    private JedisPool jedisPool;

    public RedisDao(String ip,int port) {
        jedisPool=new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema=RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                String key="secklii:"+seckillId;
                //采用自定义序列化方式，protostuff
                byte[] bytes=jedis.get(key.getBytes());
                //从缓存中获取到
                if(bytes!=null){
                    //空对象
                    Seckill seckill=schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //seckill被反序列化
                    return seckill;
                }
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
      return null;
    }

    public String putSeckill(Seckill seckill){
        // set Object(Seckill)--》序列化--》byte[]
        try {
            Jedis jedis=jedisPool.getResource();
            try{
                String key="secklii:"+seckill.getSeckillId();
                byte[] bytes=ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout=60*60;//缓存一小时
                String result=jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

}
