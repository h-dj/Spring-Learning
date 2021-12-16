-- token 验证
-- 如果token 存在，则删除
if redis.call('get', KEYS[1]) == KEYS[2] then
    return redis.call('del', KEYS[1])
else
    return 0
end