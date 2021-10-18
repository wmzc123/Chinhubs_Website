/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.service;

import com.how2java.tmall.dao.PublishDAO;
import com.how2java.tmall.es.PublishESDAO;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Category2;
import com.how2java.tmall.pojo.Publish;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.util.ImageUtil;
import com.how2java.tmall.util.Page4Navigator;
import com.how2java.tmall.util.SpringContextUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "publish")
public class PublishService {

    @Autowired
    PublishDAO publishDAO;
    @Autowired
    PublishESDAO publishESDAO;

    public void fill(List<Category2> category2s) {
        for (Category2 category2 : category2s) {
            fill(category2);
        }
    }

    public void fill(Category2 category2) {
        PublishService publishService = SpringContextUtil.getBean(PublishService.class);
        List<Publish> publishes = publishService.listByCategory2(category2);
//        productImageService.setFirstProdutImages(products);
        category2.setPublishes(publishes);
    }

    //    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Publish> listByCategory2(Category2 category2) {
        return publishDAO.findByCategory2OrderById(category2);
    }

    public void fillByRow(List<Category2> categorys) {
        int productNumberEachRow = 8;
        for (Category2 category : categorys) {
            List<Publish> publishes = category.getPublishes();
            List<List<Publish>> productsByRow = new ArrayList<>();
            for (int i = 0; i < publishes.size(); i += productNumberEachRow) {
                int size = i + productNumberEachRow;
                size = size > publishes.size() ? publishes.size() : size;
                List<Publish> publishesOfEachRow = publishes.subList(i, size);
                productsByRow.add(publishesOfEachRow);
            }
            category.setPublishesByRow(productsByRow);
        }
    }

    //    @Cacheable(key="'products-one-'+ #p0")
    public Publish get(int id) {
        return publishDAO.findOne(id);
    }

    @CacheEvict(allEntries = true)
    public void add(Publish bean, HttpServletRequest request) {
        bean.setCreat(new Date());
        bean.setStatus(0);
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
    public Page4Navigator<Publish> search(String keyword, int start, int size, int navigatePages) {
        Publish publish = new Publish();
        publish.setGood(keyword);
        publish.setStatus(0);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true).withMatcher("good", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<Publish> example = Example.of(publish, matcher);
        Sort sort = new Sort(Sort.Direction.DESC, "creat");
        PageRequest pageRequest = new PageRequest(start, size, sort);
        Page<Publish> pageFromJPA = publishDAO.findAll(example, pageRequest);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    //    @Cacheable(key = "'publishs-one-'+ #p0")
    public List<Publish> search(User user) {
        Publish publish = new Publish();
        publish.setUserid(user.getId());
        Example<Publish> example = Example.of(publish);
        return publishDAO.findAll(example);
    }

    //    @Cacheable(key = "'publishs-one-'+ #p0")
    public Page4Navigator<Publish> search(String keyword, int userid, int start, int size, int navigatePages) {
        Publish publish = new Publish();
        publish.setUserid(userid);
        publish.setKeyword(keyword);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true).withMatcher("keyword", ExampleMatcher.GenericPropertyMatchers.contains());
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

}

/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */
