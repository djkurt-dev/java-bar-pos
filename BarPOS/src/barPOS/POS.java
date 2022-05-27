package barPOS;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

public class POS extends JFrame {
	private JPanel panelProducts;
	private JPanel contentPane;
	private JPanel menuPanel;
	private JTextField login_userField;
	private JPasswordField login_passwordField;
	private JTextField signup_userField;
	private JPasswordField signup_passwordField;
	private JPanel prodBtnGrp;
	private JLabel lblOrderID;
	private JLabel lblTotal;
	private JLabel lblChange;
	private JLabel mainNav;
	private JTextArea receipt;
	private JButton btnHome_;
	private JLabel lblCurrentUser_;
	private JTable orderTable;
	private JTable liquorTable;
	private JTable pulutanTable;
	private JTextField cashTextField;
	private JTextField editField_name;
	private JTextField editField_price;
	private JTextField editField_qty;
	private JPanel currentPanel;
	private JTable selectedTable;
	private JButton btnPay;
	private JPanel Inventory;
	private JDateChooser dateFrom;
	private JDateChooser dateTo;
	private JLabel totalLiquors;
	private JLabel totalPulutan;
	private JLabel allTotal;
	private JButton btnExportPdf;
	private JTextArea salesPdf;
	private JPanel salesPanel;
	private JTable salesLiquor;
	private JTable salesPulutan;
	
	AddProduct addProduct = new AddProduct();
	User user = new User();
	Order order = new Order();
	
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;	
	
	Object[] row;
	String currentUser;
	int logged = 0;
	Long currentOrderId;

	Double totalAmt,ccash,change,liquorSalesTotal,pulutanSalesTotal;
	
	PosUtils pos = new PosUtils();
	DecimalFormat d = new DecimalFormat("0.00");
	DefaultTableModel model = new DefaultTableModel();
	DefaultTableModel liquorTableModel = new DefaultTableModel();
	DefaultTableModel pulutanTableModel = new DefaultTableModel();
	DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	TableColumnModel tcm;
	DefaultTableModel dtmPulutan;
	DefaultTableModel dtmLiquor;
	
	ArrayList<String> ord_productName = new ArrayList<String>();
	ArrayList<Double> ord_price = new ArrayList<Double>();
	ArrayList<Integer> ord_qty = new ArrayList<Integer>();
	ArrayList<Integer> ord_ctg = new ArrayList<Integer>();
	ArrayList<Integer> ord_id = new ArrayList<Integer>();
	
	ArrayList<String> fetchedNames = new ArrayList<String>();
	ArrayList<Double> fetchedPrices = new ArrayList<Double>();
	ArrayList<Integer> fetchedQty = new ArrayList<Integer>();
	ArrayList<Integer> fetchedId = new ArrayList<Integer>();
	
	public POS() {
		initComponents();
		conn = DbConnection.connect();	
		setIconImage(Toolkit.getDefaultToolkit().getImage(POS.class.getResource("/img/icon-large.png")));
		SetTableHeaderStyle(orderTable);
		SetTableHeaderStyle(liquorTable);
		SetTableHeaderStyle(pulutanTable);
		SetTableHeaderStyle(salesLiquor);
		SetTableHeaderStyle(salesPulutan);
		setPaddingForTable(liquorTable);
		setPaddingForTable(pulutanTable);		
		displayAllLiquor();
		displayAllPulutan();
		displayAllLiquorSales();
		displayAllPulutanSales();
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					POS frame = new POS();
					frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}	
	
