/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */

package com.how2java.tmall.web;

import com.how2java.tmall.comparator.*;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import com.how2java.tmall.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderService orderService;

//    old首页数据接口
    @GetMapping("/forehome")
    public Object home() {
        List<Category> cs = categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
        categoryService.removeCategoryFromProduct(cs);
        return cs;
    }

    @PostMapping("/foreregister")
    public Object register(@RequestBody User user) {
        String name = user.getName();
        String password = user.getPassword();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);

        if (exist) {
            String message = "用户名已经被使用,不能使用";
            return Result.fail(message);
        }

        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        int times = 2;
        String algorithmName = "md5";

        String encodedPassword = new SimpleHash(algorithmName, password, salt, times).toString();

        user.setSalt(salt);
        user.setPassword(encodedPassword);

        userService.add(user);

        return Result.success();
    }

        @PostMapping("/forelogin")
    public Object login(@RequestBody User userParam, HttpSession session) {
        String name = userParam.getName();
        name = HtmlUtils.htmlEscape(name);

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, userParam.getPassword());
        try {
            subject.login(token);
            User user = userService.getByName(name);
//	    	subject.getSession().setAttribute("user", user);
            session.setAttribute("user", user);
            return Result.success();
        } catch (AuthenticationException e) {
            String message = "账号密码错误";
            return Result.fail(message);
        }

    }

    // Chinhubs登陆


//    @RequestMapping(value = "/pcLoginByWeiXin", method = RequestMethod.POST)
//    @ResponseStatus(HttpStatus.OK)
//    public SourceDataBean<UserLoginBean> pcLoginByWeiXin(@RequestBody WeiXinLoginBean bean) {
//        SourceDataBean<UserLoginBean> sdb = new SourceDataBean<UserLoginBean>();
//        UserLoginBean userLoginBean = new UserLoginBean();
//        String loginName = bean.getLoginName();
//        String openId = bean.getOpenId();
//        String code = bean.getCode(); // 微信code
//        /** 错误信息 **/
//        if (StringUtils.isBlank(userId) && StringUtils.isBlank(code) && loginName == null) {
//            sdb.setMessage("501", "微信code为空");
//            return sdb;
//        }
//        if (openId == null && code != null) {
//            // 第一次进入界面，code不空，openid为空，根据code获取openid，然后查询是否存在用户信息。
//            Map<String, String> accessTokenMap = getPcWXAccessToken(code); // 获取getWXAccessToken（微信网站PC扫码登录）
//            /** 请求微信服务器错误 **/
//            if (accessTokenMap.get("errcode") != null) {
//                sdb.setMessage(accessTokenMap.get("errcode"), accessTokenMap.get("errmsg"));
//                return sdb;
//            }
//            openId = accessTokenMap.get("openid");
//            accessToken = accessTokenMap.get("access_token");
//            unionid = accessTokenMap.get("unionid");
//
//            // 查询出微信信息
//            Map<String, String> wxUserMap = this.getPcWeiXinUserInfo(openId, accessToken); // 获得微信用户信息
//
//            /** 获取微信信息异常 **/
//            if (wxUserMap.get("errcode") != null) {
//                logger.error("LoginController ==> loginByWeiXin.getWeiXinUserInfo(){errcode:" + wxUserMap.get("errcode")
//                        + ",errmsg:" + wxUserMap.get("errmsg") + "}");
//                sdb.setMessage(wxUserMap.get("errcode"), wxUserMap.get("errmsg"));
//                return sdb;
//
//
//
//            }
//
//
//        }
//        return sdb;
//    }

    /**
     * 获取getPcWXAccessToken
     */
    @Value("${weixin.pc.fw.accessTokenUrl}")
    private String pcAccessTokenUrl;

    @Value("${weixin.pc.fw.appID}")
    private String pcAppID;

    @Value("${weixin.pc.fw.appsecret}")
    private String pcAppsecret;

