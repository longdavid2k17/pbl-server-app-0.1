package polsl.pblserverapp.model;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Shape implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shapeId;
    private String name;
    private Date creationDate;
    private String command;
    @ManyToMany
    @Size(min = 1,message = "Musi zostać wybrany przynajmniej jeden parametr")
    private List<SwitchParameter> parametersList = new ArrayList<>();
}
