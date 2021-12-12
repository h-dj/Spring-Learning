package cn.hdj.repeatsubmit.service;

import cn.hdj.repeatsubmit.po.CartPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2021-12-11
 */
public interface ICartService extends IService<CartPO> {

    void addCart(CartPO cartPO) throws InterruptedException;
}
