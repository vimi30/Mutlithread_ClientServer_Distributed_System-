/*
 * references:
 * https://github.com/ankitrathore25/multi-clients-chat-application/blob/master/src/phase1/ClientView.java
 * https://medium.com/@HeptaDecane/file-transfer-via-java-sockets-e8d4f30703a5
 * https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
 * https://stackoverflow.com/questions/17622324/bufferedreader-then-write-to-txt-file
 * 
 * */import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
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
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientGUITest {

	private JFrame frame;
	private JTextArea clientConsole;
	private JTextField userName;
	private JTextField enterMsg;
	private static int port = 8818;
	private static JButton fcButton;
	private static JButton clientDisconnetButton;
	//private static JButton connectButton;
	private static JButton sendButton;
	DataInputStream inStream;
	DataOutputStream outStream;
	String filePath;
	Socket s;
	
	String userClientName;
	
	volatile static Set<String> userNames = new HashSet();
	
	

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
			userName.setText(userClientName);
			userName.setEditable(false);
			enterMsg.setEditable(false);
			outStream = new DataOutputStream(s.getOutputStream());
			inStream = new DataInputStream(s.getInputStream()); // initilize input and output stream
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
				
				try {
					sendFile(filePath);
					inStream.close();
					outStream.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			private void sendFile(String filePath) throws Exception {
				// TODO Auto-generated method stub
				
				
				
				
		        int bytes = 0;
		        File file = new File(filePath);
		        FileInputStream fileInputStream = new FileInputStream(file);
		        
		        // send file size
		        outStream.writeLong(file.length());  
		        // break file into chunks
		        byte[] buffer = new byte[4*1024];
		        while ((bytes=fileInputStream.read(buffer))!=-1){
		        	outStream.write(buffer,0,bytes);
		        	outStream.flush();
		        }
		        fileInputStream.close();
		        
		        clientConsole.append("File sent: "+file.getName()+"\n");
		        
				
				
			}
		});
		sendButton.setBounds(332, 214, 66, 23);
		sendButton.setVisible(true);
		frame.getContentPane().add(sendButton);
		
		clientConsole = new JTextArea();
		clientConsole.setEditable(false);
		clientConsole.setBounds(33, 73, 368, 130);
		clientConsole.setVisible(true);
		frame.getContentPane().add(clientConsole);
		
		JLabel userNameLable = new JLabel("User Name:");
		userNameLable.setBounds(33, 32, 96, 30);
		frame.getContentPane().add(userNameLable);
		
		userName = new JTextField();
		userName.setBounds(113, 37, 101, 20);
		frame.getContentPane().add(userName);
		userName.setColumns(10);
		
	
		
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
		fcButton.setVisible(true);
		frame.getContentPane().add(fcButton);
		
		clientDisconnetButton = new JButton("Disconnect");
		clientDisconnetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					
					outStream.writeUTF("QUITPROCESS"); // closes the thread and show the message on server and client's message
												// board
					clientConsole.append("You are disconnected now.\n");
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
