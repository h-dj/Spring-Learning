-- lua in redis

local key = KEYS[1] -- 锁标识
local value = ARGV[1] -- 全局唯一值
local ttl = tonumber(ARGV[2]) or 0 -- 过期时间，默认不过期

if (redis.call('exists', key) == 0) then
    redis.call('hincrby', key, value, 1)
    if (ttl > 0) then
        redis.call('expire', key, ttl)
    end
    return 1
end

if (redis.call('hexists', key, value) == 1) then
    redis.call('hincrby', key, value, 1)
    return 1
end

return 0