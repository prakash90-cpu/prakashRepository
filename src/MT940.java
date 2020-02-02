import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import org.apache.log4j.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Servlet implementation class MT940_PARSER
 */
// Removed All commented code for better analyze on 08Jan2016. Backup is created before removing
// comments in same with file name MT940_BKP_08Jan2016
public class MT940 extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MT940()
	{
		super();

	}

	private String BANK_NAME = "";
	Properties CONFIG_PROP = null;
	String MT940_CONTENT = new String();
	private String UPLOAD_DIRECTORY = new String();
	private String DOWNLOAD_FILE = new String();
	String FILE_NAME = new String();
	MT940 oMT940 = null;
	// FOR ZIP
	String OUTPUT_ZIP_FILE = null;
	String SOURCE_FOLDER = null;
	List<String> fileList;
	String timeStamp, accNo;
	int i, accountCount, STATEMENT_NUMBER;
	File subDirName = null;
	Logger logger = null;
	
	

	public List<File> uploadedFileList = new ArrayList<File>();

	public int[] searchWorkBook(Sheet sheet, String propertyName)
	{

		int cellIndex[] = { -1, -1 };
		try
		{
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();

			while (rowIterator.hasNext())
			{

				Row row = rowIterator.next();

				Iterator<Cell> cell = row.iterator();

				while (cell.hasNext())
				{

					Cell rowCell = cell.next();
					
					
					
					
				//	System.out.println(Cell.CELL_TYPE_STRING);

					if (rowCell.getCellType() == Cell.CELL_TYPE_STRING)
					{

						if (rowCell.getStringCellValue().contains(CONFIG_PROP.getProperty(propertyName)))
						{

							cellIndex[0] = rowCell.getRowIndex();
							cellIndex[1] = rowCell.getColumnIndex();
						}
					}
					
					

				}

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return cellIndex;
	}

	public int[] searchWorkBook(XSSFSheet sheet, String propertyName)
	{

		int cellIndex[] = { -1, -1 };
		try
		{
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();

			while (rowIterator.hasNext())
			{

				Row row = rowIterator.next();

				Iterator<Cell> cell = row.iterator();

				while (cell.hasNext())
				{

					Cell rowCell = cell.next();

					if (rowCell.getCellType() == Cell.CELL_TYPE_STRING)
					{

						if (rowCell.getStringCellValue().contains(CONFIG_PROP.getProperty(propertyName)))
						{

							cellIndex[0] = rowCell.getRowIndex();
							cellIndex[1] = rowCell.getColumnIndex();
						}
					}

				}

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.info("Exception occured at searchWorkBook() :" + e);
		}

		return cellIndex;
	}

	public void generateMT940(String BANK_NAME)
	{

		MT940_CONTENT = new String();

		CONFIG_PROP = new Properties();

		File BANK_STATEMENT = null;
		InputStream in = null;

		BANK_STATEMENT = new File(subDirName + File.separator + FILE_NAME);
		String OutputFileName = "";
		
		Cell Statement_Date;
		String temp2="";

		try
		{
			in = getClass().getResourceAsStream(BANK_NAME + ".properties");

			CONFIG_PROP.load(in);

			// logger.info(CONFIG_PROP.getProperty("20"));

		}
		catch (IOException e)
		{

			e.printStackTrace();
			logger.info("Errro Occured at getting property file :" + e);
		}

		try
		{

			FileInputStream file = new FileInputStream(BANK_STATEMENT);

			XSSFWorkbook xWorkbook = null;
			XSSFSheet xSheet = null;

			Workbook workbook = null;
			Sheet sheet = null;

			if (BANK_STATEMENT.getName().substring(BANK_STATEMENT.getName().length() - 5).equalsIgnoreCase(".xlsx"))
			{
				// Get the workbook instance for XLSX file
				workbook = new XSSFWorkbook(file);

				// Get first sheet from the workbook
				sheet = workbook.getSheetAt(0);
			}
			else
			{

				// Get the workbook instance for XLS file
				workbook = new HSSFWorkbook(file);

				// Get first sheet from the workbook
				sheet = workbook.getSheetAt(0);

			}

			int cellIndex_20[] = searchWorkBook(sheet, "20");
			int cellIndex_28C[] = searchWorkBook(sheet, "28C");
			
			//Added by prakash 7th Sept 2018
			
			String account="";
			String DATACELL = CONFIG_PROP.getProperty("DATACELL");
			
			/*if(CONFIG_PROP.getProperty("Statement_Row") == null || CONFIG_PROP.getProperty("Statement_Column") == null){
				temp2 = "Null Found";
			}else{
				temp2 = sheet.getRow(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Row"))).getCell(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Column"))).toString();//yub
			}*/
			
			
			
			
			
			if(BANK_NAME.equalsIgnoreCase("VIJAYA")){
				 Double doublevalue = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[0]+1).getNumericCellValue();
		            BigDecimal BD = new BigDecimal(doublevalue.toString());
		            Long Longval = BD.longValue();
		            account = Long.toString(Longval).trim();
		          
			}
			else if( BANK_NAME.equalsIgnoreCase("DENA")){
				String doublevalue = sheet.getRow(Integer.parseInt(CONFIG_PROP.getProperty("account_Row"))).getCell(Integer.parseInt(CONFIG_PROP.getProperty("account_Column"))).getStringCellValue();
	            BigDecimal BD = new BigDecimal(doublevalue.toString());
	            Long Longval = BD.longValue();
	            account = Long.toString(Longval).trim();
				
	           
				
			}
			
			else if( BANK_NAME.equalsIgnoreCase("ANDHRA")){
				String statement = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue();
	           String acc[]=statement.split(" ");
				
	           account=acc[3];
	           temp2 = acc[10];//yub
			}
			
			else if(BANK_NAME.equalsIgnoreCase("PNB") ){
				int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
				temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]).toString();//yub
				String dateArray[]=temp2.split("to");
				temp2=dateArray[1];
				
				
			}
			
			
			else{
				if (DATACELL.equalsIgnoreCase("ADJACENT"))
				account=sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue();
				//temp2 = sheet.getRow(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Row"))).getCell(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Column"))).toString();//yub
			}
			
			//System.out.println(sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[0]+1).getNumericCellValue());
			
           
           
            
			

			if (DATACELL.equalsIgnoreCase("ADJACENT"))
			{

				if (cellIndex_20[0] != -1 && cellIndex_20[1] != -1)
				{
					// We need to write logic to remove leading zeroes in
					// Account Number according to Amiraj
					//MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue().replaceAll("_", "") + System.getProperty("line.separator");
					MT940_CONTENT += ":20:" + account.replaceAll("_", "") + System.getProperty("line.separator");
					//MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue().replaceAll("_", "") + System.getProperty("line.separator");
					MT940_CONTENT += ":25:" +account.replaceAll("_", "") + System.getProperty("line.separator");
					accNo = MT940_CONTENT.substring(9, MT940_CONTENT.length() - 1);
                    //commented MT940_CONTENT and OutputFileName is previous code and new one is chenged by prakash
					//OutputFileName = sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue() + System.getProperty("line.separator");
					OutputFileName = account + System.getProperty("line.separator");
					if (CONFIG_PROP.getProperty("28C").equalsIgnoreCase("EMPTY"))
					{
						MT940_CONTENT += ":28C:" + STATEMENT_NUMBER + System.getProperty("line.separator");
					}
					else
					{
						// We need to write logic to remove leading zeroes in
						// Account Number according to Amiraj
						MT940_CONTENT += ":28C:" + sheet.getRow(cellIndex_28C[0]).getCell((cellIndex_28C[1] + 1)).getStringCellValue() + System.getProperty("line.separator");
					}

				}
				else
				{
					logger.info("Search Value not found");
				}

			}
			else if (DATACELL.equalsIgnoreCase("SAMECELL"))
			{
				if (cellIndex_20[0] != -1 && cellIndex_20[1] != -1)
				{
					// We need to write logic to remove leading zeroes in
					// Account Number according to Amiraj requirement
					if ("OBC".equals(BANK_NAME))
					{
						MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim() + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim() + System.getProperty("line.separator");
						OutputFileName = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim();						
						temp2 = sheet.getRow(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Row"))).getCell(Integer.parseInt(CONFIG_PROP.getProperty("Statement_Column"))).getStringCellValue().split(":")[3].trim();//yub
					}
					else if ("ICICI".equals(BANK_NAME))
					{
						MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						OutputFileName = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim();
					}
					
					
					else if ("ANDHRA".equals(BANK_NAME))
					{
						MT940_CONTENT += ":20:" + account + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + account + System.getProperty("line.separator");
						OutputFileName = account ;
					}
					
					else if ("IDBI".equals(BANK_NAME))
					{
						MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						OutputFileName = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim();
						
					}
					else
					{
						MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim() + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim() + System.getProperty("line.separator");
						OutputFileName = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim() + System.getProperty("line.separator");
					}

					if (CONFIG_PROP.getProperty("28C").equalsIgnoreCase("EMPTY"))
					{
						MT940_CONTENT += ":28C:" + STATEMENT_NUMBER + System.getProperty("line.separator");

					}
					else
					{
						// We need to write logic to remove leading zeroes in
						// Account Number according to Amiraj
						MT940_CONTENT += ":28C:" + sheet.getRow(cellIndex_28C[0]).getCell(cellIndex_28C[1]).getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim() + System.getProperty("line.separator");
					}

				}
				else
				{
					logger.info("Search Value not found");
				}
			}
			// Writing this code to Standard charterd bank on 8.12.15
			else if (DATACELL.equalsIgnoreCase("BELOW"))
			{
				if (cellIndex_20[0] != -1 && cellIndex_20[1] != -1)
				{
					if ("SCB".equals(BANK_NAME))

					{
						MT940_CONTENT += ":20:" + String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue())) + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue())) + System.getProperty("line.separator");

						OutputFileName = String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue()));
					}
					if (CONFIG_PROP.getProperty("28C").equalsIgnoreCase("EMPTY"))
					{
						MT940_CONTENT += ":28C:" + STATEMENT_NUMBER + System.getProperty("line.separator");

					}
					else
					{
						// We need to write logic to remove leading zeroes in
						// Account Number according to Amiraj
						MT940_CONTENT += ":28C:" + sheet.getRow(cellIndex_28C[0]).getCell(cellIndex_28C[1]).getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim() + System.getProperty("line.separator");
					}
				}
				

			}

			int cellIndex_60F[] = searchWorkBook(sheet, "60F");
			// This is only for CANARA BANK.
			if (cellIndex_60F[0] == -1)
			{
				cellIndex_60F = searchWorkBook(sheet, "temp");
				
			}

			int cellIndex_60F_DATE[] = searchWorkBook(sheet, "OPENING_BALANCE_DATE");

			int cellIndex_61[] = { -1, -1 };
			int cellIndex_86[] = { -1, -1 };
			

			DateFormat OPENING_BALANCE_DATE_FORMAT=new SimpleDateFormat("yyMMdd");
			
			/*if(BANK_NAME!=null && !BANK_NAME.equals("") && BANK_NAME.equalsIgnoreCase("VIJAYA"))
				OPENING_BALANCE_DATE_FORMAT=new SimpleDateFormat("ddMMyy");
			else
				OPENING_BALANCE_DATE_FORMAT=new SimpleDateFormat("yyMMdd");*/
			
			Cell OPENING_BALANCE_CELL = null;
			Cell OPENING_BALANCE_DATE_CELL = null;

			MT940_CONTENT += ":60F:C";

			if (CONFIG_PROP.getProperty("EMPTY_ROW_AFTER_TRANS_HEADER").equalsIgnoreCase("TRUE"))
			{
				
				
				
				OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 2).getCell(cellIndex_60F_DATE[1]);

				cellIndex_61[0] = cellIndex_60F_DATE[0] + 2;
				cellIndex_86[0] = cellIndex_60F_DATE[0] + 2;

			}
			else if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
			{
				OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 4).getCell(cellIndex_60F_DATE[1]);

				cellIndex_61[0] = cellIndex_60F_DATE[0] + 4;
				cellIndex_86[0] = cellIndex_60F_DATE[0] + 4;

			}
			
			else if("INDIAN BANK".equalsIgnoreCase(BANK_NAME)){
				String dateValue=sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(cellIndex_60F_DATE[1]).getStringCellValue();
				if(dateValue!=null && !(dateValue.trim()).equals("") && !dateValue.equalsIgnoreCase("Service")){
					OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(cellIndex_60F_DATE[1]);

					cellIndex_61[0] = cellIndex_60F_DATE[0] + 1;
					cellIndex_86[0] = cellIndex_60F_DATE[0] + 1;
				}
				else{
					OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 2).getCell(cellIndex_60F_DATE[1]);
					cellIndex_61[0] = cellIndex_60F_DATE[0] + 2;
					cellIndex_86[0] = cellIndex_60F_DATE[0] + 2;	
					
				}
				
			}
			else
			{
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-FALSE"+cellIndex_60F_DATE[0]+1+","+cellIndex_60F_DATE[1]);
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-FALSE"+cellIndex_60F[0]+1+","+cellIndex_60F[1]);
				
				OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(cellIndex_60F_DATE[1]);

				cellIndex_61[0] = cellIndex_60F_DATE[0] + 1;
				cellIndex_86[0] = cellIndex_60F_DATE[0] + 1;

			}
			if(!BANK_NAME.equalsIgnoreCase("VIJAYA") && !BANK_NAME.equalsIgnoreCase("UBI")&& !BANK_NAME.equalsIgnoreCase("OBC") && !BANK_NAME.equalsIgnoreCase("DENA")){

			cellIndex_61[1] = Integer.parseInt(CONFIG_PROP.getProperty("61"));
			
			
			}
			
			//Statement_Date = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(cellIndex_60F_DATE[1]);
			System.out.println(">>>>>BANK: "+BANK_NAME);
			System.out.println(">>>>>Cell with Date Value:"+temp2);
			System.out.println(">>>>>rowIndex: "+cellIndex_60F_DATE[0] + "\n>>>>>columnIndex: "+cellIndex_60F_DATE[1]);

			
			cellIndex_86[1] = Integer.parseInt(CONFIG_PROP.getProperty("86"));

			switch (OPENING_BALANCE_DATE_CELL.getCellType())
			{

				case Cell.CELL_TYPE_NUMERIC:
					
				
					MT940_CONTENT += OPENING_BALANCE_DATE_FORMAT.format(OPENING_BALANCE_DATE_CELL.getDateCellValue()) + "INR";
					break;

				case Cell.CELL_TYPE_STRING:
					if(BANK_NAME.equalsIgnoreCase("VIJAYA") ){
						 DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					    Date date = (Date) formatter.parse(OPENING_BALANCE_DATE_CELL.getStringCellValue());
						
						MT940_CONTENT += OPENING_BALANCE_DATE_FORMAT.format(date) + "INR";
						
					}
					else{
					MT940_CONTENT += OPENING_BALANCE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(OPENING_BALANCE_DATE_CELL.getStringCellValue())) + "INR";
					}
					break;

			}

			if (CONFIG_PROP.getProperty("OPENING_BALANCE").equalsIgnoreCase("EMPTY"))
			{
				int OPEN_BALANCE_ROW_NUMBER = -1;

				if (CONFIG_PROP.getProperty("EMPTY_ROW_AFTER_TRANS_HEADER").equalsIgnoreCase("TRUE"))
				{
					OPEN_BALANCE_ROW_NUMBER = cellIndex_60F_DATE[0] + 2;
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 2).getCell(Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN")));
				}
				else if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
				{
					OPEN_BALANCE_ROW_NUMBER = cellIndex_60F_DATE[0] + 4;
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 4).getCell(Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN")));
				}
				
				else if ("INDIAN BANK".equalsIgnoreCase(BANK_NAME))
				{
					OPEN_BALANCE_ROW_NUMBER = cellIndex_60F_DATE[0] + 1;
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN")));
				}
				
				else
				{
					// logger.info(cellIndex_60F_DATE[0]+1+" ,"+Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN")));
					OPEN_BALANCE_ROW_NUMBER = cellIndex_60F_DATE[0] + 1;
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN")));
					
					
				}

				switch (OPENING_BALANCE_CELL.getCellType())
				{

					case Cell.CELL_TYPE_NUMERIC:
						getCR_DR_Amount(OPEN_BALANCE_ROW_NUMBER, sheet, OPENING_BALANCE_CELL.getNumericCellValue());
						break;

					case Cell.CELL_TYPE_STRING:
						if(BANK_NAME.equalsIgnoreCase("VIJAYA")){
						String amount=OPENING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" Cr", "").replace(" Dr", "").replaceAll("\\s", "").trim();
						
						amount=amount.substring(0, amount.length()-1);
						double price=new Double(amount);
						
						getCR_DR_Amount(OPEN_BALANCE_ROW_NUMBER, sheet,price);
						}
						else
						
						getCR_DR_Amount(OPEN_BALANCE_ROW_NUMBER, sheet, new Double(OPENING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace("CR", "").replace("DR", "").replace(" CR", "").replace(" DR", "").replace("Cr", "").replace("Dr", "").replaceAll(String.valueOf((char) 160), "").trim()).doubleValue());
						break;

				}

			}
			else
			{
				if (DATACELL.equalsIgnoreCase("ADJACENT"))
				{
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F[0]).getCell((cellIndex_60F[1] + 1));

				}
				else if (DATACELL.equalsIgnoreCase("SAMECELL"))
				{
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F[0]).getCell(cellIndex_60F[1]);

				}
				// Added for SCB on 8.12.15
				else if (DATACELL.equalsIgnoreCase("BELOW"))
				{
					OPENING_BALANCE_CELL = sheet.getRow(cellIndex_60F[0] + 1).getCell(cellIndex_60F[1]);

				}

				switch (OPENING_BALANCE_CELL.getCellType())
				{
					case Cell.CELL_TYPE_NUMERIC:
						if (DATACELL.equalsIgnoreCase("SAMECELL"))
							MT940_CONTENT += new BigDecimal(String.valueOf(OPENING_BALANCE_CELL.getNumericCellValue()).split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN) + System.getProperty("line.separator");
						else
							MT940_CONTENT += new BigDecimal(String.valueOf(OPENING_BALANCE_CELL.getNumericCellValue()).trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN) + System.getProperty("line.separator");
						break;

					case Cell.CELL_TYPE_STRING:
						if (DATACELL.equalsIgnoreCase("SAMECELL"))
							MT940_CONTENT += String.valueOf(new BigDecimal(OPENING_BALANCE_CELL.getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim().replaceAll(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replaceAll(".", ",") + System.getProperty("line.separator");
						else
							MT940_CONTENT += String.valueOf(new BigDecimal(OPENING_BALANCE_CELL.getStringCellValue().trim().replaceAll(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
						break;

				}
			}

			MT940_CONTENT = MT940_CONTENT.replace(".", ",");
			int CHEQUE_NUMBER = 0,DEBIT_BALANCE=0,CREDIT_BALANCE=0,DESCRIPTION=0;
			int VALUE_DATE_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("VALUE_DATE"));
			int TRANS_DATE_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("TRANS_DATE"));
			
			if(BANK_NAME.equalsIgnoreCase("VIJAYA")|| BANK_NAME.equalsIgnoreCase("UBI") || BANK_NAME.equalsIgnoreCase("OBC") || BANK_NAME.equalsIgnoreCase("DENA")){
				/*DEBIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("Withdrawal"));
				CREDIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("Deposit"));
				DESCRIPTION = Integer.parseInt(CONFIG_PROP.getProperty("TransactionRemarks"));*/
				DEBIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("DEBIT"));
				 CREDIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("CREDIT"));
				 DESCRIPTION = Integer.parseInt(CONFIG_PROP.getProperty("DESCRIPTION"));
			}
			else{
				CHEQUE_NUMBER = Integer.parseInt(CONFIG_PROP.getProperty("CHEQUE_NUMBER"));
				DEBIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("DEBIT"));
				 CREDIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("CREDIT"));
				 DESCRIPTION = Integer.parseInt(CONFIG_PROP.getProperty("DESCRIPTION"));
			}
			
			
			int CR_DR_INDICATOR = -1;
			
			int CLOSING_BALANCE_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("VALUE_COLUMN"));
			int TRANS_REF_INDEX = 0;
			if ("SCB".equalsIgnoreCase(BANK_NAME))
			{
				TRANS_REF_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("TRXN_REFF"));
			}

			String CLOSING_DATE = new String();
			String CLOSING_BALANCE = new String();

			String description = "";
			String chequeNoCell = "";
			String[] descArr = null;
			String refNo = "";
			String transRef = "";

			Cell VALUE_DATE_CELL = null;
			Cell TRANS_DATE_CELL = null;
			Cell CHEQUE_NUMBER_CELL = null;
			Cell DEBIT_BALANCE_CELL = null;
			Cell CREDIT_BALANCE_CELL = null;
			Cell CR_DR_INDICATOR_CELL = null;
			Cell DESCRIPTION_CELL = null;
			Cell CLOSING_BALANCE_CELL = null;
			Cell TRANS_REF_CELL = null;

			DateFormat TRANS_DATE_FORMAT = new SimpleDateFormat("MMdd");
			DateFormat VALUE_DATE_FORMAT = new SimpleDateFormat("yyMMdd");

			String TRANSACTION_TYPE = new String();
			//String CREDIT_DEBIT_MARK = "";

			for (int i = cellIndex_61[0]; i <= sheet.getLastRowNum(); i++)
			{
				String CREDIT_DEBIT_MARK = "";
				try
				{

					String END_OF_FILE_CONTENT = CONFIG_PROP.getProperty("END_OF_FILE_CONTENT");
					// logger.info(END_OF_FILE_CONTENT);

					// logger.info("row,col:       "+cellIndex_60F_DATE[0]+","+cellIndex_60F_DATE[1]);
					// logger.info("row number       "+i);

					Cell NEXT_ROW_CELL = sheet.getRow(i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);

					String NEXT_ROW_CELL_CONTENT = new String();
					
					if (NEXT_ROW_CELL == null && !BANK_NAME.equalsIgnoreCase("INDUSIND BANK")&& !BANK_NAME.equalsIgnoreCase("INDIAN BANK") )
					{
						break;
					}
					
					if ("INDUSIND BANK".equalsIgnoreCase(BANK_NAME)&& NEXT_ROW_CELL == null)
					{
						NEXT_ROW_CELL = sheet.getRow(++i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);
						temp2 = temp2.split("To")[1];
					}
					
					/*else if ("INDIAN BANK".equalsIgnoreCase(BANK_NAME)&& NEXT_ROW_CELL == null )
					{
						NEXT_ROW_CELL = sheet.getRow(++i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);
						
					}
					
					else if ("INDIAN BANK".equalsIgnoreCase(BANK_NAME)&& NEXT_ROW_CELL != null){
						
						switch (NEXT_ROW_CELL.getCellType())
						{
							case Cell.CELL_TYPE_NUMERIC:
								break;

							case Cell.CELL_TYPE_STRING:
								NEXT_ROW_CELL = sheet.getRow(++i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);
								while(NEXT_ROW_CELL==null){
									NEXT_ROW_CELL = sheet.getRow(++i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);
									
								}
								break;

						}
						
					}*/
					
					
					
					else if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
					{
						Cell NEXT_ROW_CELL_CORP = sheet.getRow(i + 1).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);
						String NEXT_ROW_CELL_CONTENT_CORP = "";
						switch (NEXT_ROW_CELL_CORP.getCellType())
						{
							case Cell.CELL_TYPE_NUMERIC:
								break;

							case Cell.CELL_TYPE_STRING:
								NEXT_ROW_CELL_CONTENT_CORP = NEXT_ROW_CELL_CORP.getStringCellValue();
								break;

						}
						if (NEXT_ROW_CELL_CONTENT_CORP.equalsIgnoreCase(END_OF_FILE_CONTENT))
						{
							break;
						}
					}
					
					

					switch (NEXT_ROW_CELL.getCellType())
					{
						case Cell.CELL_TYPE_NUMERIC:
							break;

						case Cell.CELL_TYPE_STRING:
							NEXT_ROW_CELL_CONTENT = NEXT_ROW_CELL.getStringCellValue();
							break;

					}

					if (NEXT_ROW_CELL_CONTENT.contains(END_OF_FILE_CONTENT))
					{
						break;
					}

					if (NEXT_ROW_CELL != null)
					{

						VALUE_DATE_CELL = sheet.getRow(i).getCell(VALUE_DATE_INDEX, Row.RETURN_BLANK_AS_NULL);
						TRANS_DATE_CELL = sheet.getRow(i).getCell(TRANS_DATE_INDEX, Row.RETURN_BLANK_AS_NULL);
						CHEQUE_NUMBER_CELL = sheet.getRow(i).getCell(CHEQUE_NUMBER, Row.RETURN_BLANK_AS_NULL);
						DEBIT_BALANCE_CELL = sheet.getRow(i).getCell(DEBIT_BALANCE, Row.RETURN_BLANK_AS_NULL);
						CREDIT_BALANCE_CELL = sheet.getRow(i).getCell(CREDIT_BALANCE, Row.RETURN_BLANK_AS_NULL);
						// CR_DR_INDICATOR_CELL=sheet.getRow(i).getCell(CR_DR_INDICATOR,Row.RETURN_BLANK_AS_NULL);
						DESCRIPTION_CELL = sheet.getRow(i).getCell(DESCRIPTION, Row.RETURN_BLANK_AS_NULL);
						CLOSING_BALANCE_CELL = sheet.getRow(i).getCell(CLOSING_BALANCE_INDEX, Row.RETURN_BLANK_AS_NULL);
						TRANS_REF_CELL = sheet.getRow(i).getCell(TRANS_REF_INDEX, Row.RETURN_BLANK_AS_NULL);

						switch (VALUE_DATE_CELL.getCellType())
						{

							case Cell.CELL_TYPE_NUMERIC:
								CLOSING_DATE = VALUE_DATE_FORMAT.format(VALUE_DATE_CELL.getDateCellValue());
								if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
								MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(VALUE_DATE_CELL.getDateCellValue());
								break;

							case Cell.CELL_TYPE_STRING:
								if(BANK_NAME.equalsIgnoreCase("VIJAYA")){
									 DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
								    Date date = (Date) formatter.parse(VALUE_DATE_CELL.getStringCellValue());
									
								    CLOSING_DATE= VALUE_DATE_FORMAT.format(date);
								    MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(date);
									
								}
								
								/*else if(BANK_NAME.equalsIgnoreCase("INDIAN BANK")){
									
									if(!VALUE_DATE_CELL.getStringCellValue().equalsIgnoreCase("BRANCH (") ){
									
									CLOSING_DATE = VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));
									MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));
									}
									
								}*/
								
								
								else{
									
									if(BANK_NAME.equalsIgnoreCase("INDUSIND BANK")&& i>22){
										DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
									    Date date = (Date) formatter.parse(VALUE_DATE_CELL.getStringCellValue());
										
									    CLOSING_DATE= VALUE_DATE_FORMAT.format(date);
									    if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
									    MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(date);
										
									}
									else{
								CLOSING_DATE = VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));
								MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));
								}
								}
								break;

						}

						switch (TRANS_DATE_CELL.getCellType())
						{

							case Cell.CELL_TYPE_NUMERIC:
								if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
								MT940_CONTENT += TRANS_DATE_FORMAT.format(TRANS_DATE_CELL.getDateCellValue());
								break;

							case Cell.CELL_TYPE_STRING:
								if(BANK_NAME.equalsIgnoreCase("VIJAYA")){
									 DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
								    Date date = (Date) formatter.parse(TRANS_DATE_CELL.getStringCellValue());
									
									MT940_CONTENT += TRANS_DATE_FORMAT.format(date);
									
								}
								else{
									if(BANK_NAME.equalsIgnoreCase("INDUSIND BANK")&& i>22){
										DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
									    Date date = (Date) formatter.parse(TRANS_DATE_CELL.getStringCellValue());
										
									    CLOSING_DATE= VALUE_DATE_FORMAT.format(date);
									    if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
									    MT940_CONTENT += TRANS_DATE_FORMAT.format(date);
										
									}
									
									/*else if(BANK_NAME.equalsIgnoreCase("INDIAN BANK")){
										if(!TRANS_DATE_CELL.getStringCellValue().equalsIgnoreCase("SERVICE"))
											MT940_CONTENT += TRANS_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(TRANS_DATE_CELL.getStringCellValue()));
									}*/
									
								else{
								MT940_CONTENT += TRANS_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(TRANS_DATE_CELL.getStringCellValue()));
								}
								}
								break;

						}

						if (CONFIG_PROP.getProperty("CR_DR_ON_SAME_COL").equalsIgnoreCase("TRUE"))
						{
							CR_DR_INDICATOR = Integer.parseInt(CONFIG_PROP.getProperty("CR_DR_INDICATOR"));
							CR_DR_INDICATOR_CELL = sheet.getRow(i).getCell(CR_DR_INDICATOR, Row.RETURN_BLANK_AS_NULL);
							String statement = CR_DR_INDICATOR_CELL.getStringCellValue().trim();

							if (statement.equalsIgnoreCase("CR") || statement.equalsIgnoreCase("C") || statement.equalsIgnoreCase("Cr."))
							{
								CREDIT_DEBIT_MARK = "C";
								TRANSACTION_TYPE = "NCHK";
							}
							else
							{
								CREDIT_DEBIT_MARK = "D";
								TRANSACTION_TYPE = "NTRF";
							}

						}
						else
						{

							if (DEBIT_BALANCE_CELL != null)
							{
								switch (DEBIT_BALANCE_CELL.getCellType())
								{
									case Cell.CELL_TYPE_NUMERIC:

										if (DEBIT_BALANCE_CELL.getNumericCellValue() != 0)
										{
											CREDIT_DEBIT_MARK = "D";
											TRANSACTION_TYPE = "NTRF";
										}
										break;

									case Cell.CELL_TYPE_STRING:
										if (!DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
										{

											CREDIT_DEBIT_MARK = "D";
											TRANSACTION_TYPE = "NTRF";
										}
										break;
								}

							}

							if (CREDIT_BALANCE_CELL != null)
							{
								switch (CREDIT_BALANCE_CELL.getCellType())
								{
									case Cell.CELL_TYPE_NUMERIC:
										if (CREDIT_BALANCE_CELL.getNumericCellValue() != 0)
										{
											CREDIT_DEBIT_MARK = "C";
											TRANSACTION_TYPE = "NCHK";
										}

										break;

									case Cell.CELL_TYPE_STRING:
										if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ")&& !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
										{
											CREDIT_DEBIT_MARK = "C";
											TRANSACTION_TYPE = "NCHK";
										}
										break;
								}

							}

						}

						MT940_CONTENT += CREDIT_DEBIT_MARK;

						if (CREDIT_DEBIT_MARK.equalsIgnoreCase("C"))
						{

							switch (CREDIT_BALANCE_CELL.getCellType())
							{
								case Cell.CELL_TYPE_NUMERIC:
									if (CREDIT_BALANCE_CELL.getNumericCellValue() != 0)
									{
										MT940_CONTENT += String.valueOf(new BigDecimal(CREDIT_BALANCE_CELL.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + TRANSACTION_TYPE;
									}
									break;

								case Cell.CELL_TYPE_STRING:

									if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
									{
										// logger.info("@"+CREDIT_BALANCE_CELL.getStringCellValue().replace(" ","")+"@"+CREDIT_BALANCE_CELL.getStringCellValue().length());
										MT940_CONTENT += String.valueOf(new BigDecimal(CREDIT_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" ", "").replaceAll(String.valueOf((char) 160), "").trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + TRANSACTION_TYPE;

									}
									break;
							}

						}
						else if (CREDIT_DEBIT_MARK.equalsIgnoreCase("D"))
						{
							switch (DEBIT_BALANCE_CELL.getCellType())
							{
								case Cell.CELL_TYPE_NUMERIC:
									if (DEBIT_BALANCE_CELL.getNumericCellValue() != 0)
									{
										MT940_CONTENT += String.valueOf(new BigDecimal(DEBIT_BALANCE_CELL.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + TRANSACTION_TYPE;
									}
									break;

								case Cell.CELL_TYPE_STRING:
									if (!DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
									{ // logger.info("@"+DEBIT_BALANCE_CELL.getStringCellValue().replace(" ","")+"@"+DEBIT_BALANCE_CELL.getStringCellValue().length());
										MT940_CONTENT += String.valueOf(new BigDecimal(DEBIT_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" ", "").replace(String.valueOf((char) 160), " ").trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + TRANSACTION_TYPE;
									}
									break;
							}

						}

						try
						{
							refNo = "";
							description = "";
							chequeNoCell = "";
							descArr = null;
							description = DESCRIPTION_CELL.getStringCellValue();
							if (CHEQUE_NUMBER_CELL != null)
							{
								switch (CHEQUE_NUMBER_CELL.getCellType())
								{
									case Cell.CELL_TYPE_NUMERIC:
										chequeNoCell = String.valueOf(Double.valueOf(CHEQUE_NUMBER_CELL.getNumericCellValue()).intValue());
										break;

									case Cell.CELL_TYPE_STRING:
										chequeNoCell = CHEQUE_NUMBER_CELL.getStringCellValue();
										break;
								}
							}
							if ("SBI".equalsIgnoreCase(BANK_NAME))
							{
                                 int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
								
								temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]+1).toString();//yub
								
								
								if (DESCRIPTION_CELL != null)
								{
									switch (DESCRIPTION_CELL.getCellType())
									{
										case Cell.CELL_TYPE_NUMERIC:

											break;
										case Cell.CELL_TYPE_STRING:
											if (description.trim().startsWith("BY TRANSFER-NEFT"))
											{
												descArr = description.split("\\*");
												refNo = descArr[2];
												// logger.info(refNo);
											}
											else if (description.trim().startsWith("BY TRANSFER-INB") || description.trim().startsWith("TO TRANSFER-INB"))
											{
												descArr = chequeNoCell.trim().split(" ");
												refNo = descArr[0].trim();

											}
											else if (description.trim().startsWith("BY TRANSFER-RTGS"))
											{
												descArr = description.trim().split(" ");
												refNo = descArr[2].trim();

											}
											else if (description.trim().startsWith("CREDIT-TR FR"))
											{
												descArr = description.trim().substring("CREDIT-TR FR ".length()).split("-");
												refNo = descArr[0].trim();

											}
											else if (description.trim().startsWith("BY CLEARING / CH"))
											{
												refNo = description.trim().substring(description.indexOf("ByClearing") + 10, description.indexOf("ByClearing") + 16).replaceFirst("^0+(?!$)", "");
											}
											else if (description.trim().startsWith("BY TRANSFER-IMPS"))
											{
												descArr = description.trim().split("/");
												refNo = descArr[1].trim();

											}
											else if (description.trim().startsWith("BY TRANSFER-GRPT"))
											{
												descArr = description.trim().split("\\*");
												refNo = descArr[2].trim();

											}
											
											
											
											
											else
											{
												if (CHEQUE_NUMBER_CELL != null)
												{
													refNo = "";
												}
											}
											break;
									}
								}
							}
							else if ("UBI".equalsIgnoreCase(BANK_NAME))
							{
								
                                 int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
								
								temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]+3).toString();//yub
								
								if (description.trim().startsWith("BY INST"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim().replaceFirst("^0+(?!$)", "");
								}
								//adding elseif condition as CR 0130108. new file format given by ammiraj
								//added on 03Feb2016
								else if (CHEQUE_NUMBER_CELL != null)
								{
									refNo = chequeNoCell.trim().substring(chequeNoCell.trim().length()-6);
								}
								
								else if (description.trim().startsWith("NEFT") || description.trim().startsWith("RTGS"))
								{
									if(description.equalsIgnoreCase("NEFT  SBI RACPC SION")){
										descArr = description.trim().split("  ");
										refNo = descArr[1].trim();
									}
									
									else if(description.trim().startsWith("NEFTO")){
										descArr = description.trim().split("-");
										refNo = descArr[1].trim();
									}
									else{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
									}
								}
								
								
								
								
								
								
							}
							//adding idbi bank for rtgs and cheque as per CR 0130108 given by ammiraj
							//added on 03feb2016
							else if ("IDBI".equalsIgnoreCase(BANK_NAME))
							{
								int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
								temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]).toString();//yub
								String dateArray[]=temp2.split("to");
								temp2=dateArray[1];
								
								if (description.trim().toLowerCase().contains("rtgs"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
								else if (CHEQUE_NUMBER_CELL != null)
								{
									refNo = chequeNoCell.trim();
								}
							}
							else if ("CANARABANK".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("RTGSIW:"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("NEFT"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[2].trim();
								}
								else if (description.trim().startsWith("By Clg") || description.trim().startsWith("MB-IMPS") || description.trim().startsWith("MB-IMPS") || description.trim().startsWith("Online Cheque Return"))
								{
									refNo = chequeNoCell.trim();
								}
							}
							
							else if ("DENA".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("RTGS IW"))
								{
									descArr = description.trim().split("IW");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("NEFT IW"))
								{
									descArr = description.trim().split("IW");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("Loan"))
								{
									
									refNo = description.trim();
								}
								
								
							}
							

							else if ("PNB".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("Dr.for RTGS Customer"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("Chrgs for RTGS Cust Pymnt"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("BY CLG/CHQ"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim().split(" ")[1].trim();
								}
								else if (description.trim().startsWith("RTGS/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[2].trim();
								}
							}
							else if ("BOI".equalsIgnoreCase(BANK_NAME))
							{
								int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
								temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]).toString();//yub
								String dateArray[]=temp2.split("to");
								temp2=dateArray[1].trim();
								
								if (description.trim().startsWith("BY CLG/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
								if (description.trim().startsWith("BY CLG-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[2].trim();
								}
								if (description.trim().startsWith("IMPS/RRN:"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].split(":")[1].trim();
								}
							}
							else if ("BOB".equalsIgnoreCase(BANK_NAME))
							{
								int Startdate_Index[] = searchWorkBook(sheet, "Statement_Row");
								temp2 = sheet.getRow(Startdate_Index[0]).getCell(Startdate_Index[1]).toString();//yub
								String dateArray[]=temp2.split("to");
								temp2=dateArray[1].trim();
								
								if (description.trim().startsWith("BY INST"))
								{
									refNo = description.substring("BY INST ".length(), description.indexOf(":"));
								}
								if (description.trim().startsWith("NEFT"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								if (description.trim().startsWith("RTGS"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								if (description.trim().startsWith("BY CLG/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
							}

							else if ("SBH".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("CHQ TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.trim().startsWith("TO TRANSFER-INB--"))
								{
									descArr = chequeNoCell.trim().split(" ");
									refNo = descArr[0].trim();
								}
								else if (description.trim().startsWith("BY CLEARING"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("BY TRANSFER-NEFT "))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.trim().startsWith("BY TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.trim().startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
							}

							else if ("SYNDICATE".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("NEFT"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[1].trim();
								}
								else if(description.trim().startsWith("RTGS:"))
								{
									 descArr = description.trim().split(":"); 
									 refNo = descArr[1].trim(); 
								}
								else
								{
									refNo = chequeNoCell.trim();
								}
							}

							

							else if ("SBBJ".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("CHQ TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.trim().startsWith("CAS PRES CHQ"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("BY TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								/* added below 2 conditions as per CR(128343) raised by Amiraj. added on 20Jan2016 */
								/* added 22 characters condition for neft as mentioned in the email ion 20Jan2016 */
								else if (description.trim().startsWith("BY TRANSFER-NEFT"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim().substring(0,22);
								}
								else if (description.trim().toLowerCase().contains("cheque"))
								{
									refNo =  chequeNoCell.trim();
								}
							}
							else if ("BOM".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("BY TRF CORR RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[4].trim();
								}
								else if (description.trim().startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[3].trim();
								}
								/* added below 4 conditions as per CR(128343) raised by Amiraj. added on 20Jan2016*/
								else if (description.trim().toLowerCase().contains("by clg") || description.trim().toLowerCase().contains("cheque"))
								{
									refNo =  chequeNoCell.trim();
								}
								else if ( description.trim().toLowerCase().contains("by trf neft"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[3].trim();
								}
								else if ( description.trim().toLowerCase().contains("by trf rtgs"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[3].trim();
								}
								else if ( description.trim().toLowerCase().contains("to transfer rtgs"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[3].trim();
								}
								
							}
							else if ("CBI".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("BY CLEARING /"))
								{
									refNo = chequeNoCell.trim();
								}
								else if (description.trim().startsWith("BY TRANSFER"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[descArr.length - 1].trim();
								}
								else if (description.trim().startsWith("CHEQUE DEPOSIT"))
								{
									refNo = chequeNoCell.trim();
								}
								else if (description.trim().startsWith("OUT-CHQ RETURN"))
								{
									refNo = chequeNoCell.trim();
								}
							}

							else if ("OBC".equalsIgnoreCase(BANK_NAME))
							{
								String chequeNoValue = chequeNoCell.trim();
								if ("".equalsIgnoreCase(chequeNoValue))
								{
									if (description.trim().startsWith("By Inst")||description.trim().startsWith("KALYA-By Inst"))
									{
										descArr = description.trim().split(".");
										refNo = descArr[1].trim();
									}
									/* added below 1 conditions as per CR(128343) raised by Amiraj. added on 20Jan2016*/
									else if (description.trim().toLowerCase().contains("reject"))
									{
										refNo = description.split(":")[1];
									}
									
									else if(description.trim().startsWith("NEFT")){
										descArr = description.trim().split("-");
										refNo = descArr[1].trim();
									}
									
								}
								else
								{
									refNo = chequeNoValue;
								}
							}
							else if ("ICICI".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("RTGS:"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].split("/")[0].trim();
								}
								else if (description.trim().startsWith("RTGS-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("NEFT-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("BIL/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
								else if (description.trim().startsWith("REJECT:"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.contains("/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim().replaceFirst("^0+(?!$)", "");
								}

								// .replaceFirst("^0+(?!$)", "");
							}
							else if ("VIJAYA".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("RTGS"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
							}
							else if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("RTGS"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].split(" ")[0].trim();
								}
							}
							
							else if ("INDUSIND BANK".equalsIgnoreCase(BANK_NAME))
							{
								if (description.trim().startsWith("TRF FRM")||description.trim().startsWith("FT TO"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
							}
							
							else if ("SCB".equalsIgnoreCase(BANK_NAME))
							{
								if (chequeNoCell != "")
								{
									refNo = chequeNoCell.trim();
								}
								else
								{
									// xxxxxxx
									transRef = DESCRIPTION_CELL.getStringCellValue();
									String NewRef = (transRef.indexOf("|") == -1) ? transRef : transRef.substring(transRef.lastIndexOf("|") + 1).split(" ")[0].trim();
									refNo = NewRef;
								}

							}

							if (refNo != null && !"".equals(refNo))
							{
								if (refNo.length() > 16)
								{
									refNo = refNo + "//";
								}
								if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
								MT940_CONTENT += refNo;
							}
							else
							{
								if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
								MT940_CONTENT += "NOREF";
							}
						}
						catch (Exception e)
						{
							// MT940_CONTENT+="NONREF";
							MT940_CONTENT += "NOREF";
							e.printStackTrace();
							logger.info("Exception occured at line 1112 :" + e);
						}
						if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
						MT940_CONTENT += System.getProperty("line.separator");

						if (DESCRIPTION_CELL != null)
						{
							switch (DESCRIPTION_CELL.getCellType())
							{
								case Cell.CELL_TYPE_NUMERIC:
									MT940_CONTENT += ":86:" + DESCRIPTION_CELL.getNumericCellValue() + System.getProperty("line.separator");
									break;

								case Cell.CELL_TYPE_STRING:
									String temp86 = DESCRIPTION_CELL.getStringCellValue().replaceAll("0000000000000000000000000000", "").replaceAll(" +", " ");

									/* Replacing service tax with ST */
									if (description.toLowerCase().contains("service tax"))
									{
										temp86 = temp86.replace("Service Tax", "ST");
										temp86 = temp86.replace("Service tax", "ST");
										temp86 = temp86.replace("service Tax", "ST");
										temp86 = temp86.replace("service tax", "ST");
										temp86 = temp86.replace("SERVICE TAX", "ST");
									}
									temp86 = temp86.replace("BY TRANSFER-", "");
									String temp = "";
									if ("SBI".equalsIgnoreCase(BANK_NAME))
									{
										String descriptionArr[] = temp86.split("\\*");
										for (int j = 0; j < descriptionArr.length; j++)
										{
											if (j != 1)
											{
												temp = temp + " " + descriptionArr[j];
											}
										}
										temp86 = temp;
									}
									if(CREDIT_BALANCE_CELL!=null || DEBIT_BALANCE_CELL!=null)
									MT940_CONTENT += ":86:" + temp86 + System.getProperty("line.separator");
									break;
							}
						}

						if (CLOSING_BALANCE_CELL != null)
						{
							switch (CLOSING_BALANCE_CELL.getCellType())
							{
								case Cell.CELL_TYPE_NUMERIC:
									CLOSING_BALANCE = String.valueOf(new BigDecimal(CLOSING_BALANCE_CELL.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",");
									break;

								case Cell.CELL_TYPE_STRING:
									if(BANK_NAME.equalsIgnoreCase("VIJAYA")){
										String balance=CLOSING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" Cr", "").replace(" Dr", "");
										balance=balance.substring(0, balance.length()-1);
										CLOSING_BALANCE = String.valueOf(new BigDecimal(balance.replaceAll("\\s", "").trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",");
									}
									else
									CLOSING_BALANCE = String.valueOf(new BigDecimal(CLOSING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace("CR", "").replace("DR", "").replace(" CR", "").replace(" DR", "").replace("Cr", "").replace("Dr", "").replaceAll(String.valueOf((char) 160), "").trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",");
									break;
							}

						}

					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					logger.info("Exception occured at line 1184 :" + e);
				}

			}

			if (CONFIG_PROP.getProperty("CLOSING_BALANCE").equalsIgnoreCase("AVAILABLE"))
			{
				int cellIndex_62F[] = searchWorkBook(sheet, "62F");
				Cell CLOSING_BAL_CELL = null;

				if (DATACELL.equalsIgnoreCase("ADJACENT"))
				{
					CLOSING_BAL_CELL = sheet.getRow(cellIndex_62F[0]).getCell((cellIndex_62F[1] + 1));

				}
				else if (DATACELL.equalsIgnoreCase("SAMECELL"))
				{
					CLOSING_BAL_CELL = sheet.getRow(cellIndex_62F[0]).getCell(cellIndex_62F[1]);

				}
				else if (DATACELL.equalsIgnoreCase("BELOW"))
				{
					CLOSING_BAL_CELL = sheet.getRow(cellIndex_62F[0] + 1).getCell(cellIndex_62F[1]);

				}

				MT940_CONTENT += ":62F:C" + CLOSING_DATE + "INR";

				switch (CLOSING_BAL_CELL.getCellType())
				{
					case Cell.CELL_TYPE_NUMERIC:
						// MT940_CONTENT+=new
						// BigDecimal(OPENING_BALANCE_CELL.getNumericCellValue()).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
						if (DATACELL.equalsIgnoreCase("SAMECELL"))
							MT940_CONTENT += String.valueOf(new BigDecimal(String.valueOf(CLOSING_BAL_CELL.getNumericCellValue()).split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
						else
							MT940_CONTENT += String.valueOf(new BigDecimal(String.valueOf(CLOSING_BAL_CELL.getNumericCellValue()).trim().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
						break;

					case Cell.CELL_TYPE_STRING:
						if (DATACELL.equalsIgnoreCase("SAMECELL"))
							MT940_CONTENT += String.valueOf(new BigDecimal(CLOSING_BAL_CELL.getStringCellValue().split(CONFIG_PROP.getProperty("DATA_SEPERATOR"))[1].trim().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
						else
							MT940_CONTENT += String.valueOf(new BigDecimal(CLOSING_BAL_CELL.getStringCellValue().trim().replaceAll(",", "")).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
						break;

				}
			}
			else
			{
				MT940_CONTENT += ":62F:C" + CLOSING_DATE + "INR" + CLOSING_BALANCE;
			}

			logger.info(MT940_CONTENT);

			// Commenting the below line to make the output file name as account
			// no as requested by amiraj in ticket #68127
			// DOWNLOAD_FILE=UPLOAD_DIRECTORY+File.separator+FILE_NAME.substring(0,FILE_NAME.indexOf("."))+".txt";
			// DOWNLOAD_FILE=UPLOAD_DIRECTORY+File.separator+OutputFileName.replaceAll("(\\r|\\n)",
			// "")+".txt";
			
			//SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");//yub
			SimpleDateFormat  simpleDateFormat = new SimpleDateFormat(CONFIG_PROP.getProperty("TO_DATE_FORMAT"));//yub
			Date date = simpleDateFormat.parse(temp2);
			//temp2=date.toString();
			
			SimpleDateFormat formatter = new SimpleDateFormat("ddMMYYYY");
			temp2 = formatter.format(date);
			
			System.out.println("PPPPPP"+temp2);
			
			/*DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy ");
			//Date date = new Date();
			System.out.println(">>>>>>>>>>."+dateFormat.parse(temp2));*/
			
			
			//DOWNLOAD_FILE = UPLOAD_DIRECTORY + File.separator + OutputFileName.trim()+dateFormat.format(date).trim()+".txt";
			DOWNLOAD_FILE = UPLOAD_DIRECTORY + File.separator + OutputFileName.trim()+temp2+".txt";

			// As requested by amiraj to remove the leading _ for account no.
			// 1.07.15
			if (OutputFileName.contains("_"))
			{
				OutputFileName = OutputFileName.substring(1);
				DOWNLOAD_FILE = UPLOAD_DIRECTORY + File.separator + OutputFileName.trim()+temp2+".txt";
			}

			File f = new File(DOWNLOAD_FILE);
			// Check if file exist or not ,if exist show error .
			if (f.exists())
			{
				// throw new AccountNoAlreadyExist
				// ("You have uploaded mutiple files for account number");
				accountCount++;

			}
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(MT940_CONTENT.getBytes());
			fos.close();

			// Commenting the below line to make the output file name as account
			// no as requested by amiraj in ticket #68127
			// DOWNLOAD_FILE=FILE_NAME.substring(0,FILE_NAME.indexOf("."))+".txt";
			// DOWNLOAD_FILE=OutputFileName.replaceAll("(\\r|\\n)", "")+".txt";
			
			
			//uncomment it
			//DOWNLOAD_FILE = OutputFileName.trim()+dateFormat.format(date).trim()+".txt";
			
			DOWNLOAD_FILE = OutputFileName.trim()+temp2+".txt";
			// PrintWriter pw=new
			// PrintWriter("MT940_STATEMENTS"+File.separator+BANK_NAME+".txt");
			// PrintWriter pw=new
			// PrintWriter("MT940_STATEMENTS"+File.separator+DOWNLOAD_FILE);
			PrintWriter pw = new PrintWriter(UPLOAD_DIRECTORY + File.separator + DOWNLOAD_FILE);
			pw.write(MT940_CONTENT);
			pw.close();

			CONFIG_PROP = null;
			in.close();
			file.close();
			

		}

		catch (Exception e)
		{
			e.printStackTrace();
			logger.info("Exception occured at generateMT940() :" + e);
		}

	}

	public void getCR_DR_Amount(int OPEN_BALANCE_ROW_NUMBER, Sheet sheet, double FIRST_ROW_TRANS_VALUE)
	{
		int CR_DR_INDICATOR = -1;

		Cell CR_DR_INDICATOR_CELL = null;
		Cell DEBIT_BALANCE_CELL=null;
		Cell CREDIT_BALANCE_CELL=null;
		
		
			
		 DEBIT_BALANCE_CELL = sheet.getRow(OPEN_BALANCE_ROW_NUMBER).getCell(Integer.parseInt(CONFIG_PROP.getProperty("DEBIT")), Row.RETURN_BLANK_AS_NULL);
		 CREDIT_BALANCE_CELL = sheet.getRow(OPEN_BALANCE_ROW_NUMBER).getCell(Integer.parseInt(CONFIG_PROP.getProperty("CREDIT")), Row.CREATE_NULL_AS_BLANK);
		
		
		String CREDIT_DEBIT_MARK = new String();

		String TRANSACTION_TYPE = new String();
		if (CONFIG_PROP.getProperty("CR_DR_ON_SAME_COL").equalsIgnoreCase("TRUE"))
		{
			CR_DR_INDICATOR = Integer.parseInt(CONFIG_PROP.getProperty("CR_DR_INDICATOR"));
			CR_DR_INDICATOR_CELL = sheet.getRow(OPEN_BALANCE_ROW_NUMBER).getCell(CR_DR_INDICATOR, Row.RETURN_BLANK_AS_NULL);

			String CRDR = CR_DR_INDICATOR_CELL.getStringCellValue().trim();
			// if(CR_DR_INDICATOR_CELL.getStringCellValue().trim().equalsIgnoreCase("CR"))
			if (CRDR.equalsIgnoreCase("CR") || CRDR.equalsIgnoreCase("C"))
			{
				CREDIT_DEBIT_MARK = "C";
				TRANSACTION_TYPE = "NCHK";
			}
			else
			{
				CREDIT_DEBIT_MARK = "D";
				TRANSACTION_TYPE = "NTRF";
			}

		}
		else
		{

			if (DEBIT_BALANCE_CELL != null)
			{
				switch (DEBIT_BALANCE_CELL.getCellType())
				{
					case Cell.CELL_TYPE_NUMERIC:

						if (DEBIT_BALANCE_CELL.getNumericCellValue() != 0)
						{
							CREDIT_DEBIT_MARK = "D";
							TRANSACTION_TYPE = "NTRF";
						}
						break;

					case Cell.CELL_TYPE_STRING:
						if (!DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
						{

							CREDIT_DEBIT_MARK = "D";
							TRANSACTION_TYPE = "NTRF";
						}
						break;
				}

			}

			
			if (CREDIT_BALANCE_CELL != null )
			{
				switch (CREDIT_BALANCE_CELL.getCellType())
				{
					case Cell.CELL_TYPE_NUMERIC:
						if (CREDIT_BALANCE_CELL.getNumericCellValue() != 0)
						{
							CREDIT_DEBIT_MARK = "C";
							TRANSACTION_TYPE = "NCHK";
						}

						break;

					case Cell.CELL_TYPE_STRING:
						if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
						{
							CREDIT_DEBIT_MARK = "C";
							TRANSACTION_TYPE = "NCHK";
						}
						break;
				}

			}

		}

		if (CREDIT_DEBIT_MARK.equalsIgnoreCase("C"))
		{

			switch (CREDIT_BALANCE_CELL.getCellType())
			{
				case Cell.CELL_TYPE_NUMERIC:
					if (CREDIT_BALANCE_CELL.getNumericCellValue() != 0)
					{
						// MT940_CONTENT+=new
						// BigDecimal(OPENING_BALANCE_CELL.getNumericCellValue()).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
						MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE - CREDIT_BALANCE_CELL.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
					}
					break;

				case Cell.CELL_TYPE_STRING:

					if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
					{
						// logger.info("@"+CREDIT_BALANCE_CELL.getStringCellValue().replace(" ","")+"@"+CREDIT_BALANCE_CELL.getStringCellValue().length());
						String creditBalCell = CREDIT_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replaceAll(" ", "").replaceAll(String.valueOf((char) 160), "").trim();
						MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE - Double.parseDouble(creditBalCell)).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");

					}
					break;
			}

		}
		else if (CREDIT_DEBIT_MARK.equalsIgnoreCase("D"))
		{
			switch (DEBIT_BALANCE_CELL.getCellType())
			{
				case Cell.CELL_TYPE_NUMERIC:
					if (DEBIT_BALANCE_CELL.getNumericCellValue() != 0)
					{
						// MT940_CONTENT+=new
						// BigDecimal(OPENING_BALANCE_CELL.getNumericCellValue()).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
						MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE + DEBIT_BALANCE_CELL.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",").replace(String.valueOf((char) 160), "") + System.getProperty("line.separator");
					}
					break;

				case Cell.CELL_TYPE_STRING:
					if (!DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !DEBIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
					{ // logger.info("@"+DEBIT_BALANCE_CELL.getStringCellValue().replace(" ","")+"@"+DEBIT_BALANCE_CELL.getStringCellValue().length());
						MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE + Double.parseDouble(DEBIT_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replaceAll(" ", "").replace(String.valueOf((char) 160), " "))).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
					}
					break;
			}

		}
		
		if(CREDIT_BALANCE_CELL ==null && DEBIT_BALANCE_CELL==null ){
			MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE - 0).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
			
		}
		
		if(sheet.getSheetName().equalsIgnoreCase("Statement of Account") && OPEN_BALANCE_ROW_NUMBER==19 ){
			
			MT940_CONTENT += String.valueOf(new BigDecimal(FIRST_ROW_TRANS_VALUE - 0).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",") + System.getProperty("line.separator");
		}
		
		
		
		

	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		try
		{
			// response.sendError(407,"Need authentication!!!");
		String sys = System.getProperty("os.name");
			InputStream in = getClass().getResourceAsStream("PATH.properties");
			Properties CONFIG_PATH = new Properties();
			CONFIG_PATH.load(in);

			logger = Logger.getLogger(MT940.class);
			// logger.info("START");
			logger.info("System Name :" + sys);
			DOWNLOAD_FILE = "";
			Connection mySqlConn = null;
			Statement myStmt = null;
			ResultSet myRs = null;
			String MainDir = null;

			if (sys.contains("Win"))
			{

				UPLOAD_DIRECTORY = CONFIG_PATH.getProperty("WINO");
				MainDir = CONFIG_PATH.getProperty("WINM");

			}
			else
			{
				UPLOAD_DIRECTORY = CONFIG_PATH.getProperty("LINO");
				MainDir = CONFIG_PATH.getProperty("LINM");

			}
			logger.info("Main Directory:" + MainDir);
			logger.info("Upload Directory:" + UPLOAD_DIRECTORY);
			String BANK_NAME = new String();

			// logger.info("BBB");
			// To clean the output directory before every iteration.
           
         System.out.println("Current thread is"+Thread.currentThread().getName());
      /* Thread t=Thread.currentThread();
       t.stop();*/
			
			FileUtils.cleanDirectory(new File(UPLOAD_DIRECTORY));
			
			
			// FOR CONNECTING TO MySql DB
			// logger.info("CONNECTING TO MYSQL");
			try
			{
				// Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				// Open a connection
				if (sys.contains("Win"))
				{
					//mySqlConn =(Connection) DriverManager.getConnection("jdbc:mysql://localhost/mt940_db", "root", "root");// FOR DEV
					 mySqlConn=(Connection) DriverManager.getConnection("jdbc:mysql://172.29.10.77/MT940_DB","mt940admin","mt940admin");//UAT
					//mySqlConn=(Connection) DriverManager.getConnection("jdbc:mysql://172.29.7.94/MT940_DB","mt940admin","mt940admin"); //prod
				}
				else
				{
					// mySqlConn=DriverManager.getConnection("jdbc:mysql://172.29.7.94/MT940_DB","mt940admin","mt940admin");//FOR PROD
					// mySqlConn=(Connection) DriverManager.getConnection("jdbc:mysql://172.29.10.77/MT940_DB","mt940admin","mt940admin");//UAT
					 mySqlConn=(Connection) DriverManager.getConnection("jdbc:mysql://localhost/MT940_DB","mt940admin","mt940admin");
					//mySqlConn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/MT940_DB", "mt940admin", "mt940admin");// FOR PROD
				}

				logger.info("CONNECTED" + mySqlConn);
				myStmt = (Statement) mySqlConn.createStatement();
			}
			catch (Exception se)
			{
				se.printStackTrace();
				logger.error("Exception for DB connection :" + se);

			}
			// MT940 statements location
			File dirSub = new File(MainDir);
			logger.info("Main directory  :" + dirSub);
			// int numberOfSubfolders=0;
			File listDir[] = dirSub.listFiles();

			for (int i = 0; i < listDir.length; i++)
			{
				if (listDir[i].isDirectory())
				{
					subDirName = new File(dirSub + File.separator + listDir[i].getName()); // Go into that Sub directory.
					BANK_NAME = listDir[i].getName();
					logger.info("Bank Name :" + BANK_NAME);
					// FOR DATABASE EXECUtion
					String query = "SELECT * FROM statement_count  WHERE BANK_NAME='" + BANK_NAME + "'";
					// Process the result
					myRs = myStmt.executeQuery(query);
					// Extract Results
					if (myRs != null && myRs.next())
					{
						myRs.beforeFirst();
						while (myRs.next())
						{
							// Retrieve by column name
							String bank = myRs.getString("BANK_NAME");
							STATEMENT_NUMBER = myRs.getInt("COUNT");
							// Display values
							// logger.info("Bank Name: " + bank );
							// logger.info(" Statement Number: " + STATEMENT_NUMBER );
						}
						for (File f : subDirName.listFiles())
						{
							if (accountCount > 0) // to check for duplicate account number.
							{

								response.sendRedirect("ErrorFile.html");
								accountCount = 0;
								logger.info("Duplicate account number exist directed to error page");
								// The root cause of IllegalStateException exception is a java servlet is attempting to write to the output stream (response) after the response has been committed.
								return;

							}
							FILE_NAME = f.getName();
							generateMT940(BANK_NAME);
							STATEMENT_NUMBER++;

							// logger.info( f.getName() );
						}
						// To update statement number into the DB for every bank.
						String query2 = "UPDATE statement_count set COUNT='" + STATEMENT_NUMBER + "' WHERE BANK_NAME='" + BANK_NAME + "'";
						myStmt.executeUpdate(query2);
					}
					else
					{
						logger.info("Database entry not found for Bank Name:" + BANK_NAME);
					}
					//Added by prakash 5 Sept 2018
					//myRs.close();
				}

			}
			mySqlConn.close();
			myStmt.close();
			myRs.close();

			if (sys.contains("Win"))
			{
				logger.info("FTP not available Windows System");
				//Added by prakash
				FtpFilesUpload();
			}
			else
			{
				logger.info("FTP is available for Linux System");
				FtpFilesUpload();
			}

			// To delete the xls files after conversion as requested 3.07.15
			for (int i = 0; i < listDir.length; i++)
			{
				// subDirName=new File(dirSub+File.separator+listDir[i].getName()); //Go into that Sub directory.
				if (listDir[i].isDirectory())
				{
					FileUtils.cleanDirectory(new File(dirSub + File.separator + listDir[i].getName()));
				}
			}
			logger.info("Excel Files are deleted");

			logger.info("DD");
			response.sendRedirect("SuccessFile.html");
			logger.info("END");

			return;// TO avoid IllegalStateException in java servlet.

			// response.addHeader("Refresh","index.html");
			// request.getRequestDispatcher("index.html").forward(request,response);

		}
		catch (Exception ef)
		{
			ef.printStackTrace();
			logger.info("Exception occured at servlet() :" + ef);
		}
	}

	public String getBANK_NAME()
	{
		return BANK_NAME;
	}

	public void setBANK_NAME(String bANK_NAME)
	{
		BANK_NAME = bANK_NAME;
	}

	public String parseDate(String date)
	{
		return "";
	}

	public int[] searchWorkBookWrapper(File BANK_STATEMENT, XSSFSheet xsheet, HSSFSheet sheet, String propertyName)
	{
		if ("xlsx".equals(BANK_STATEMENT.getName().substring(BANK_STATEMENT.getName().lastIndexOf("."), BANK_STATEMENT.getName().length() - 1)))
		{
			return searchWorkBook(xsheet, propertyName);
		}
		else
		{
			return searchWorkBook(sheet, propertyName);
		}
	}

	// For Log4j
	public void init(ServletConfig config) throws ServletException
	{
		logger = Logger.getRootLogger();

	}

	// FOR TRANSFER OF FILES TO THE SAP SERVER.
	public void FtpFilesUpload()
	{
		String server = "192.168.0.37";
		int port = 21;
		String user = "7328";
		String pass = "hdfc12";

		FTPClient ftpClient = new FTPClient();
		try
		{
			ftpClient.connect(server, port);
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode))
			{

				logger.info("FTP server refused connection.");
				// throw new FTPException("FTP server refused connection.");

			}
			boolean loggin = ftpClient.login(user, pass);
			if (!loggin)
			{
				logger.info("FTP server failed to connect.");
			}
			else
			{
				logger.info("Connected to server.");
				logger.info("Connected to server.");
			}
			
			// this method switches data connection mode from server-to-client
			// (default mode)
			// to client-to-server which can pass through firewall.
			// There might be some connection issues if this method is not
			// invoked.
			// Use passive mode as default because most of us are
			// behind firewalls these days.
			ftpClient.enterLocalPassiveMode();

			// this method sets file type to be transferred, either as ASCII
			// text file or binary file.
			// It is recommended to set file type to FTP.BINARY_FILE_TYPE,
			// rather than FTP.ASCII_FILE_TYPE.
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			// This is the FTP server's directory
			// ftpClient.changeWorkingDirectory("C:\\Users\\DevendraSahu.Nichebi\\Music\\FTP_output");
			//ftpClient.changeWorkingDirectory("//Test");

			File tempOutputDir = new File(UPLOAD_DIRECTORY);
			System.out.println("upload directory"+UPLOAD_DIRECTORY);
			InputStream input = null;
			for (File f : tempOutputDir.listFiles())
			{
				String convertedFileName = null;
				convertedFileName = f.getName();

				input = new FileInputStream(UPLOAD_DIRECTORY + File.separator + convertedFileName);
				// store the file in the remote server
				ftpClient.storeFile(convertedFileName, input);
				logger.info("Successfully Uploaded File to Server :" + convertedFileName);
			}

			input.close();
			

			ftpClient.logout();
			ftpClient.disconnect();

		}
		catch (Exception fe)
		{
			fe.printStackTrace();
			logger.info("SAP server exception." + fe);

		}
		
		
		
	}
	
	


}
