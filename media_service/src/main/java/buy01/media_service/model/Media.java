package buy01.media_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection="media")
public class Media {

    @Id
    private String id;

    private String imagePath;

    private String productId;

}