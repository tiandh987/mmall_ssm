package com.tiandh.service;

import com.github.pagehelper.PageInfo;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.Product;
import com.tiandh.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer productId,Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> productSearch(Integer productId,String productName,Integer pageNum,Integer pageSize);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,
                                                         Integer pageNum,Integer pageSize,String orderBy);

}
