package packseckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import packseckill.dto.Exposer;
import packseckill.dto.SeckillExecution;
import packseckill.entity.Seckill;
import packseckill.exception.RepeatKillException;
import packseckill.exception.SeckillCloseException;
import packseckill.exception.SeckillException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xuweijie on 2017/2/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;


    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() throws Exception {
        long seckillId=1000;
        Seckill seckill=seckillService.getById(seckillId);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId=1000;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);

        if (exposer.isExposed())
        {
            logger.info("exposer={}",exposer);

            long userPhone=13502171127L;
            String md5=exposer.getMd5();

            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("seckillExecution={}",seckillExecution);
            }catch (RepeatKillException e)
            {
                e.printStackTrace();
            }catch (SeckillCloseException e1)
            {
                e1.printStackTrace();
            }
        }else {
            //秒杀未开启
            logger.info("exposer={}",exposer);
        }

    }

}