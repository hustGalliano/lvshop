local uri_args = ngx.req.get_uri_args()
local product_id = uri_args["productId"]

local cache_ngx = ngx.shared.my_cache

local hot_product_cache_key = "hot_product_"..product_id

-- 取消热点数据
-- 在本地缓存中将热点数据缓存的值设置为false
cache_ngx:set(hot_product_cache_key, "false", 60)
