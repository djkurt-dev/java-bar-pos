package barPOS;

public class Order {
	private Long orderID;
	private String orderDate;
	private Double amountPaid;
	
	public void setOrderID(Long orderID) {
		this.orderID = orderID;
	}
	
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	
	public void setAmountPaid(Double amountPaid) {
		this.amountPaid = amountPaid;
	}
	
	public Long getOrderID() {
		return orderID;
	}
	
	public String getOrderDate() {
		return orderDate;
	}
	
	public Double getAmountPaid() {
		return amountPaid;
	}
}
