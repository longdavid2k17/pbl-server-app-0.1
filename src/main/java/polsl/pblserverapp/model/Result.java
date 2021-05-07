package polsl.pblserverapp.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class Result
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String creationDate;
    private String endingDate;
    private String resultStatus;
    private String resultsUrl;
    private Long shapeId;
    private String fullCommand;
}
