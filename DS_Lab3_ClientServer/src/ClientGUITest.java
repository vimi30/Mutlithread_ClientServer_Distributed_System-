/*Vimlendra Bouddh
 * ID: 1001753108
 * references:
 * https://github.com/ankitrathore25/multi-clients-chat-application/blob/master/src/phase1/ClientView.java
 * https://medium.com/@HeptaDecane/file-transfer-via-java-sockets-e8d4f30703a5
 * https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
 * https://stackoverflow.com/questions/17622324/bufferedreader-then-write-to-txt-file
 * https://www.geeksforgeeks.org/java-swing-jtogglebutton-class/
 * http://www.beginwithjava.com/java/file-input-output/writing-text-file.html
 * https://stackoverflow.com/questions/10240694/java-socket-api-how-to-tell-if-a-connection-has-been-closed
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
import java.net.SocketException;
import java.net.UnknownHostException;
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
	private static int secondPort = 9999;
	
	
	public boolean isMainServerOnline = true;
	public boolean isBackUpServerOnline = false;
	
	DataInputStream inStream;
	DataOutputStream outStream;
	
	DataInputStream secondInStream,defalutInputStream;
	DataOutputStream secondOutStream,defaultOutputStream;
	
	String filePath;
	private Socket s;
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
			this.outStream = new DataOutputStream(s.getOutputStream()); // initilize output stream
			this.inStream = new DataInputStream(s.getInputStream()); // initilize input stream
			
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
		frame.setTitle("Main Server");
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
					
				checkIfServerOnline();  // Check which server is online 
				
				setInputOutputStreams();  // set input and output Streams according to the online server
					
				
				
				if(mode.equals("SENDINGFILE")) {
					
					try {
						
						defaultOutputStream.writeUTF(mode); // send which function to perform on the server side {receive the incoming file} 
						sendFile(filePath);
						enterMsg.setText("");
						
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					
				}
				
				if(mode.equals("MSGING")) {
					
					String msg = enterMsg.getText().trim();
					
					sendMsg(msg);
					
					enterMsg.setText("");
					
					
				}
				
				
			}

			private void sendMsg(String msg) { // this function is called when client sends a message (Words)
				
				
//				clientConsole.append("Sending msg to port: "+s.getPort()+"\n");
				try {
					
					defaultOutputStream.writeUTF(mode);
						
					defaultOutputStream.writeUTF(msg);
									
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}

			private void sendFile(String filePath) throws Exception { // this function is called when client wants to send a text file

/*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		        
				clientConsole.append("Sending file............... \n");
				
				File file = new File(filePath);
				byte[] b = new byte[(int)file.length()];
				defaultOutputStream.writeLong(file.length());
		        FileInputStream fis = new FileInputStream(file);
				fis.read(b, 0, b.length);
				defaultOutputStream.write(b, 0, b.length);
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
	                
	                if(selected) {  // set mode : means if a client want to send a file or just message
	                	
	                	mode = "SENDINGFILE";
	                	fcButton.setVisible(true);
	                	enterMsg.setEditable(false);
	                	
	                }else {
	                	
	                	mode = "MSGING";
	                	fcButton.setVisible(false);
	                	enterMsg.setEditable(true);
	                	
	                }
	      
//	                System.out.println("Action - selected=" + mode + "\n");
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
					
					checkIfServerOnline(); // check which server is online
					setInputOutputStreams(); // set input and output stream accordingly
					disconnectClient(); // disconnects the client( close the input/output streams )
					frame.dispose();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		});
		clientDisconnetButton.setBounds(312, 36, 89, 23);
		clientDisconnetButton.setVisible(true);
		frame.getContentPane().add(clientDisconnetButton);
	}

	public void setInputOutputStreams() {  // this function sets the input and output streams according to the server that is online
		
		if(isBackUpServerOnline == true) {
			
			defalutInputStream = secondInStream;
			defaultOutputStream = secondOutStream;
			
		}else {
			
			defalutInputStream = inStream;
			
			defaultOutputStream = outStream;
			
		}
		
	}

	public void disconnectClient() throws IOException { // this function disconnect the client from the server
		
		defaultOutputStream.writeUTF("QUITPROCESS"); // closes the thread and show the message on server and client's message
//		clientConsole.append("You are disconnected now.\n");
		defalutInputStream.close();
		defaultOutputStream.close();
		
	}

	public void checkIfServerOnline() {  //this function check if the main server is online, if not, it connects the client to backup server
		
		try {
			
			
			if(isMainServerOnline==true && isBackUpServerOnline==false) {
				
				outStream.writeUTF("stilOnline");
				
				isMainServerOnline = inStream.readBoolean();
				
				
			}
			

		} catch (SocketException e) {
//			clientConsole.append("Main Server went offline\n");
			
			clientConsole.append("Main Server Crashed\n");
			clientConsole.append("Connecting to the Back-up Server\n");
			
			try {
				this.s = new Socket("localhost",secondPort);
				secondOutStream = new DataOutputStream(s.getOutputStream()); // initilize output stream
				secondInStream = new DataInputStream(s.getInputStream()); // initilize input stream
				secondOutStream.writeUTF(userClientName);
				
				isBackUpServerOnline = true;
				
				clientConsole.append("Connection Successful..!\n");
			} catch (UnknownHostException e1) {
				System.out.println("Server Crashed");
			} catch (IOException e1) {
				
			}

		} catch (IOException e) {
			
		
		}
		
		
	}
}
