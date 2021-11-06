/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.service;

import com.how2java.tmall.dao.ProductDAO;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.util.ImageUtil;
import com.how2java.tmall.util.Page4Navigator;
import com.how2java.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "Product")
public class ProductService {

    @Autowired
    ProductDAO productDAO;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }

    public void fill(Category category) {
        ProductService productService = SpringContextUtil.getBean(ProductService.class);
        List<Product> products = productService.listByCategory(category);
        productImageService.setFirstProdutImages(products);
        //将该分类下的所有发布商品查出,放入分类对象中
        category.setProducts(products);
    }

    //    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Product> listByCategory(Category category) {
        return productDAO.findByCategoryOrderById(category);
    }

    public void fillByRow(List<Category> categorys) {
        int ProductNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> Products = category.getProducts();
            List<List<Product>> ProductsByRow = new ArrayList<>();
            for (int i = 0; i < Products.size(); i += ProductNumberEachRow) {
                int size = i + ProductNumberEachRow;
                size = size > Products.size() ? Products.size() : size;
                List<Product> ProductsOfEachRow = Products.subList(i, size);
                ProductsByRow.add(ProductsOfEachRow);
            }
            category.setProductsByRow(ProductsByRow);
        }
    }

    //    @Cacheable(key="'products-one-'+ #p0")
    public Product get(int id) {
        return productDAO.findOne(id);
    }

    @CacheEvict(allEntries = true)
    public void add(Product bean, HttpServletRequest request) {
        bean.setCreateDate(new Date());
        bean.setStatus(0);
        productDAO.save(bean);
    }

    public String saveOrUpdateImageFile(HttpServletRequest request) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> multipartfiles = multipartRequest.getFiles("image");
        File imageFolder = new File(request.getServletContext().getRealPath("img/Product"));
        String imageStr = "";
        for (MultipartFile image : multipartfiles) {
            String imgeName = UUID.randomUUID().toString().replace("-", "");
            File file = new File(imageFolder, imgeName + ".jpg");
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            image.transferTo(file);
            BufferedImage img = ImageUtil.change2jpg(file);
            ImageIO.write(img, "jpg", file);
            imageStr = imageStr + imgeName + ",";
        }
        return imageStr;
    }

    @CacheEvict(allEntries = true)
    public void delete(int id) {
        productDAO.delete(id);
    }

    //    @Cacheable(key = "'Products-one-'+ #p0")
    public List<Product> search(String keyword) {
        Product Product = new Product();
        Product.setKeyword(keyword);
        Product.setStatus(0);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true).withMatcher("keyword", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<Product> example = Example.of(Product, matcher);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        List<Product> Products = productDAO.findAll(example, sort);
        for (Product bean : Products) {
            productImageService.setFirstProdutImage(bean);
            setSaleAndReviewNumber(bean);
        }
        return Products;
    }

    //    @Cacheable(key = "'Products-one-'+ #p0")
    public List<Product> search(User user) {
        Product Product = new Product();
        Product.setUserid(user.getId());
        Example<Product> example = Example.of(Product);
        return productDAO.findAll(example);
    }

    //    @Cacheable(key = "'Products-one-'+ #p0")
    public Page4Navigator<Product> search(String keyword, int userid, int start, int size, int navigatePages) {
        Product Product = new Product();
        Product.setUserid(userid);
        Product.setKeyword(keyword);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true).withMatcher("keyword", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<Product> example = Example.of(Product, matcher);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Product> pageFromJPA = productDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    //    @Cacheable(key = "'Products-one-'+ #p0")
    public Page4Navigator<Product> listAll(int userid, int start, int size, int navigatePages) {
        Product Product = new Product();
        Product.setUserid(userid);
        Example<Product> example = Example.of(Product);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Product> pageFromJPA = productDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    //    @Cacheable(key = "'Products-one-'+ #p0")
    public Page4Navigator<Product> list(int userid, Integer status, int start, int size, int navigatePages) {
        Product Product = new Product();
        Product.setUserid(userid);
        Product.setStatus(status);
        Example<Product> example = Example.of(Product);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Product> pageFromJPA = productDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    @CacheEvict(allEntries = true)
    public void update(Product bean) {
        productDAO.save(bean);
    }

    public void setSaleAndReviewNumber(Product Product) {
        int saleCount = orderItemService.getSaleCount(Product);
        Product.setSaleCount(saleCount);


        int reviewCount = reviewService.getCount(Product);
        Product.setReviewCount(reviewCount);

    }

}