//    private Map<String, String> getPcWXAccessToken(String code) {
//        Map<String, String> resMap = new HashMap<String, String>();
//        StringBuffer target = new StringBuffer();
//        target.append(pcAccessTokenUrl).append("appid=").append(pcAppID).append("&secret=").append(pcAppsecret)
//                .append("&code=").append(code).append("&grant_type=authorization_code");
//        ClientResponseEntity responceEntity = HttpClientUtil.getMethod(target.toString(), "zh-cn");
//        String resMessageString = responceEntity.getMessage();
//        JSONObject jSONObject = JSON.parseObject(resMessageString);
//        if (jSONObject != null && jSONObject.get("errcode") != null) { // 有错误码
//            String errcode = String.valueOf(jSONObject.get("errcode"));
//            String errmsg = String.valueOf(jSONObject.get("errmsg"));
//            resMap.put("errmsg", errmsg);
//            resMap.put("errcode", errcode);
//        } else {
//            String accessToken = String.valueOf(jSONObject.get("access_token"));
//            String refreshToken = String.valueOf(jSONObject.get("refresh_token"));
//            String openid = String.valueOf(jSONObject.get("openid"));
//            String expiresIn = String.valueOf(jSONObject.get("expires_in"));
//            String unionid = String.valueOf(jSONObject.get("unionid"));
//
//            resMap.put("access_token", accessToken);
//            resMap.put("refresh_token", refreshToken);
//            resMap.put("openid", openid);
//            resMap.put("expires_in", expiresIn);
//            resMap.put("unionid", unionid);
//        }
//        return resMap;
//    }

    /**
     * 获得微信用户信息
     *
     * @param openId
     * @param accessToken
     * @return
     */
    @Value("${weixin.pc.fw.userInfoUrl}")
    private String pcUserInfoUrl;

