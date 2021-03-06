-- 获取get请求参数
local uri_args = ngx.req.get_uri_args()
local product_id = uri_args["productId"]
local product_info = uri_args["productInfo"]

local product_cache_key = "product_info_"..product_id

local cache_ngx = ngx.shared.my_cache

-- 在本地缓存中加入热点数据，有效期为1小时
cache_ngx:set(product_cache_key,product_info,60 * 60)
