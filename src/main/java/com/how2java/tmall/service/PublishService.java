/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.service;

import com.how2java.tmall.dao.PublishDAO;
import com.how2java.tmall.es.PublishESDAO;
import com.how2java.tmall.pojo.Publish;
import com.how2java.tmall.util.ImageUtil;
import com.how2java.tmall.util.Page4Navigator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "publish")
public class PublishService {

    @Autowired
    PublishDAO publishDAO;
    @Autowired
    PublishESDAO publishESDAO;

    //    @Cacheable(key="'products-one-'+ #p0")
    public Publish get(int id) {
        return publishDAO.findOne(id);
    }

    @CacheEvict(allEntries = true)
    public void add(Publish bean, HttpServletRequest request) {
        try {
            bean.setImgs(saveOrUpdateImageFile(request));
            publishDAO.save(bean);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String saveOrUpdateImageFile(HttpServletRequest request) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> multipartfiles = multipartRequest.getFiles("image");
        File imageFolder = new File(request.getServletContext().getRealPath("img/publish"));
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
        publishDAO.delete(id);
    }

    //    @Cacheable(key = "'publishs-one-'+ #p0")
    public Page4Navigator<Publish> search(String good, int userid, int start, int size, int navigatePages) {
        Publish publish = new Publish();
        publish.setUserid(userid);
        publish.setGood(good);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true).withMatcher("good", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<Publish> example = Example.of(publish, matcher);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Publish> pageFromJPA = publishDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    //    @Cacheable(key = "'publishs-one-'+ #p0")
    public Page4Navigator<Publish> listAll(int userid, int start, int size, int navigatePages) {
        Publish publish = new Publish();
        publish.setUserid(userid);
        Example<Publish> example = Example.of(publish);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Publish> pageFromJPA = publishDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    //    @Cacheable(key = "'publishs-one-'+ #p0")
    public Page4Navigator<Publish> list(int userid, Integer status, int start, int size, int navigatePages) {
        Publish publish = new Publish();
        publish.setUserid(userid);
        publish.setStatus(status);
        Example<Publish> example = Example.of(publish);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Publish> pageFromJPA = publishDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    @CacheEvict(allEntries = true)
    public void update(Publish bean) {
        publishDAO.save(bean);
    }

//	@Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
//	public Page4Navigator<Product> list(int cid, int start, int size,int navigatePages) {
//    	Category category = categoryService.get(cid);
//    	Sort sort = new Sort(Sort.Direction.DESC, "id");
//    	Pageable pageable = new PageRequest(start, size, sort);
//    	Page<Product> pageFromJPA =productDAO.findByCategory(category,pageable);
//    	return new Page4Navigator<>(pageFromJPA,navigatePages);
//	}

//	public void fill(List<Category> categorys) {
//		for (Category category : categorys) {
//			fill(category);
//		}
//	}


//	@Cacheable(key="'products-cid-'+ #p0.id")
//	public List<Product> listByCategory(Category category){
//		return productDAO.findByCategoryOrderById(category);
//	}
//
//	public void fill(Category category) {
//		PublishService productService = SpringContextUtil.getBean(PublishService.class);
//		List<Product> products = productService.listByCategory(category);
//		productImageService.setFirstProdutImages(products);
//		category.setProducts(products);
//	}
//
//
//	public void fillByRow(List<Category> categorys) {
//        int productNumberEachRow = 8;
//        for (Category category : categorys) {
//            List<Product> products =  category.getProducts();
//            List<List<Product>> productsByRow =  new ArrayList<>();
//            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
//                int size = i+productNumberEachRow;
//                size= size>products.size()?products.size():size;
//                List<Product> productsOfEachRow =products.subList(i, size);
//                productsByRow.add(productsOfEachRow);
//            }
//            category.setProductsByRow(productsByRow);
//        }
//	}
//
//
//	public void setSaleAndReviewNumber(Product product) {
//        int saleCount = orderItemService.getSaleCount(product);
//        product.setSaleCount(saleCount);
//
//
//        int reviewCount = reviewService.getCount(product);
//        product.setReviewCount(reviewCount);
//
//	}


//	public void setSaleAndReviewNumber(List<Product> products) {
//		for (Product product : products)
//			setSaleAndReviewNumber(product);
//	}

    public List<Publish> search(String keyword, int start, int size) {
        initDatabase2ES();
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                .add(QueryBuilders.matchPhraseQuery("name", keyword),
                        ScoreFunctionBuilders.weightFactorFunction(100))
                .scoreMode("sum")
                .setMinScore(10);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(functionScoreQueryBuilder).build();

        Page<Publish> page = publishESDAO.search(searchQuery);
        return page.getContent();
    }

    private void initDatabase2ES() {
        Pageable pageable = new PageRequest(0, 5);
        Page<Publish> page = publishESDAO.findAll(pageable);
        if (page.getContent().isEmpty()) {
            List<Publish> publishs = publishDAO.findAll();
            for (Publish publish : publishs) {
                publishESDAO.save(publish);
            }
        }
    }
}

/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */
