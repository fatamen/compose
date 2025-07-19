package tw.com.ispan.eeit.model.entity.store;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tw.com.ispan.eeit.model.entity.OwnerBean;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.CategorySearchedBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.food.FoodClassBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;

@Data
@Entity
@Table(name = "store")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StoreBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) // 建議對 ManyToOne 關聯設置 LAZY
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OwnerBean owner;

    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String address;

    @Convert(converter = tw.com.ispan.eeit.model.converter.PointToGeographyConverter.class)
    @Column(name = "store_coords", columnDefinition = "GEOGRAPHY")
    @JsonIgnore // 忽略這個欄位的 JSON 序列化，避免 JTS Point 的無限遞歸
    private Point storeCoords;

    @Column(name = "lng")
    private Double lng;

    private Double lat;

    @Column(name = "store_intro", columnDefinition = "varchar(max)")
    private String storeIntro;

    @Column(columnDefinition = "varchar(max)")
    private String photo;

    @Column(name = "is_open")
    private Boolean isOpen;

    private Float score;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // orphanRemoval
    private Set<FoodBean> foods;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FoodClassBean> foodClasses;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<OrderBean> orders;

    @ManyToMany
    @JoinTable(name = "store_category", joinColumns = @JoinColumn(name = "store_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @JsonManagedReference
    private Set<CategoryBean> categories; // 您已經改為 Set，這是好的

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL) // 評論通常不應該因為 Store 刪除而刪除，請根據業務邏輯調整 cascade
    @BatchSize(size = 25)
    @JsonManagedReference
    private Set<CommentBean> comments; // 您已經改為 Set，這是好的

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<CategorySearchedBean> categorySearched;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<OpenHourBean> OpenHours;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<SpecGroupBean> specGroups;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @JsonManagedReference
    private List<SpecialHoursBean> specialHours;

    // ------------多對多關聯表-----------------------------------------
    @ManyToMany(mappedBy = "favoriteStores")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UserBean> favoritedByUsers = new HashSet<>();
}