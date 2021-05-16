/*
 * References:
 * https://medium.com/@HeptaDecane/file-transfer-via-java-sockets-e8d4f30703a5
 * https://github.com/ankitrathore25/multi-clients-chat-application/blob/master/src/phase1/ServerView.java
 * https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
 * https://stackoverflow.com/questions/17622324/bufferedreader-then-write-to-txt-file
 * 
 * */
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Server {

	private JFrame frame;
	private static JLabel onlineUserLable;
	public static JTextArea serverConsole;
	public static JTextArea onlineUsers;
	private ServerSocket serverSocket;
	static HashSet<String> onlineUserSet = new HashSet<String>();
	private static DataOutputStream cOutStream;
    private static DataInputStream dataInputStream;
    private static HashSet<String> dict = new HashSet<String>();

	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					Server window = new Server();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		loadDictonary();
	
	}



	private static void loadDictonary() throws Exception { //Loading Dictionary (loading all the words form loacal file in to HashSet)
		// TODO Auto-generated method stub
		File words = 
			      new File("words.txt"); 
			    Scanner sc = new Scanner(words); 
			  
			    while (sc.hasNextLine()) 
			      dict.add(sc.nextLine().toLowerCase()); 
	}



	/**
	 * Create the application.
	 */
	public Server() {
		initialize();
		
		try {
			serverSocket = new ServerSocket(8818);
			serverConsole.append("Server Online!\n");
			serverConsole.append("Waiting for Clients!\n");
			new ClientAccept().start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class ClientAccept extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();  // create a socket for client
					
					cOutStream = new DataOutputStream(clientSocket.getOutputStream());
					dataInputStream = new DataInputStream(clientSocket.getInputStream()); // this will receive the username sent from client register view
					String uName = dataInputStream.readUTF();
					 // create an output stream for client
					
					if ( onlineUserSet!= null && onlineUserSet.contains(uName)) { // if username is in use then we need to prompt user to enter new name
						cOutStream.writeUTF("Username already Exist");
					} else {
						
						onlineUserSet.add(uName);
						cOutStream.writeUTF("");// clear the existing message
						
						
						
						serverConsole.append(uName + " Connected...\n");// print message on server that new client has been connected.
						updateOnlineUsersListView();
						
						new Communicate(clientSocket,uName).start();
						
					}
				} catch (IOException ioex) {  // throw any exception occurs
					ioex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Communicate extends Thread { // this class reads the messages coming from client and take appropriate actions
		Socket client;
		String Id;
		private Communicate(Socket s, String uname) { // socket and username will be provided by client
			this.client = s;
			this.Id = uname;
		}

		@Override
		public void run() {
			 
				try {
					while(!onlineUserSet.isEmpty() && onlineUserSet.contains(Id)){
					cOutStream = new DataOutputStream(client.getOutputStream());
					dataInputStream = new DataInputStream(client.getInputStream());// read message from client
					
						if (dataInputStream !=null && dataInputStream.readUTF().equals("QUITPROCESS")) { // if a client's process is killed then notify on the server
							onlineUserSet.remove(Id); // remove that client from active usre set
							serverConsole.append(Id + " disconnected....\n"); // print message on server message board
							updateOnlineUsersListView();
	
							}else {
								
								String receviedFileName = "ReceivedFile"+Id+".txt";  // filename for received file
								receiveFile(receviedFileName);
								dataInputStream.close();
								cOutStream.close();
																
							}
						}
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}

		private void checkSpell(String receviedFileName, String updatedFileName) throws Exception {// checking all the words form file if they exist in the dictionary
			// TODO Auto-generated method stub
			
			File file = new File(receviedFileName); // getting the file received from client into file object 
			BufferedWriter bw = new  BufferedWriter(new FileWriter(updatedFileName));//
			BufferedReader br = new BufferedReader(new FileReader(receviedFileName)); 
			  
			String st; 
			while ((st = br.readLine()) != null) { 
				String stringArray[] = st.split("\\s");
				for(int i=0;i<stringArray.length;i++) {
					
					if(!dict.contains(stringArray[i].toLowerCase())) {
						
						stringArray[i] = "["+stringArray[i]+"]";   // putting [] around the words in the file that are not present in the dictionary
						
					}
				}
				
				bw.write(arrayToString(stringArray));
				
			} 
			
			bw.close();
			
		}

		private String arrayToString(String[] stringArray) {  // converting ArrayOfStrings into a long string
			// TODO Auto-generated method stub
			StringBuffer sf = new StringBuffer();
			for(String word: stringArray ) {
				
				sf.append(word);
				sf.append(" ");
				
			}
			return sf.toString();
		}

		private void receiveFile(String fileName) throws Exception { // receiving file from the client
			// TODO Auto-generated method stub
			
			
			int bytes = 0;
	        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
	        
	        long size = dataInputStream.readLong();// read file size
	        //serverConsole.append("file Size = " + size+"\n");
	        byte[] buffer = new byte[4*1024];
	        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
	            fileOutputStream.write(buffer,0,bytes);
	            size  = size - bytes;      // read upto file size
	        }
	       fileOutputStream.close();
	       
	       String updatedFileName = Id+"spellCheckedFile.txt";
	       serverConsole.append(Id+" : File received\n");
	       checkSpell(fileName,updatedFileName);	
	       serverConsole.append(Id+"'s File updated check new fileName "+ updatedFileName+"\n");
	       
	       		/*bytes = 0;
		        File file = new File("E:/Eclipse/eclipse-workspace/DS_Lab1_ClientServer/"+updatedFileName);
		       // serverConsole.append("file name found: "+ file.getName()+"\n");
		        FileInputStream fileInputStream = new FileInputStream(file);
		        //serverConsole.append("new File InputStream generated \n");
		        
	       

	       
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		onlineUserLable = new JLabel("Online Users:");
		onlineUserLable.setBounds(48, 55, 91, 14);
		frame.getContentPane().add(onlineUserLable);
		
		serverConsole = new JTextArea();
		serverConsole.setBounds(174, 84, 377, 272);
		frame.getContentPane().add(serverConsole);
		
		onlineUsers = new JTextArea();
		onlineUsers.setBounds(48, 84, 103, 272);
		frame.getContentPane().add(onlineUsers);
	}
	
	private void updateOnlineUsersListView() { // update list of online user on server console
		// TODO Auto-generated method stub
		
		Server.onlineUsers.setText(null);
		if(!onlineUserSet.isEmpty()) {
			
			for(String name:onlineUserSet) {
				
				Server.onlineUsers.append(name+"\n");
				
			}
			
		}
		
	}

}
