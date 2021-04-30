package polsl.pblserverapp.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public Shape(long shapeId, String name, String creationDate, String command, List<SwitchParameter> switchParameters)
    {
        SimpleDateFormat formatter5=new SimpleDateFormat("E, MMM dd yyyy HH:mm:ss");

        this.shapeId = shapeId;
        this.name = name;
        try
        {
            this.creationDate = formatter5.parse(creationDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            this.creationDate = new Date();
        }
        this.command = command;
        this.parametersList = switchParameters;
    }

    public Shape() {
    }
}
