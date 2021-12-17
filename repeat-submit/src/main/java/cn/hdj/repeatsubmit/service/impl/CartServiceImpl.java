package cn.hdj.repeatsubmit.service.impl;

import cn.hdj.repeatsubmit.po.CartPO;
import cn.hdj.repeatsubmit.mapper.CartMapper;
import cn.hdj.repeatsubmit.service.ICartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2021-12-11
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, CartPO> implements ICartService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCart(CartPO cartPO) throws InterruptedException {

        LambdaQueryWrapper<CartPO> queryWrapper = Wrappers.<CartPO>lambdaQuery()
                .eq(CartPO::getMemberId, cartPO.getMemberId())
                .eq(CartPO::getProductId, cartPO.getProductId())
                .eq(CartPO::getProductSkuId, cartPO.getProductSkuId());
        //查询商品，已添加到购物车的，就增加数量即可(业务逻辑幂等)
        //因为 select 和 save 操作不是串行执行的，可能有两个线程同时查询到商品没有添加到购物车
        //然后同一个商品被两个线程分别入库了，导致购物车出现相同商品的两条记录
        List<CartPO> list = this.list(queryWrapper);
        //模拟耗时
        TimeUnit.SECONDS.sleep(1);
        if (list == null || list.isEmpty()) {
            //添加到购物车
            this.save(cartPO);
            System.err.println("添加成功!");
        } else {
            CartPO updateCartPO = list.get(0);
            //数量加一
            LambdaUpdateWrapper<CartPO> updateWrapper = Wrappers.<CartPO>lambdaUpdate()
                    .eq(CartPO::getId, updateCartPO.getId())
                    .setSql("quantity = quantity + 1");

            this.update(updateWrapper);
        }
    }
}
