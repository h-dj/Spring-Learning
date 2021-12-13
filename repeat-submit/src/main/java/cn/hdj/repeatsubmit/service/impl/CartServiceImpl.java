package cn.hdj.repeatsubmit.service.impl;

import cn.hdj.repeatsubmit.po.CartPO;
import cn.hdj.repeatsubmit.mapper.CartMapper;
import cn.hdj.repeatsubmit.service.ICartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        List<CartPO> list = this.list(queryWrapper);
        if (list == null || list.isEmpty()) {
            //添加到购物车
            //模拟耗时
            TimeUnit.SECONDS.sleep(20);
            this.save(cartPO);
            System.err.println("添加成功!");
        } else {
            //数量加一
            CartPO updateCartPO = list.get(0);
            updateCartPO.setQuantity(updateCartPO.getQuantity() + 1);
            this.updateById(updateCartPO);
        }
    }
}
