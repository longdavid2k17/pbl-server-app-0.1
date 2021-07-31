package polsl.pblserverapp.model;

import lombok.Data;

@Data
public class Filter
{
    private String startDate;
    private String startHour;
    private String endDate;
    private String endHour;
    private String userId;
    private String status;
}
