package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

class MedicalServiceImplTest {

    private final String patientId = "23bf5o15-q2f1-d1df-1y1x-o1fq2f14bf5o";
    private final BigDecimal patientNormalTemperature = new BigDecimal("36.6");
    private final BloodPressure patientNormalBloodPressure = new BloodPressure(120, 80);
    private final PatientInfo patientInfo = new PatientInfo(
            patientId, "Василий", "Тёркин",
            LocalDate.of(1987, 05, 05),
            new HealthInfo(patientNormalTemperature, patientNormalBloodPressure)
    );

    private final String warningMessage = String.format("Warning, patient with id: %s, need help", patientId);

    private final PatientInfoFileRepository patientRepositoryMock = Mockito.mock(PatientInfoFileRepository.class);
    private final SendAlertService sendAlertMock = Mockito.mock(SendAlertService.class);
    private final MedicalService medicalService = new MedicalServiceImpl(patientRepositoryMock, sendAlertMock);
    private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @BeforeEach
    void setUp() {
        Mockito.when(patientRepositoryMock.getById(patientId)).thenReturn(patientInfo);
        Mockito.doAnswer((Answer<Void>) invocationOnMock -> {
            System.out.println(warningMessage);
            return null;
        }).when(sendAlertMock).send(Mockito.any());
    }


    @Test
    void checkBloodPressure_badValue() {
        medicalService.checkBloodPressure(patientId, new BloodPressure(121, 68));
        Mockito.verify(sendAlertMock).send(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), warningMessage);
        Mockito.verify(sendAlertMock, Mockito.only()).send(warningMessage);
    }

    @Test
    void checkBloodPressure_normalValue() {
        medicalService.checkBloodPressure(patientId, patientNormalBloodPressure);
        Mockito.verify(sendAlertMock, Mockito.never()).send(warningMessage);
    }

    @Test
    void checkTemperature_badValue() {
        medicalService.checkTemperature(patientId, new BigDecimal("35.05"));
        Mockito.verify(sendAlertMock).send(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), warningMessage);
        Mockito.verify(sendAlertMock, Mockito.only()).send(warningMessage);
    }

    @Test
    void checkTemperature_normalValue() {
        medicalService.checkTemperature(patientId, patientNormalTemperature);
        Mockito.verify(sendAlertMock, Mockito.never()).send(warningMessage);
    }
}