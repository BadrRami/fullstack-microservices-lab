package service;

import dao.EtudiantDAO;
import model.Etudiant;
import exception.EmailDejaUtiliseException;
import java.util.List;

public class EtudiantService {

    private EtudiantDAO etudiantDAO;

    public EtudiantService(EtudiantDAO etudiantDAO) {
        this.etudiantDAO = etudiantDAO;
    }

    public List<Etudiant> afficherEtudiants() {
        return etudiantDAO.afficherEtudiants();
    }

    public Etudiant chercherEtudiantParId(int id) {

        if (id <= 0) {
            return null;
        }

        return etudiantDAO.chercherEtudiantParId(id);
    }

    public boolean ajouterEtudiant(Etudiant etudiant)
        throws EmailDejaUtiliseException {

    if (!donneesValides(etudiant)) {
        return false;
    }

    if (etudiantDAO.emailExiste(etudiant.getEmail())) {
        throw new EmailDejaUtiliseException(
                "Cet email est déjà utilisé."
        );
    }

    etudiantDAO.ajouterEtudiant(etudiant);

    return true;
}

    public boolean modifierEtudiant(Etudiant etudiant)
        throws EmailDejaUtiliseException {

        if (etudiant == null || etudiant.getId() <= 0) {
            return false;
        }

        if (!donneesValides(etudiant)) {
            return false;
        }

        if (etudiantDAO.emailExistePourAutreEtudiant(
                etudiant.getEmail(),
                etudiant.getId())) {

            throw new EmailDejaUtiliseException(
                    "Cet email est déjà utilisé par un autre étudiant."
            );
        }

        return etudiantDAO.modifierEtudiant(etudiant);
    }

    public boolean supprimerEtudiant(int id) {

        if (id <= 0) {
            return false;
        }

        return etudiantDAO.supprimerEtudiant(id);
    }

    private boolean donneesValides(Etudiant etudiant) {

        if (etudiant == null) {
            return false;
        }

        if (etudiant.getNom() == null ||
                etudiant.getNom().isBlank() ||
                etudiant.getNom().length() > 50) {
            return false;
        }

        if (etudiant.getPrenom() == null ||
                etudiant.getPrenom().isBlank() ||
                etudiant.getPrenom().length() > 50) {
            return false;
        }

        if (etudiant.getAge() <= 0 ||
                etudiant.getAge() > 120) {
            return false;
        }

        if (etudiant.getEmail() == null ||
                etudiant.getEmail().isBlank()) {
            return false;
        }

        if (!etudiant.getEmail().contains("@")) {
            return false;
        }

        return true;
    }
}