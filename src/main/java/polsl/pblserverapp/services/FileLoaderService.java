package polsl.pblserverapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.utils.ApacheCommonsCsvUtil;

import java.io.InputStream;
import java.util.List;

@Service
public class FileLoaderService
{
    private final ShapeRepository shapeRepository;
    private static final Logger log = LoggerFactory.getLogger(FileLoaderService.class);

    public FileLoaderService(ShapeRepository shapeRepository)
    {
        this.shapeRepository = shapeRepository;
    }

    public void store(InputStream file)
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
}
