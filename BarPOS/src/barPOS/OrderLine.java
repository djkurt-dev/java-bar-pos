package barPOS;

public class OrderLine extends Order{
	private String productName;
	private int orderQty;
	private int productID;
	private int categoryID;
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public void setProductID(int productID) {
		this.productID = productID;
	}
	
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	
	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public int getProductID() {
		return productID;
	}
	
	public int getCategoryID() {
		return categoryID;
	}
	
	public int getOrderQty() {
		return orderQty;
	}
}
