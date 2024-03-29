package cn.hdj.repeatsubmit.controller;


import cn.hdj.repeatsubmit.aspect.ApiRepeatLockSubmit;
import cn.hdj.repeatsubmit.aspect.ApiRepeatTokenSubmit;
import cn.hdj.repeatsubmit.aspect.ApiRepeatUniqueIdSubmit;
import cn.hdj.repeatsubmit.po.CartPO;
import cn.hdj.repeatsubmit.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * <p>
 * 购物车表 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2021-12-11
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;


    //@ApiRepeatUniqueIdSubmit(keyExpression = "@cartController.getUserId()+'_'+#cartPO.getProductId() +'_'+#cartPO.getProductSkuId()")
    //@ApiRepeatLockSubmit(keyExpression = "@cartController.getUserId()+'_'+#cartPO.getProductId() +'_'+#cartPO.getProductSkuId()")
    @ApiRepeatTokenSubmit()
    @PostMapping(value = "/add")
    public String add(@RequestBody CartPO cartPO) throws InterruptedException {
        cartPO.setMemberId(getUserId());
        iCartService.addCart(cartPO);
        return "ok";
    }


    /**
     * 获取当前登录用户ID
     *
     * @return
     */
    public Long getUserId() {
        return 1001L;
    }

}

