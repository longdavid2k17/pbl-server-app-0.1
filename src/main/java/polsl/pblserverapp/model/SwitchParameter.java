package polsl.pblserverapp.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class SwitchParameter
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String parameter;

    public SwitchParameter(String parameter)
    {
        this.parameter = parameter;
    }

    public SwitchParameter()
    {

    }
}
