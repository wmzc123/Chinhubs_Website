/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.web;

import com.how2java.tmall.pojo.Publish;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.service.PublishService;
import com.how2java.tmall.util.ImageUtil;
import com.how2java.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class PublishController {

    @Autowired
    PublishService publishService;
    @Autowired
    ProductService productService;
//    @Autowired
//    PublishImageService publishImageService;

//    @GetMapping("/categories/{cid}/products")
//    public Page4Navigator<Product> list(@PathVariable("cid") int cid, @RequestParam(value = "start", defaultValue = "0") int start,@RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
//    	start = start<0?0:start;
//    	Page4Navigator<Product> page =productService.list(cid, start, size,5 );
//
//        productImageService.setFirstProdutImages(page.getContent());
//
//    	return page;
//    }

    @GetMapping("/publishs/search/{userid}")
    public Page4Navigator<Publish> search(@RequestParam("good") String good, @RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Publish> page = publishService.search(good, userid, start, size, 5);
        return page;
    }

    @GetMapping("/publishs/listAll/{userid}")
    public Page4Navigator<Publish> listAll(@RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Publish> page = publishService.listAll(userid, start, size, 5);
        return page;
    }

    @GetMapping("/publishs/list/{userid}")
    public Page4Navigator<Publish> list(@RequestParam("status") Integer status, @RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Publish> page = publishService.list(userid, status, start, size, 5);
        return page;
    }


    @PostMapping("/publishs")
    public Object add(Publish bean,HttpServletRequest request) throws Exception {
        bean.setCreat(new Date());
        publishService.add(bean,request);
        return bean;
    }


    @DeleteMapping("/publishs/{id}")
    public String delete(@PathVariable("id") int id, HttpServletRequest request) throws Exception {
        publishService.delete(id);
        return null;
    }

    @PutMapping("/publishs")
    public Object update(@RequestBody Publish bean) throws Exception {
        publishService.update(bean);
        return bean;
    }

}
