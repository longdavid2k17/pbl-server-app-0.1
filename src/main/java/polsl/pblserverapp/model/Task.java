package polsl.pblserverapp.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Data
public class Task
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    private String creationDate;
    private String creationHour;
    private Shape shape;
    private String ownerUsername;
    private Long ownerId;
    private List<String> argsValues;

    @Override
    public String toString()
    {
        return "Task{" +
                "taskId=" + taskId +
                ", creationDate='" + creationDate + '\'' +
                ", creationHour='" + creationHour + '\'' +
                ", shape=" + shape +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", ownerId=" + ownerId +
                ", argsValues=" + argsValues +
                '}';
    }
}
