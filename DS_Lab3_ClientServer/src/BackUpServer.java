import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class BackUpServer {
	
	JFrame frame;
	private static JLabel onlineUserLable;
	public static JTextArea serverConsole;
	public static JTextArea onlineUsers;
	private ServerSocket serverSocket;
	static HashSet<String> onlineUserSet = new HashSet<String>();
	private static DataOutputStream cOutStream;
    private static DataInputStream dataInputStream;
    private static HashSet<String> dict = new HashSet<String>();
    private static ConcurrentLinkedQueue<String> serverQ = new ConcurrentLinkedQueue<String>(); // message queue
    
    boolean serverOnline = true;

	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					BackUpServer window = new BackUpServer();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		loadDictonary();
		
		TimerTask task = new TimerTask() {// This thread will update the lexicon in every 60 seconds
		      @Override
		      public void run() {
		        
		            try {
		                
		                File f1 = new File("BackUpLexicon.txt");
		                if(!f1.exists()) {
		                   f1.createNewFile();
		                }

		                FileWriter fileWritter = new FileWriter(f1.getName(),true);
		                PrintWriter out = new PrintWriter(fileWritter);
		                
		                while(!serverQ.isEmpty()) {
		                	
		                	String s =  serverQ.poll();
		                	
		                	if(!dict.contains(s)) {
		                		
		                		dict.add(s);
		                		out.println(s);
		                		
		                	}
		                	
		                	
		                } 
		                out.close();
		               serverConsole.append("Backup Lexicon Updated.....\n"); 
		               //System.out.println("Lexicon Updated......");
		             } catch(IOException e){
		                e.printStackTrace();
		             }

		        	
		       
		      }
		    };
		    Timer timer = new Timer();
		    long delay = 1*1000;
		    long intevalPeriod = 60 * 1000; 
		    // schedules the task to be run in an interval 
		    timer.scheduleAtFixedRate(task, delay,
		                                intevalPeriod);
		    //serverConsole.append("Lexicon Updated\n");
	
	}



	private static void loadDictonary() throws Exception { //Loading Dictionary (loading all the words form loacal file in to HashSet)
		
		File lexicon = new File("BackUpLexicon.txt"); 
		if(!lexicon.exists()) {
			
			lexicon.createNewFile();
			
		}
		Scanner sc = new Scanner(lexicon); 
			  
		while (sc.hasNextLine()) {
			    	
			dict.add(sc.nextLine().toLowerCase());
			    	
			} 
			      
		sc.close();
	}



	/**
	 * Create the application.
	 */
	public BackUpServer() {
		initialize();
		
		try {
			serverSocket = new ServerSocket(9999);
			serverConsole.append("Backup Server Online!\n");
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
		DataOutputStream dos;
		DataInputStream dis;
		private Communicate(Socket s, String uname) { // socket and username will be provided by client
			this.client = s;
			this.Id = uname;
		}

		@Override
		public void run() {
			
	
			 
				try {
					
					
					while(!onlineUserSet.isEmpty() && onlineUserSet.contains(Id)){
						dos = new DataOutputStream(client.getOutputStream());
						dis = new DataInputStream(client.getInputStream());// read message from client
					
						String command = dis.readUTF();// Fetching Command from the User What he want to do
						
						if (command.equals("QUITPROCESS")) { // if a client's press disconnect
							onlineUserSet.remove(Id); // remove that client from active usee set
							serverConsole.append(Id + " disconnected....\n"); // print message on serverConsole
							updateOnlineUsersListView();
	
							}
						
						//serverConsole.append("Quit Checked.............. \n");
						if (command.equals("SENDINGFILE")){
								
								//serverConsole.append(" Waiting for the file.........\n");
								String receviedFileName = "ReceivedFile"+Id+".txt";  // filename for received file
								receiveFile(receviedFileName);

																
							}
						
						
						if(command.equals("MSGING")) { // receives messages
							
							String msgReceived = dis.readUTF();
							
							String[] strArray = msgReceived.split("\\s");
							
							for(int i=0;i<strArray.length;i++) {
								
								serverQ.offer(strArray[i]);
								
							}
							
							serverConsole.append(Id +" says "+msgReceived+"\n");
							
						}
						
						if(command.equals("stilOnline")) { // notify client that server is online
							
							
							dos.writeBoolean(serverOnline);
							
						}
						
						}
					
					dis.close();
					dos.close();
					
				} catch (Exception e) {
					
					
					onlineUserSet.remove(Id); // remove that client from active usee set
					serverConsole.append(Id +"  went Offline!"+"\n"); // print message on serverConsole
					updateOnlineUsersListView();
					
//					e.printStackTrace();
				}
			
			
		}

		private void checkSpell(String receviedFileName, String updatedFileName) throws Exception {// checking all the words form file if they exist in the dictionary
		
			
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
			br.close();
			if(file.delete()) {
				System.out.println("Deleted Receiving File...................\n");
			}else {
				System.out.println("Could Not Delete Receiving File...................\n");
			}
			
		}

		private String arrayToString(String[] stringArray) {  // converting ArrayOfStrings into a long string
			
			StringBuffer sf = new StringBuffer();
			for(String word: stringArray ) {
				
				sf.append(word);
				sf.append(" ");
				
			}
			return sf.toString();
		}
		
		

		private void receiveFile(String fileName) throws Exception { // receiving file from the client
	       
	       /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
	       serverConsole.append("Receiving File...................\n");
	       
	       long size = dis.readLong(); 
	       
	       byte[] b = new byte[(int)size];
	       FileOutputStream fileOutputStream = new FileOutputStream(fileName);
	       dis.read(b, 0, b.length);
	       //serverConsole.append(" Read Complete... \n");
	       fileOutputStream.write(b, 0, b.length);
	       fileOutputStream.close();
	       serverConsole.append(Id+" : File received\n");
	       String updatedFileName = Id+"spellCheckedFileFromBackUpServer.txt";
	       
	       checkSpell(fileName,updatedFileName);	
	       serverConsole.append(Id+"'s File updated\n");
	       serverConsole.append(Id+" check new file Named "+ updatedFileName+"\n");
	       
	       
	       
	       /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

	       

	       
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Backup Server");
		frame.getContentPane().setLayout(null);
		
		onlineUserLable = new JLabel("Online Users:");
		onlineUserLable.setBounds(48, 55, 91, 14);
		frame.getContentPane().add(onlineUserLable);
		
		serverConsole = new JTextArea();
		serverConsole.setBounds(174, 84, 377, 272);
		serverConsole.setEditable(false);
		JScrollPane scroll = new JScrollPane ( serverConsole );
		scroll.setBounds(174, 84, 377, 272);
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		frame.getContentPane().add(scroll);
		//frame.getContentPane().add(serverConsole);
		
		onlineUsers = new JTextArea();
		onlineUsers.setBounds(48, 84, 103, 272);
		frame.getContentPane().add(onlineUsers);
	}
	
	private void updateOnlineUsersListView() { // update list of online user on server console
		
		
		BackUpServer.onlineUsers.setText(null);
		if(!onlineUserSet.isEmpty()) {
			
			for(String name:onlineUserSet) {
				
				BackUpServer.onlineUsers.append(name+"\n");
				
			}
			
		}
		
	}
	
	
	

}
