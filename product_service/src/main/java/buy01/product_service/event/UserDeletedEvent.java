package buy01.product_service.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    private String userId;

}