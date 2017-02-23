package packseckill.exception;

/**
 * 秒杀关闭异常
 * Created by xuweijie on 2017/2/21.
 */
public class SeckillCloseException extends SeckillException {
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
