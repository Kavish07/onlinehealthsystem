package com.system.OnlineHealthSystem.config;

import com.onlinehealthsystem.AppointmentDAO;
import com.onlinehealthsystem.DoctorDAO;
import com.onlinehealthsystem.TreatmentDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wraps the still-unmigrated legacy DAOs as Spring beans purely so migrated
 * controllers (e.g. {@code PatientController}) can take them as
 * constructor-injected collaborators - needed to keep populating
 * session attributes like "listDoctors"/"listTreatments" that those
 * not-yet-migrated entities' JSPs still depend on - instead of each
 * controller doing {@code new DoctorDAO()} itself, which would make them
 * impossible to substitute with a mock in a {@code @WebMvcTest}.
 *
 * None of this changes the legacy DAOs themselves; delete each bean here
 * once its entity gets its own Spring Data repository/service in a later
 * slice.
 */
@Configuration
public class LegacyDaoBeansConfig {

    @Bean
    public DoctorDAO doctorDAO() {
        return new DoctorDAO();
    }

    @Bean
    public TreatmentDAO treatmentDAO() {
        return new TreatmentDAO();
    }

    @Bean
    public AppointmentDAO appointmentDAO() {
        return new AppointmentDAO();
    }
}
