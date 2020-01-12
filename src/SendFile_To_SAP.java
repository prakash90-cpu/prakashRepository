import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

@WebServlet("/sendFile")
public class SendFile_To_SAP extends HttpServlet {
	

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	
		InputStream in = getClass().getResourceAsStream("PATH.properties");
		Properties CONFIG_PATH = new Properties();
		CONFIG_PATH.load(in);

		  FileOutputStream fos = null;
		  Boolean hasDuplicate = false;
		
	/*
	 public static void main(String[] args) {*/
		  //Production
		 String server = "192.168.81.19";
		 
		 //UAT
		 //String server = "172.29.8.86";
		 
			//String server = "localhost";
			int port = 21;
			String user = "sftpuser";
			String pass = "sftpuser";
	 
	        FTPClient ftpClient = new FTPClient();
	        String sys = System.getProperty("os.name");
	        String localDirPath="";
	        try {
	            // connect and login to the server
	            ftpClient.connect(server, port);
	            ftpClient.login(user, pass);
	 
	            // use local passive mode to pass firewall
	            ftpClient.enterLocalPassiveMode();
	 
	            System.out.println("Connected");
	 
	            String remoteDirPath = "/oracle/LED/OTHER_BRS/in";
	            String archivePath="/oracle/LED/OTHER_BRS/archive";
	            
	            //For windows machine
	           // String localDirPath = "C:/MT940_output";
	            
	            
	            //For Linux machine
		           //String localDirPath=CONFIG_PATH.getProperty("LINO");
		            //String localDirPath="/opt/PHO/MT940_output";
		            
		            if (sys.contains("Win")){
		            	localDirPath=CONFIG_PATH.getProperty("WINO");
		            }else{
		            	localDirPath=CONFIG_PATH.getProperty("LINO");
		            }
	            
	            File folder = new File(localDirPath);
	            String[] outputFolderFiles=folder.list();
	            
               for(int i=0;i<outputFolderFiles.length;i++){
            	   fos = new FileOutputStream(outputFolderFiles[i]);
            	 boolean duplicate=ftpClient.retrieveFile(remoteDirPath+"/" + outputFolderFiles[i], fos);
            	 boolean archive=ftpClient.retrieveFile(archivePath+"/" + outputFolderFiles[i], fos);
            	 
            	 
            	 
            	 if(duplicate || archive){
            		 System.out.println("file is duplicate");
            		 hasDuplicate = true;
            		 /*RequestDispatcher reqdisp=  request.getRequestDispatcher("ErrorFile.html");
           	         reqdisp.forward(request, response);*/
            		 
            	 }
            	
            	 else{
            		 //FTPUtil.uploadDirectory(ftpClient, remoteDirPath, localDirPath, "");
            		 FTPUtil.uploadSingleFile(ftpClient, localDirPath+java.io.File.separator+outputFolderFiles[i], remoteDirPath+"/"+outputFolderFiles[i]);
            		 /*RequestDispatcher reqdisp=  request.getRequestDispatcher("SapSuccess.jsp");
           	      reqdisp.forward(request, response);*/
            	 }
            	   
               }
               
	            if(hasDuplicate){
	            	RequestDispatcher reqdisp=  request.getRequestDispatcher("ErrorFile.html");
          	         reqdisp.forward(request, response);
	            }else{
	            	RequestDispatcher reqdisp=  request.getRequestDispatcher("SapSuccess.jsp");
	           	      reqdisp.forward(request, response);
	            }
	          
	            
	           
	 
	           
	 
	            // log out and disconnect from the server
	            ftpClient.logout();
	            ftpClient.disconnect();
	 
	            System.out.println("Disconnected");
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	        
	     
	        
	        
	    }
	
}
