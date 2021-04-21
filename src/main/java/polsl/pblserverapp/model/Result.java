package polsl.pblserverapp.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class Result
{
    @Id
    private Long id;
    private Date creationDate;
    private String resultStatus;
    private String url;


}
