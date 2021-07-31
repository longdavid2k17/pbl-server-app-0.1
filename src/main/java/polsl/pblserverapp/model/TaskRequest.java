package polsl.pblserverapp.model;

import lombok.Data;

import java.util.List;

@Data
public class TaskRequest
{
    private List<String> selectedTasks;
}
