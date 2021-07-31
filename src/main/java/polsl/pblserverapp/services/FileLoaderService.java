package polsl.pblserverapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.utils.ApacheCommonsCsvUtil;
import polsl.pblserverapp.utils.ApacheXlsxUtil;

import java.io.InputStream;
import java.util.List;

@Service
public class FileLoaderService
{
    private final ShapeRepository shapeRepository;
    private static final Logger log = LoggerFactory.getLogger(FileLoaderService.class);
    private final QueueService queueService;

    public FileLoaderService(ShapeRepository shapeRepository, QueueService queueService)
    {
        this.shapeRepository = shapeRepository;
        this.queueService = queueService;
    }

    public void storeCSV(InputStream file)
    {
        try
        {
            List<Shape> shapeList = ApacheCommonsCsvUtil.parseCsvFile(file);
            shapeRepository.saveAll(shapeList);
            log.info("File imported successfully!");
        }
        catch(Exception e)
        {
            log.error("Error while importing .csv file. Code: "+e.getMessage());
            throw new RuntimeException("Error! " + e.getMessage());
        }
    }

    public void storeExcelFile(InputStream file, Long ownerId, Long queueId)
    {
        try
        {
            List<String> loadedTasks;
            if(ApacheXlsxUtil.format.equals(ApacheXlsxUtil.xlsxExtension))
            {
               loadedTasks  = ApacheXlsxUtil.parseXlsxFile(file);
            }
            else
            {
                loadedTasks = ApacheXlsxUtil.parseXlsFile(file);
            }

            log.info("File imported successfully! List size: "+loadedTasks.size());
            queueService.sendTaskList(loadedTasks, ownerId, queueId);
        }
        catch(Exception e)
        {
            log.error("Error while importing .xlsx file. Code: "+e.getMessage());
            throw new RuntimeException("Error! " + e.getMessage());
        }
    }

    public List<String> loadAndReturnExcelTasks(InputStream file, Long ownerId, Long queueId)
    {
        try
        {
            List<String> loadedTasks;
            if(ApacheXlsxUtil.format.equals(ApacheXlsxUtil.xlsxExtension))
            {
                loadedTasks  = ApacheXlsxUtil.parseXlsxFile(file);
            }
            else
            {
                loadedTasks = ApacheXlsxUtil.parseXlsFile(file);
            }

            log.info("File imported successfully! List size: "+loadedTasks.size());
            return loadedTasks;
        }
        catch(Exception e)
        {
            log.error("Error while importing .xlsx file. Code: "+e.getMessage());
            throw new RuntimeException("Error! " + e.getMessage());
        }
    }
}