	private void SetTableHeaderStyle(JTable table){
		table.getTableHeader().setOpaque(false);
		table.getTableHeader().setBackground(new Color(5, 59, 86));
		table.getTableHeader().setForeground(new Color(255, 255, 255));
		table.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 19));   
    }
	
	class labelRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component) value;
        }
    }
	
	class colorRenderer extends DefaultTableCellRenderer{
	   Color bg, fg;
	   public colorRenderer(Color bg, Color fg) {
	      super();
	      this.bg = bg;
	      this.fg = fg;
	   }
	   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
	   {
	      Component cell = super.getTableCellRendererComponent(table, value, 
	      isSelected, hasFocus, row, column);
	      cell.setBackground(bg);
	      cell.setForeground(fg);
	      ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
	      cell.setFont(new Font("Segoe UI", Font.BOLD, 18));
	      return cell;
	   }
	}
	
	public void payOrder() {
		try {
			ccash = Double.parseDouble(cashTextField.getText());
			change = computeChange(totalAmt, ccash);
			if(change < 0) {
				lblChange.setFont(new Font("Segoe UI", Font.PLAIN, 20));
				lblChange.setForeground(new Color(225, 6, 0));
				lblChange.setText("Insufficient cash");
			} else {
				lblChange.setFont(new Font("Segoe UI", Font.BOLD, 35));
				lblChange.setForeground(new Color(0, 100, 0));
				lblChange.setText(d.format(change).toString());
				
				order.setOrderID(currentOrderId);
				order.setAmountPaid(totalAmt);
				order.setOrderDate(pos.getOrderDate());
				
				pos.addOrderUpdateProducts(order,ord_productName, ord_qty, ord_ctg, ord_id);
				printReceipt(order, ord_productName, ord_qty, ord_price, ccash, change, currentUser);
				refreshTable(liquorTable);
				refreshTable(pulutanTable);
				displayAllLiquorSales();
				displayAllPulutanSales();
			}						
			
		} catch (NumberFormatException e2) {
			System.out.println(e2.toString());
			lblChange.setFont(new Font("Segoe UI", Font.PLAIN, 20));
			lblChange.setForeground(new Color(225, 6, 0));
			lblChange.setText("Insufficient cash");
		}
	}
	
	DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer() {
	    Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
	    @Override
	    public Component getTableCellRendererComponent(JTable table,
	            Object value, boolean isSelected, boolean hasFocus,
	            int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
	                row, column);
	        setBorder(BorderFactory.createCompoundBorder(getBorder(), padding));
	        return this;
	    }
	};
	

	
	public void setPaddingForTable(JTable table) {
		table.getColumnModel().getColumn(0).setCellRenderer(paddingRenderer);
		table.getColumnModel().getColumn(1).setCellRenderer(paddingRenderer);
	}
	
	public void gotoMenu(JPanel current) {
		current.setVisible(false);
		menuPanel.setVisible(true);	
		mainNav.setVisible(false);
		btnHome_.setVisible(false);
		editField_name.setText("");
		editField_price.setText("");
		editField_qty.setText("");
		lblCurrentUser_.setVisible(false);
	}
	
	// add row item to table
	public void addItemToTable(String item, Double price, int qty, int category, int id) {		
		JLabel del = new JLabel();
		JLabel plus = new JLabel();
		JLabel minus = new JLabel();
				
		del.setText("DEL");	
		del.setForeground(Color.RED);
		del.setFont(new Font("Segoe UI", Font.BOLD, 18));
		
		plus.setText("+");
		plus.setForeground(Color.GREEN);
		plus.setFont(new Font("Segoe UI", Font.BOLD, 18));
		
		minus.setText("-");
		minus.setForeground(Color.RED);
		minus.setFont(new Font("Segoe UI", Font.BOLD, 18));
		
		row = new Object[]{"DEL", item, "-", qty, "+",price,category,id};
        model = (DefaultTableModel) orderTable.getModel();
        
        boolean noExist = true;
        if(model.getRowCount() > 0) {
        	for(int i=0; i< model.getRowCount(); i++) {
        		String prod = orderTable.getValueAt(i, 1).toString();
        		if(prod.equals(item)) {
        			int q = Integer.parseInt(model.getValueAt(i, 3).toString());
        			q += 1;
        			model.setValueAt(q, i, 3);
        			noExist=false;
        			break;
        		}
        	}
        	if(noExist) {
        		model.addRow(row);
        	}
        }
        else {
        	model.addRow(row);
		}
        computeTotalAmt();
	}
	
	public void addQty() {
		int row = orderTable.getSelectedRow();
		int add = Integer.parseInt(model.getValueAt(row, 3).toString()) + 1;
		model.setValueAt(add, row, 3);
		computeTotalAmt();
	}
	
	public void minusQty() {
		int row = orderTable.getSelectedRow();
		int subt = Integer.parseInt(model.getValueAt(row, 3).toString()) - 1;
		model.setValueAt(subt, row, 3);
		if(subt == 0) {
			model.removeRow(row);
		}
		computeTotalAmt();
	}
	
	public void removeItem() {
		int row = orderTable.getSelectedRow();
		model.removeRow(row); 
		computeTotalAmt();
	}
	
	public void clearOrder() {		
		model = (DefaultTableModel) orderTable.getModel();
		int rowCount = model.getRowCount();
		if(rowCount>0) {
			for(int i=rowCount-1; i >= 0; i--) {				
				model.removeRow(i);
			}
		}		
		computeTotalAmt();		
	}
	
	public Long newOrder() {
		Long newOrderId = pos.generateOrderId();
		clearOrder();		
		cashTextField.setText(null);
		lblChange.setText(null);
		computeTotalAmt();
		return newOrderId;
	}
	
	public void computeTotalAmt() {
		ord_productName.clear();
		ord_price.clear();
		ord_qty.clear();
		ord_ctg.clear();
		ord_id.clear();
		
		totalAmt=0.00;
		String name;
		int qt,catg,id;
		Double price=0.00;
		
		if(model.getRowCount() > 0) {
			for(int i=0; i < model.getRowCount(); i++) {
				name = model.getValueAt(i, 1).toString();
				qt = Integer.parseInt(model.getValueAt(i, 3).toString());
				price = Double.parseDouble(model.getValueAt(i, 5).toString());
				catg = Integer.parseInt(model.getValueAt(i, 6).toString());
				id = Integer.parseInt(model.getValueAt(i, 7).toString());
				totalAmt += Double.valueOf(qt) * price;
				
				ord_productName.add(name);
				ord_price.add(price);
				ord_qty.add(qt);
				ord_ctg.add(catg);
				ord_id.add(id);
			}
		}
		lblTotal.setText(d.format(totalAmt).toString());
	}
	
	public double computeChange(Double total, Double cash) {		
		Double change = cash  - total;
		return change;
	}
	
	public void printReceipt(Order order, ArrayList<String> product, ArrayList<Integer> quantity, ArrayList<Double> price,Double cash, Double change,String cashierName) {
		receipt.setText(receipt.getText()+"\n***********************************************\r\n");
		receipt.setText(receipt.getText()+"\t\t TAGAY Station                   \r\n");		
		receipt.setText(receipt.getText()+"\t      Catarman, Camiguin             \r\n");
		receipt.setText(receipt.getText()+"\t         0966-621-0511               \r\n");
		receipt.setText(receipt.getText()+"***********************************************\r\n\n");
		receipt.setText(receipt.getText()+"\n\nOrder No. "+order.getOrderID()+" \r\n");//insert order number
		receipt.setText(receipt.getText()+"Cashier: "+cashierName+" \r\n"); //insert cashier name
		receipt.setText(receipt.getText()+"Date Time: "+ order.getOrderDate() +" \r\n");
		receipt.setText(receipt.getText()+"\nITEM(S)\t\t\tQTY\tPRICE\tAMOUNT\r\n");
		receipt.setText(receipt.getText()+"-----------------------------------------------\r\n");
		for(int i=0; i<product.size(); i++) {
			String pname = product.get(i);
			int qty = quantity.get(i);
			Double pr = price.get(i);
			Double tot = Double.valueOf(qty)*pr;
			receipt.setText(receipt.getText()+pname+"\t"+qty+"\t"+pr+"\t"+d.format(tot)+"\r\n");
		}		
		receipt.setText(receipt.getText()+"-----------------------------------------------\r\n");
		receipt.setText(receipt.getText()+"\n\n\t\tSUBTOTAL \t"+ d.format(order.getAmountPaid())+" \r\n");
		receipt.setText(receipt.getText()+"\t\tTOTAL \t\t"+ d.format(order.getAmountPaid())+" \r\n"); //insert total
		receipt.setText(receipt.getText()+"\t\tCASH \t\t"+d.format(cash)+" \r\n");
		receipt.setText(receipt.getText()+"\t\tCHANGE\t\t"+d.format(change)+"\r\n");
		receipt.setText(receipt.getText()+"\n\n***********************************************\r\n");
		receipt.setText(receipt.getText()+"    THANK YOU FOR DRINKING AT TAGAY STATION       \r\n");
		receipt.setText(receipt.getText()+"                  COME AGAIN!!!       \r\n");
		receipt.setText(receipt.getText()+"***********************************************\r\n");
		receipt.setText(receipt.getText()+"      POS Developed by: Kursoft Solutions\r\n");
		try {
			receipt.print();
			
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public void generateLiquorBtn(int id) {
		panelProducts.removeAll();
		panelProducts.revalidate();
		panelProducts.repaint();
		
		fetchedNames = pos.fetchLiquorNameByCtgId(id);
		fetchedPrices = pos.fetchLiquorPriceByCtgId(id);
		fetchedQty = pos.fetchLiquorQtyByCtgId(id);
		fetchedId = pos.fetchLiquorIdByCtgId(id);
		
		int nameSize = fetchedNames.size();
		int grid_row=1,grid_col = 5, width = 995, height=565;
		
		if(nameSize==3) {
			width = 564;
		} 
		if(nameSize == 4) {
			width = 727;
		}		
		if(nameSize==5) {
			width=890;
		}		
		if(nameSize <=5) {
			height = 240;
		}		
		if(nameSize > 5 && nameSize <10) {
			grid_row = 2;
			grid_col=5;
			width=920;
			height=480;
		}
		if(nameSize > 10) {
			grid_row = 3;
			height=720;
		}
		if(nameSize > 15) {
			grid_row = 4;
			height=960;
		}
		
		panelProducts.setBounds(65, 70, width, height);
		panelProducts.setLayout(new GridLayout(grid_row, grid_col, 0, 0));
		
		int i;
		int x=10,y=11;
		String[] btnBg = new String[] {"vodka","whiskey","brandy","beer","rum","gin","tequila"};
		for(i=0; i<fetchedNames.size() && i<fetchedPrices.size() && i<fetchedQty.size();i++) {			
			String fname = fetchedNames.get(i);
			Double fprice = fetchedPrices.get(i);
			int fqty = fetchedQty.get(i);
			int fid = fetchedId.get(i);
			
			prodBtnGrp = new JPanel();			
			prodBtnGrp.setBackground(Color.WHITE);
			prodBtnGrp.setBounds(x, y, 163, 221);
			prodBtnGrp.setVisible(true);
			prodBtnGrp.setLayout(null);
			
			JButton btnLiquor = new JButton("");
			btnLiquor.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String product = fname;
					double price = fprice;
					int id = fid;
					int qty = 1;	
					int category = 0;
					if(fqty != 0) {
						addItemToTable(product, price, qty, category,id);
					}
				}
				public void mousePressed(MouseEvent e) {
					btnLiquor.setIcon(new ImageIcon(POS.class.getResource("/img/btn-"+(btnBg[id-1])+"-clicked.png")));
				}
				public void mouseReleased(MouseEvent e) {
					btnLiquor.setIcon(new ImageIcon(POS.class.getResource("/img/btn-"+(btnBg[id-1])+".png")));
				}
			});			
			prodBtnGrp.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String product = fname;
					double price = fprice;
					int qty = 1;	
					int category = 0;
					if(fqty != 0) {
						addItemToTable(product, price, qty, category,id);
					}
				}
				public void mousePressed(MouseEvent e) {
					btnLiquor.setIcon(new ImageIcon(POS.class.getResource("/img/btn-"+(btnBg[id-1])+"-clicked.png")));
				}
				public void mouseReleased(MouseEvent e) {
					btnLiquor.setIcon(new ImageIcon(POS.class.getResource("/img/btn-"+(btnBg[id-1])+".png")));
				}
			});							
			btnLiquor.setIcon(new ImageIcon(POS.class.getResource("/img/btn-"+(btnBg[id-1])+".png")));
			btnLiquor.setFont(new Font("Segoe UI", Font.PLAIN, 19));
			btnLiquor.setBounds(0, 0, 163, 163);
			btnLiquor.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(btnLiquor);
			
			JLabel lblProdName = new JLabel("");
			lblProdName.setHorizontalAlignment(SwingConstants.CENTER);
			lblProdName.setFont(new Font("Segoe UI", Font.ITALIC, 18));
			lblProdName.setBounds(0, 163, 163, 29);
			lblProdName.setText(fname);
			lblProdName.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(lblProdName);
			
			JLabel lblPrice = new JLabel("");
			lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
			lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 17));
			lblPrice.setBounds(0, 189, 163, 29);
			lblPrice.setText("Php "+d.format(fprice));
			lblPrice.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(lblPrice);		
			
			if(fqty==0) {
				lblPrice.setForeground(new Color(225, 6, 0));
				lblPrice.setText("Out of Stock");
			} else {
				lblPrice.setText("Php "+d.format(fprice));
			}
			
			panelProducts.add(prodBtnGrp);		
			x+=189;
		}
	}
	
	public void generatePulutanBtn() {
		panelProducts.removeAll();
		panelProducts.revalidate();
		panelProducts.repaint();

		fetchedNames = pos.fetchPulutanNames();
		fetchedPrices = pos.fetchPulutanPrice();
		fetchedQty = pos.fetchPulutanQty();
		fetchedId = pos.fetchPulutanId();
		
		int nameSize = fetchedNames.size();
		int grid_row=1,grid_col = 5, width = 920, height=565;
		
		if(nameSize==3) {
			width = 564;
		} 
		if(nameSize == 4) {
			width = 727;
		}
		
		if(nameSize==5) {
			width=890;
		}
		
		if(nameSize <=5) {
			height = 240;
		}		
		if(nameSize > 5 && nameSize <10) {
			grid_row = 2;
			grid_col=5;
			width=920;
			height=480;
		}
		if(nameSize > 10) {
			grid_row = 3;			
			height=480;
		}
		if(nameSize > 15) {
			grid_row = 4;
			height=960;
		}
		panelProducts.setBounds(65, 70, width, height);
		panelProducts.setLayout(new GridLayout(grid_row, grid_col, 0, 0));
		
		int i;
		int x=10,y=11;
		for(i=0; i<fetchedNames.size() && i<fetchedPrices.size() && i<fetchedQty.size();i++) {

			String fname = fetchedNames.get(i);
			Double fprice = fetchedPrices.get(i);
			int fqty = fetchedQty.get(i);
			int fid = fetchedId.get(i);
			
			prodBtnGrp = new JPanel();			
			prodBtnGrp.setBackground(Color.WHITE);
			prodBtnGrp.setBounds(x, y, 163, 221);
			prodBtnGrp.setVisible(true);
			prodBtnGrp.setLayout(null);
			
			JButton btnPulutan = new JButton("");
			btnPulutan.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String product = fname;
					double price = fprice;
					int id = fid;
					int qty = 1;			
					int category = 1;
					if(fqty != 0) {
						addItemToTable(product, price, qty, category,id);
					}
				}				
				public void mousePressed(MouseEvent e) {
					btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pulutan-clicked.png")));
				}
				public void mouseReleased(MouseEvent e) {
					btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pulutan.png")));
				}
			});			
			prodBtnGrp.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String product = fname;
					double price = fprice;
					int id = fid;
					int qty = 1;	
					int category = 1;
					if(fqty != 0) {
						addItemToTable(product, price, qty, category,id);
					}
				}				
				public void mousePressed(MouseEvent e) {
					btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pulutan-clicked.png")));
				}
				public void mouseReleased(MouseEvent e) {
					btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pulutan.png")));
				}
			});							
			btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pulutan.png")));
			btnPulutan.setFont(new Font("Segoe UI", Font.PLAIN, 19));
			btnPulutan.setBounds(0, 0, 163, 163);
			btnPulutan.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(btnPulutan);
						
			JLabel lblProdName = new JLabel("");
			lblProdName.setHorizontalAlignment(SwingConstants.CENTER);
			lblProdName.setFont(new Font("Segoe UI", Font.ITALIC, 18));
			lblProdName.setBounds(0, 163, 163, 29);
			lblProdName.setText(fname);
			lblProdName.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(lblProdName);
			
			JLabel lblPrice = new JLabel("");
			lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
			lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 17));
			lblPrice.setBounds(0, 189, 163, 29);
			lblPrice.setCursor(new Cursor(Cursor.HAND_CURSOR));
			prodBtnGrp.add(lblPrice);
			
			if(fqty==0) {
				lblPrice.setForeground(new Color(225, 6, 0));
				lblPrice.setText("Out of Stock");
			} else {
				lblPrice.setText("Php "+d.format(fprice));
			}
			
			panelProducts.add(prodBtnGrp);

			x+=189;
		}
	}
	
	public void refreshTable(JTable table) {
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		dtm.setRowCount(0);
		if(table == liquorTable) {
			displayAllLiquor();
		} 
		else {
			displayAllPulutan();
		}
	}
	
	public void generatePdf(JTable liquor, JTable pulutan, Date from, Date to, Double liqTotal, Double pulutanTotal, Double totalSales){
		dtmLiquor = (DefaultTableModel) liquor.getModel();
		dtmPulutan  = (DefaultTableModel) pulutan.getModel();
		
		SimpleDateFormat df = new SimpleDateFormat("y MMM d");
		String dFrom = df.format(from);
		String dTo = df.format(to);
		
		salesPdf.setText(salesPdf.getText()+"\n\n\t\t   TAGAY Station\r\n");		
		salesPdf.setText(salesPdf.getText()+"\t\tCatarman, Camiguin\r\n");
		salesPdf.setText(salesPdf.getText()+"\t\t   0966-621-0511\r\n");
		salesPdf.setText(salesPdf.getText()+"\n\n\t\t SALES SUMMARY\r\n\n");
		salesPdf.setText(salesPdf.getText()+"From: "+dFrom+"\r\n");
		salesPdf.setText(salesPdf.getText()+"To: "+dTo+"\r\n\n\n");
		salesPdf.setText(salesPdf.getText()+"--------------------------------------------------------------------------------------------------------------\r\n");
		salesPdf.setText(salesPdf.getText()+"Order ID\t Item\t\tQty\tPrice\tSubtotal\r\n");
		salesPdf.setText(salesPdf.getText()+"--------------------------------------------------------------------------------------------------------------\r\n");
		salesPdf.setText(salesPdf.getText()+"LIQUORS\r\n");
		for(int i=0; i < dtmLiquor.getRowCount(); i++) {
			Long orderID = Long.parseLong(dtmLiquor.getValueAt(i, 0).toString());
			String item = dtmLiquor.getValueAt(i, 1).toString();
			int quan = Integer.parseInt(dtmLiquor.getValueAt(i, 2).toString());
			Double price = Double.parseDouble(dtmLiquor.getValueAt(i, 3).toString());
			Double subt = Double.valueOf(quan)*price;
			salesPdf.setText(salesPdf.getText()+orderID+"\t"+item+"\t"+quan+"\t"+d.format(price)+"\t"+d.format(subt)+"\r\n");
		}
		salesPdf.setText(salesPdf.getText()+"\nPULUTAN\r\n");	
		for(int i=0; i < dtmPulutan.getRowCount(); i++) {
			Long orderID = Long.parseLong(dtmPulutan.getValueAt(i, 0).toString());
			String item = dtmPulutan.getValueAt(i, 1).toString();
			int quan = Integer.parseInt(dtmPulutan.getValueAt(i, 2).toString());
			Double price = Double.parseDouble(dtmPulutan.getValueAt(i, 3).toString());
			Double subt = Double.valueOf(quan)*price;
			salesPdf.setText(salesPdf.getText()+orderID+"\t"+item+"\t"+quan+"\t"+d.format(price)+"\t"+d.format(subt)+"\r\n");
		}
		salesPdf.setText(salesPdf.getText()+"--------------------------------------------------------------------------------------------------------------\r\n");
		salesPdf.setText(salesPdf.getText()+"\n\nTOTAL (LIQUORS): \t\tPhp "+d.format(liqTotal)+"\r\n");
		salesPdf.setText(salesPdf.getText()+"TOTAL (PULUTAN): \t\tPhp "+d.format(pulutanTotal)+"\r\n");
		salesPdf.setText(salesPdf.getText()+"TOTAL SALES: \t\tPhp "+d.format(totalSales)+"\r\n");
		
		try {
			salesPdf.print();
			salesPdf.setText("");
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
	
	public void computeSalesTotal(JTable table) {		
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		for(int i=0; i < dtm.getRowCount(); i++) {
			Double subtotal = Double.parseDouble(dtm.getValueAt(i, 4).toString());
			if (table == salesLiquor) {
				liquorSalesTotal += subtotal;
			}
			else {
				pulutanSalesTotal+=subtotal;
			}
		}
		totalLiquors.setText(d.format(liquorSalesTotal).toString());
		totalPulutan.setText(d.format(pulutanSalesTotal).toString());
		allTotal.setText(d.format(liquorSalesTotal+pulutanSalesTotal).toString());
	}
	
	public void generateLiquorSales(Date dfrom, Date dto) {
		dtmLiquor = (DefaultTableModel) salesLiquor.getModel();
		dtmLiquor.setRowCount(0);
		
		SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
		String from = df.format(dfrom);
		String to = df.format(dto);
		
		Long fromLong = Long.parseLong(from);
		Long toLong = Long.parseLong(to);
		
		try {
			String fetchLiquorSales = "SELECT orderID, liquorName, orderQty, price FROM orderline INNER JOIN liquors ON orderline.productID = liquors.liquorID AND orderline.categoryID=liquors.productCategory";
			ps = conn.prepareStatement(fetchLiquorSales);
			rs = ps.executeQuery();
					
			while(rs.next()) {
				Long orderID = rs.getLong("orderID");
				String liqName = rs.getString("liquorName");
				int qty = rs.getInt("orderQty");
				Double price = rs.getDouble("price");
				
				String stringOrderId = Long.toString(orderID);
				String slicedID = stringOrderId.substring(0,6);
				
				Long slicedOrderID = Long.parseLong(slicedID);
				
				int idFrom = slicedOrderID.compareTo(fromLong);
				int idTo = slicedOrderID.compareTo(toLong);
				
				if((idFrom >= 0 && idTo <= 0)) {
					row = new Object[]{orderID, liqName, qty, d.format(price),d.format(Double.valueOf(qty)*price)};
			        dtmLiquor.addRow(row);
				}				
			}
			liquorSalesTotal=0.00;
			computeSalesTotal(salesLiquor);
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void generatePulutanSales(Date dfrom, Date dto) {
		dtmPulutan = (DefaultTableModel) salesPulutan.getModel();
		dtmPulutan.setRowCount(0);
		
		SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
		String from = df.format(dfrom);
		String to = df.format(dto);
		
		Long fromLong = Long.parseLong(from);
		Long toLong = Long.parseLong(to);
		
		try {
			String fetchPulutanSales = "SELECT orderID, pulutanName, orderQty, price FROM orderline INNER JOIN pulutan ON orderline.productID = pulutan.pulutanID AND orderline.categoryID=pulutan.productCategory";
			ps = conn.prepareStatement(fetchPulutanSales);
			rs = ps.executeQuery();
					
			while(rs.next()) {
				Long orderID = rs.getLong("orderID");
				String liqName = rs.getString("pulutanName");
				int qty = rs.getInt("orderQty");
				Double price = rs.getDouble("price");
				
				String stringOrderId = Long.toString(orderID);
				String slicedID = stringOrderId.substring(0,6);
				
				Long slicedOrderID = Long.parseLong(slicedID);
				
				int idFrom = slicedOrderID.compareTo(fromLong);
				int idTo = slicedOrderID.compareTo(toLong);
				
				if((idFrom >= 0 && idTo <= 0)) {
					row = new Object[]{orderID, liqName, qty, d.format(price),d.format(Double.valueOf(qty)*price)};
			        dtmPulutan.addRow(row);
				}				
			}
			pulutanSalesTotal=0.00;
			computeSalesTotal(salesPulutan);
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void displayAllLiquorSales() {	
		dtmLiquor = (DefaultTableModel) salesLiquor.getModel();
		dtmLiquor.setRowCount(0);
		try {
			String fetchLiquorSales = "SELECT orderID, liquorName, orderQty, price FROM orderline INNER JOIN liquors ON orderline.productID = liquors.liquorID AND orderline.categoryID=liquors.productCategory";
			ps = conn.prepareStatement(fetchLiquorSales);
			rs = ps.executeQuery();
					
			while(rs.next()) {
				Long orderID = rs.getLong("orderID");
				String liqName = rs.getString("liquorName");
				int qty = rs.getInt("orderQty");
				Double price = rs.getDouble("price");
				
				
				row = new Object[]{orderID, liqName, qty, d.format(price),d.format(Double.valueOf(qty)*price)};
		        dtmLiquor.addRow(row);
			}
			liquorSalesTotal=0.00;
			computeSalesTotal(salesLiquor);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void displayAllPulutanSales() {
		dtmPulutan = (DefaultTableModel) salesPulutan.getModel();
		dtmPulutan.setRowCount(0);
		try {
			String fetchPulutanSales = "SELECT orderID, pulutanName, orderQty, price FROM orderline INNER JOIN pulutan ON orderline.productID = pulutan.pulutanID AND orderline.categoryID=pulutan.productCategory";
			ps = conn.prepareStatement(fetchPulutanSales);
			rs = ps.executeQuery();
					
			while(rs.next()) {
				Long orderID = rs.getLong("orderID");
				String liqName = rs.getString("pulutanName");
				int qty = rs.getInt("orderQty");
				Double price = rs.getDouble("price");
				
				
				row = new Object[]{orderID, liqName, qty, d.format(price),d.format(Double.valueOf(qty)*price)};
		        dtmPulutan.addRow(row);
			}
			pulutanSalesTotal=0.00;
			computeSalesTotal(salesPulutan);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e2) {
				System.out.println(e2.toString());
			}
		}
	}
	
	public void displayAllLiquor() {
		try {
			String fetchAll = "SELECT categoryName, liquorName, price, qty FROM liquors INNER JOIN liquorCategories ON liquors.categoryID = liquorCategories.categoryID ORDER BY liquors.categoryID";
			ps = conn.prepareStatement(fetchAll);
			rs = ps.executeQuery();
						
			while(rs.next()) {
				
				String ctgName = rs.getString("categoryName");
				String liquorName = rs.getString("liquorName");
				Double price = rs.getDouble("price");
				int qty = rs.getInt("qty");
				
				row = new Object[]{ctgName, liquorName, d.format(price), qty,"DELETE"};
		        model = (DefaultTableModel) liquorTable.getModel();
		        model.addRow(row);
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
	}
	
	public void displayAllPulutan() {
		try {
			String fetchAll = "SELECT * FROM pulutan ORDER BY pulutanName";
			ps = conn.prepareStatement(fetchAll);
			rs = ps.executeQuery();
			
			while(rs.next()) {
				
				String ctgName = "Pulutan";
				String pulutanName = rs.getString("pulutanName");
				Double price = rs.getDouble("price");
				int qty = rs.getInt("qty");
				
				row = new Object[]{ctgName, pulutanName, d.format(price),qty,"DELETE"};
		        model = (DefaultTableModel) pulutanTable.getModel();
		        model.addRow(row);
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
	}
	
	public void displayForEdit(JTable table) {
		selectedTable = table;
		int row = table.getSelectedRow();
		String name_for_edit = table.getValueAt(row, 1).toString();
		Double price_for_edit = Double.parseDouble(table.getValueAt(row, 2).toString());
		int qty_for_edit = Integer.parseInt(table.getValueAt(row, 3).toString());
		
		editField_name.setText(name_for_edit);
		editField_price.setText(d.format(price_for_edit).toString());
		editField_qty.setText(Integer.toString(qty_for_edit));	
	}
	
	@SuppressWarnings({ "serial", "serial" })
	public void initComponents() {
		
		setTitle("Tagay Station POS");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1536, 864);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblCurrentUser_ = new JLabel("");
		lblCurrentUser_.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrentUser_.setForeground(Color.WHITE);
		lblCurrentUser_.setFont(new Font("Segoe UI", Font.PLAIN, 22));
		lblCurrentUser_.setBounds(1122, 25, 234, 39);
		contentPane.add(lblCurrentUser_);
		
		btnHome_ = new JButton("");
		btnHome_.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				gotoMenu(currentPanel);
				refreshTable(liquorTable);
				refreshTable(pulutanTable);
				displayAllLiquorSales();
				displayAllPulutanSales();
				dateFrom.setDate(null);
				dateTo.setDate(null);
			}
		});
		btnHome_.setIcon(new ImageIcon(POS.class.getResource("/img/btn-home2.png")));
		btnHome_.setBounds(1428, 20, 47, 45);
		btnHome_.setVisible(false);
		contentPane.add(btnHome_);
		
		mainNav = new JLabel("New label");
		mainNav.setIcon(new ImageIcon(POS.class.getResource("/img/ts-navbar-main-bg.png")));
		mainNav.setBounds(0, 0, 1520, 88);
		mainNav.setVisible(false);
		
		contentPane.add(mainNav);
		
		salesPanel = new JPanel();
		salesPanel.setBounds(0, 88, 1520, 737);
		contentPane.add(salesPanel);
		salesPanel.setLayout(null);
		salesPanel.setVisible(false);
		
		JButton btnBacktoInv = new JButton("");
		btnBacktoInv.setIcon(new ImageIcon(POS.class.getResource("/img/sales-inv-btn.png")));
		btnBacktoInv.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				salesPanel.setVisible(false);
				Inventory.setVisible(true);
				currentPanel = Inventory;
				displayAllLiquorSales();
				displayAllPulutanSales();
				dateFrom.setDate(null);
				dateTo.setDate(null);
			}
		});
		
		btnExportPdf = new JButton("");
		btnExportPdf.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Double totalSales = liquorSalesTotal+pulutanSalesTotal;
				generatePdf(salesLiquor, salesPulutan,dateFrom.getDate(),dateTo.getDate(), liquorSalesTotal,pulutanSalesTotal, totalSales);
			}
		});
		btnExportPdf.setIcon(new ImageIcon(POS.class.getResource("/img/sales-export-pdf.png")));
		btnExportPdf.setBounds(1174, 566, 276, 45);
		salesPanel.add(btnExportPdf);
		btnBacktoInv.setBounds(1174, 622, 276, 39);
		salesPanel.add(btnBacktoInv);
		
		dateFrom = new JDateChooser();
		dateFrom.setBounds(1127, 338, 356, 30);
		salesPanel.add(dateFrom);
		
		dateTo = new JDateChooser();
		dateTo.setBounds(1127, 443, 356, 30);
		salesPanel.add(dateTo);
		
		allTotal = new JLabel("");
		allTotal.setFont(new Font("Segoe UI", Font.BOLD, 30));
		allTotal.setForeground(Color.WHITE);
		allTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		allTotal.setBounds(883, 663, 162, 40);
		salesPanel.add(allTotal);
		
		totalLiquors = new JLabel("");
		totalLiquors.setHorizontalAlignment(SwingConstants.RIGHT);
		totalLiquors.setForeground(new Color(0, 51, 102));
		totalLiquors.setFont(new Font("Segoe UI", Font.BOLD, 30));
		totalLiquors.setBounds(627, 663, 162, 40);
		salesPanel.add(totalLiquors);
		
		totalPulutan = new JLabel("");
		totalPulutan.setHorizontalAlignment(SwingConstants.RIGHT);
		totalPulutan.setForeground(new Color(0, 51, 102));
		totalPulutan.setFont(new Font("Segoe UI", Font.BOLD, 30));
		totalPulutan.setBounds(374, 663, 162, 40);
		salesPanel.add(totalPulutan);
		
		JScrollPane scrollPaneLiquor = new JScrollPane();
		scrollPaneLiquor.setBounds(11, 94, 519, 502);
		salesPanel.add(scrollPaneLiquor);
		
		salesLiquor = new JTable();
		salesLiquor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		scrollPaneLiquor.setViewportView(salesLiquor);
		salesLiquor.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"OrderID", "Item Name", "Qty", "Price", "Subtotal"
			}
		) {
			Class[] columnTypes = new Class[] {
				Long.class, String.class, Integer.class, Double.class, Double.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		salesLiquor.getColumnModel().getColumn(0).setResizable(false);
		salesLiquor.getColumnModel().getColumn(1).setResizable(false);
		salesLiquor.getColumnModel().getColumn(1).setPreferredWidth(150);
		salesLiquor.getColumnModel().getColumn(2).setResizable(false);
		salesLiquor.getColumnModel().getColumn(2).setPreferredWidth(27);
		salesLiquor.getColumnModel().getColumn(3).setResizable(false);
		salesLiquor.getColumnModel().getColumn(4).setResizable(false);
		
		salesLiquor.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		salesLiquor.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		salesLiquor.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		salesLiquor.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		salesLiquor.setRowHeight(30);
		
		salesLiquor.getColumnModel().getColumn(1).setCellRenderer(paddingRenderer);
		
		JScrollPane scrollPanePulutan = new JScrollPane();
		scrollPanePulutan.setBounds(535, 94, 517, 502);
		salesPanel.add(scrollPanePulutan);
		
		salesPulutan = new JTable();
		salesPulutan.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		scrollPanePulutan.setViewportView(salesPulutan);
		salesPulutan.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"OrderID", "Item Name", "Qty", "Price", "Subtotal"
			}
		) {
			Class[] columnTypes = new Class[] {
				Long.class, String.class, Integer.class, Double.class, Double.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		salesPulutan.getColumnModel().getColumn(0).setResizable(false);
		salesPulutan.getColumnModel().getColumn(1).setResizable(false);
		salesPulutan.getColumnModel().getColumn(1).setPreferredWidth(150);
		salesPulutan.getColumnModel().getColumn(2).setResizable(false);
		salesPulutan.getColumnModel().getColumn(2).setPreferredWidth(27);
		salesPulutan.getColumnModel().getColumn(3).setResizable(false);
		salesPulutan.getColumnModel().getColumn(4).setResizable(false);
		
		salesPulutan.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		salesPulutan.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		salesPulutan.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		salesPulutan.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		salesPulutan.setRowHeight(30);
		
		salesPulutan.getColumnModel().getColumn(1).setCellRenderer(paddingRenderer);
		
		JButton btnGenerate = new JButton("");
		btnGenerate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorSales(dateFrom.getDate(), dateTo.getDate());
				generatePulutanSales(dateFrom.getDate(), dateTo.getDate());
			}
		});
		btnGenerate.setIcon(new ImageIcon(POS.class.getResource("/img/sales-generate-btn.png")));
		btnGenerate.setBounds(1174, 510, 276, 45);
		salesPanel.add(btnGenerate);
		
		salesPdf = new JTextArea();
		salesPdf.setFont(new Font("Arial", Font.PLAIN, 11));
		salesPdf.setBounds(314, 11, 547, 621);
		salesPdf.setVisible(false);
		salesPanel.add(salesPdf);
		
		JLabel salesBg = new JLabel("New label");
		salesBg.setIcon(new ImageIcon(POS.class.getResource("/img/pos-sales-bg.png")));
		salesBg.setBounds(0, 0, 1520, 737);
		salesPanel.add(salesBg);
		
		Inventory = new JPanel();
		Inventory.setBounds(0, 88, 1520, 737);
		contentPane.add(Inventory);
		Inventory.setVisible(false);
		Inventory.setLayout(null);
		
		JButton btnCancel = new JButton("");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editField_name.setText("");
				editField_price.setText("");
				editField_qty.setText("");
				pulutanTable.revalidate();
				pulutanTable.repaint();
				liquorTable.revalidate();
				liquorTable.repaint();
				
			}
		});
		btnCancel.setIcon(new ImageIcon(POS.class.getResource("/img/inv-cancel-btn.png")));
		btnCancel.setBounds(1159, 405, 339, 33);
		Inventory.add(btnCancel);
		
		editField_qty = new JTextField();
		editField_qty.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		editField_qty.setColumns(10);
		editField_qty.setBounds(1159, 310, 336, 30);
		Inventory.add(editField_qty);
		
		editField_name = new JTextField();
		editField_name.setEditable(false);
		editField_name.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		editField_name.setBounds(1159, 183, 336, 30);
		Inventory.add(editField_name);
		editField_name.setColumns(10);
		
		editField_price = new JTextField();
		editField_price.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		editField_price.setColumns(10);
		editField_price.setBounds(1159, 247, 336, 30);
		Inventory.add(editField_price);
		
		JButton btnSave = new JButton("");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable activeTable = selectedTable;
				
				String editName = editField_name.getText();
				Double editPrice = Double.valueOf(editField_price.getText());
				int editQty = Integer.parseInt(editField_qty.getText());
				
				Liquor liquor = new Liquor(editName, editPrice, editQty, 0);
				Pulutan pulutan = new Pulutan(editName, editPrice, editQty);
				
				if(activeTable == liquorTable) {
					pos.editLiquor(liquor);
					refreshTable(liquorTable);
				} else {
					pos.editPulutan(pulutan);
					refreshTable(pulutanTable);
				}
				editField_name.setText("");
				editField_price.setText("");
				editField_qty.setText("");
			}
		});
		btnSave.setIcon(new ImageIcon(POS.class.getResource("/img/inv-save-btn.png")));
		btnSave.setBounds(1159, 362, 339, 33);
		Inventory.add(btnSave);
		
		JButton btnAddProd = new JButton("");
		btnAddProd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				addProduct.show();
				if(addProduct.added) {
					refreshTable(liquorTable);
					refreshTable(pulutanTable);
				}
			}
		});
		btnAddProd.setIcon(new ImageIcon(POS.class.getResource("/img/add-new.png")));
		btnAddProd.setBounds(1159, 497, 339, 33);
		Inventory.add(btnAddProd);
		
		JButton btnViewSales = new JButton("");
		btnViewSales.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Inventory.setVisible(false);
				salesPanel.setVisible(true);
				currentPanel = salesPanel;
			}
		});
		btnViewSales.setIcon(new ImageIcon(POS.class.getResource("/img/view-sales.png")));
		btnViewSales.setBounds(1159, 541, 339, 33);
		Inventory.add(btnViewSales);
		
		JScrollPane liquor_scrollpane = new JScrollPane();
		liquor_scrollpane.setViewportBorder(null);
		liquor_scrollpane.setBounds(19, 67, 537, 650);
		Inventory.add(liquor_scrollpane);
		
		liquorTable = new JTable();
		liquorTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				displayForEdit(liquorTable);
				int col = liquorTable.getSelectedColumn();
				int row = liquorTable.getSelectedRow();
				if(col == 4) {
					String productName = liquorTable.getValueAt(row, 1).toString();
					int confirm = JOptionPane.showConfirmDialog(null,"Are you sure to delete "+productName+"?", "Confirm Delete Product",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(confirm == JOptionPane.YES_OPTION) {
						pos.deleteLiquor(productName);
						editField_name.setText("");
						editField_price.setText("");
						editField_qty.setText("");
						refreshTable(liquorTable);
					} 					
				}
			}
		});
		liquorTable.setBorder(null);
		liquorTable.setFont(new Font("Segoe UI", Font.BOLD, 16));
		liquor_scrollpane.setViewportView(liquorTable);
		liquorTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Category", "Name", "Price", "Qty", "Delete"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, Double.class, Integer.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		liquorTable.getColumnModel().getColumn(0).setPreferredWidth(59);
		liquorTable.getColumnModel().getColumn(1).setPreferredWidth(142);
		liquorTable.getColumnModel().getColumn(2).setPreferredWidth(55);
		liquorTable.getColumnModel().getColumn(3).setPreferredWidth(35);
		liquorTable.getColumnModel().getColumn(4).setPreferredWidth(45);
		
		liquorTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		liquorTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		liquorTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		
		liquorTable.getColumnModel().getColumn(4).setCellRenderer(new colorRenderer(Color.WHITE, new Color(135,49,51)));

		liquorTable.setRowHeight(30);
		
		JScrollPane pulutan_scrollPane = new JScrollPane();
		pulutan_scrollPane.setBounds(578, 67, 537, 650);
		Inventory.add(pulutan_scrollPane);
		
		pulutanTable = new JTable();
		pulutanTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				displayForEdit(pulutanTable);
				int col = pulutanTable.getSelectedColumn();
				int row = pulutanTable.getSelectedRow();
				if(col == 4) {
					String productName = pulutanTable.getValueAt(row, 1).toString();
					int confirm = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete "+productName+"?", "Confirm Delete Product",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(confirm == JOptionPane.YES_OPTION) {
						pos.deletePulutan(productName);
						editField_name.setText("");
						editField_price.setText("");
						editField_qty.setText("");
						refreshTable(pulutanTable);
					}
				}
			}
		});
		pulutanTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Category", "Name", "Price", "Qty", "Delete"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, Double.class, Integer.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		pulutanTable.getColumnModel().getColumn(0).setPreferredWidth(59);
		pulutanTable.getColumnModel().getColumn(1).setPreferredWidth(142);
		pulutanTable.getColumnModel().getColumn(2).setPreferredWidth(55);
		pulutanTable.getColumnModel().getColumn(3).setPreferredWidth(35);
		pulutanTable.getColumnModel().getColumn(4).setPreferredWidth(45);
		pulutan_scrollPane.setViewportView(pulutanTable);
		pulutanTable.setFont(new Font("Segoe UI", Font.BOLD, 16));
		
		pulutanTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		pulutanTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		pulutanTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		
		pulutanTable.getColumnModel().getColumn(4).setCellRenderer(new colorRenderer(Color.WHITE, new Color(135,49,51)));
		
		pulutanTable.setRowHeight(30);
		
		JButton btnRefresh = new JButton("");
		btnRefresh.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				refreshTable(liquorTable);
				refreshTable(pulutanTable);
			}
		});
		btnRefresh.setIcon(new ImageIcon(POS.class.getResource("/img/btn-refresh.png")));
		btnRefresh.setBounds(1306, 585, 57, 57);
		Inventory.add(btnRefresh);
		
		JLabel inventoryBG = new JLabel("New label");
		inventoryBG.setBounds(0, 0, 1520, 737);
		inventoryBG.setIcon(new ImageIcon(POS.class.getResource("/img/pos-inventory-bg2.png")));
		Inventory.add(inventoryBG);
		
		JPanel POS = new JPanel();
		POS.setBounds(0, 88, 1520, 737);
		contentPane.add(POS);
		POS.setVisible(false);
		POS.setLayout(null);		
