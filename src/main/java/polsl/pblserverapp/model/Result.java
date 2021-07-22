package polsl.pblserverapp.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Data
public class Result
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String creationDate;
    private String creationHour;
    private String endingDate;
    private String endingHour;
    private String resultStatus;
    private String errorCode;
    private String queueName;
    private Long shapeId;
    private String ownerUsername;
    private Long ownerId;
    @Size(max = 1000)
    @Column(length = 1000)
    private String fullCommand;
}
