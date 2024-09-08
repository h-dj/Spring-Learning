local rate = redis.call('hget', KEYS[1], 'rate');
local interval = redis.call('hget', KEYS[1], 'interval');
local type = redis.call('hget', KEYS[1], 'type');
assert(rate ~= false and interval ~= false and type ~= false, 'RateLimiter is not initialized')
              
local valueName = KEYS[2];
local permitsName = KEYS[4];
if type == '1' then 
    valueName = KEYS[3];
    permitsName = KEYS[5];
end;

assert(tonumber(rate) >= tonumber(ARGV[1]), 'Requested permits amount could not exceed defined rate'); 

local currentValue = redis.call('get', valueName); 
local res;
if currentValue ~= false then 
       local expiredValues = redis.call('zrangebyscore', permitsName, 0, tonumber(ARGV[2]) - interval); 
       local released = 0; 
       for i, v in ipairs(expiredValues) do 
            local random, permits = struct.unpack('Bc0I', v);
            released = released + permits;
       end; 

       if released > 0 then 
            redis.call('zremrangebyscore', permitsName, 0, tonumber(ARGV[2]) - interval); 
            if tonumber(currentValue) + released > tonumber(rate) then 
                 currentValue = tonumber(rate) - redis.call('zcard', permitsName); 
            else 
                 currentValue = tonumber(currentValue) + released; 
            end; 
            redis.call('set', valueName, currentValue);
       end;

       if tonumber(currentValue) < tonumber(ARGV[1]) then 
           local firstValue = redis.call('zrange', permitsName, 0, 0, 'withscores'); 
           res = 3 + interval - (tonumber(ARGV[2]) - tonumber(firstValue[2]));
       else 
           redis.call('zadd', permitsName, ARGV[2], struct.pack('Bc0I', string.len(ARGV[3]), ARGV[3], ARGV[1])); 
           redis.call('decrby', valueName, ARGV[1]); 
           res = nil; 
       end; 
else 
       redis.call('set', valueName, rate); 
       redis.call('zadd', permitsName, ARGV[2], struct.pack('Bc0I', string.len(ARGV[3]), ARGV[3], ARGV[1])); 
       redis.call('decrby', valueName, ARGV[1]); 
       res = nil; 
end;

local ttl = redis.call('pttl', KEYS[1]); 
if ttl > 0 then 
    redis.call('pexpire', valueName, ttl); 
    redis.call('pexpire', permitsName, ttl); 
end; 
return res;,