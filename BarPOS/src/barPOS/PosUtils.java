package barPOS;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

public class PosUtils {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	
	SimpleDateFormat formatter;
    Date date = new Date(); 
    OrderLine orderLine = new OrderLine();
    int tempRand;
	
	public PosUtils() {
		conn = DbConnection.connect();
	}
	
	//******************************************** LOGIN ******************************************************//
	public boolean loginValidate(String uname, String password) {
		boolean loginSuccess = false;
		try {
			String sql = "SELECT username,password FROM users where username = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,uname);
			rs = ps.executeQuery();
			
			String fetchUser = rs.getString(1);
			String fetchPass = rs.getString(2);
			
			if(uname.equals(fetchUser) && password.equals(fetchPass)) {
				loginSuccess = true;
			}
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return loginSuccess;
	}
	
	//******************************************** SIGN UP ******************************************************//
	public boolean userExists(String user) {		
		boolean existence=false;
		try {	
			String sql = "SELECT username FROM users where username = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,user);
			rs = ps.executeQuery();

			System.out.println(rs.isClosed());
			if(!(rs.isClosed()) || !(rs.wasNull())) {
				return existence = true;				
			}
		} catch (SQLException e1) {				
			System.out.println(e1.toString());						
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return existence;	
	}
	
	public boolean addUser(String uname, String pass) {
		boolean success = false;
		try {
			String sql = "INSERT into users(username,password) VALUES(?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uname);
			ps.setString(2, pass);						
			ps.execute();
			success=true;
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return success;
	}
	
	//********************************************FETCH LIQUOR DATA ******************************************************//
	public ArrayList<Integer> fetchLiquorIdByCtgId(int id){
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			String sql = "SELECT liquorID FROM liquors WHERE categoryID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				int fid = rs.getInt("liquorID");				
				ids.add(fid);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return ids;
	}
	public ArrayList<Integer> fetchLiquorQtyByCtgId(int id) {
		ArrayList<Integer> qtys = new ArrayList<Integer>();
		try {
			String sql = "SELECT qty FROM liquors WHERE categoryID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				int qty = rs.getInt("qty");				
				qtys.add(qty);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return qtys;
	}
	
	public ArrayList<Double> fetchLiquorPriceByCtgId(int id) {
		ArrayList<Double> prices = new ArrayList<Double>();
		try {
			String sql = "SELECT price FROM liquors WHERE categoryID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				Double price = rs.getDouble("price");				
				prices.add(price);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return prices;
	}
	public ArrayList<String> fetchLiquorNameByCtgId(int id) {
		ArrayList<String> names = new ArrayList<String>();
		try {
			String sql = "SELECT liquorName FROM liquors WHERE categoryID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {			
				String liquorName = rs.getString("liquorName");				
				names.add(liquorName);		
			}
			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return names;
	}
	
	//******************************************** FETCH PULUTAN DATA ******************************************************//
	public ArrayList<Integer> fetchPulutanId() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			String sql = "SELECT pulutanID FROM pulutan";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				int pid = rs.getInt("pulutanID");				
				ids.add(pid);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return ids;
	}
	public ArrayList<Integer> fetchPulutanQty() {
		ArrayList<Integer> qtys = new ArrayList<Integer>();
		try {
			String sql = "SELECT * FROM pulutan ORDER BY pulutanID";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				int qty = rs.getInt("qty");				
				qtys.add(qty);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return qtys;
	}
	public ArrayList<Double> fetchPulutanPrice() {
		ArrayList<Double> prices = new ArrayList<Double>();
		try {
			String sql = "SELECT * FROM pulutan";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();			
			while (rs.next()) {			
				Double price = rs.getDouble("price");				
				prices.add(price);		
			}			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return prices;
	}
	
	public ArrayList<String> fetchPulutanNames() {
		ArrayList<String> names = new ArrayList<String>();
		try {
			String sql = "SELECT * FROM pulutan";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {			
				String pulutanName = rs.getString("pulutanName");				
				names.add(pulutanName);		
			}
			
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return names;
	}
	
	//******************************************** Order Functions ******************************************************//
	public long generateOrderId() {
		formatter = new SimpleDateFormat("yyMMdd");
		tempRand  = (int) ((Math.random() * ((9999 - 1000) + 1)) + 1000);
		long orderID = Long.parseLong(formatter.format(date)+tempRand);
		return orderID;
	}
	
	public String getOrderDate() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String ordDate = now.format(format);
		
		return ordDate;
	}

	public void addOrderUpdateProducts(Order ord, ArrayList<String> product,ArrayList<Integer> qty,ArrayList<Integer> category, ArrayList<Integer> pid) {
		Long id = ord.getOrderID();
		String date = ord.getOrderDate();
		Double total = ord.getAmountPaid();
		
		try {
			String insertOrder = "INSERT INTO orders(orderID,orderDate,amountPaid) VALUES(?,?,?)";
			ps = conn.prepareStatement(insertOrder);
			ps.setLong(1, id);
			ps.setString(2, date);
			ps.setDouble(3, total);			
			ps.execute();
			System.out.println("Order added.");
			ps = null;
			
			for(int i=0; i < product.size(); i++) {			
				
				orderLine.setProductName(product.get(i));
				orderLine.setOrderID(id);
				orderLine.setProductID(pid.get(i));
				orderLine.setOrderQty(qty.get(i));
				orderLine.setCategoryID(category.get(i));
				
				String name = orderLine.getProductName();
				Long ord_id = orderLine.getOrderID();
				int quantity = orderLine.getOrderQty();
				int pr_id = orderLine.getProductID();
				int pr_ctg = orderLine.getCategoryID();
				
				String insertOrderLine = "INSERT INTO orderline(orderID,productID,orderQty,categoryID) VALUES(?,?,?,?)";
				ps = conn.prepareStatement(insertOrderLine);
				ps.setLong(1, ord_id);
				ps.setInt(2, pr_id);
				ps.setInt(3, quantity);
				ps.setInt(4, pr_ctg);
				ps.execute();		
				System.out.println("Orderline added.");
				ps = null;
				
				if(category.get(i) == 0) {
					String updateProduct = "UPDATE liquors set qty = qty - ? WHERE liquorName = ?";
					ps = conn.prepareStatement(updateProduct);
					ps.setInt(1, quantity);
					ps.setString(2, name);
					ps.execute();
					System.out.println("Liquor updated.");
					ps = null;
				}
				else{
					String updateProduct = "UPDATE pulutan set qty = qty - (?*250) WHERE pulutanName = ?";
					ps = conn.prepareStatement(updateProduct);
					ps.setInt(1, quantity);
					ps.setString(2, name);
					ps.execute();
					System.out.println("Pulutan updated.");
					ps = null;
				}
				
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void cancelOrder(Long orderID,ArrayList<String> product,ArrayList<Integer> qty,ArrayList<Integer> category) {
		try {
			deleteInvoice(orderID);
			
			String deleteOrder = "DELETE FROM orders WHERE orderID=?";
			ps = conn.prepareStatement(deleteOrder);
			ps.setLong(1, orderID);		
			ps.execute();
			System.out.println("Order deleted.");
			ps = null;
			
			String deleteOrderLine = "DELETE FROM orderline WHERE orderID=?";
			ps = conn.prepareStatement(deleteOrderLine);
			ps.setLong(1, orderID);		
			ps.execute();
			ps = null;
			
			for(int i=0; i < product.size(); i++) {								
				if(category.get(i) == 0) {
					String updateProduct = "UPDATE liquors set qty = qty + ? WHERE liquorName = ?";
					ps = conn.prepareStatement(updateProduct);
					ps.setInt(1, qty.get(i));
					ps.setString(2, product.get(i));
					ps.execute();
					System.out.println("Liquor updated.");
					ps = null;
				}
				else{
					String updateProduct = "UPDATE pulutan set qty = qty + (?*250) WHERE pulutanName = ?";
					ps = conn.prepareStatement(updateProduct);
					ps.setInt(1, qty.get(i));
					ps.setString(2, product.get(i));
					ps.execute();
					System.out.println("Pulutan updated.");
					ps = null;
				}
				
			}
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void deleteInvoice(Long orderID) {
		String stringOrderId = Long.toString(orderID);
		String slicedID = stringOrderId.substring(6,10);
		File file = new File("D:\\eclispe-workspace\\BarPOS\\src\\invoice\\"+slicedID+".pdf");
		file.delete();
	}
	
	
	//******************************************** Inventory Functions ******************************************************//
	public void editLiquor(Product p) {
		try {
			String pname = p.getProductName();
			Double pprice = p.getPrice();
			int pqty = p.getQty();
			
			String updateQty = "UPDATE liquors set qty=? WHERE liquorName=?";
			
			ps = conn.prepareStatement(updateQty);
			ps.setInt(1, pqty);
			ps.setString(2, pname);
			ps.execute();
			ps = null;
			
			String updatePrice = "UPDATE liquors set price=? WHERE liquorName=?";
			ps = conn.prepareStatement(updatePrice);
			ps.setDouble(1, pprice);
			ps.setString(2, pname);
			ps.execute();
			JOptionPane.showMessageDialog(null, "Liquor updated successfully.");
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	public void editPulutan(Product p) {
		try {
			String pname = p.getProductName();
			Double pprice = p.getPrice();
			int pqty = p.getQty();
			
			String updateQty = "UPDATE pulutan set qty=? WHERE pulutanName=?";
			ps = conn.prepareStatement(updateQty);
			ps.setInt(1, pqty);
			ps.setString(2, pname);
			ps.execute();
			ps = null;
			
			String updatePrice = "UPDATE pulutan set price=? WHERE pulutanName=?";
			ps = conn.prepareStatement(updatePrice);
			ps.setDouble(1, pprice);
			ps.setString(2, pname);
			ps.execute();
			JOptionPane.showMessageDialog(null, "Pulutan updated successfully.");
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public boolean addLiquor(Liquor l) {
		boolean added = false;
		try {
			String nameToAdd = l.getProductName();
			Double priceToAdd = l.getPrice();
			int qtyToAdd = l.getQty();
			int ctgIdToAdd = l.getCategoryId();	
			
			String sql = "INSERT INTO liquors(categoryID,liquorName,price,qty,productCategory) VALUES(?,?,?,?,?)";
			
			ps = conn.prepareStatement(sql);			
			ps.setInt(1, ctgIdToAdd);
			ps.setString(2, nameToAdd);
			ps.setDouble(3, priceToAdd);
			ps.setInt(4, qtyToAdd);
			ps.setInt(5, 0);
			
			ps.execute();
			JOptionPane.showMessageDialog(null, "Product added successfully.", "Tagay Station POS", JOptionPane.INFORMATION_MESSAGE);
			added = true;
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
		return added;
	}
	
	public boolean addPulutan(Pulutan p) {
		boolean added = false;
		try {
			String nameToAdd = p.getProductName();
			Double priceToAdd = p.getPrice();
			int qtyToAdd = p.getQty();
			
			String sql = "INSERT INTO pulutan(pulutanName,price,qty,unit,productCategory) VALUES(?,?,?,?,?)";
			
			ps = conn.prepareStatement(sql);			
			ps.setString(1, nameToAdd);
			ps.setDouble(2, priceToAdd);
			ps.setInt(3, qtyToAdd);
			ps.setString(4, "g");
			ps.setInt(5, 1);
			
			ps.execute();
			JOptionPane.showMessageDialog(null, "Product added successfully.", "Tagay Station POS", JOptionPane.INFORMATION_MESSAGE);
			added = true;
		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (SQLException e2) {
				System.out.println(e2.toString());
			}
		}
		return added;
	}
	
	public void deleteLiquor(String name) {
		try {
			String sql = "DELETE FROM liquors WHERE liquorName=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			
			ps.execute();

			JOptionPane.showMessageDialog(null, "Liquor deleted successfully.", "Tagay Station POS", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (SQLException e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void deletePulutan(String name) {
		try {
			String sql = "DELETE FROM pulutan WHERE pulutanName=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			
			ps.execute();
			
			JOptionPane.showMessageDialog(null, "Pulutan deleted successfully.", "Tagay Station POS", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				ps.close();
			} catch (SQLException e2) {
				System.out.println(e2.toString());
			}
		}
	}
}