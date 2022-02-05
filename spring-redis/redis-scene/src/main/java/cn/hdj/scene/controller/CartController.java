package cn.hdj.scene.controller;

import cn.hdj.scene.entity.Products;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: Redis 使用 Hash 保存购物车
 *
 * 添加商品 -> hset cart:1001 10088 1
 *
 * 增加数量 -> hincrby cart:1001 10088 1
 *
 * 商品总数 -> hlen cart:1001
 *
 * 删除商品 -> hdel cart:1001 10088
 *
 * 获取购物车所有商品 -> hgetall cart:1001
 *
 * @Author huangjiajian
 * @Date 2022/2/1 下午4:06
 *
 * @see <a>https://www.yuque.com/h_dj/kd0g9f/pefeqn</a>
 */
@RequestMapping(value = "/carts")
@RestController
public class CartController {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 添加产品
     */
    @PutMapping(value = "/products")
    public void addProduct(@RequestBody Products products){
        redisTemplate.opsForHash()
                .putIfAbsent("cart:"+getUserId(),products.getId()+":"+products.getSkuId(),1L);
    }

    /**
     * 添加产品数量
     */
    @PutMapping(value = "/products/count")
    public void addProductCount(@RequestBody Products products){
        redisTemplate.opsForHash()
                .increment("cart:"+getUserId(),products.getId()+":"+products.getSkuId(),1L);
    }

    /**
     * 商品总数
     */
    @GetMapping(value = "/products/sum")
    public Long sumOfProductsForUser(Long productId,Long skuId){
       return redisTemplate.opsForHash()
                .size("cart:"+getUserId());
    }


    /**
     * 删除商品
     */
    @DeleteMapping(value = "/products")
    public Long sumOfProductsForUser(@RequestBody Products products){
        return redisTemplate.opsForHash()
                .delete("cart:"+getUserId(),products.getId()+":"+products.getSkuId());
    }


    /**
     * 获取购物车所有商品
     */
    @GetMapping(value = "/products")
    public List<Products> getAllProducets(){

        Map<String, Long> entries = redisTemplate.<String, Long>opsForHash()
                .entries("cart:" + getUserId());

        List<Products> list = entries.entrySet()
                .stream()
                .map(entry -> {

                    String key = entry.getKey();
                    Long value = entry.getValue();

                    List<String> split = StrUtil.split(key, ":");


                    Products products = new Products();
                    products.setId(Long.parseLong(split.get(0)));
                    products.setSkuId(split.get(1));
                    products.setCount(value);
                    return products;
                })
                .collect(Collectors.toList());
        return list;
    }


    public Long getUserId(){
        return 10001L;
    }
}
