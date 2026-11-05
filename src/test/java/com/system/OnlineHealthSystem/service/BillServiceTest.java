package com.system.OnlineHealthSystem.service;

import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.entity.Treatment;
import com.system.OnlineHealthSystem.repository.TreatmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Test
    void listBills_returnsTheBillOfEachOfThePatientsTreatments() {
        Bill first = new Bill();
        Bill second = new Bill();
        Treatment t1 = new Treatment();
        t1.setBill(first);
        Treatment t2 = new Treatment();
        t2.setBill(second);
        when(treatmentRepository.findByPatientLoginId(7)).thenReturn(List.of(t1, t2));

        assertThat(new BillService(treatmentRepository).listBills(7)).containsExactly(first, second);
    }
}
