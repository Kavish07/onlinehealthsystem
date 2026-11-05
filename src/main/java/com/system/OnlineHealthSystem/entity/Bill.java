package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Maps the {@code Bill} table. {@code BillID} is a generated surrogate key,
 * one row per {@link Treatment} (owned by {@code Treatments.BillID}, see
 * that entity's {@code bill} field). Modeled as its own entity/repository
 * rather than embedded, because {@code TreatmentDAO.updateTreatment} updates
 * a Bill row addressed directly by its own {@code BillID} - a value the
 * legacy servlet passes straight through from the edit form - independently
 * of the owning Treatment row (see {@code TreatmentService.updateTreatment}).
 */
@Entity
@Table(name = "Bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BillID")
    private Integer billId;

    @Column(name = "Date")
    private LocalDate billDate;

    @Column(name = "Amount")
    private Integer amount;

    @Column(name = "Paid")
    private boolean paid;

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
