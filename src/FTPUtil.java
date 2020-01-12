import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPUtil {
	public static void uploadDirectory(FTPClient ftpClient,
	        String remoteDirPath, String localParentDir, String remoteParentDir)
	        throws IOException {
		 
		System.out.println("LISTING directory: " + localParentDir);
		 
	    File localDir = new File(localParentDir);
	    File[] subFiles = localDir.listFiles();
	    if (subFiles != null && subFiles.length > 0) {
	        for (File item : subFiles) {
	            String remoteFilePath = remoteDirPath + "/" + remoteParentDir
	                    + "/" + item.getName();
	            if (remoteParentDir.equals("")) {
	                remoteFilePath = remoteDirPath + "/" + item.getName();
	            }
	 
	 
	            if (item.isFile()) {
	                // upload the file
	                String localFilePath = item.getAbsolutePath();
	                System.out.println("About to upload the file: " + localFilePath);
	                System.out.println("remote to upload the file: " + remoteFilePath);
	               /* boolean uploaded = uploadSingleFile(ftpClient,
	                        localFilePath, item.getName());*/
	                
	                boolean uploaded = uploadSingleFile(ftpClient,
	                        localFilePath, remoteFilePath);
	                
	                System.out.println(">>>>>>>>>"+uploaded);
	                if (uploaded) {
	                    System.out.println("UPLOADED a file to: "
	                            + remoteFilePath);
	                } else {
	                    System.out.println("COULD NOT upload the file: "
	                            + localFilePath);
	                }
	            } else {
	                // create directory on the server
	                boolean created = ftpClient.makeDirectory(remoteFilePath);
	                if (created) {
	                    System.out.println("CREATED the directory: "
	                            + remoteFilePath);
	                } else {
	                    System.out.println("COULD NOT create the directory: "
	                            + remoteFilePath);
	                }
	 
	                // upload the sub directory
	                String parent = remoteParentDir + "/" + item.getName();
	                if (remoteParentDir.equals("")) {
	                    parent = item.getName();
	                }
	 
	                localParentDir = item.getAbsolutePath();
	                uploadDirectory(ftpClient, remoteDirPath, localParentDir,
	                        parent);
	            }
	        }
	    }
 
    }
 
	public static boolean uploadSingleFile(FTPClient ftpClient,
	        String localFilePath, String remoteFilePath) throws IOException {
	    File localFile = new File(localFilePath);
	    boolean status=false;
	    boolean done=false;
	 
	    InputStream inputStream = new FileInputStream(localFile);
	    try {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        
	        
			String firstRemoteFile = remoteFilePath;
			
			System.out.println("Start uploading first file");
			done = ftpClient.storeFile(firstRemoteFile, inputStream);
			
	        
			inputStream.close();
			if (done) {
				System.out.println("The first file is uploaded successfully.");
			}
	        
	        
	        
	        
	        
	        
	       /*ftpClient.enterLocalPassiveMode();
			ftpClient.setControlEncoding("GBK");

			
			int replyCode = ftpClient.getReplyCode();
			System.err.println("?????"+replyCode);
	        status=ftpClient.storeFile(remoteFilePath, inputStream);*/
	    } 
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    
	    finally {
	        inputStream.close();
	    }
	    
	    return status;
	    
	}
	

}
