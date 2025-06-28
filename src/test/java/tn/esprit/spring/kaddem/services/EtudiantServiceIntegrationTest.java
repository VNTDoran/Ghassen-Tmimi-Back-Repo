package tn.esprit.spring.kaddem.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.spring.kaddem.entities.Etudiant;
import tn.esprit.spring.kaddem.entities.Option;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EtudiantServiceIntegrationTest {

    @Autowired
    private IEtudiantService etudiantService;

    @Test
    void testIntegrationAddAndRetrieveEtudiant() {
        // Create new student
        Etudiant newEtudiant = new Etudiant();
        newEtudiant.setNomE("Test");
        newEtudiant.setPrenomE("Integration");
        newEtudiant.setOp(Option.SE);

        // Add student
        Etudiant addedEtudiant = etudiantService.addEtudiant(newEtudiant);
        assertNotNull(addedEtudiant.getIdEtudiant());

        // Retrieve student
        Etudiant retrievedEtudiant = etudiantService.retrieveEtudiant(addedEtudiant.getIdEtudiant());
        assertEquals("Test", retrievedEtudiant.getNomE());

        // Cleanup
        etudiantService.removeEtudiant(addedEtudiant.getIdEtudiant());
    }
}