package tn.esprit.spring.kaddem.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.spring.kaddem.entities.*;
import tn.esprit.spring.kaddem.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class EtudiantServiceImpl implements IEtudiantService {

	private static final Logger logger = LogManager.getLogger(EtudiantServiceImpl.class);

	@Autowired
	private EtudiantRepository etudiantRepository;

	@Autowired
	private ContratRepository contratRepository;

	@Autowired
	private EquipeRepository equipeRepository;

	@Autowired
	private DepartementRepository departementRepository;

	@Override
	public List<Etudiant> retrieveAllEtudiants() {
		logger.info("Entering retrieveAllEtudiants()");
		try {
			List<Etudiant> etudiants = (List<Etudiant>) etudiantRepository.findAll();
			logger.info("Successfully retrieved {} students", etudiants.size());
			return etudiants;
		} catch (Exception e) {
			logger.error("Error retrieving students: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to retrieve students", e);
		}
	}

	@Override
	public Etudiant addEtudiant(Etudiant e) {
		logger.info("Adding new student: {} {}", e.getNomE(), e.getPrenomE());
		try {
			if (e == null) {
				logger.warn("Attempted to add null student");
				throw new IllegalArgumentException("Student cannot be null");
			}
			Etudiant savedEtudiant = etudiantRepository.save(e);
			logger.info("Successfully added student with ID: {}", savedEtudiant.getIdEtudiant());
			return savedEtudiant;
		} catch (Exception ex) {
			logger.error("Error adding student: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to add student", ex);
		}
	}

	@Override
	public Etudiant updateEtudiant(Etudiant e) {
		logger.info("Updating student with ID: {}", e.getIdEtudiant());
		try {
			if (!etudiantRepository.existsById(e.getIdEtudiant())) {
				logger.warn("Student with ID {} not found for update", e.getIdEtudiant());
				throw new RuntimeException("Student not found with ID: " + e.getIdEtudiant());
			}
			Etudiant updatedEtudiant = etudiantRepository.save(e);
			logger.info("Successfully updated student with ID: {}", updatedEtudiant.getIdEtudiant());
			return updatedEtudiant;
		} catch (Exception ex) {
			logger.error("Error updating student: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to update student", ex);
		}
	}

	@Override
	public Etudiant retrieveEtudiant(Integer idEtudiant) {
		logger.info("Retrieving student with ID: {}", idEtudiant);
		try {
			Optional<Etudiant> etudiant = etudiantRepository.findById(idEtudiant);
			if (etudiant.isPresent()) {
				logger.info("Successfully retrieved student with ID: {}", idEtudiant);
				return etudiant.get();
			} else {
				logger.warn("Student with ID {} not found", idEtudiant);
				throw new RuntimeException("Student not found with ID: " + idEtudiant);
			}
		} catch (Exception e) {
			logger.error("Error retrieving student: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to retrieve student", e);
		}
	}

	@Override
	public void removeEtudiant(Integer idEtudiant) {
		logger.info("Deleting student with ID: {}", idEtudiant);
		try {
			if (!etudiantRepository.existsById(idEtudiant)) {
				logger.warn("Student with ID {} not found for deletion", idEtudiant);
				throw new RuntimeException("Student not found with ID: " + idEtudiant);
			}
			etudiantRepository.deleteById(idEtudiant);
			logger.info("Successfully deleted student with ID: {}", idEtudiant);
		} catch (Exception e) {
			logger.error("Error deleting student: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to delete student", e);
		}
	}

	@Override
	public void assignEtudiantToDepartement(Integer etudiantId, Integer departementId) {
		logger.info("Assigning student {} to department {}", etudiantId, departementId);
		try {
			Etudiant etudiant = etudiantRepository.findById(etudiantId)
					.orElseThrow(() -> {
						logger.warn("Student with ID {} not found", etudiantId);
						return new RuntimeException("Student not found with ID: " + etudiantId);
					});

			Departement departement = departementRepository.findById(departementId)
					.orElseThrow(() -> {
						logger.warn("Department with ID {} not found", departementId);
						return new RuntimeException("Department not found with ID: " + departementId);
					});

			etudiant.setDepartement(departement);
			etudiantRepository.save(etudiant);
			logger.info("Successfully assigned student {} to department {}", etudiantId, departementId);
		} catch (Exception e) {
			logger.error("Error assigning student to department: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to assign student to department", e);
		}
	}

	@Override
	@Transactional
	public Etudiant addAndAssignEtudiantToEquipeAndContract(Etudiant e, Integer idContrat, Integer idEquipe) {
		logger.info("Adding and assigning student to contract {} and team {}", idContrat, idEquipe);
		try {
			// Save the student first
			Etudiant savedEtudiant = etudiantRepository.save(e);
			logger.debug("Saved student with ID: {}", savedEtudiant.getIdEtudiant());

			// Assign contract
			Contrat contrat = contratRepository.findById(idContrat)
					.orElseThrow(() -> {
						logger.warn("Contract with ID {} not found", idContrat);
						return new RuntimeException("Contract not found with ID: " + idContrat);
					});

			contrat.setEtudiant(savedEtudiant);
			contratRepository.save(contrat);
			logger.debug("Assigned contract {} to student {}", idContrat, savedEtudiant.getIdEtudiant());

			// Assign team
			Equipe equipe = equipeRepository.findById(idEquipe)
					.orElseThrow(() -> {
						logger.warn("Team with ID {} not found", idEquipe);
						return new RuntimeException("Team not found with ID: " + idEquipe);
					});

			equipe.getEtudiants().add(savedEtudiant);
			equipeRepository.save(equipe);
			logger.debug("Assigned student {} to team {}", savedEtudiant.getIdEtudiant(), idEquipe);

			logger.info("Successfully added and assigned student");
			return savedEtudiant;
		} catch (Exception ex) {
			logger.error("Error adding and assigning student: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to add and assign student", ex);
		}
	}

	@Override
	public List<Etudiant> getEtudiantsByDepartement(Integer idDepartement) {
		logger.info("Getting students by department {}", idDepartement);
		try {
			List<Etudiant> etudiants = etudiantRepository.findEtudiantsByDepartement_IdDepart(idDepartement);
			logger.info("Found {} students in department {}", etudiants.size(), idDepartement);
			return etudiants;
		} catch (Exception e) {
			logger.error("Error getting students by department: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to get students by department", e);
		}
	}
}