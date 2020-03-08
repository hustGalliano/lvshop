-- // 引入json 解析类库
local cjson = require("cjson")
local producer = require("resty.kafka.producer")

local broker_list = {
    { host = "hang1", port = 9092 },
    { host = "hang2", port = 9092 },
    { host = "hang3", port = 9092 }
}

local log_json = {}
log_json["request_module"] = "product_detail_info"
log_json["headers"] = ngx.req.get_headers()
log_json["uri_args"] = ngx.req.get_uri_args()
log_json["body"] = ngx.req.read_body()
log_json["http_version"] = ngx.req.http_version()
log_json["method"] = ngx.req.get_method()
log_json["raw_header"] = ngx.req.raw_header()
log_json["body_data"] = ngx.req.get_body_data()

local message = cjson.encode(log_json)

-- 获取请求参数
local uri_args = ngx.req.get_uri_args()
local product_id = uri_args["productId"]
local shop_id = uri_args["shopId"]

local async_producer = producer:new(broker_list, { producer_type = "async" })
local ok, err = async_producer:send("access-log", product_id, message)

if not ok then  
    ngx.log(ngx.ERR, "kafka send err:", err)  
    return  
end

-- 获取之前声明的test_cache 缓存对象
local cache_ngx = ngx.shared.my_cache

-- 组装商品、店铺缓存key
local product_cache_key = "product_info_"..product_id
local shop_cache_key = "shop_info_"..shop_id

-- 从nginx 本地缓存中获取商品、店铺信息
local product_cache = cache_ngx:get(product_cache_key)
local shop_cache = cache_ngx:get(shop_cache_key)

-- 判断 商品缓存 是否有数据，如果没有，发送请求到商品缓存服务获取
if product_cache == "" or product_cache == nil then
  local http = require("resty.http")
  local httpc = http.new()

  -- 发送请求到商品缓存服务获取
  local resp,err = httpc:request_uri("http://hang1:8080",{
    method = "GET",
    path = "/getProductInfo?productId="..product_id
  })

  product_cache = resp.body

  -- 获取请求响应数据，并设置 nginx 本地缓存，过期时间在一个范围内随机
  math.randomseed(tostring(os.time()):reverse():sub(1, 7))
  local expire_time = math.random(600, 1200)  
  cache_ngx:set(product_cache_key, product_cache, expire_time)
end

-- 判断 店铺缓存 是否有数据，如果没有，发送请求到店铺缓存服务获取
if shopCache == "" or shopCache == nil then
  local http = require("resty.http")
  local httpc = http.new()

  -- 发送请求到店铺缓存服务获取
  local resp,err = httpc:request_uri("http://hang1:8080",{
    method = "GET",
    path = "/getShopInfo?shopId="..shop_id
  })

  -- 获取请求响应数据，并设置nginx 本地缓存，过期时间为 10 分钟
  shop_cache = resp.body
  cache_ngx:set(shop_cache_key, shop_cache, 10 * 60)
end

-- // 对商品、店铺数据进行json 转换
local product_cache_JSON = cjson.decode(product_cache)
local shop_cache_JSON = cjson.decode(shop_cache)

-- 构造模板渲染对象
local context = {
  productId = product_cache_JSON.id,
  productName = product_cache_JSON.name,
  productPrice = product_cache_JSON.price,
  productPictureList = product_cache_JSON.pictureList,
  productSpecification = product_cache_JSON.specification,
  productService = product_cache_JSON.service,
  productColor = product_cache_JSON.color,
  productSize = product_cache_JSON.size,
  shopId = shop_cache_JSON.id,
  shopName = shop_cache_JSON.name,
  shopLevel = shop_cache_JSON.level,
  shopGoodCommentRate = shop_cache_JSON.goodCommentRate
}
-- 引入template 解析渲染类库
local template = require("resty.template")
-- 渲染模板
template.render("product.html", context)
