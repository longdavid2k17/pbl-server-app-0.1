package polsl.pblserverapp.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import polsl.pblserverapp.dao.SwitchParameterRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.model.SwitchParameter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ApacheCommonsCsvUtil
{
    private static final Logger log = LoggerFactory.getLogger(ApacheCommonsCsvUtil.class);
    private static String csvExtension = "csv";
    private static SwitchParameterRepository switchParameterRepository;

    public ApacheCommonsCsvUtil(SwitchParameterRepository switchParameterRepository)
    {
        this.switchParameterRepository = switchParameterRepository;
    }

    public static void shapesToCsv(Writer writer, List<Shape> shapeList) throws IOException
    {
        try (CSVPrinter csvPrinter = new CSVPrinter(writer,
                CSVFormat.DEFAULT.withHeader("shapeId", "name", "creationDate", "command","parametersList")))
        {
            if(shapeList.size()==0)
            {
                throw new Exception("No shape can be exported. List is empty!");
            }
            else
            {
                for (Shape shape : shapeList)
                {
                    List<String> data = Arrays.asList(String.valueOf(shape.getShapeId()), String.valueOf(shape.getName()),
                            String.valueOf(shape.getCreationDate()), String.valueOf(shape.getCommand()),String.valueOf(shape.getParametersList().toString()));
                    csvPrinter.printRecord(data);
                }
                csvPrinter.flush();
                log.info("Exporting .csv file ended successfully!");
            }
        }
        catch (Exception e)
        {
            log.error("Error while exporting file! Code: "+e.getMessage());
        }
    }

    public static List<Shape> parseCsvFile(InputStream is)
    {
        BufferedReader fileReader = null;
        CSVParser csvParser = null;
        List<Shape> shapeList = new ArrayList<Shape>();

        try
        {
            fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            csvParser = new CSVParser(fileReader,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords)
            {
                //List<SwitchParameter> switchParameters = String.valueOf(csvRecord.get("parametersList"));
                List<SwitchParameter>switchParameters = switchParameterRepository.findAll();
                Shape shape = new Shape(Long.parseLong(csvRecord.get("shapeId")), csvRecord.get("name"),
                        csvRecord.get("creationDate"), csvRecord.get("command"),switchParameters);
                shapeList.add(shape);
            }
        }
        catch (Exception e)
        {
            System.out.println("Reading CSV Error!");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fileReader.close();
                csvParser.close();
            }
            catch (IOException e)
            {
                System.out.println("Closing fileReader/csvParser Error!");
                e.printStackTrace();
            }
        }
        return shapeList;
    }

    public static boolean isCSVFile(MultipartFile file)
    {
        String extension = file.getOriginalFilename().split("\\.")[1];
        if(!extension.equals(csvExtension))
        {
            return false;
        }
        return true;
    }
}
