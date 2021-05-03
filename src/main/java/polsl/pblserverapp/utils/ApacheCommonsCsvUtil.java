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
import java.util.Scanner;

@Service
public class ApacheCommonsCsvUtil
{
    private static final Logger log = LoggerFactory.getLogger(ApacheCommonsCsvUtil.class);
    private static String csvExtension = "csv";
    private static SwitchParameterRepository switchParameterRepository;
    private static final String COMMA_DELIMITER = ",";

    public ApacheCommonsCsvUtil(SwitchParameterRepository switchParameterRepository)
    {
        this.switchParameterRepository = switchParameterRepository;
    }

    public static void shapesToCsv(Writer writer, List<Shape> shapeList) throws IOException
    {
        try (CSVPrinter csvPrinter = new CSVPrinter(writer,
                CSVFormat.DEFAULT.withHeader("shapeId", "name", "command","parametersList")))
        {
            if(shapeList.size()==0)
            {
                throw new Exception("No shape can be exported. List is empty!");
            }
            else
            {
                for (Shape shape : shapeList)
                {
                    List<String> data = Arrays.asList(String.valueOf(shape.getShapeId()), String.valueOf(shape.getName()), String.valueOf(shape.getCommand()),String.valueOf(shape.getParametersList().toString()));
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
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(is))
        {
            while (scanner.hasNextLine())
            {
                records.add(getRecordFromLine(scanner.nextLine()));
            }
        }

        int counter = 0;
        List<Shape> shapeList = new ArrayList<>();
        for(List<String> s : records)
        {
            if(!s.toString().equals("[shapeId, name, command, parametersList]"))
            {
                Shape shape = new Shape();
                shape.setShapeId(Long.valueOf(s.get(0)));
                shape.setName(s.get(1));
                shape.setCommand(s.get(2));

                List<SwitchParameter> switchParameters = new ArrayList<>();

                List<String> tablicaArg = new ArrayList<>();
                for(int i=3;i<s.size();i++)
                {
                    tablicaArg.add(s.get(i));
                }
                for(int j=0;j< tablicaArg.size();j++)
                {
                    Long switchId = null;
                    String switchValue = null;

                    if(j==0 || j%2==0)
                    {
                        String idString = tablicaArg.get(j);
                        idString = idString.substring(idString.indexOf("=") + 1);
                        switchId = Long.valueOf(idString);
                        counter++;
                    }
                    else
                    {
                        String switchString = tablicaArg.get(j);
                        switchString = switchString.substring(switchString.indexOf("=") + 1);
                        switchString = switchString.substring(0, switchString.indexOf(")"));
                        switchValue = switchString;
                        counter++;
                    }
                    if(counter==2)
                    {
                        SwitchParameter loadedSwitch = switchParameterRepository.getBySwitchParam(switchValue);
                        switchParameters.add(loadedSwitch);
                        counter=0;
                    }
                }
                shape.setParametersList(switchParameters);
                shapeList.add(shape);
            }
            else continue;
        }
        return shapeList;
    }

    public static List<String> getRecordFromLine(String line)
    {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line))
        {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext())
            {
                values.add(rowScanner.next());
            }
        }
        return values;
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
