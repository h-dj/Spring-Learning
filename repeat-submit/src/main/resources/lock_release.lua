-- lua in redis
local key = KEYS[1] -- 锁标识
local value = ARGV[1] -- 全局唯一值

if (redis.call('hexists', key, value) == 1) then
    -- 当计数器为0时才真正删除锁
    if (redis.call('hincrby', key, value, -1) < 1) then
        redis.call('del', key)
    end
    return 1
end

return 0