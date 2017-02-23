package packseckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import packseckill.entity.Seckill;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xuweijie on 2017/2/21.
 * 配置Spring和Junit整合,junit启动时加载Spring IOC容器:@RunWith(SpringJUnit4ClassRunner.class)
 * spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入Dao实现依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() throws Exception {
        long id=1000;
        Seckill seckill=seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() throws Exception {
        List<Seckill> list=seckillDao.queryAll(0,100);
        for (Seckill seckill:list){
            System.out.println(seckill);
        }
    }

    @Test
    public void reduceNumber() throws Exception {
        int updateCount=seckillDao.reduceNumber(1000L,new Date());
        System.out.println("updateCount:"+updateCount);
    }

}