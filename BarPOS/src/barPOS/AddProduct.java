package barPOS;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AddProduct extends JFrame {

	private JPanel contentPane;
	private JTextField nameText;
	private JTextField priceTextField;
	private JTextField qtyTextField;
	private JComboBox liquorComboBox;
	PosUtils pos = new PosUtils();
	Liquor liquor;
	Pulutan pulutan;
	
	boolean added;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddProduct frame = new AddProduct();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public AddProduct() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(POS.class.getResource("/img/icon-large.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 469, 544);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 453, 502);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JRadioButton pulutanRdBtn = new JRadioButton("Pulutan");
		pulutanRdBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				liquorComboBox.setEnabled(false);
				System.out.println("Pulutan selected");
			}
		});
		pulutanRdBtn.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		pulutanRdBtn.setBackground(Color.WHITE);
		pulutanRdBtn.setBounds(226, 94, 91, 23);
		panel.add(pulutanRdBtn);
		
		JRadioButton liquorRdBtn = new JRadioButton("Liquor");
		liquorRdBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				liquorComboBox.setEnabled(true);
				System.out.println("Liquor selected");
			}
		});
		liquorRdBtn.setBackground(Color.WHITE);
		liquorRdBtn.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		liquorRdBtn.setBounds(142, 94, 82, 23);
		panel.add(liquorRdBtn);
		
		ButtonGroup rd = new ButtonGroup();
		rd.add(liquorRdBtn);
		rd.add(pulutanRdBtn);
		
		nameText = new JTextField();
		nameText.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		nameText.setBounds(101, 159, 260, 33);
		panel.add(nameText);
		nameText.setColumns(10);
		
		priceTextField = new JTextField();
		priceTextField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		priceTextField.setColumns(10);
		priceTextField.setBounds(101, 311, 260, 33);
		panel.add(priceTextField);
		
		qtyTextField = new JTextField();
		qtyTextField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		qtyTextField.setColumns(10);
		qtyTextField.setBounds(101, 379, 260, 33);
		panel.add(qtyTextField);
		
		liquorComboBox = new JComboBox();
		liquorComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		liquorComboBox.setModel(new DefaultComboBoxModel(new String[] {"Vodka", "Whiskey", "Brandy", "Beer", "Rum", "Gin", "Tequila"}));
		liquorComboBox.setBounds(101, 241, 260, 33);
		panel.add(liquorComboBox);
		
		JButton btnAdd = new JButton("");
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String prodName = nameText.getText();
				String liquorCtg = liquorComboBox.getSelectedItem().toString();
				Double prodPrice = Double.parseDouble(priceTextField.getText());
				int prodQty = Integer.parseInt(qtyTextField.getText());
				String[] liqCateg = {"Vodka","Whiskey","Brandy","Beer","Rum","Gin","Tequila"};
				int ctgId=0;				
				
				for(int i=0; i<liqCateg.length;i++) {
					if(liquorCtg.equals(liqCateg[i])) {
						ctgId = i+1;
					}
				}
				
				if(liquorRdBtn.isSelected()) {
					liquor = new Liquor(prodName, prodPrice, prodQty, ctgId);	
					
					added = pos.addLiquor(liquor);
					
					if(added) {				
						dispose();
					}		
				} else {
					pulutan = new Pulutan(prodName, prodPrice, prodQty);
					
					added = pos.addPulutan(pulutan);
					
					if(added) {
						dispose();
					}
				}
				
				rd.clearSelection();
				nameText.setText("");
				liquorComboBox.setSelectedIndex(0);
				priceTextField.setText("");
				qtyTextField.setText("");
				
			}
		});
		btnAdd.setIcon(new ImageIcon(AddProduct.class.getResource("/img/ts-addProduct-addBtn.png")));
		btnAdd.setBounds(243, 436, 100, 34);
		panel.add(btnAdd);
		
		JButton btnCancel = new JButton("");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
		btnCancel.setIcon(new ImageIcon(AddProduct.class.getResource("/img/ts-addProduct-cancelBtn.png")));
		btnCancel.setBounds(124, 436, 100, 34);
		panel.add(btnCancel);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(AddProduct.class.getResource("/img/ts-addProduct-bg2.png")));
		lblNewLabel.setBounds(0, 0, 453, 520);
		panel.add(lblNewLabel);
	}
}
