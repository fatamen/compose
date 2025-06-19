package tw.com.ispan.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detail")
public class DetailBean {
	// @JsonIgnoreProperties("detail")
	@JsonManagedReference("product")
	@OneToOne
	@JoinColumn(
		name = "photoid",
		referencedColumnName = "id"
	)
	private ProductBean product;
	public ProductBean getProduct() {
		return product;
	}
	public void setProduct(ProductBean product) {
		this.product = product;
	}
	
	@Id
	@Column(name = "photoid")
	private Integer photoid;
	
	@Column(name = "photo")
	private byte[] photo;
	
	@Override
	public String toString() {
		return "DetailBean [photoid=" + photoid + "]";
	}
	public Integer getPhotoid() {
		return photoid;
	}
	public void setPhotoid(Integer photoid) {
		this.photoid = photoid;
	}
	public byte[] getPhoto() {
		return photo;
	}
	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}
}
