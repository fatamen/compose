package tw.com.ispan.eeit.model.entity.store;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.food.FoodBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec_group")
public class SpecGroupBean implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @JsonBackReference
    private StoreBean store;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "min_select")
    private Integer minSelect;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "sort")
    private Integer sort;

    // 一對多：一個規格群組 -> 多個規格選項
    @OneToMany(mappedBy = "specGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SpecBean> specs;

    // 多對多：一個規格群組 -> 被多個品項使用
    @ManyToMany(mappedBy = "specGroups",fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FoodBean> foods;
}