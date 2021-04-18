package polsl.pblserverapp.model;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;

@Entity
public class Shape
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shapeId;
    private String name;
    private int parametersCount;

    @ElementCollection
    private List<String> parametersList;
    // TODO może wyrzucać wyjatek
}
