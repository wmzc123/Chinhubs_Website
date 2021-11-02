/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.web;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.util.Page4Navigator;
import com.how2java.tmall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class ProductController {
    
    @Autowired
    ProductService productService;

    @GetMapping("/Products/search/{userid}")
    public Page4Navigator<Product> search(@RequestParam("keyword") String keyword, @RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Product> page = productService.search(keyword, userid, start, size, 5);
        return page;
    }

    @GetMapping("/Products/listAll/{userid}")
    public Page4Navigator<Product> listAll(@RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Product> page = productService.listAll(userid, start, size, 5);
        return page;
    }

    @GetMapping("/Products/list/{userid}")
    public Page4Navigator<Product> list(@RequestParam("status") Integer status, @RequestParam("start") Integer start, @RequestParam("size") Integer size, @PathVariable("userid") int userid) throws Exception {
        start = start < 0 ? 0 : start;
        Page4Navigator<Product> page = productService.list(userid, status, start, size, 5);
        return page;
    }


    @PostMapping("/Products")
    public Object add(HttpSession session, Product bean, HttpServletRequest request) throws Exception {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        bean.setUserid(user.getId());
        productService.add(bean,request);
        return bean;
    }


    @DeleteMapping("/Products/{id}")
    public String delete(@PathVariable("id") int id, HttpServletRequest request) throws Exception {
        productService.delete(id);
        return null;
    }

    @PutMapping("/Products")
    public Object update(@RequestBody Product bean) throws Exception {
        productService.update(bean);
        return bean;
    }

    @GetMapping("/Product")
    public Product get(@RequestParam("pid") int pid) throws Exception {
        Product bean=productService.get(pid);
        return bean;
    }

}
