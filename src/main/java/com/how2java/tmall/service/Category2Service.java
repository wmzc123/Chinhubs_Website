/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	

package com.how2java.tmall.service;

import com.how2java.tmall.dao.Category2DAO;
import com.how2java.tmall.pojo.Category2;
import com.how2java.tmall.pojo.Publish;
import com.how2java.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="categorie2s")
public class Category2Service {

	@Autowired
	Category2DAO category2DAO;


	@CacheEvict(allEntries=true)
//	@CachePut(key="'category-one-'+ #p0")
	public void add(Category2 bean) {
		category2DAO.save(bean);
	}

	@CacheEvict(allEntries=true)
//	@CacheEvict(key="'category-one-'+ #p0")
	public void delete(int id) {
		category2DAO.delete(id);
	}

	
	@Cacheable(key="'categories-one-'+ #p0")
	public Category2 get(int id) {
		Category2 c= category2DAO.findOne(id);
		return c;
	}

	@CacheEvict(allEntries=true)
//	@CachePut(key="'category-one-'+ #p0")
	public void update(Category2 bean) {
		category2DAO.save(bean);
	}

	@Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
	public Page4Navigator<Category2> list(int start, int size, int navigatePages) {
    	Sort sort = new Sort(Sort.Direction.DESC, "id");
		Pageable pageable = new PageRequest(start, size,sort);
		Page pageFromJPA =category2DAO.findAll(pageable);
		
		return new Page4Navigator<>(pageFromJPA,navigatePages);
	}

//	@Cacheable(key="'categories-all'")
	public List<Category2> list() {
    	Sort sort = new Sort(Sort.Direction.DESC, "id");
		return category2DAO.findAll(sort);
	}

	//这个方法的用处是删除Product对象上的 分类。 为什么要删除呢？ 因为在对分类做序列还转换为 json 的时候，会遍历里面的 products, 然后遍历出来的产品上，又会有分类，接着就开始子子孙孙无穷溃矣地遍历了，就搞死个人了
	//而在这里去掉，就没事了。 只要在前端业务上，没有通过产品获取分类的业务，去掉也没有关系
	
	public void removeCategory2FromPublish(List<Category2> cs) {
		for (Category2 category : cs) {
			removeCategory2FromPublish(category);
		}
	}

	public void removeCategory2FromPublish(Category2 category) {
		List<Publish> publishes =category.getPublishes();
		if(null!=publishes) {
			for (Publish publish : publishes) {
				publish.setCategory2(null);
			}
		}
		
		List<List<Publish>> publishsByRow =category.getPublishesByRow();
		if(null!=publishsByRow) {
			for (List<Publish> ps : publishsByRow) {
				for (Publish p: ps) {
					p.setCategory2(null);
				}
			}
		}
	}
}

/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	
