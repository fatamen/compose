package tw.com.ispan.repository;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tw.com.ispan.domain.ProductBean;
import tw.com.ispan.util.DatetimeConverter;

@Repository
public class ProductDAOImpl implements ProductDAO {
	@PersistenceContext
	private EntityManager entityManager;
	// 获取Session对象
	public EntityManager getSession() {
		return entityManager;
	}
	
	@Override
	public List<ProductBean> find(JSONObject obj) {
		Integer id = obj.isNull("id") ? null : obj.getInt("id");
		String name = obj.isNull("name") ? null : obj.getString("name");
		Double startPrice = obj.isNull("startPrice") ? null : obj.getDouble("startPrice");
		Double endPrice = obj.isNull("endPrice") ? null : obj.getDouble("endPrice");
		String startMake = obj.isNull("startMake") ? null : obj.getString("startMake");
		String endMake = obj.isNull("endMake") ? null : obj.getString("endMake");
		Integer startExpire = obj.isNull("startExpire") ? null : obj.getInt("startExpire");
		Integer endExpire = obj.isNull("endExpire") ? null : obj.getInt("endExpire");

		int start = obj.isNull("start") ? 0 : obj.getInt("start");
		int rows = obj.isNull("rows") ? 5 : obj.getInt("rows");
		String order = obj.isNull("order") ? "id" : obj.getString("order");
		boolean dir = obj.isNull("dir") ? false : obj.getBoolean("dir");
		
//		select * from product
//		where id = ? and name like ? and price > ? and price < ? and make > ? and make < ? and expire > ? and expire < ?
//		order by ? ?sc
		
		CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<ProductBean> criteriaQuery = criteriaBuilder.createQuery(ProductBean.class);
		
//		from product
		Root<ProductBean> table = criteriaQuery.from(ProductBean.class);
		
//		where start
//		where id = ? and name like ? and price > ? and price < ? and make > ? and make < ? and expire > ? and expire < ?
		List<Predicate> predicates = new ArrayList<>();
		if(id!=null) {
//			id = ?
			predicates.add(criteriaBuilder.equal(table.get("id"), id));
		}
		if(name!=null && name.length()!=0) {
//			name like ?
			predicates.add(criteriaBuilder.like(table.get("name"), "%"+name+"%"));
		}
		if(startPrice!=null) {
//			price > ?
			predicates.add(criteriaBuilder.greaterThan(table.get("price"), startPrice));
		}
		if(endPrice!=null) {
//			price < ?
			predicates.add(criteriaBuilder.lessThan(table.get("price"), endPrice));
		}
		if(startMake!=null && startMake.length()!=0) {
//			make > ?
			java.util.Date make = DatetimeConverter.parse(startMake, "yyyy-MM-dd");
			predicates.add(criteriaBuilder.greaterThan(table.get("make"), make));
		}
		if(endMake!=null && endMake.length()!=0) {
//			make < ?
			java.util.Date make = DatetimeConverter.parse(endMake, "yyyy-MM-dd");
			predicates.add(criteriaBuilder.lessThan(table.get("make"), make));			
		}
		if(startExpire!=null) {
//			expire > ?
			predicates.add(criteriaBuilder.greaterThan(table.get("expire"), startExpire));
		}
		if(endExpire!=null) {
//			expire < ?
			predicates.add(criteriaBuilder.lessThan(table.get("expire"), endExpire));
		}
		if(predicates!=null && !predicates.isEmpty()) {
			Predicate[] array = predicates.toArray(new Predicate[0]);
			criteriaQuery = criteriaQuery.where(array);
		}
//		where end
		
//		order by ?
		if(dir) {
			criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.desc(table.get(order)));
		} else {
			criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.asc(table.get(order)));
		}
		TypedQuery<ProductBean> typedQuery = this.getSession().createQuery(criteriaQuery)
				.setFirstResult(start)
				.setMaxResults(rows);
		List<ProductBean> result = typedQuery.getResultList();
		if(result!=null && !result.isEmpty()) {
			return result;
		} else {
			return null;			
		}
	}
	
	@Override
	public long count(JSONObject obj) {
		Integer id = obj.isNull("id") ? null : obj.getInt("id");
		String name = obj.isNull("name") ? null : obj.getString("name");
		Double startPrice = obj.isNull("startPrice") ? null : obj.getDouble("startPrice");
		Double endPrice = obj.isNull("endPrice") ? null : obj.getDouble("endPrice");
		String startMake = obj.isNull("startMake") ? null : obj.getString("startMake");
		String endMake = obj.isNull("endMake") ? null : obj.getString("endMake");
		Integer startExpire = obj.isNull("startExpire") ? null : obj.getInt("startExpire");
		Integer endExpire = obj.isNull("endExpire") ? null : obj.getInt("endExpire");
		
//		select count(*) from product
//		where id = ? and name like ? and price > ? and price < ? and make > ? and make < ? and expire > ? and expire < ?

		CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

//		from product
		Root<ProductBean> table = criteriaQuery.from(ProductBean.class);
		
//		select count(*)
		criteriaQuery = criteriaQuery.select(
//				count(*)
				criteriaBuilder.count(table)
		);
		
//		where start
//		where id = ? and name like ? and price > ? and price < ? and make > ? and make < ? and expire > ? and expire < ?
		List<Predicate> predicates = new ArrayList<>();
		if(id!=null) {
//			id = ?
			predicates.add(criteriaBuilder.equal(table.get("id"), id));
		}
		if(name!=null && name.length()!=0) {
//			name like ?
			predicates.add(criteriaBuilder.like(table.get("name"), "%"+name+"%"));
		}
		if(startPrice!=null) {
//			price > ?
			predicates.add(criteriaBuilder.greaterThan(table.get("price"), startPrice));
		}
		if(endPrice!=null) {
//			price < ?
			predicates.add(criteriaBuilder.lessThan(table.get("price"), endPrice));
		}
		if(startMake!=null && startMake.length()!=0) {
//			make > ?
			java.util.Date make = DatetimeConverter.parse(startMake, "yyyy-MM-dd");
			predicates.add(criteriaBuilder.greaterThan(table.get("make"), make));
		}
		if(endMake!=null && endMake.length()!=0) {
//			make < ?
			java.util.Date make = DatetimeConverter.parse(endMake, "yyyy-MM-dd");
			predicates.add(criteriaBuilder.lessThan(table.get("make"), make));			
		}
		if(startExpire!=null) {
//			expire > ?
			predicates.add(criteriaBuilder.greaterThan(table.get("expire"), startExpire));
		}
		if(endExpire!=null) {
//			expire < ?
			predicates.add(criteriaBuilder.lessThan(table.get("expire"), endExpire));
		}
		if(predicates!=null && !predicates.isEmpty()) {
			Predicate[] array = predicates.toArray(new Predicate[0]);
			criteriaQuery = criteriaQuery.where(array);
		}
//		where end
		
		TypedQuery<Long> typedQuery = this.getSession().createQuery(criteriaQuery);
		Long result = typedQuery.getSingleResult();
		if(result!=null) {
			return result.longValue();
		} else {
			return 0;
		}
	}
}
