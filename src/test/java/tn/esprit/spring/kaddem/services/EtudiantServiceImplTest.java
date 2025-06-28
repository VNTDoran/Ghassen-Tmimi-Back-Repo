package tn.esprit.spring.kaddem.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.kaddem.entities.Etudiant;
import tn.esprit.spring.kaddem.entities.Option;
import tn.esprit.spring.kaddem.repositories.EtudiantRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtudiantServiceImplTest {

    @Mock
    private EtudiantRepository etudiantRepository;

    @InjectMocks
    private EtudiantServiceImpl etudiantService;

    private Etudiant etudiant;

    @BeforeEach
    void setUp() {
        etudiant = new Etudiant();
        etudiant.setIdEtudiant(1);
        etudiant.setNomE("Doe");
        etudiant.setPrenomE("John");
        etudiant.setOp(Option.SE);
    }

    @Test
    void retrieveAllEtudiants_ShouldReturnAllStudents() {
        // Arrange
        when(etudiantRepository.findAll()).thenReturn(Arrays.asList(etudiant));

        // Act
        List<Etudiant> result = etudiantService.retrieveAllEtudiants();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).getNomE());
        verify(etudiantRepository, times(1)).findAll();
    }

    @Test
    void addEtudiant_ShouldSaveAndReturnStudent() {
        // Arrange
        when(etudiantRepository.save(any(Etudiant.class))).thenReturn(etudiant);

        // Act
        Etudiant result = etudiantService.addEtudiant(etudiant);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getIdEtudiant());
        verify(etudiantRepository, times(1)).save(etudiant);
    }

    @Test
    void updateEtudiant_ShouldUpdateAndReturnStudent() {
        // Arrange
        when(etudiantRepository.existsById(1)).thenReturn(true); // ✅ mock existence
        etudiant.setNomE("Updated");
        when(etudiantRepository.save(any(Etudiant.class))).thenReturn(etudiant);

        // Act
        Etudiant result = etudiantService.updateEtudiant(etudiant);

        // Assert
        assertEquals("Updated", result.getNomE());
        verify(etudiantRepository).existsById(1);
        verify(etudiantRepository).save(etudiant);
    }



    @Test
    void retrieveEtudiant_WhenExists_ShouldReturnStudent() {
        // Arrange
        when(etudiantRepository.findById(1)).thenReturn(Optional.of(etudiant));

        // Act
        Etudiant result = etudiantService.retrieveEtudiant(1);

        // Assert
        assertNotNull(result);
        assertEquals("Doe", result.getNomE());
    }

    @Test
    void retrieveEtudiant_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(etudiantRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            etudiantService.retrieveEtudiant(1);
        });
    }

    @Test
    void removeEtudiant_ShouldCallDelete() {
        // Arrange
        when(etudiantRepository.existsById(1)).thenReturn(true);
        doNothing().when(etudiantRepository).deleteById(1);

        // Act
        etudiantService.removeEtudiant(1);

        // Assert
        verify(etudiantRepository).existsById(1);
        verify(etudiantRepository).deleteById(1);
    }



    @Test
    void getEtudiantsByDepartement_ShouldReturnStudents() {
        // Arrange
        when(etudiantRepository.findEtudiantsByDepartement_IdDepart(1))
                .thenReturn(Arrays.asList(etudiant));

        // Act
        List<Etudiant> result = etudiantService.getEtudiantsByDepartement(1);

        // Assert
        assertEquals(1, result.size());
        verify(etudiantRepository, times(1))
                .findEtudiantsByDepartement_IdDepart(1);
    }
}