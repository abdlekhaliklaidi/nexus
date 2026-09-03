package buy01.gateway_service.event;

import java.util.ArrayList;

import lombok.*;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddblacklistEvent {
    private ArrayList<String> blacklist;
}