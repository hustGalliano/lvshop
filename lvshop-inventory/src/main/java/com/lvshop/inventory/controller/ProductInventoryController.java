package com.lvshop.inventory.controller;


import com.lvshop.inventory.service.ProductInventoryService;
import com.lvshop.inventory.service.RequestAsyncProcessService;
import com.lvshop.inventory.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvshop.inventory.pojo.ProductInventory;
import com.lvshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.lvshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.lvshop.inventory.request.Request;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品库存Controller
 * @author Galliano
 * 
 * 解决缓存和数据库双写不一致的具体思路：
 *   1、来了一个更新商品库存的请求，然后此时会先删除redis缓存，然后模拟卡顿5秒钟；
 *
 *   2、在卡顿时间内，手动发送一个商品缓存的读请求，
 *   	    因为此时redis中没有缓存，就会去MySQL中查最新数据，然后刷新到Redis缓存中的操作（也就是刷新商品库存的Redis缓存）
 *
 *   3、此时后来的这个读请求会和之前的更新商品库存的请求路由到同一个内存队列中，所以这个读请求会阻塞住，等待之前的操作执行；
 *
 *   4、5秒过后，写请求完成了更新MySQL商品库存后，读请求才会执行
 *
 *   5、读请求执行的时候，会将最新的库存从数据库中查询出来，然后更新到缓存中
 *
 * 具体实现了缓存和数据库双写一致性
 */
@RestController
public class ProductInventoryController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryController.class);

	@Autowired
	private RequestAsyncProcessService requestAsyncProcessService;
	@Autowired
	private ProductInventoryService productInventoryService;

	@RequestMapping("/saveProductInventory")
	public Response saveProductInventory(ProductInventory productInventory) {
		LOGGER.info("[接收到新增商品库存的请求] 商品id = "
				+ productInventory.getProductId() + ", 商品库存数量 = " + productInventory.getInventoryCnt());

		productInventoryService.saveProductInventory(productInventory);

		return new Response(Response.SUCCESS);
	}


	/**
	 * 更新商品库存
	 * @param productInventory
	 * @return
	 */
	@RequestMapping("/updateProductInventory")
	public Response updateProductInventory(ProductInventory productInventory) {
		LOGGER.info("[接收到更新商品库存的请求] 商品id = "
				+ productInventory.getProductId() + ", 商品库存数量 = " + productInventory.getInventoryCnt());
		
		try {
			// 生成一个更新MySQL库存的请求
			Request request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);

			requestAsyncProcessService.process(request);

			return new Response(Response.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Response.FAILURE);
		}
	}
	
	/**
	 * 获取商品库存
	 */
	@RequestMapping("/getProductInventory")
	public ProductInventory getProductInventory(Long productId) {
		LOGGER.info("[接收到一个商品库存的读请求] 商品id = " + productId);
		
		ProductInventory productInventory = null;
		
		try {
			// 生成一个刷新Redis缓存中库存的请求
			Request request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService);

			requestAsyncProcessService.process(request);
			
			// 将请求扔给service异步去处理以后，就需要while(true)一会儿，在这里hang住
			// 尝试等待前面的商品库存更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中
			long startTime = System.currentTimeMillis();
			long endTime = 0L;
			long waitTime = 0L;
			
			// 尝试在200ms内，从缓存中获取结果
			// 具体的waitTime的上限需要视情况而定
			while(true) {
				if(waitTime > 200) break;
				
				// 尝试读取库存的redis缓存数据
				productInventory = productInventoryService.getProductInventoryCache(productId);
				
				// 如果读取到了数据，那么就可以把结果返回
				if(productInventory != null) {
					LOGGER.info("[在200ms内读取到了redis中的库存缓存] 商品id = "
							+ productInventory.getProductId() + ", 商品库存数量 = " + productInventory.getInventoryCnt());
					return productInventory;
				}
				
				// 如果没有读取到结果，那么等待一段时间
				else {
					Thread.sleep(20);
					endTime = System.currentTimeMillis();
					waitTime = endTime - startTime;
				}
			}
			LOGGER.info("[缓存中无数据，现在去数据库读取数据......]");
			// 运行到此处，证明在200ms内，没有从Redis中拿到数据，
			// 也就是说，Redis中很可能真的没有这个数据，就直接从MySQL中读取数据
			productInventory = productInventoryService.findProductInventory(productId);

			if(productInventory != null) {
				// 将数据设置到缓存中
				productInventoryService.setProductInventoryCache(productInventory); 

				return productInventory;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 连MySQL中都没有数据，所以直接返回一个默认值。
		return new ProductInventory(productId, -1L);  
	}
}
