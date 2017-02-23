package packseckill.exception;

/**
 * 秒杀相关业务异常
 * Created by xuweijie on 2017/2/21.
 */
public class SeckillException extends RuntimeException{
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
