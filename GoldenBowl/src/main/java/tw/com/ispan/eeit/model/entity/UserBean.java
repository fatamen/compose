package tw.com.ispan.eeit.model.entity;

//0709核對
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.comment.CategorySearchedBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.food.UserTagBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class UserBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = true, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String phone;

    @Column(name = "sigup_date")
    private LocalDateTime signupDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_verify")
    private Boolean isVerify;

    @Column(name = "hide_until")
    private LocalDateTime hideUntil;

    // ------------comment資料夾-----------------------------------
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CategorySearchedBean> categorySearched;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CommentBean> comments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LikedFoodBean> likedFoods;
    // ------------food 資料夾-----------------------------------
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserTagBean> userTags;
    // ------------order 資料夾-----------------------------------

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderBean> orders;

    // ------------多對多關聯表------------------------------------
    // 多對多關係：User 與 Store 通過 favorite_store 表格關聯
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "favorite_store", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "store_id"))
    @JsonManagedReference
    private Set<StoreBean> favoriteStores = new HashSet<>();

    // 多對多關係：User 與 Food 通過 favorite_food 表格關聯
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "favorite_food", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "food_id"))
    @JsonManagedReference
    private Set<FoodBean> favoriteFoods = new HashSet<>();
}