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
    private Shape shape;
    private List<String> argsValues;

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", creationDate='" + creationDate + '\'' +
                ", shape=" + shape +
                ", argsValues=" + argsValues +
                '}';
    }
}
