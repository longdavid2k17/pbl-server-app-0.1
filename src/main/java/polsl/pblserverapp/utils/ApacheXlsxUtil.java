package polsl.pblserverapp.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApacheXlsxUtil
{
    private static final Logger logger = LoggerFactory.getLogger(ApacheXlsxUtil.class);
    public static final String xlsExtension = "xls";
    public static final String xlsxExtension = "xlsx";
    public static String format;

    public static boolean isExcelFile(MultipartFile file)
    {
        String extension = file.getOriginalFilename().split("\\.")[1];
        format = extension;
        if(!extension.equals(xlsExtension) && !extension.equals(xlsxExtension))
        {
            return false;
        }
        return true;
    }

    public static List<String> parseXlsxFile(InputStream file)
    {
        try
        {
            XSSFWorkbook wb = new XSSFWorkbook(OPCPackage.open(file));
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row;
            XSSFCell cell;

            int rows;
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0;
            int tmp;

            for(int i = 0; i < 10 || i < rows; i++)
            {
                row = sheet.getRow(i);
                if(row != null)
                {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if(tmp > cols) cols = tmp;
                }
            }


            //wczytywanie danych do polecenia

            List<String> params = new ArrayList<>();
            List<String> commandParts = new ArrayList<>();
            for(int r = 0; r < rows; r++)
            {
                row = sheet.getRow(r);
                if(row != null)
                {
                    String partOfCommand = new String();
                    for (int c = 0; c < cols; c++)
                    {
                        cell = row.getCell((short) c);
                        if (cell != null)
                        {
                            if (r == 0)
                            {
                                params.add(String.valueOf(cell));
                            }
                            else
                            {
                                commandParts.add(String.valueOf(cell));
                            }
                        }
                    }
                    if(!partOfCommand.equals(""))
                    {
                        commandParts.add(partOfCommand);
                    }

                }
            }


            //Składnie poleceń do jednego stringa

            List<String> fullCommands = new ArrayList<>();
            String buildedCommand = new String();
            int counter=0;
            for(int i=0;i<commandParts.size();i++)
            {
                if(counter==0)
                {
                    buildedCommand+=commandParts.get(i)+" ";
                }
                if(counter>=1)
                {
                    buildedCommand+=" "+params.get(counter-1)+commandParts.get(i)+" ";
                }

                if(counter==params.size())
                {
                    fullCommands.add(buildedCommand);
                    counter=0;
                    buildedCommand = new String();
                }
                else
                {
                    counter++;
                }
            }
            return fullCommands;
        }
        catch(Exception ioe)
        {
            logger.error(ioe.getMessage());
            return new ArrayList<>();
        }

    }

    public static List<String> parseXlsFile(InputStream file)
    {
        try
        {
            HSSFWorkbook wb=new HSSFWorkbook(file);
            HSSFSheet sheet=wb.getSheetAt(0);
            HSSFRow row;
            HSSFCell cell;

            int rows;
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0;
            int tmp;

            for(int i = 0; i < 10 || i < rows; i++)
            {
                row = sheet.getRow(i);
                if(row != null)
                {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if(tmp > cols) cols = tmp;
                }
            }

            List<String> params = new ArrayList<>();
            List<String> commandParts = new ArrayList<>();
            for(int r = 0; r < rows; r++)
            {
                row = sheet.getRow(r);
                if(row != null)
                {
                    String partOfCommand = new String();
                    for (int c = 0; c < cols; c++)
                    {
                        cell = row.getCell((short) c);
                        if (cell != null)
                        {
                            if (r == 0)
                            {
                                params.add(String.valueOf(cell));
                            }
                            else
                            {
                                commandParts.add(String.valueOf(cell));
                            }
                        }
                    }
                    if(!partOfCommand.equals(""))
                    {
                        commandParts.add(partOfCommand);
                    }

                }
            }
            List<String> fullCommands = new ArrayList<>();
            String buildedCommand = new String();
            int counter=0;
            for(int i=0;i<commandParts.size();i++)
            {
                if(counter==0)
                {
                    buildedCommand+=commandParts.get(i)+" ";
                }
                if(counter>=1)
                {
                    buildedCommand+=" "+params.get(counter-1)+commandParts.get(i)+" ";
                }

                if(counter==params.size())
                {
                    fullCommands.add(buildedCommand);
                    counter=0;
                    buildedCommand = new String();
                }
                else
                {
                    counter++;
                }
            }
            return fullCommands;
        }
        catch(Exception ioe)
        {
            logger.error(ioe.getMessage());
            return new ArrayList<>();
        }
    }
}
