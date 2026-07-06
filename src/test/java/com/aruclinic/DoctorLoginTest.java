package com.aruclinic;

import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.User;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
public class DoctorLoginTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private com.aruclinic.service.PrescriptionService prescriptionService;

    @Autowired
    private com.aruclinic.repository.PatientRepository patientRepository;

    @Autowired
    private com.aruclinic.repository.PrescriptionRepository prescriptionRepository;

    @Autowired
    private com.aruclinic.service.LocationService locationService;

    @Autowired
    private com.aruclinic.service.PatientService patientService;

    @Autowired
    private com.aruclinic.service.DoctorService doctorService;

    @Autowired
    private com.aruclinic.service.UserService userService;

    @Autowired
    private com.aruclinic.service.AdminService adminService;

    @Autowired
    private com.aruclinic.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.aruclinic.service.AppointmentService appointmentService;

    @Test
    public void testLocationService() {
        assertNotNull(locationService);
        com.aruclinic.service.LocationService.LocationDetails details = locationService.lookupPincode("791111");
        assertNotNull(details);
        System.out.println("==========================================");
        System.out.println("TEST pincode 791111: Cities=" + details.cities + ", District=" + details.district + ", State=" + details.state);
        System.out.println("==========================================");
        assertFalse(details.cities.isEmpty());
        assertTrue(details.cities.contains("Itanagar"));
        assertEquals("Papum Pare", details.district);
        assertEquals("Arunachal Pradesh", details.state);

        com.aruclinic.service.LocationService.LocationDetails details2 = locationService.lookupPincode("110001");
        assertNotNull(details2);
        System.out.println("==========================================");
        System.out.println("TEST pincode 110001: Cities=" + details2.cities + ", District=" + details2.district + ", State=" + details2.state);
        System.out.println("==========================================");
        assertFalse(details2.district.isEmpty());
        assertFalse(details2.cities.isEmpty());
    }

    @Test
    public void testDoctorSeededData() {
        // 1. Verify User exists
        Optional<User> userOpt = userRepository.findByEmail("doctor@example.com");
        assertTrue(userOpt.isPresent(), "User doctor@example.com should be seeded");
        User user = userOpt.get();
        System.out.println("User email: " + user.getEmail());
        System.out.println("User roles count: " + user.getRoles().size());
        user.getRoles().forEach(role -> System.out.println("User role: " + role.getName()));

        // 2. Verify Doctor exists
        Optional<Doctor> docOpt = doctorRepository.findByEmail("doctor@example.com");
        assertTrue(docOpt.isPresent(), "Doctor entity doctor@example.com should be seeded");
        Doctor doctor = docOpt.get();
        System.out.println("Doctor name: " + doctor.getName());
        System.out.println("Doctor id: " + doctor.getId());

        // 3. Verify UserDetails authority
        UserDetails userDetails = userDetailsService.loadUserByUsername("doctor@example.com");
        assertNotNull(userDetails);
        System.out.println("UserDetails username: " + userDetails.getUsername());
        userDetails.getAuthorities().forEach(auth -> System.out.println("UserDetails authority: " + auth.getAuthority()));

        boolean hasDoctorRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_DOCTOR".equals(auth.getAuthority()));
        assertTrue(hasDoctorRole, "User should have ROLE_DOCTOR authority");
    }

    @Test
    public void testCreatePrescription() {
        com.aruclinic.entity.Patient patient = patientRepository.findAll().get(0);
        Doctor doctor = doctorRepository.findAll().get(0);

        com.aruclinic.dto.PrescriptionDto dto = new com.aruclinic.dto.PrescriptionDto();
        dto.setPatientId(patient.getId());
        dto.setDoctorId(doctor.getId());
        dto.setPrescriptionDate(java.time.LocalDate.now());
        dto.setDiagnosis("Test Diagnosis");
        dto.setSymptoms("Test Symptoms");
        dto.setStatus("ACTIVE");

        List<com.aruclinic.dto.PrescriptionItemDto> items = new java.util.ArrayList<>();
        items.add(new com.aruclinic.dto.PrescriptionItemDto(null, null, "Paracetamol", "500mg", 5));
        dto.setItems(items);

        com.aruclinic.dto.PrescriptionDto saved = prescriptionService.createPrescription(dto);
        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertTrue(prescriptionRepository.findById(saved.getId()).isPresent());
    }

    @Test
    public void testSearchPrescriptions() {
        List<com.aruclinic.dto.PrescriptionDto> results = prescriptionService.searchPrescriptions("David");
        assertFalse(results.isEmpty(), "Should find prescriptions for patient David");
        System.out.println("Search results size: " + results.size());
        results.forEach(r -> System.out.println("Result: " + r.getPrescriptionId() + " - " + r.getPatientName()));
    }

    @Test
    public void testProfileSynchronization() {
        // Create a test User & corresponding Patient
        User testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("johndoe@sync-test.com");
        testUser.setMobileNumber("9876543210");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        com.aruclinic.entity.Patient testPatient = new com.aruclinic.entity.Patient();
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("johndoe@sync-test.com");
        testPatient.setMobileNumber("9876543210");
        testPatient.setDateOfBirth(java.time.LocalDate.of(1996, 1, 1));
        testPatient.setAge(30);
        testPatient = patientRepository.save(testPatient);

        // Update Patient through PatientService
        com.aruclinic.dto.PatientDto updateDto = new com.aruclinic.dto.PatientDto();
        updateDto.setFirstName("Johnny");
        updateDto.setLastName("Doey");
        updateDto.setEmail("johnnydoey@sync-test.com");
        updateDto.setMobileNumber("9876543211");

        patientService.updatePatient(testPatient.getId(), updateDto);

        // Verify Patient updated
        com.aruclinic.entity.Patient updatedPat = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals("Johnny", updatedPat.getFirstName());
        assertEquals("johnnydoey@sync-test.com", updatedPat.getEmail());

        // Verify User updated automatically in real-time
        User updatedUsr = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("Johnny", updatedUsr.getFirstName());
        assertEquals("johnnydoey@sync-test.com", updatedUsr.getEmail());
        assertEquals("9876543211", updatedUsr.getMobileNumber());
    }

    @Test
    public void testDoctorRescheduleAvailability() {
        // Create a Doctor with OUT_OF_OFFICE status
        Doctor testDoctor = new Doctor();
        testDoctor.setName("Dr. Unavailable");
        testDoctor.setQualification("MD");
        testDoctor.setExperience(10);
        testDoctor.setDepartment("Cardiology");
        testDoctor.setSpecialization("Cardiology");
        testDoctor.setMobileNumber("1234567891");
        testDoctor.setEmail("unavailable@clinic.com");
        testDoctor.setStatus("OUT_OF_OFFICE");
        testDoctor = doctorRepository.save(testDoctor);

        // Verify that status reflects OUT_OF_OFFICE
        Doctor fetched = doctorRepository.findById(testDoctor.getId()).orElseThrow();
        assertEquals("OUT_OF_OFFICE", fetched.getStatus());

        // Create an appointment for a patient and original doctor
        com.aruclinic.entity.Patient testPat = new com.aruclinic.entity.Patient();
        testPat.setFirstName("Jane");
        testPat.setLastName("Doe");
        testPat.setEmail("janedoe@resch.com");
        testPat.setMobileNumber("9876543299");
        testPat.setDateOfBirth(java.time.LocalDate.of(1996, 1, 1));
        testPat.setAge(30);
        testPat = patientRepository.save(testPat);

        com.aruclinic.entity.Appointment appt = new com.aruclinic.entity.Appointment();
        appt.setPatient(testPat);
        appt.setDoctor(testDoctor); // Dr. Unavailable
        appt.setAppointmentDate(java.time.LocalDate.now().plusDays(2));
        appt.setAppointmentTime(java.time.LocalTime.of(9, 0));
        appt.setAppointmentDateTime(java.time.LocalDateTime.of(java.time.LocalDate.now().plusDays(2), java.time.LocalTime.of(9, 0)));
        appt.setStatus(com.aruclinic.entity.AppointmentStatus.SCHEDULED);
        appt = appointmentRepository.save(appt);

        // Create a same-specialty doctor who is available
        Doctor otherDoctor = new Doctor();
        otherDoctor.setName("Dr. Specialty Backup");
        otherDoctor.setQualification("MD");
        otherDoctor.setExperience(8);
        otherDoctor.setDepartment("Cardiology");
        otherDoctor.setSpecialization("Cardiology");
        otherDoctor.setMobileNumber("1234567895");
        otherDoctor.setEmail("backup@clinic.com");
        otherDoctor.setStatus("AVAILABLE");
        otherDoctor = doctorRepository.save(otherDoctor);

        // Trigger reschedule with reassign to otherDoctor
        appointmentService.patientRescheduleAppointment(
            appt.getId(), 
            java.time.LocalDate.now().plusDays(3), 
            java.time.LocalTime.of(10, 0), 
            "original doc busy, reassigned", 
            otherDoctor.getId()
        );

        // Verify reassign
        com.aruclinic.entity.Appointment rescheduledAppt = appointmentRepository.findById(appt.getId()).orElseThrow();
        assertEquals(otherDoctor.getId(), rescheduledAppt.getDoctor().getId());
        assertEquals(java.time.LocalDate.now().plusDays(3), rescheduledAppt.getAppointmentDate());
        assertEquals(java.time.LocalTime.of(10, 0), rescheduledAppt.getAppointmentTime());
    }
}
