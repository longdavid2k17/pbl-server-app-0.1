package polsl.pblserverapp.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class QueueConfiguration implements Serializable
{
    private final Logger log = LoggerFactory.getLogger(QueueConfiguration.class);
    private QueueConfiguration instance = null;
    private String hostAddress;
    private int port;
    private String outputQueueName,inputQueueName;

    public QueueConfiguration()
    {

    }

    public String getHostAddress()
    {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getOutputQueueName()
    {
        return outputQueueName;
    }

    public void setOutputQueueName(String outputQueueName)
    {
        this.outputQueueName = outputQueueName;
    }

    public String getInputQueueName()
    {
        return inputQueueName;
    }

    public void setInputQueueName(String inputQueueName)
    {
        this.inputQueueName = inputQueueName;
    }

    @Override
    public String toString()
    {
        return "QueueConfiguration{" +
                "hostAddress='" + hostAddress + '\'' +
                ", port=" + port +
                ", outputQueueName='" + outputQueueName + '\'' +
                ", inputQueueName='" + inputQueueName + '\'' +
                '}';
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadSettings()
    {
        try(Scanner scanner = new Scanner(new File("settings.txt")))
        {
            List<String> loadedSettings = new ArrayList();
            while (scanner.hasNext())
            {
                loadedSettings.add(scanner.nextLine());
            }
            if(loadedSettings.size()!=4)
            {
                throw new Exception("Loaded values are not valid!");
            }
            else
            {
                setHostAddress(loadedSettings.get(0));
                setPort(Integer.parseInt(loadedSettings.get(1)));
                setInputQueueName(loadedSettings.get(2));
                setOutputQueueName(loadedSettings.get(3));
                instance = this;
            }
        }
        catch (Exception e)
        {
            log.warn("Error! Exception message  : "+e.getMessage());
            log.warn("Loading default settings and creating settings file");
            setHostAddress("localhost");
            setPort(5566);
            setInputQueueName("results");
            setOutputQueueName("tasks");
            createNewSettingsFile(this);
            instance = this;
        }
        finally
        {
            log.info("Loaded configuration: "+instance.toString());
        }
    }

    public void createNewSettingsFile(QueueConfiguration queueConfiguration)
    {
        log.warn("Creating new configuration file!");
        File outputFile = new File("settings.txt");
        try
        {
            boolean hasCreatedFile = outputFile.createNewFile();
            FileWriter writer = new FileWriter(outputFile);
            try(BufferedWriter buffer = new BufferedWriter(writer))
            {
                if(hasCreatedFile)
                {
                    buffer.write(queueConfiguration.getHostAddress()+"\n");
                    buffer.write(queueConfiguration.getPort()+"\n");
                    buffer.write(queueConfiguration.getInputQueueName()+"\n");
                    buffer.write(queueConfiguration.getOutputQueueName());
                    log.info("New configuration file created!");
                }
            }
            catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
        }
    }

    public QueueConfiguration getInstance()
    {
        return instance;
    }
}
