package com.bento.crm.deal.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * One product/service line on a deal's order. Owned entirely by the deal: lines are replaced
 * wholesale whenever the deal is written with an {@code orderLines} array, and deleted with it.
 *
 * <p>The table has existed since V4 but nothing wrote to it, so line items entered in the
 * "New Deal" form were silently discarded and only the computed total survived.</p>
 */
@Entity
@Table(name = "deal_order_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealOrderLine extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Deal deal;

    private String product;

    @Column(columnDefinition = "text")
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal qty;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(precision = 19, scale = 2)
    private BigDecimal total;

    private String vendor;
}
