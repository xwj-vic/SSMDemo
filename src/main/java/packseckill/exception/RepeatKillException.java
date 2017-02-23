package packseckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * Created by xuweijie on 2017/2/21.
 */
public class RepeatKillException extends SeckillException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
