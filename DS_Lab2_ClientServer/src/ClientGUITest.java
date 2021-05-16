/*
 * references:
 * https://github.com/ankitrathore25/multi-clients-chat-application/blob/master/src/phase1/ClientView.java
 * https://medium.com/@HeptaDecane/file-transfer-via-java-sockets-e8d4f30703a5
 * https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
 * https://stackoverflow.com/questions/17622324/bufferedreader-then-write-to-txt-file
 * https://www.geeksforgeeks.org/java-swing-jtogglebutton-class/
 * http://www.beginwithjava.com/java/file-input-output/writing-text-file.html
 * 
 * */import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JList;
import javax.swing.JFileChooser;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientGUITest {

	private JFrame frame;
	private JTextArea clientConsole;
	//private JTextField userName;
	private JTextField enterMsg;
	private static JButton fcButton;
	private static JButton clientDisconnetButton;
	//private static JButton connectButton;
	private static JButton sendButton;
	DataInputStream inStream;
	DataOutputStream outStream;
	String filePath;
	Socket s;
	String mode = "MSGING";
	String userClientName;
	

	/**
	 * Create the application.
	 */
	public ClientGUITest() {
		initialize();		
	}
	
	public ClientGUITest(String id, Socket s) {
		
		initialize();// initilize UI components
		
		
		this.userClientName = id;
		this.s  = s;
		
		try {
			frame.setTitle("Client View - " + userClientName); // set title of UI
			
			enterMsg.setEditable(true);
			outStream = new DataOutputStream(s.getOutputStream()); // initilize output stream
			inStream = new DataInputStream(s.getInputStream()); // initilize input stream
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(mode.equals("SENDINGFILE")) {
					
					try {
						sendFile(filePath);
						enterMsg.setText("");
//						inStream.close();
//						outStream.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				if(mode.equals("MSGING")) {
					
					
					String msg = enterMsg.getText().trim();
					
					sendMsg(msg);
					
					enterMsg.setText("");
					
					
				}
				
				
			}

			private void sendMsg(String msg) {
				try {
					
					outStream.writeUTF(mode);
					
					outStream.writeUTF(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			private void sendFile(String filePath) throws Exception {

/*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		        
				clientConsole.append("Sending file............... \n");
				
				File file = new File(filePath);
				byte[] b = new byte[(int)file.length()];
				outStream.writeLong(file.length());
		        FileInputStream fis = new FileInputStream(file);
				fis.read(b, 0, b.length);
				//clientConsole.append("Read Complete\n");
				// 04. Send file
				//OutputStream os = s.getOutputStream();
				outStream.write(b, 0, b.length);
				fis.close();
				
				clientConsole.append("File sent: "+file.getName()+"\n");
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------------*/		        
		   	
			}
		});
		sendButton.setBounds(332, 214, 66, 23);
		sendButton.setVisible(true);
		frame.getContentPane().add(sendButton);
		
		JToggleButton toggleButton = new JToggleButton("File");
		
		toggleButton.setBounds(33, 32, 96, 30);
		
		ActionListener actionListener = new ActionListener()
	        {
	  
	            // actionPerformed() method is invoked 
	            // automatically whenever you click on
	            // registered component
	            public void actionPerformed(ActionEvent actionEvent)
	            {
	      
	                AbstractButton abstractButton = 
	                (AbstractButton)actionEvent.getSource();
	      
	                // return true or false according
	                // to the selection or deselection
	                // of the button
	                boolean selected = abstractButton.getModel().isSelected();
	                
	                if(selected) {
	                	
	                	mode = "SENDINGFILE";
	                	fcButton.setVisible(true);
	                	enterMsg.setEditable(false);
	                	
	                }else {
	                	
	                	mode = "MSGING";
	                	fcButton.setVisible(false);
	                	enterMsg.setEditable(true);
	                	
	                }
	      
	                System.out.println("Action - selected=" + mode + "\n");
	            }
	          };
	          
	          toggleButton.addActionListener(actionListener);      
		
		frame.getContentPane().add(toggleButton);	
		clientConsole = new JTextArea();
		clientConsole.setEditable(false);
		clientConsole.setBounds(33, 73, 368, 130);
		clientConsole.setVisible(true);
		JScrollPane scroll = new JScrollPane ( clientConsole );
		scroll.setBounds(33, 73, 368, 130);
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		frame.getContentPane().add(scroll);
		
		enterMsg = new JTextField();
		enterMsg.setBounds(33, 215, 212, 20);
		enterMsg.setVisible(true);
		frame.getContentPane().add(enterMsg);
		enterMsg.setColumns(10);
		
		JList list = new JList();
		list.setBounds(165, 217, 1, 1);
		frame.getContentPane().add(list);
		
		fcButton = new JButton("select");
		fcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter ff = new FileNameExtensionFilter("TEXT FILES","txt","TEXT");
				fc.setFileFilter(ff);
				fc.showOpenDialog(frame);
				
				filePath = fc.getSelectedFile().getPath();
				
				try {
					outStream.writeUTF(mode);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				enterMsg.setText("Selected File: " + fc.getSelectedFile().getName()+"\n");
				
				
			}
		});
		fcButton.setBounds(255, 214, 75, 23);
		fcButton.setVisible(false);
		frame.getContentPane().add(fcButton);
		
		clientDisconnetButton = new JButton("Disconnect");
		clientDisconnetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					
					outStream.writeUTF("QUITPROCESS"); // closes the thread and show the message on server and client's message
												// board
					clientConsole.append("You are disconnected now.\n");
					inStream.close();
					outStream.close();
					s.close();
					frame.dispose();
					// close the frame 
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		});
		clientDisconnetButton.setBounds(312, 36, 89, 23);
		clientDisconnetButton.setVisible(true);
		frame.getContentPane().add(clientDisconnetButton);
	}
}
