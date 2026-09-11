package com.bento.crm.deal.dto;

import com.bento.crm.deal.model.DealOrderLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** Wire shape of one order line, used both on write (id ignored) and on read. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineDto {

    private UUID id;
    private String product;
    private String description;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal total;
    private String vendor;

    public static OrderLineDto fromEntity(DealOrderLine line) {
        return OrderLineDto.builder()
                .id(line.getId())
                .product(line.getProduct())
                .description(line.getDescription())
                .qty(line.getQty())
                .unitPrice(line.getUnitPrice())
                .discount(line.getDiscount())
                .total(line.getTotal())
                .vendor(line.getVendor())
                .build();
    }

    /** qty × unitPrice − discount when the client did not send a total. */
    public BigDecimal effectiveTotal() {
        if (total != null) {
            return total;
        }
        if (qty == null || unitPrice == null) {
            return null;
        }
        BigDecimal gross = qty.multiply(unitPrice);
        return discount != null ? gross.subtract(discount) : gross;
    }
}
