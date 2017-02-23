package packseckill.service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import packseckill.dao.SeckillDao;
import packseckill.dao.SuccessKilledDao;
import packseckill.dao.cache.RedisDao;
import packseckill.dto.Exposer;
import packseckill.dto.SeckillExecution;
import packseckill.entity.Seckill;
import packseckill.entity.SuccessKilled;
import packseckill.enums.SeckillStatEnum;
import packseckill.exception.RepeatKillException;
import packseckill.exception.SeckillCloseException;
import packseckill.exception.SeckillException;
import packseckill.service.SeckillService;

import java.sql.Time;
import java.util.Date;
import java.util.List;

/**
 * Created by xuweijie on 2017/2/21.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    private final String salt="shsdssljdd'l.";//用于混淆md5

    @Autowired
    private RedisDao redisDao;

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {

        //优化点：缓存优化：超时的基础上的维护性
        //1.访问redis
        Seckill seckill=redisDao.getSeckill(seckillId);
        if(seckill==null){
            //2.访问数据库
            seckill=seckillDao.queryById(seckillId);
            if(seckill==null){
                return new Exposer(false,seckillId);
            }else {
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //获取当前系统的时间
        Date nowTime=new Date();
        if(nowTime.getTime()<startTime.getTime() ||
                nowTime.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base=seckillId+"/"+salt;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5==null||!md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime=new Date();
        try {
            //否则更新了库存，秒杀成功,增加明细
            int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount<=0)
            {
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存
                int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
                if(updateCount<=0){
                    //没有更新到记录,秒杀结束
                    throw new SeckillCloseException("seckill is closed");
                }else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1) {
            throw e1;
        }catch (RepeatKillException e2) {
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所以编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }
}
