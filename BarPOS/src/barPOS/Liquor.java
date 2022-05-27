package barPOS;

public class Liquor extends Product{
	int categoryId;	
	public Liquor(String name, Double price, int quantity, int categoryID) {
		this.productName = name;
		this.productPrice = price;
		this.productQty = quantity;
		this.categoryId = categoryID;
	}
	
	int getCategoryId() {
		return categoryId;
	}
}
