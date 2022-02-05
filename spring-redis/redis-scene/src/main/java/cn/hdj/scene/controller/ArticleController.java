package cn.hdj.scene.controller;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.bitMap.BitMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/1 下午10:17
 * @see <a>https://www.yuque.com/h_dj/kd0g9f/pefeqn</a>
 */
@RequestMapping(value = "/articles")
@RestController
public class ArticleController {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 点赞
     */
    @PutMapping(value = "/like")
    public void like(Long articleId){
        redisTemplate.opsForSet()
                .add("like:"+articleId,getUserId());
    }

    /**
     * 取消点赞
     */
    @PutMapping(value = "/unlike")
    public void unlike(Long articleId){
        redisTemplate.opsForSet()
                .remove("like:"+articleId,getUserId());
    }

    /**
     * 检查用户是否已点赞
     */
    @GetMapping(value = "/isLike")
    public Boolean isLike(Long articleId){
        return redisTemplate.opsForSet().isMember("like:"+articleId,getUserId());
    }


    /**
     * 获取所有点赞的用户
     */
    @DeleteMapping(value = "/like/users")
    public List<String> likeUsers(Long articleId){
        return redisTemplate.opsForSet().members("like:"+articleId)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }


    /**
     * 返回已点赞用户数
     */
    @GetMapping(value = "/like/sum")
    public Integer likeSum(Long articleId){
        return this.redisTemplate.opsForSet().size("like:"+articleId).intValue();
    }


    public Long getUserId(){
        return 10001L;
    }


    {
        BitMapBloomFilter
    }
}
