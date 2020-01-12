import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.RefAddr;
//import javax.servlet.http.HttpServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.zip.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.dom4j.xpath.SubstringTest;

import com.sun.jmx.snmp.Timestamp;

import java.sql.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.xml.Log4jEntityResolver;
import org.apache.log4j.BasicConfigurator;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import sun.security.action.GetLongAction;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Servlet implementation class MT940_PARSER
 */
public class MT940_BKP_08Jan2016 extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MT940_BKP_08Jan2016()
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

	// PrintWriter out =null;

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

					if (rowCell.getCellType() == Cell.CELL_TYPE_STRING)
					{

						if (rowCell.getStringCellValue().contains(CONFIG_PROP.getProperty(propertyName)))
						{

							cellIndex[0] = rowCell.getRowIndex();
							cellIndex[1] = rowCell.getColumnIndex();
							/*
							 * if("SBI".equalsIgnoreCase(this.getBANK_NAME()) && row.getRowNum() ==18) { return cellIndex; }
							 */
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
							/*
							 * if("SBI".equalsIgnoreCase(this.getBANK_NAME()) && row.getRowNum() ==18) { return cellIndex; }
							 */
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

		// InputStream in =
		// this.getClass().getClassLoader().getResourceAsStream(BANK_NAME+".properties");

		BANK_STATEMENT = new File(subDirName + File.separator + FILE_NAME);
		String OutputFileName = "";

		// BANK_STATEMENT=new
		// File(getServletContext().getRealPath("MT940_STATEMENTS"+File.separator+BANK_NAME+".xls"));

		try
		{
			in = getClass().getResourceAsStream(BANK_NAME + ".properties");
			// in =
			// getServletContext().getResourceAsStream("PROPERTY_FILES"+File.separator+BANK_NAME+".properties");

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
			// if("xlsx".equals(BANK_STATEMENT.getName().substring(BANK_STATEMENT.getName().lastIndexOf("."),
			// BANK_STATEMENT.getName().length())))
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

			String DATACELL = CONFIG_PROP.getProperty("DATACELL");

			if (DATACELL.equalsIgnoreCase("ADJACENT"))
			{

				if (cellIndex_20[0] != -1 && cellIndex_20[1] != -1)
				{
					// We need to write logic to remove leading zeroes in
					// Account Number according to Amiraj
					MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue().replaceAll("_", "") + System.getProperty("line.separator");
					MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue().replaceAll("_", "") + System.getProperty("line.separator");
					accNo = MT940_CONTENT.substring(9, MT940_CONTENT.length() - 1);

					OutputFileName = sheet.getRow(cellIndex_20[0]).getCell((cellIndex_20[1] + 1)).getStringCellValue() + System.getProperty("line.separator");

					if (CONFIG_PROP.getProperty("28C").equalsIgnoreCase("EMPTY"))
					{
						// MT940_CONTENT+=":28C:"+System.getProperty("line.separator");
						/*
						 * Properties props=new Properties(); InputStream fis=getServletContext ().getResourceAsStream("PROPERTY_FILES" +File.separator+"STATEMENT_NUMBERS.properties");
						 * props.load(fis); fis.close(); int STATEMENT_NUMBER=Integer .parseInt(props.getProperty(BANK_NAME)); //logger.info(STATEMENT_NUMBER);
						 */
						MT940_CONTENT += ":28C:" + STATEMENT_NUMBER + System.getProperty("line.separator");

						/*
						 * File f=new File(getServletContext().getRealPath("PROPERTY_FILES" +File.separator+"STATEMENT_NUMBERS.properties")); FileOutputStream fos=new FileOutputStream(f);
						 * props.setProperty(BANK_NAME, String.valueOf(++STATEMENT_NUMBER)); props.store(fos, null); fos.close();
						 */
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
					}
					else if ("ICICI".equals(BANK_NAME))
					{
						MT940_CONTENT += ":20:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim() + System.getProperty("line.separator");
						OutputFileName = sheet.getRow(cellIndex_20[0]).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[2].trim();
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
						/*
						 * Properties props=new Properties(); InputStream fis=getServletContext ().getResourceAsStream("PROPERTY_FILES" +File.separator+"STATEMENT_NUMBERS.properties");
						 * props.load(fis); fis.close(); int STATEMENT_NUMBER=Integer .parseInt(props.getProperty(BANK_NAME));
						 */
						MT940_CONTENT += ":28C:" + STATEMENT_NUMBER + System.getProperty("line.separator");
						/*
						 * File f=new File(getServletContext().getRealPath("PROPERTY_FILES" +File.separator+"STATEMENT_NUMBERS.properties")); FileOutputStream fos=new FileOutputStream(f);
						 * props.setProperty(BANK_NAME, String.valueOf(++STATEMENT_NUMBER)); props.store(fos, null); fos.close();
						 */

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
						// MT940_CONTENT+=":20:"+sheet.getRow(cellIndex_20[0]+1).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim()+System.getProperty("line.separator");
						// MT940_CONTENT+=":25:"+sheet.getRow(cellIndex_20[0]+1).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim()+System.getProperty("line.separator");
						MT940_CONTENT += ":20:" + String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue())) + System.getProperty("line.separator");
						MT940_CONTENT += ":25:" + String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue())) + System.getProperty("line.separator");

						// OutputFileName =
						// sheet.getRow(cellIndex_20[0]+1).getCell(cellIndex_20[1]).getStringCellValue().split(" ")[5].trim();
						OutputFileName = String.valueOf(new BigDecimal(sheet.getRow(cellIndex_20[0] + 1).getCell(cellIndex_20[1]).getNumericCellValue()));
					}
				}

			}

			int cellIndex_60F[] = searchWorkBook(sheet, "60F");
			// This is only for CANARA BANK.
			if (cellIndex_60F[0] == -1)
			{
				cellIndex_60F = searchWorkBook(sheet, "temp");
				;
			}

			int cellIndex_60F_DATE[] = searchWorkBook(sheet, "OPENING_BALANCE_DATE");

			int cellIndex_61[] = { -1, -1 };
			int cellIndex_86[] = { -1, -1 };

			DateFormat OPENING_BALANCE_DATE_FORMAT = new SimpleDateFormat("yyMMdd");

			Cell OPENING_BALANCE_CELL = null;
			Cell OPENING_BALANCE_DATE_CELL = null;

			MT940_CONTENT += ":60F:C";

			if (CONFIG_PROP.getProperty("EMPTY_ROW_AFTER_TRANS_HEADER").equalsIgnoreCase("TRUE"))
			{
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-TRUE"+(cellIndex_60F_DATE[0]+2)+","+cellIndex_60F_DATE[1]);
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-TRUE"+cellIndex_60F[0]+1+","+cellIndex_60F[1]);
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
			else
			{
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-FALSE"+cellIndex_60F_DATE[0]+1+","+cellIndex_60F_DATE[1]);
				// logger.info("EMPTY_ROW_AFTER_TRANS_HEADER-FALSE"+cellIndex_60F[0]+1+","+cellIndex_60F[1]);
				OPENING_BALANCE_DATE_CELL = sheet.getRow(cellIndex_60F_DATE[0] + 1).getCell(cellIndex_60F_DATE[1]);

				cellIndex_61[0] = cellIndex_60F_DATE[0] + 1;
				cellIndex_86[0] = cellIndex_60F_DATE[0] + 1;

			}

			cellIndex_61[1] = Integer.parseInt(CONFIG_PROP.getProperty("61"));
			cellIndex_86[1] = Integer.parseInt(CONFIG_PROP.getProperty("86"));

			switch (OPENING_BALANCE_DATE_CELL.getCellType())
			{

				case Cell.CELL_TYPE_NUMERIC:
					MT940_CONTENT += OPENING_BALANCE_DATE_FORMAT.format(OPENING_BALANCE_DATE_CELL.getDateCellValue()) + "INR";
					break;

				case Cell.CELL_TYPE_STRING:
					// if(OPENING_BALANCE_DATE_CELL.getStringCellValue().contains("/"))
					// MT940_CONTENT+=OPENING_BALANCE_DATE_FORMAT.format(new
					// SimpleDateFormat("dd/MM/yyyy").parse(OPENING_BALANCE_DATE_CELL.getStringCellValue()))+"INR";
					// else
					// MT940_CONTENT+=OPENING_BALANCE_DATE_FORMAT.format(OPENING_BALANCE_DATE_FORMAT.parse(OPENING_BALANCE_DATE_CELL.getStringCellValue()))+"INR";
					MT940_CONTENT += OPENING_BALANCE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(OPENING_BALANCE_DATE_CELL.getStringCellValue())) + "INR";
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
						// MT940_CONTENT+=new
						// BigDecimal(OPENING_BALANCE_CELL.getNumericCellValue()).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
						break;

					case Cell.CELL_TYPE_STRING:
						getCR_DR_Amount(OPEN_BALANCE_ROW_NUMBER, sheet, new Double(OPENING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" CR", "").replace(" DR", "").replaceAll(String.valueOf((char) 160), "").trim()).doubleValue());
						// MT940_CONTENT+=new BigDecimal(new
						// Double(OPENING_BALANCE_CELL.getStringCellValue().replaceAll(",","").replace(" CR",
						// "").replace(" DR",
						// "").trim())).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
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
						// MT940_CONTENT+=new
						// BigDecimal(OPENING_BALANCE_CELL.getNumericCellValue()).setScale(2,BigDecimal.ROUND_HALF_EVEN)+System.getProperty("line.separator");
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

			int VALUE_DATE_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("VALUE_DATE"));
			int TRANS_DATE_INDEX = Integer.parseInt(CONFIG_PROP.getProperty("TRANS_DATE"));
			int CHEQUE_NUMBER = Integer.parseInt(CONFIG_PROP.getProperty("CHEQUE_NUMBER"));
			int DEBIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("DEBIT"));
			int CREDIT_BALANCE = Integer.parseInt(CONFIG_PROP.getProperty("CREDIT"));
			int CR_DR_INDICATOR = -1;
			int DESCRIPTION = Integer.parseInt(CONFIG_PROP.getProperty("DESCRIPTION"));
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
			String CREDIT_DEBIT_MARK = new String();

			for (int i = cellIndex_61[0]; i <= sheet.getLastRowNum(); i++)
			{
				try
				{

					String END_OF_FILE_CONTENT = CONFIG_PROP.getProperty("END_OF_FILE_CONTENT");
					// logger.info(END_OF_FILE_CONTENT);

					// logger.info("row,col:       "+cellIndex_60F_DATE[0]+","+cellIndex_60F_DATE[1]);
					// logger.info("row number       "+i);

					Cell NEXT_ROW_CELL = sheet.getRow(i).getCell(cellIndex_60F_DATE[1], Row.RETURN_BLANK_AS_NULL);

					String NEXT_ROW_CELL_CONTENT = new String();
					if (NEXT_ROW_CELL == null)
					{
						break;
					}
					if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
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
								MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(VALUE_DATE_CELL.getDateCellValue());
								break;

							case Cell.CELL_TYPE_STRING:
								// if(VALUE_DATE_CELL.getStringCellValue().contains("/"))
								// {
								// CLOSING_DATE=VALUE_DATE_FORMAT.format(new
								// SimpleDateFormat("dd/MM/yyyy").parse(VALUE_DATE_CELL.getStringCellValue()));
								// MT940_CONTENT+=":61:"+VALUE_DATE_FORMAT.format(new
								// SimpleDateFormat("dd/MM/yyyy").parse(VALUE_DATE_CELL.getStringCellValue()))+"INR";
								// }
								// else
								// {
								// CLOSING_DATE=VALUE_DATE_FORMAT.format(VALUE_DATE_FORMAT.parse(VALUE_DATE_CELL.getStringCellValue()));
								// MT940_CONTENT+=":61:"+VALUE_DATE_FORMAT.format(VALUE_DATE_FORMAT.parse(VALUE_DATE_CELL.getStringCellValue()))+"INR";
								// }

								CLOSING_DATE = VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));
								MT940_CONTENT += ":61:" + VALUE_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(VALUE_DATE_CELL.getStringCellValue()));

								break;

						}

						switch (TRANS_DATE_CELL.getCellType())
						{

							case Cell.CELL_TYPE_NUMERIC:
								MT940_CONTENT += TRANS_DATE_FORMAT.format(TRANS_DATE_CELL.getDateCellValue());
								break;

							case Cell.CELL_TYPE_STRING:
								// if(TRANS_DATE_CELL.getStringCellValue().contains("/"))
								// {
								// MT940_CONTENT+=TRANS_DATE_FORMAT.format(new
								// SimpleDateFormat("dd/MM/yyyy").parse(TRANS_DATE_CELL.getStringCellValue()));
								// }
								// else
								// {
								// MT940_CONTENT+=TRANS_DATE_FORMAT.format(TRANS_DATE_FORMAT.parse(TRANS_DATE_CELL.getStringCellValue()));
								// }

								MT940_CONTENT += TRANS_DATE_FORMAT.format(new SimpleDateFormat(CONFIG_PROP.getProperty("STATEMENT_DATE_FORMAT")).parse(TRANS_DATE_CELL.getStringCellValue()));
								break;

						}

						if (CONFIG_PROP.getProperty("CR_DR_ON_SAME_COL").equalsIgnoreCase("TRUE"))
						{
							CR_DR_INDICATOR = Integer.parseInt(CONFIG_PROP.getProperty("CR_DR_INDICATOR"));
							CR_DR_INDICATOR_CELL = sheet.getRow(i).getCell(CR_DR_INDICATOR, Row.RETURN_BLANK_AS_NULL);
							String statement = CR_DR_INDICATOR_CELL.getStringCellValue().trim();

							if (statement.equalsIgnoreCase("CR") || statement.equalsIgnoreCase("C"))
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
										if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
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

									if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
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
							// Temp Comment to check the SBI file
							/*
							 * if(CHEQUE_NUMBER_CELL!=null) { switch(CHEQUE_NUMBER_CELL.getCellType()) { case Cell.CELL_TYPE_NUMERIC: MT940_CONTENT+=CHEQUE_NUMBER_CELL .getNumericCellValue(); break;
							 * case Cell.CELL_TYPE_STRING: MT940_CONTENT+=CHEQUE_NUMBER_CELL .getStringCellValue(); break; } } else { //MT940_CONTENT+="NONREF"; MT940_CONTENT+="NOREF"; }
							 */
							// Temp Comment to check the SBI file
							if ("SBI".equalsIgnoreCase(BANK_NAME))
							{
								if (DESCRIPTION_CELL != null)
								{
									switch (DESCRIPTION_CELL.getCellType())
									{
										case Cell.CELL_TYPE_NUMERIC:

											break;
										case Cell.CELL_TYPE_STRING:
											/*
											 * description = DESCRIPTION_CELL.getStringCellValue (); chequeNoCell = CHEQUE_NUMBER_CELL. getStringCellValue();
											 */
											if (description.trim().startsWith("BY TRANSFER-NEFT"))
											{
												descArr = description.split("\\*");
												refNo = descArr[2];
												// logger.info(refNo);
											}
											else if (description.trim().startsWith("BY TRANSFER-INB"))
											{
												descArr = chequeNoCell.trim().split(" ");
												// refNo =
												// description.substring(description.indexOf("ITA"),
												// description.indexOf("ITA") + 10
												// );
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
											/*
											 * else if(description.startsWith("DEBIT-TR TO" )) { refNo = description.trim().substring ("DEBIT-TR TO " .length(),"DEBIT-TR TO ".length() + 11); }
											 */
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
								if (description.startsWith("BY INST"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim().replaceFirst("^0+(?!$)", "");
								}
							}
							else if ("CANARABANK".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("RTGSIW:"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("NEFT"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[2].trim();
								}
								else if (description.startsWith("By Clg") || description.startsWith("MB-IMPS") || description.startsWith("MB-IMPS") || description.startsWith("Online Cheque Return"))
								{
									refNo = chequeNoCell.trim();
								}
							}

							else if ("PNB".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("Dr.for RTGS Customer"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("Chrgs for RTGS Cust Pymnt"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("BY CLG/CHQ"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim().split(" ")[1].trim();
								}
								else if (description.startsWith("RTGS/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[2].trim();
								}
							}
							else if ("BOI".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("BY CLG/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
								if (description.startsWith("BY CLG-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[2].trim();
								}
								if (description.startsWith("IMPS/RRN:"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].split(":")[1].trim();
								}
							}
							else if ("BOB".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("BY INST"))
								{
									refNo = description.substring("BY INST ".length(), description.indexOf(":"));
								}
								if (description.startsWith("NEFT"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								if (description.startsWith("RTGS"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								if (description.startsWith("BY CLG/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
							}

							else if ("SBH".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("CHQ TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.startsWith("   TO TRANSFER-INB--"))
								{
									descArr = chequeNoCell.trim().split(" ");
									refNo = descArr[0].trim();
								}
								else if (description.startsWith("BY CLEARING"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("   BY TRANSFER-NEFT "))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.startsWith("   BY TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
							}

							else if ("SYND".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("NEFT"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[1].trim();
								}
							}

							else if ("INDUSIND".equalsIgnoreCase(BANK_NAME))
							{
								// Need to read pdf and write Complete logic
							}

							else if ("SBBJ".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("CHQ TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
								else if (description.startsWith("CAS PRES CHQ"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split("--");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("   BY TRANSFER-RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[2].trim();
								}
							}
							else if ("BOM".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("BY TRF CORR RTGS"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[4].trim();
								}
								else if (description.startsWith("CHQ TRANSFER"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[3].trim();
								}
							}
							else if ("CBI".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("BY CLEARING /"))
								{
									refNo = chequeNoCell.trim();
								}
								else if (description.startsWith("BY TRANSFER"))
								{
									descArr = description.trim().split(" ");
									refNo = descArr[descArr.length - 1].trim();
								}
								else if (description.startsWith("CHEQUE DEPOSIT"))
								{
									refNo = chequeNoCell.trim();
								}
								else if (description.startsWith("OUT-CHQ RETURN"))
								{
									refNo = chequeNoCell.trim();
								}
							}

							else if ("OBC".equalsIgnoreCase(BANK_NAME))
							{
								String chequeNoValue = chequeNoCell.trim();
								if ("".equalsIgnoreCase(chequeNoValue))
								{
									if (description.startsWith("BY INST"))
									{
										descArr = description.trim().split(" ");
										refNo = descArr[2].trim();
									}
								}
								else
								{
									refNo = chequeNoValue;
								}
							}
							else if ("ICICI".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("RTGS:"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].split("/")[0].trim();
								}
								else if (description.startsWith("RTGS-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("NEFT-"))
								{
									descArr = description.trim().split("-");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("BIL/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim();
								}
								else if (description.startsWith("REJECT:"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
								else if (description.contains("/"))
								{
									descArr = description.trim().split("/");
									refNo = descArr[1].trim().replaceFirst("^0+(?!$)", "");
								}
								/*
								 * else if(description.contains("\\")) { descArr = description.trim().split("\\"); refNo = descArr[2].trim().replaceFirst("^0+(?!$)", ""); }
								 */

								// .replaceFirst("^0+(?!$)", "");
							}
							else if ("SYNDICATE".equalsIgnoreCase(BANK_NAME))
							{
								/*
								 * if(description.startsWith(""))//Condition is not know for this { descArr = description.trim().split("-"); refNo = descArr[1].trim(); }
								 */
							}
							else if ("VIJAYA".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("Chrgs for RTGS"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].trim();
								}
							}
							else if ("CORPORATION".equalsIgnoreCase(BANK_NAME))
							{
								if (description.startsWith("RTGS"))
								{
									descArr = description.trim().split(":");
									refNo = descArr[1].split(" ")[0].trim();
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
									transRef = TRANS_REF_CELL.getStringCellValue();
									String NewRef = (transRef.indexOf("|") == -1) ? transRef : transRef.substring(transRef.indexOf("|") + 1, transRef.length());
									// String
									// NewRef=transRef.substring(transRef.indexOf("|")+1,
									// transRef.length());
									refNo = NewRef;
								}

							}

							if (refNo != null && !"".equals(refNo))
							{
								if (refNo.length() > 16)
								{
									refNo = refNo + "//";
								}
								MT940_CONTENT += refNo;
							}
							else
							{
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
									/*
									 * if(temp86.length() > 65) { temp86 = temp86.substring(0, 64); }
									 */
									/*
									 * if(temp86.length() > 50) { temp86 = temp86.substring(0, 49); }
									 */
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
									CLOSING_BALANCE = String.valueOf(new BigDecimal(CLOSING_BALANCE_CELL.getStringCellValue().replaceAll(",", "").replace(" CR", "").replace(" DR", "").replaceAll(String.valueOf((char) 160), "").trim()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).replace(".", ",");
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
			DOWNLOAD_FILE = UPLOAD_DIRECTORY + File.separator + OutputFileName.trim() + ".txt";

			/*
			 * if (BANK_NAME.equalsIgnoreCase("SBI")) { OutputFileName=OutputFileName.substring(1); DOWNLOAD_FILE=UPLOAD_DIRECTORY +File.separator+OutputFileName.trim()+".txt"; }
			 */
			// As requested by amiraj to remove the leading _ for account no.
			// 1.07.15
			if (OutputFileName.contains("_"))
			{
				OutputFileName = OutputFileName.substring(1);
				DOWNLOAD_FILE = UPLOAD_DIRECTORY + File.separator + OutputFileName.trim() + ".txt";
			}

			File f = new File(DOWNLOAD_FILE);
			// Check if file exist or not ,if exist show error .
			if (f.exists())
			{
				// throw new AccountNoAlreadyExist
				// ("You have uploaded mutiple files for account number");
				accountCount++;
				// response.sendRedirect("/WEB-INF/ErrorFile.html");
				/*
				 * String sub=DOWNLOAD_FILE.substring(0, DOWNLOAD_FILE.length()-4); //out.print("ABCD"); DOWNLOAD_FILE=sub+"_"+i+".txt"; f=new File(DOWNLOAD_FILE);
				 */
			}
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(MT940_CONTENT.getBytes());
			fos.close();

			// Commenting the below line to make the output file name as account
			// no as requested by amiraj in ticket #68127
			// DOWNLOAD_FILE=FILE_NAME.substring(0,FILE_NAME.indexOf("."))+".txt";
			// DOWNLOAD_FILE=OutputFileName.replaceAll("(\\r|\\n)", "")+".txt";
			DOWNLOAD_FILE = OutputFileName.trim() + ".txt";
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
		Cell DEBIT_BALANCE_CELL = sheet.getRow(OPEN_BALANCE_ROW_NUMBER).getCell(Integer.parseInt(CONFIG_PROP.getProperty("DEBIT")), Row.RETURN_BLANK_AS_NULL);
		Cell CREDIT_BALANCE_CELL = sheet.getRow(OPEN_BALANCE_ROW_NUMBER).getCell(Integer.parseInt(CONFIG_PROP.getProperty("CREDIT")), Row.RETURN_BLANK_AS_NULL);

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
						if (!CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("0") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("            ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase(" - ") && !CREDIT_BALANCE_CELL.getStringCellValue().equalsIgnoreCase("-  "))
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

			/*
			 * String login="abc"; request.setAttribute("myname",login); request.getRequestDispatcher("FailureFile.jsp").forward(request, response);
			 */

			// out = response.getWriter();
			// commented on 3-06-2015 for ZIP
			// String DIRECTORY0=request.getSession().getServletContext().getRealPath("MT940_STATEMENTS");
			// timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			// String temp=UPLOAD_DIRECTORY +"\\"+ timeStamp;
			// String temp=DIRECTORY0 +"\\"+ "aaa";
			// String dirLocation=DIRECTORY0 +"\\"+ timeStamp;
			// String baseDir="MT940_STATEMENTS\\"+timeStamp;

			// FOR WINDOWS
			// UPLOAD_DIRECTORY="F:\\MT940_OUTPUT";
			// UPLOAD_DIRECTORY="//opt//PHO//MT940_output";

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
			// File dir=new File(dirLocation);
			// dir.mkdirs();

			// UPLOAD_DIRECTORY=request.getSession().getServletContext().getRealPath("MT940_STATEMENTS");
			// UPLOAD_DIRECTORY=request.getSession().getServletContext().getRealPath(baseDir);
			String BANK_NAME = new String();

			// logger.info("BBB");
			// To clean the output directory before every iteration.

			FileUtils.cleanDirectory(new File(UPLOAD_DIRECTORY));

			// List<String> FileNameAll=new ArrayList<String>();

			// String s[]=new String[20];

			// process only if its multipart content

			// commented for Folder Logic

			/*
			 * if(ServletFileUpload.isMultipartContent(request)) { try { List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request); for(FileItem item :
			 * multiparts){ if(item.isFormField()){ if(item.getFieldName().equalsIgnoreCase("BANK")) { BANK_NAME=item.getString(); this.setBANK_NAME(BANK_NAME); } } else { FILE_NAME = new
			 * File(item.getName()).getName(); FILE_NAME = FilenameUtils.getName(FILE_NAME); FileNameAll.add(FILE_NAME); item.write( new File(UPLOAD_DIRECTORY + File.separator + FILE_NAME)); } } }
			 * catch (Exception ex) { request.setAttribute("message", "File Upload Failed due to " + ex); ex.printStackTrace(); } } else {
			 * request.setAttribute("message","Sorry this Servlet only handles file upload request"); }
			 */

			// FOR CONNECTING TO MySql DB
			// logger.info("CONNECTING TO MYSQL");
			try
			{
				// Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				// Open a connection
				// mySqlConn=DriverManager.getConnection("jdbc:mysql://172.29.7.94/MT940_DB","mt940admin","mt940admin");//FOR PROD
				// mySqlConn=DriverManager.getConnection("jdbc:mysql://localhost/statement_number","root","");//FOR DEV
				// mySqlConn=DriverManager.getConnection("jdbc:mysql://localhost/MT940_DB","root","");//FOR UAT
				if (sys.contains("Win"))
				{
					mySqlConn = DriverManager.getConnection("jdbc:mysql://localhost/MT940_DB", "root", "");// FOR DEV
					// mySqlConn=DriverManager.getConnection("jdbc:mysql://172.29.10.77/MT940_DB","mt940admin","mt940admin");//UAT
				}
				else
				{
					// mySqlConn=DriverManager.getConnection("jdbc:mysql://172.29.7.94/MT940_DB","mt940admin","mt940admin");//FOR PROD
					mySqlConn = DriverManager.getConnection("jdbc:mysql://localhost/MT940_DB", "mt940admin", "mt940admin");// FOR PROD
				}

				logger.info("CONNECTED" + mySqlConn);
				// logger.info("CONNECTED"+mySqlConn);
				// Execute SQL query
				myStmt = mySqlConn.createStatement();
				// String query="SELECT *  from  statement_count where ";
				// String query="SELECT * FROM statement_count  WHERE BANK_NAME='"+BANK_NAME+"'";

				// Process the result
				// myRs =myStmt.executeQuery(query);

				// Extract Results
				/*
				 * while(myRs.next()) { //Retrieve by column name String bank = myRs.getString("BANK_NAME"); STATEMENT_NUMBER = myRs.getInt("COUNT"); //count++; // String
				 * query2="UPDATE statement_count set COUNT='"+count+"' WHERE BANK_NAME='"+BANK_NAME+"'"; //myStmt.executeUpdate(query2); //myRs.update // String fimyRst = myRs.getString("fimyRst");
				 * // String last = myRs.getString("last"); //Display values logger.info("Bank Name: " + bank ); logger.info(" Statement Number: " + STATEMENT_NUMBER ); //logger.info(", FimyRst: " +
				 * fimyRst + "<br>"); // logger.info(", Last: " + last + "<br>"); }
				 */

			}
			catch (Exception se)
			{
				se.printStackTrace();
				logger.error("Exception for DB connection :" + se);

			}
			// MT940 statements location
			// File dirSub = new File("F:\\MT940_STATEMENTS");
			// File dirSub = new File("//opt//PHO//MT940_Uploads");
			File dirSub = new File(MainDir);
			logger.info("Main directory  :" + dirSub);
			// int numberOfSubfolders=0;
			File listDir[] = dirSub.listFiles();
			/*
			 * for (int i = 0; i < listDir.length; i++) { if (listDir[i].isDirectory()) { numberOfSubfolders++; } } logger.info("No of dir " + numberOfSubfolders);
			 */

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
				}

			}

			// java.util.Date dat= new java.util.Date();
			// Timestamp d= new Timestamp(dat.getTime());

			// SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			// String string = dateFormat.format(new Date());
			// logger.info(string);

			// String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			// File dir=new File(timeStamp);
			// dir.mkdirs();
			// OUTPUT_ZIP_FILE = UPLOAD_DIRECTORY + "\\download.zip" ;
			// SOURCE_FOLDER = UPLOAD_DIRECTORY;
			// MT940 appZip = new MT940();
			// fileList = new ArrayList<String>();
			// .generateFileList(new File(SOURCE_FOLDER));
			// fileList.zipIt(OUTPUT_ZIP_FILE);

			// String zipFile = UPLOAD_DIRECTORY + "\\download.zip" ;
			// String sourceDir = UPLOAD_DIRECTORY;

			/*
			 * ZipStart(); // DOWNLOAD_FILE=UPLOAD_DIRECTORY+"\\DOWN_"+timeStamp+".zip"; //DOWNLOAD_FILE=UPLOAD_DIRECTORY+"\\ABCD_"+timeStamp+".zip";
			 * DOWNLOAD_FILE=UPLOAD_DIRECTORY+"\\"+BANK_NAME+"_"+timeStamp+".zip"; // DOWNLOAD_FILE=timeStamp+".zip"; BufferedInputStream buf = null; ServletOutputStream myOut = null; try { myOut =
			 * response.getOutputStream(); File myfile = new File(DOWNLOAD_FILE); if (myfile.exists()) { //myfile.createNewFile(); //set response headers // response.setHeader("Cache-Control",
			 * "max-age=60"); //response.setHeader("Cache-Control", "must-revalidate"); String downloadFileName=DOWNLOAD_FILE.substring(153); response.setContentType("application/zip");
			 * //response.addHeader("Content-Disposition", "attachment; filename=" +DOWNLOAD_FILE); response.addHeader("Content-Disposition", "attachment; filename=" +downloadFileName);
			 * response.setContentLength((int) myfile.length()); FileInputStream input = new FileInputStream(myfile); buf = new BufferedInputStream(input); int readBytes = 0; //read from the file;
			 * write to the ServletOutputStream while ((readBytes = buf.read()) != -1) { myOut.write(readBytes); } } } catch (Exception exp) { } finally { //close the input/output streams if (myOut !=
			 * null) { try { myOut.close(); } catch (IOException ex) { } } if (buf != null) { try { buf.close(); } catch (IOException ex) { } } }
			 */
			/*
			 * try { //response.setContentType("application/zip"); response.setHeader("Content-Encoding", "gzip"); response.setHeader("Content-Disposition","attachment; filename="+DOWNLOAD_FILE);
			 * byte[] arBytes = new byte[30000]; FileInputStream is = new FileInputStream(DOWNLOAD_FILE); is.read(arBytes); ServletOutputStream op = response.getOutputStream(); op.write(arBytes);
			 * op.flush(); is.close(); } catch (Exception ex) { request.setAttribute("message", "CAN NOT DOWNLOAD " + ex); }
			 */
			/*
			 * try { response.setContentType("application/zip"); response.addHeader("Content-Disposition","attachment; filename="+DOWNLOAD_FILE); ServletOutputStream ouputStream=
			 * response.getOutputStream(); //ZipOutputStream out = new ZipOutputStream(ouputStream); ZipOutputStream out = new ZipOutputStream(response.getOutputStream()); out.putNextEntry(new
			 * ZipEntry(DOWNLOAD_FILE)); out.finish(); out.close(); } catch (Exception ex) { request.setAttribute("message", "CAN NOT DOWNLOAD " + ex); }
			 */
			/*
			 * //response.setContentType("text/plain"); response.setContentType("application/zip"); response.setHeader("Content-Disposition","attachment;filename="+DOWNLOAD_FILE); ServletContext ctx =
			 * getServletContext(); InputStream is = ctx.getResourceAsStream("/MT940_STATEMENTS/"+DOWNLOAD_FILE); int BYTES_DOWNLOAD = 1024; int read=0; byte[] bytes = new byte[BYTES_DOWNLOAD];
			 * OutputStream os = response.getOutputStream(); while((read = is.read(bytes))!= -1) { os.write(bytes, 0, read); } os.flush(); os.close();
			 */

			// PrintWriter out=response.getWriter();
			// out.println("<script>window.location.href='MT940_STATEMENTS/"+DOWNLOAD_FILE+"'</script>");
			// appZip.generateFileList(new File(SOURCE_FOLDER));
			// appZip.zipIt(OUTPUT_ZIP_FILE);

			/*
			 * String query2="UPDATE statement_count set COUNT='"+STATEMENT_NUMBER+"' WHERE BANK_NAME='"+BANK_NAME+"'"; myStmt.executeUpdate(query2);
			 */

			mySqlConn.close();
			myStmt.close();
			myRs.close();

			if (sys.contains("Win"))
			{
				logger.info("FTP not available Windows System");

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
			ftpClient.changeWorkingDirectory("//Test");

			File tempOutputDir = new File(UPLOAD_DIRECTORY);
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

	/*
	 * protected void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException { res.setContentType( "text/html" ); // processing the request not shown... int
	 * bar=3; // here we decide to send the value "bar" in parameter // "foo" to the JSP page example.jsp: res.sendRedirect( "FailureFile.jsp?foo=bar" ); }
	 */
}

// for zip
/*
 * public void ZipStart() { //String zipFileName="ABCD_"+timeStamp+".zip"; String zipFileName=BANK_NAME+"_"+timeStamp+".zip"; String zipFile = UPLOAD_DIRECTORY + "\\"+zipFileName; String sourceDir =
 * UPLOAD_DIRECTORY; try { //create object of FileOutputStream FileOutputStream fout = new FileOutputStream(zipFile); //create object of ZipOutputStream from FileOutputStream ZipOutputStream zout =
 * new ZipOutputStream(fout); //create File object from source directory File fileSource = new File(sourceDir); addDirectory(zout, fileSource); //close the ZipOutputStream zout.close();
 * logger.info("Zip file has been created!"); } catch(IOException ioe) { logger.info("IOException :" + ioe); } } private static void addDirectory(ZipOutputStream zout, File fileSource) { //get
 * sub-folder/files list File[] files = fileSource.listFiles(); logger.info("Adding directory " + fileSource.getName()); for(int i=0; i < files.length; i++) { //if the file is directory, call the
 * function recursively if(files[i].isDirectory()) { addDirectory(zout, files[i]); continue; } we are here means, its file and not directory, so add it to the zip file try {
 * if((files[i].getName().substring(files[i].getName().length()-4).equalsIgnoreCase (".txt"))) { logger.info("Adding file " + files[i].getName()); //create byte buffer byte[] buffer = new byte[1024];
 * //create object of FileInputStream FileInputStream fin = new FileInputStream(files[i]); zout.putNextEntry(new ZipEntry(files[i].getName())); After creating entry in the zip file, actually write the
 * file. int length; while((length = fin.read(buffer)) > 0) { zout.write(buffer, 0, length); } After writing the file to ZipOutputStream, use void closeEntry() method of ZipOutputStream class to close
 * the current entry and position the stream to write the next entry. zout.closeEntry(); //close the InputStream fin.close(); } else { logger.info("EXCEL FILE SKIPED"); } } catch(IOException ioe) {
 * logger.info("IOException :" + ioe); } } }
 */
// FOR Folder Navigation
/*
 * public void navigate(File dir) { String[] dirContent=dir.list(); for (int j=0;j<dirContent.length;j++) { logger.info(" "+dirContent[j]); File child=new File(dir,dirContent[j]);
 * if(child.isDirectory())navigate(child); } }
 */

/*
 * public void zipIt(String zipFile){ byte[] buffer = new byte[1024]; try{ FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos);
 * logger.info("Output to Zip : " + zipFile); for(String file : this.fileList){ logger.info("File Added : " + file); ZipEntry ze= new ZipEntry(file); zos.putNextEntry(ze); FileInputStream in = new
 * FileInputStream(SOURCE_FOLDER + File.separator + file); int len; while ((len = in.read(buffer)) > 0) { zos.write(buffer, 0, len); } in.close(); } zos.closeEntry(); //remember close it zos.close();
 * logger.info("Done"); }catch(IOException ex){ ex.printStackTrace(); } } public void generateFileList(File node) { //add file only if(node.isFile()) {
 * fileList.add(generateZipEntry(node.getAbsoluteFile().toString())); } if(node.isDirectory()) { String[] subNote = node.list(); for(String filename : subNote) { generateFileList(new File(node,
 * filename)); } } } private String generateZipEntry(String file) { return file.substring(SOURCE_FOLDER.length()+1, file.length()); } public void test() { fileList = new ArrayList<String>(); }
 */

