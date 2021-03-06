local uri_args = ngx.req.get_uri_args()
local productId = uri_args["productId"]
local shopId = uri_args["shopId"]

local hosts = {"hang1", "hang2"}
local backend = ""

local hot_product_key = "hot_product_"..productId

local cache_ngx = ngx.shared.my_cache
local hot_product_flag = cache_ngx:get(hot_product_key)

-- 若是热点数据，则采用随机负载均衡，防止发生缓存热点现象
if hot_product_flag == "true" then
  math.randomseed(tostring(os.time()):reverse():sub(1, 7))
  local index = math.random(1, 2)  
  backend = "http://"..hosts[index]
else -- 若不是热点数据，则按普通数据，用 hash 分发策略
  local hash = ngx.crc32_long(productId)
  local index = (hash % 2) + 1
  backend = "http://"..hosts[index]
end

local requestPath = uri_args["requestPath"]
requestPath = "/"..requestPath.."?productId="..productId.."&shopId="..shopId

local http = require("resty.http")
local httpc = http.new()

local resp, err = httpc:request_uri(backend,{
  method = "GET",
  path = requestPath
})

if not resp then
  ngx.say("request error: ", err)
  return
end

ngx.say(resp.body)

httpc:close()