//		POS.addKeyListener(new EnterKeyListener());
//		POS.setFocusable(true);
//		POS.requestFocusInWindow();
		
		panelProducts = new JPanel();
		panelProducts.setBackground(Color.WHITE);
		POS.add(panelProducts);
		
		JButton btnVodka = new JButton("");
		btnVodka.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(1);				
			}		
			public void mousePressed(MouseEvent e) {
				btnVodka.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-vodka-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnVodka.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-vodka.png")));
			}
		});
		btnVodka.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-vodka.png")));
		btnVodka.setBounds(10, 591, 96, 44);
		btnVodka.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnVodka);
		
		JButton btnWhiskey = new JButton("");
		btnWhiskey.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(2);
			}
			
			public void mousePressed(MouseEvent e) {
				btnWhiskey.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-whiskey-active.png")));
			}
			
			public void mouseReleased(MouseEvent e) {				
				btnWhiskey.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-whiskey.png")));
			}
		});
		btnWhiskey.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-whiskey.png")));
		btnWhiskey.setBounds(116, 591, 96, 44);
		btnWhiskey.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnWhiskey);
		
		JButton btnBrandy = new JButton("");
		btnBrandy.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(3);
			}
			
			public void mousePressed(MouseEvent e) {
				btnBrandy.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-brandy-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnBrandy.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-brandy.png")));
			}
		});
		btnBrandy.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-brandy.png")));
		btnBrandy.setBounds(222, 591, 96, 44);
		btnBrandy.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnBrandy);
		
		JButton btnBeer = new JButton("");
		btnBeer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(4);
			}
			public void mousePressed(MouseEvent e) {
				btnBeer.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-beer-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnBeer.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-beer.png")));
			}
			
		});
		btnBeer.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-beer.png")));
		btnBeer.setBounds(328, 591, 96, 44);
		btnBeer.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnBeer);
		
		JButton btnRum = new JButton("");		
		btnRum.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(5);
			}
			public void mousePressed(MouseEvent e) {
				btnRum.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-rum-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnRum.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-rum.png")));
			}
		});
		btnRum.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-rum.png")));
		btnRum.setBounds(434, 591, 96, 44);
		btnRum.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnRum);
		
		JButton btnGin = new JButton("");
		btnGin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(6);
			}
			public void mousePressed(MouseEvent e) {
				btnGin.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-gin-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnGin.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-gin.png")));
			}
		});
		btnGin.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-gin.png")));
		btnGin.setBounds(540, 591, 96, 44);
		btnGin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnGin);
		
		JButton btnTequila = new JButton("");
		btnTequila.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generateLiquorBtn(7);
			}
			public void mousePressed(MouseEvent e) {
				btnTequila.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-tequila-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnTequila.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-tequila.png")));
			}
		});
		btnTequila.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-tequila.png")));
		btnTequila.setBounds(646, 591, 96, 44);
		btnTequila.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnTequila);
		
		JButton btnPulutan = new JButton("");
		btnPulutan.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				generatePulutanBtn();
			}
			public void mousePressed(MouseEvent e) {
				btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-pulutan-active.png")));
			}
			public void mouseReleased(MouseEvent e) {				
				btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-pulutan.png")));
			}
		});
		btnPulutan.setIcon(new ImageIcon(POS.class.getResource("/img/ctg-pulutan.png")));
		btnPulutan.setBounds(752, 591, 96, 44);
		btnPulutan.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnPulutan);
		
		btnPay = new JButton("");
		btnPay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				payOrder();
			}
			
			public void mousePressed(MouseEvent e) {
				btnPay.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pay-clicked.png")));
			}
			
			public void mouseReleased(MouseEvent e) {
				btnPay.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pay.png")));
			}
		});
		btnPay.setIcon(new ImageIcon(POS.class.getResource("/img/btn-pay.png")));
		btnPay.setBounds(1026, 670, 484, 54);
		btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
		POS.add(btnPay);
		
		JButton btnNewOrder = new JButton("");
		btnNewOrder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				currentOrderId = newOrder();
				lblOrderID.setText(Long.toString(currentOrderId));
				receipt.setText("");
			}
		});
		btnNewOrder.setIcon(new ImageIcon(POS.class.getResource("/img/btn-newOrder.png")));
		btnNewOrder.setBounds(826, 670, 187, 54);
		POS.add(btnNewOrder);
		
		JButton btnCancelOrder = new JButton("");
		btnCancelOrder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pos.cancelOrder(currentOrderId,ord_productName,ord_qty,ord_ctg);
				currentOrderId = newOrder();
			}
		});
		btnCancelOrder.setIcon(new ImageIcon(POS.class.getResource("/img/btn-order-cancel.png")));
		btnCancelOrder.setBounds(629, 670, 187, 54);
		POS.add(btnCancelOrder);
		
		lblOrderID = new JLabel("");
		lblOrderID.setFont(new Font("Segoe UI", Font.BOLD, 25));
		lblOrderID.setHorizontalAlignment(SwingConstants.TRAILING);
		lblOrderID.setBounds(1165, 16, 337, 33);
		lblOrderID.setForeground(new Color(5, 59, 86));
		POS.add(lblOrderID);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(1028, 55, 479, 460);
		POS.add(scrollPane);
		
		orderTable = new JTable();
		orderTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = orderTable.getSelectedColumn();
				switch (col) {
					case 0: 
						removeItem();
						break;
					case 2:
						minusQty();
						break;
					case 4:
						addQty();
						break;
					default:
						break;
				}
			}
		});
		orderTable.setRowSelectionAllowed(false);
		orderTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(orderTable);
		orderTable.setBorder(null);
		orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		orderTable.setBackground(SystemColor.textHighlightText);
		orderTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"DEL", "ITEM", "-", "QTY", "+", "PRICE", "C", "ID"
			}
		) {
			Class[] columnTypes = new Class[] {
				Object.class, String.class, String.class, Integer.class, String.class, Double.class, Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		orderTable.getColumnModel().getColumn(0).setResizable(false);
		orderTable.getColumnModel().getColumn(0).setPreferredWidth(35);
		orderTable.getColumnModel().getColumn(1).setResizable(false);
		orderTable.getColumnModel().getColumn(1).setPreferredWidth(196);
		orderTable.getColumnModel().getColumn(2).setResizable(false);
		orderTable.getColumnModel().getColumn(2).setPreferredWidth(27);
		orderTable.getColumnModel().getColumn(3).setResizable(false);
		orderTable.getColumnModel().getColumn(3).setPreferredWidth(39);
		orderTable.getColumnModel().getColumn(4).setResizable(false);
		orderTable.getColumnModel().getColumn(4).setPreferredWidth(27);
		orderTable.getColumnModel().getColumn(5).setResizable(false);
		orderTable.getColumnModel().getColumn(5).setPreferredWidth(63);
		orderTable.getColumnModel().getColumn(6).setResizable(false);
		orderTable.getColumnModel().getColumn(6).setPreferredWidth(23);
		orderTable.getColumnModel().getColumn(7).setResizable(false);
		orderTable.getColumnModel().getColumn(7).setPreferredWidth(15);
		
		orderTable.getColumnModel().getColumn(0).setCellRenderer(new colorRenderer(Color.WHITE, new Color(135,49,51)));
		orderTable.getColumnModel().getColumn(2).setCellRenderer(new colorRenderer(new Color(135,49,51), Color.WHITE));
		orderTable.getColumnModel().getColumn(4).setCellRenderer(new colorRenderer(new Color(24,146,98), Color.WHITE));
		orderTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		orderTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		orderTable.setRowHeight(30);
		
		tcm = orderTable.getColumnModel();
		tcm.removeColumn(tcm.getColumn(6));
		tcm.removeColumn(tcm.getColumn(6));
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		
		lblTotal = new JLabel("");
		lblTotal.setBackground(Color.WHITE);
		lblTotal.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTotal.setForeground(Color.ORANGE);
		lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 37));
		lblTotal.setBounds(1165, 526, 337, 33);
		POS.add(lblTotal);
		
		cashTextField = new JTextField();
		cashTextField.setForeground(new Color(139, 69, 19));
		cashTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		cashTextField.setFont(new Font("Segoe UI", Font.BOLD, 33));
		cashTextField.setBounds(1260, 576, 242, 33);
		POS.add(cashTextField);
		cashTextField.setColumns(10);
		
		lblChange = new JLabel("");
		lblChange.setHorizontalAlignment(SwingConstants.TRAILING);
		lblChange.setForeground(new Color(0, 100, 0));
		lblChange.setFont(new Font("Segoe UI", Font.BOLD, 35));
		lblChange.setBackground(Color.WHITE);
		lblChange.setBounds(1260, 614, 242, 33);
		POS.add(lblChange);
		
		JLabel lblPOSBg = new JLabel("");
		lblPOSBg.setIcon(new ImageIcon(POS.class.getResource("/img/pos-bg2.png")));
		lblPOSBg.setBounds(0, 0, 1520, 737);
		POS.add(lblPOSBg);
		
		receipt = new JTextArea();
		receipt.setForeground(new Color(0, 0, 139));
		receipt.setEditable(false);
		receipt.setFont(new Font("Courier New", Font.PLAIN, 9));
		receipt.setVisible(false);
		receipt.setBounds(616, 148, 383, 576);
		POS.add(receipt);
		
		//Declaring Panels
		JPanel signupPanel = new JPanel();
		signupPanel.setLayout(null);
		signupPanel.setBounds(0, 0, 1520, 825);
		contentPane.add(signupPanel);
		signupPanel.setVisible(false);
												
		JPanel loginPanel = new JPanel();
		loginPanel.setBounds(0, 0, 1520, 825);
		contentPane.add(loginPanel);
		loginPanel.setLayout(null);
		loginPanel.setVisible(true);
				
		menuPanel = new JPanel();
		menuPanel.setBounds(0, 0, 1520, 825);
		contentPane.add(menuPanel);
		menuPanel.setLayout(null);
		menuPanel.setVisible(false);
		
		
		
		
		//****************************************************Menu Panel****************************************************//
		JButton btnPOS = new JButton("");
		btnPOS.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				POS.setVisible(true);
				menuPanel.setVisible(false);
				lblCurrentUser_.setText(currentUser);
				lblCurrentUser_.setVisible(true);
				mainNav.setVisible(true);
				btnHome_.setVisible(true);
				currentOrderId = pos.generateOrderId();
				lblOrderID.setText(Long.toString(currentOrderId));
				currentPanel = POS;
				generateLiquorBtn(1);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				btnPOS.setIcon(new ImageIcon(POS.class.getResource("/img/btn_pos-clicked.png")));
			}			
			
			@Override
			public void mouseReleased(MouseEvent e) {
				btnPOS.setIcon(new ImageIcon(POS.class.getResource("/img/btn_pos.png")));
			}			
		});
		btnPOS.setIcon(new ImageIcon(POS.class.getResource("/img/btn_pos.png")));
		btnPOS.setBounds(859, 307, 391, 358);
		btnPOS.setCursor(new Cursor(Cursor.HAND_CURSOR));
		menuPanel.add(btnPOS);
		
		JButton btnInventory = new JButton("");
		btnInventory.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				btnInventory.setIcon(new ImageIcon(POS.class.getResource("/img/btn_inventory-clicked.png")));
			}		
			@Override
			public void mouseReleased(MouseEvent e) {
				btnInventory.setIcon(new ImageIcon(POS.class.getResource("/img/btn_inventory.png")));
			}
			@Override
			public void mouseClicked(MouseEvent e) {	
				String inputPin = JOptionPane.showInputDialog("Please input PIN:");
				if(inputPin.equals("4321")) {
					menuPanel.setVisible(false);
					Inventory.setVisible(true);
					mainNav.setVisible(true);
					btnHome_.setVisible(true);	
					lblCurrentUser_.setText(currentUser);
					lblCurrentUser_.setVisible(true);
					currentPanel = Inventory;
				} else {
					JOptionPane.showMessageDialog(null, "You're not allowed for inventory.","Hey!", JOptionPane.WARNING_MESSAGE);
				}				
			}
		});
		btnInventory.setIcon(new ImageIcon(POS.class.getResource("/img/btn_inventory.png")));
		btnInventory.setBounds(283, 307, 391, 358);
		btnInventory.setCursor(new Cursor(Cursor.HAND_CURSOR));
		menuPanel.add(btnInventory);
		
		JLabel lblCurrentUser = new JLabel("");
		lblCurrentUser.setForeground(new Color(255, 255, 255));
		lblCurrentUser.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrentUser.setFont(new Font("Segoe UI", Font.PLAIN, 22));
		lblCurrentUser.setBounds(1114, 24, 234, 39);
		menuPanel.add(lblCurrentUser);			
		
		JButton btnLogout = new JButton("");
		btnLogout.setIcon(new ImageIcon(POS.class.getResource("/img/btn_logout2.png")));
		btnLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logged=0;
				if(logged==0) {
					menuPanel.setVisible(false);
					loginPanel.setVisible(true);
					lblCurrentUser.setText(null);
					currentUser = null;
				}
			}
		});
		btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 20));
		btnLogout.setBounds(1426, 14, 50, 59);
		btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
		menuPanel.add(btnLogout);
		
		JLabel menuBg = new JLabel("");
		menuBg.setIcon(new ImageIcon(POS.class.getResource("/img/ts-menu-bg.png")));
		menuBg.setBounds(0, 0, 1520, 825);
		menuPanel.add(menuBg);		
		
		//****************************************************Sign Up Panel****************************************************//
		signup_userField = new JTextField();
		signup_userField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		signup_userField.setColumns(10);
		signup_userField.setBounds(957, 364, 475, 47);
		signup_userField.setBorder(BorderFactory.createCompoundBorder(
				signup_userField.getBorder(), 
		        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		signupPanel.add(signup_userField);
		
		signup_passwordField = new JPasswordField();
		signup_passwordField.setFont(new Font("Tahoma", Font.PLAIN, 20));
		signup_passwordField.setBounds(957, 486, 475, 47);
		signup_passwordField.setBorder(BorderFactory.createCompoundBorder(
				signup_passwordField.getBorder(), 
		        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		signupPanel.add(signup_passwordField);
		
		JLabel lblSignupAlert = new JLabel("");
		lblSignupAlert.setHorizontalAlignment(SwingConstants.CENTER);
		lblSignupAlert.setForeground(new Color(0, 100, 0));
		lblSignupAlert.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblSignupAlert.setBounds(957, 544, 475, 25);
		signupPanel.add(lblSignupAlert);
		
		JButton btnSignup = new JButton("");
		btnSignup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {		
				
				String userSignUp = signup_userField.getText();
				String passwordSignUp = signup_passwordField.getText();
				
				user.setUsername(userSignUp);
				user.setPassword(passwordSignUp);
					
				if((userSignUp.equals("")) || (userSignUp == null) || (passwordSignUp.equals("")) || (passwordSignUp == null)) {
					lblSignupAlert.setText("All fields are required.");
					lblSignupAlert.setForeground(Color.RED);
				} else {	
					if(pos.userExists(user.getUsername())) {
						lblSignupAlert.setForeground(Color.RED);
						lblSignupAlert.setText("Username is existing already.");	
					} else {													
						if(pos.addUser(user.getUsername(), user.getPassword())) {
							lblSignupAlert.setForeground(new Color(0, 100, 0));
							lblSignupAlert.setText("Sign up successful.");							
							signup_userField.setText(null);
							signup_passwordField.setText(null);
						}			
						else {
							lblSignupAlert.setForeground(Color.RED);
							lblSignupAlert.setText("Sign up failed.");
						}
					}
				}				
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				btnSignup.setIcon(new ImageIcon(POS.class.getResource("/img/btn_signup-clicked.png")));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnSignup.setIcon(new ImageIcon(POS.class.getResource("/img/btn_signup.png")));
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				btnSignup.setIcon(new ImageIcon(POS.class.getResource("/img/btn_signup.png")));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				btnSignup.setIcon(new ImageIcon(POS.class.getResource("/img/btn_signup-clicked.png")));
			}
		});
		btnSignup.setIcon(new ImageIcon(POS.class.getResource("/img/btn_signup.png")));
		btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 20));
		btnSignup.setBounds(956, 581, 478, 47);
		btnSignup.setCursor(new Cursor(Cursor.HAND_CURSOR));
		signupPanel.add(btnSignup);
		
		JLabel lblNewLabel_1 = new JLabel("Already signed up?");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblNewLabel_1.setBounds(1083, 653, 130, 33);
		signupPanel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Login here");
		lblNewLabel_1_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				loginPanel.setVisible(true);
				signupPanel.setVisible(false);
				lblSignupAlert.setText(null);
				signup_userField.setText(null);
				signup_passwordField.setText(null);
			}
		});
		lblNewLabel_1_1.setForeground(new Color(0, 51, 102));
		lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1_1.setBounds(1223, 653, 74, 33);
		lblNewLabel_1_1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		signupPanel.add(lblNewLabel_1_1);	
		
		JLabel signupBg = new JLabel("");
		signupBg.setIcon(new ImageIcon(POS.class.getResource("/img/ts-signup-bg.png")));
		signupBg.setBounds(0, 0, 1520, 825);
		signupPanel.add(signupBg);
		
			
		
		//****************************************************Login Panel****************************************************//
		login_passwordField = new JPasswordField();
		login_passwordField.setFont(new Font("Tahoma", Font.PLAIN, 20));
		login_passwordField.setBounds(957, 486, 475, 47);
		login_passwordField.setBorder(BorderFactory.createCompoundBorder(
				login_passwordField.getBorder(), 
		        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		loginPanel.add(login_passwordField);
		
		login_userField = new JTextField();
		login_userField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		login_userField.setBounds(957, 364, 475, 47);
		login_userField.setBorder(BorderFactory.createCompoundBorder(
				login_userField.getBorder(), 
		        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		loginPanel.add(login_userField);
		login_userField.setColumns(10);	
		
		JLabel lblLoginAlert = new JLabel("");
		lblLoginAlert.setHorizontalAlignment(SwingConstants.CENTER);
		lblLoginAlert.setForeground(new Color(0, 100, 0));
		lblLoginAlert.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblLoginAlert.setBounds(957, 544, 475, 25);
		loginPanel.add(lblLoginAlert);
		
		JButton btnLogin = new JButton("");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				String usernameLogin = login_userField.getText();
				String passwordLogin = login_passwordField.getText(); 
				
				user.setUsername(usernameLogin);
				user.setPassword(passwordLogin);
					
				if (pos.userExists(user.getUsername())) {
					if(pos.loginValidate(user.getUsername(), user.getPassword())) {						
						lblLoginAlert.setForeground(new Color(0, 100, 0));
						lblLoginAlert.setText("Login successful.");
						logged = 1;
						JOptionPane.showMessageDialog(null, "Login Successful", "Tagay Station POS", JOptionPane.INFORMATION_MESSAGE);
						menuPanel.setVisible(true);
						loginPanel.setVisible(false);
						login_userField.setText(null);
						login_passwordField.setText(null);
						lblLoginAlert.setText(null);
						currentUser = user.getUsername();
						lblCurrentUser.setText(currentUser);
					} else {
						lblLoginAlert.setForeground(Color.RED);
						lblLoginAlert.setText("Incorrect password.");
					}						
				}										
				else {
					lblLoginAlert.setForeground(Color.RED);
					lblLoginAlert.setText("User not found.");	
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				btnLogin.setIcon(new ImageIcon(POS.class.getResource("/img/btn_login-clicked.png")));
			}		
			
			public void mouseExited(MouseEvent e) {
				btnLogin.setIcon(new ImageIcon(POS.class.getResource("/img/btn_login2.png")));
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				btnLogin.setIcon(new ImageIcon(POS.class.getResource("/img/btn_login2.png")));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				btnLogin.setIcon(new ImageIcon(POS.class.getResource("/img/btn_login-clicked.png")));
			}
		});
		btnLogin.setIcon(new ImageIcon(POS.class.getResource("/img/btn_login2.png")));
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 20));
		btnLogin.setBounds(956, 581, 478, 47);
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loginPanel.add(btnLogin);
		
		JLabel lblNewLabel_1_2 = new JLabel("Not yet signed up?");
		lblNewLabel_1_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblNewLabel_1_2.setBounds(1074, 653, 130, 33);
		loginPanel.add(lblNewLabel_1_2);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Sign up here");
		lblNewLabel_1_1_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				signupPanel.setVisible(true);
				loginPanel.setVisible(false);
				lblLoginAlert.setText(null);
				login_userField.setText(null);
				login_passwordField.setText(null);
			}
		});
		lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1.setForeground(new Color(0, 51, 102));
		lblNewLabel_1_1_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1_1_1.setBounds(1214, 653, 95, 33);
		lblNewLabel_1_1_1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loginPanel.add(lblNewLabel_1_1_1);
		
		JLabel loginBg = new JLabel("");
		loginBg.setIcon(new ImageIcon(POS.class.getResource("/img/ts-login-bg.png")));
		loginBg.setBounds(0, 0, 1520, 825);
		loginPanel.add(loginBg);		
	}
}