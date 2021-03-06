/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	

package com.how2java.tmall.service;

import com.how2java.tmall.dao.CategoryDAO;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="categories")
public class CategoryService {

	@Autowired
	CategoryDAO categoryDAO;


	@CacheEvict(allEntries=true)
//	@CachePut(key="'category-one-'+ #p0")
	public void add(Category bean) {
		categoryDAO.save(bean);
	}

	@CacheEvict(allEntries=true)
//	@CacheEvict(key="'category-one-'+ #p0")
	public void delete(int id) {
		categoryDAO.delete(id);
	}

	
//	@Cacheable(key="'categories-one-'+ #p0")
	public Category get(int id) {
		Category c= categoryDAO.findById(id);
		return c;
	}

	@CacheEvict(allEntries=true)
//	@CachePut(key="'category-one-'+ #p0")
	public void update(Category bean) {
		categoryDAO.save(bean);
	}

//	@Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
	public Page4Navigator<Category> list(int start, int size, int navigatePages) {
    	Sort sort = new Sort(Sort.Direction.ASC, "id");
		Pageable pageable = new PageRequest(start, size,sort);
		Page pageFromJPA =categoryDAO.findAll(pageable);
		
		return new Page4Navigator<>(pageFromJPA,navigatePages);
	}

//	@Cacheable(key="'categories-all'")
	public List<Category> list() {
    	Sort sort = new Sort(Sort.Direction.ASC, "id");
		return categoryDAO.findAll(sort);
	}

	//这个方法的用处是删除Product对象上的 分类。 为什么要删除呢？ 因为在对分类做序列还转换为 json 的时候，会遍历里面的 products, 然后遍历出来的产品上，又会有分类，接着就开始子子孙孙无穷溃矣地遍历了，就搞死个人了
	//而在这里去掉，就没事了。 只要在前端业务上，没有通过产品获取分类的业务，去掉也没有关系
	
	public void removeCategoryFromProduct(List<Category> cs) {
		for (Category category : cs) {
			removeCategoryFromProduct(category);
		}
	}

	public void removeCategoryFromProduct(Category category) {
		List<Product> Products =category.getProducts();
		if(null!=Products) {
			for (Product Product : Products) {
				Product.setCategory(null);
			}
		}
		
		List<List<Product>> ProductsByRow =category.getProductsByRow();
		if(null!=ProductsByRow) {
			for (List<Product> ps : ProductsByRow) {
				for (Product p: ps) {
					p.setCategory(null);
				}
			}
		}
	}
}

