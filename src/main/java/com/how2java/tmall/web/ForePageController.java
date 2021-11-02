/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	

package com.how2java.tmall.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class ForePageController {
	@GetMapping(value="/")
	public String index(){
		return "redirect:home";
	}
	@GetMapping(value="/home")
	public String home(){
		return "fore/home";
	}
	@GetMapping(value="/register")
	public String register(){
		return "fore/register";
	}
	@GetMapping(value="/alipay")
	public String alipay(){
		return "fore/alipay";
	}
	@GetMapping(value="/bought")
	public String bought(){
		return "fore/bought";
	}
	@GetMapping(value="/buy")
	public String buy(){
		return "fore/buy";
	}
	@GetMapping(value="/cart")
	public String cart(){
		return "fore/cart";
	}
	@GetMapping(value="/category")
	public String category(){
		return "fore/category";
	}
	@GetMapping(value="/confirmPay")
	public String confirmPay(){
		return "fore/confirmPay";
	}
	@GetMapping(value="/login")
	public String login(){
		return "fore/login";
	}
	@GetMapping(value="/orderConfirmed")
	public String orderConfirmed(){
		return "fore/orderConfirmed";
	}
	@GetMapping(value="/payed")
	public String payed(){
		return "fore/payed";
	}
	@GetMapping(value="/product")
	public String product(){
		return "fore/product";
	}
	@GetMapping(value="/registerSuccess")
	public String registerSuccess(){
		return "fore/registerSuccess";
	}
	@GetMapping(value="/review")
	public String review(){
		return "fore/review";
	}
	@GetMapping(value="/search")
	public String searchResult(){
		return "fore/search";
	}
	@GetMapping(value="/publishs")
	public String publishs(){
		return "fore/publishs";
	}
	@GetMapping(value="/sell")
	public String sell(){
		return "fore/sell";
	}
	@GetMapping("/forelogout")
	public String logout( ) {
		Subject subject = SecurityUtils.getSubject();
		if(subject.isAuthenticated())
			subject.logout();
		return "redirect:home";
	}

}

