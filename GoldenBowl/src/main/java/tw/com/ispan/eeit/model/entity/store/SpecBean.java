package tw.com.ispan.eeit.model.entity.store;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec")
public class SpecBean implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    // LAZY 查詢 SpecBean 時，JPA 不會立刻去查詢關聯的SpecGroupBean。
    // 只有當第一次在程式碼中實際呼叫specBean.getSpecGroup() 時，才會發送第二條SQL 去查詢SpecGroup。
    // 可避免不必要的資料庫查詢。
    @JoinColumn(name = "spec_group_id")
    @JsonBackReference
    private SpecGroupBean specGroup;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Integer price;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "is_active", columnDefinition = "bit default 1") // 可以用 columnDefinition 確保預設值
    private Boolean isActive;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
			name="order_detail_spec",
			joinColumns=@JoinColumn(name="order_detail_id"),
			inverseJoinColumns =@JoinColumn(name="spec_id"))
    @JsonBackReference
    private List<OrderDetailBean> orderDetails; 
    // 指向 OrderDetailBean 中的 "specs" 屬性
    // 修正為 OrderDetailBean 的列表 ，表示OrderDetail指向specs
}