//    private Map<String, String> getPcWeiXinUserInfo(String openId, String accessToken) {
//        Map<String, String> resMap = new HashMap<String, String>();
//        StringBuffer url = new StringBuffer(pcUserInfoUrl);
//        url.append("access_token=").append(accessToken).append("&").append("openid=").append(openId).append("&")
//                .append("lang=zh_CN");
//        ClientResponseEntity responceEntity = HttpClientUtil.getMethod(url.toString(), "zh_CN");
//        String resMessageString = null;
//        try {
//
//            resMessageString = new String(responceEntity.getMessage().getBytes("ISO-8859-1"), "UTF-8");
//            // resMessageString =new
//            // String(responceEntity.getMessage().getBytes(),"utf-8");
//        } catch (UnsupportedEncodingException e) {
//            System.out.println("WeiXinLoginController ==> getPcWeiXinUserInfo().UnsupportedEncodingException{} 获取用户信息编码错误");
//        }
//        JSONObject jSONObject = JSON.parseObject(resMessageString);
//        if (jSONObject != null && jSONObject.get("errcode") != null) {
//            String errcode = String.valueOf(jSONObject.get("errcode"));
//            String errmsg = String.valueOf(jSONObject.get("errmsg"));
//            resMap.put("errmsg", errmsg);
//            resMap.put("errcode", errcode);
//        } else {
//            String nickname = String.valueOf(jSONObject.get("nickname"));
//            String openid = String.valueOf(jSONObject.get("openid"));
//            String sex = String.valueOf(jSONObject.get("sex"));
//            String province = String.valueOf(jSONObject.get("province"));
//            String city = String.valueOf(jSONObject.get("city"));
//            String country = String.valueOf(jSONObject.get("country"));
//            String headimgurl = String.valueOf(jSONObject.get("headimgurl"));
//            String unionid = String.valueOf(jSONObject.get("unionid"));
//
//            resMap.put("nickname", nickname);
//            resMap.put("openid", openid);
//            resMap.put("sex", sex);
//            resMap.put("province", province);
//            resMap.put("city", city);
//            resMap.put("country", country);
//            resMap.put("headimgurl", headimgurl);
//            resMap.put("unionid", unionid);
//        }
//        return resMap;
//    }


    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid") int pid) {
        Product product = productService.get(pid);

        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);

        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);


        Map<String, Object> map = new HashMap<>();
        map.put("product", product);
        map.put("pvs", pvs);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    @GetMapping("forecheckLogin")
    public Object checkLogin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated())
            return Result.success();
        else
            return Result.fail("未登录");
    }

    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable int cid, String sort) {
        Category c = categoryService.get(cid);
        productService.fill(c);
//        productService.setSaleAndReviewNumber(c.getProducts());
        categoryService.removeCategoryFromProduct(c);

        if (null != sort) {
            switch (sort) {
                case "review":
                    Collections.sort(c.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(c.getProducts(), new ProductDateComparator());
                    break;

                case "saleCount":
                    Collections.sort(c.getProducts(), new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(), new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(), new ProductAllComparator());
                    break;
            }
        }

        return c;
    }

    @PostMapping("foresearch")
    public Object search(String keyword) {
        if (null == keyword)
            keyword = "";
        List<Product> ps = productService.search(keyword);
        return ps;
    }

    @GetMapping("foreSearchByUser")
    public Object search(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Product> ps = productService.search(user);
        return Result.success(ps);
    }

    @GetMapping("forebuyone")
    public Object buyone(int pid, int num, HttpSession session) {
        return buyoneAndAddCart(pid, num, session);
    }


    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.get(pid);
        int oiid = 0;

        User user = (User) session.getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if (oi.getProduct().getId() == product.getId()) {
                oi.setNumber(oi.getNumber() + num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }

        if (!found) {
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setProduct(product);
            oi.setNumber(num);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        return oiid;
    }

    @GetMapping("forebuy")
    public Object buy(String[] oiid, HttpSession session) {
        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0;

        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            OrderItem oi = orderItemService.get(id);
            total += oi.getProduct().getPromotePrice() * oi.getNumber();
            orderItems.add(oi);
        }


        productImageService.setFirstProdutImagesOnOrderItems(orderItems);

        session.setAttribute("ois", orderItems);

        Map<String, Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);
        return Result.success(map);
    }

    @GetMapping("foreaddCart")
    public Object addCart(int pid, int num, HttpSession session) {
        buyoneAndAddCart(pid, num, session);
        return Result.success();
    }

    @GetMapping("forecart")
    public Object cart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user);
        productImageService.setFirstProdutImagesOnOrderItems(ois);
        return ois;
    }

    @GetMapping("forechangeOrderItem")
    public Object changeOrderItem(HttpSession session, int pid, int num) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");

        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if (oi.getProduct().getId() == pid) {
                oi.setNumber(num);
                orderItemService.update(oi);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("foredeleteOrderItem")
    public Object deleteOrderItem(HttpSession session, int oiid) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        orderItemService.delete(oiid);
        return Result.success();
    }

    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois = (List<OrderItem>) session.getAttribute("ois");

        float total = orderService.add(order, ois);

        Map<String, Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", total);

        return Result.success(map);
    }

    @GetMapping("forepayed")
    public Object payed(int oid) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }

    @GetMapping("forebought")
    public Object bought(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Order> os = orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(os);
        return os;
    }

    @GetMapping("forepublish")
    public Object publish(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Product> ps = productService.search(user);
        return ps;
    }

    @GetMapping("foreconfirmPay")
    public Object confirmPay(int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        orderService.cacl(o);
        orderService.removeOrderFromOrderItem(o);
        return o;
    }

    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed(int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return Result.success();
    }

    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return Result.success();
    }

    @GetMapping("forereview")
    public Object review(int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        orderService.removeOrderFromOrderItem(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p);
        productService.setSaleAndReviewNumber(p);
        Map<String, Object> map = new HashMap<>();
        map.put("p", p);
        map.put("o", o);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    @PostMapping("foredoreview")
    public Object doreview(HttpSession session, int oid, int pid, String content) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);
        orderService.update(o);

        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);

        User user = (User) session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }
}


/**
 * 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
 * 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
 * 供购买者学习，请勿私自传播，否则自行承担相关法律责任
 */
