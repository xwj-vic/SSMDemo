-- 秒杀执行存储过程
DELIMITER $$  --console;转换为$$

-- 定义存储过程
-- in:输入参数  out：输出参数
-- row_count():返回上一条修改类型sql（delete,insert,update）的影响行数
-- row_count: 0：未修改数据    >0：标识修改的行数   <0：sql错误/未执行修改sql
CREATE PROCEDURE 'seckill'.'execute_seckill'
(IN v_seckill_id bigint ,IN v_phone bigint,IN v_Kill_time TIMESTAMP ,OUT r_result INT)
  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION ;
    INSERT ignore success_killed(seckill_id,user_phone,status,create_time) VALUES(v_seckill_id,v_phone,0,v_Kill_time);  --先插入购买明细
    SELECT ROW_COUNT() INTO insert_count;
    IF(insert_count = 0) THEN
      ROLLBACK ;
      SET r_result = -1;   --重复秒杀
    ELSEIF(insert_count < 0) THEN
      ROLLBACK ;g
      SET r_result = -2;   --内部错误
    ELSE   --已经插入购买明细，接下来要减少库存
      UPDATE seckill
      SET number = number -1
      WHERE seckill_id = v_seckill_id
            AND start_time < v_Kill_time
            AND end_time > v_Kill_time
            AND number > 0;
      SELECT ROW_COUNT() INTO insert_count;
      IF (insert_count = 0)  THEN
        ROLLBACK ;
        SET r_result = 0;   --库存没有了，代表秒杀已经关闭
      ELSEIF (insert_count < 0) THEN
        ROLLBACK ;
        SET r_result = -2;   --内部错误
      ELSE
        COMMIT ;    --秒杀成功，事务提交
        SET  r_result = 1;   --秒杀成功返回值为1
      END IF;
    END IF;
  END
$$
-- 存储过程定义结束
DELIMITER ;

SET @fadeResult = -3;
-- 执行存储过程
CALL excuteSeckill(1003,13502178891,NOW(),@r_result);
-- 获取结果
SELECT @r_result;