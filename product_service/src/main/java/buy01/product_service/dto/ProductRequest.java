package buy01.product_service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    @NotBlank(message = "Category is required")
    private String category;

    private MultipartFile[] images;
}