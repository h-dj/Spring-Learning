package cn.hdj.repeatsubmit.controller;


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

    @PostMapping(value = "/add")
    public ResponseEntity add(@RequestBody CartPO cartPO) throws InterruptedException {
        cartPO.setMemberId(getUserId());
        iCartService.addCart(cartPO);
        return ResponseEntity
                .ok()
                .build();
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

