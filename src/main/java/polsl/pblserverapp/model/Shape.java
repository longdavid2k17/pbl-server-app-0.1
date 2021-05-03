package polsl.pblserverapp.model;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Shape implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shapeId;
    private String name;
    private String command;
    @ManyToMany
    private List<SwitchParameter> parametersList = new ArrayList<>();

    public Shape() {
    }
}
