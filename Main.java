import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

import dao.EtudiantDAO;
import database.ConnexionDB;
import model.Etudiant;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        Connection connexion = ConnexionDB.getConnexion();

        if (connexion == null) {
            System.out.println("Impossible de se connecter à PostgreSQL.");
            input.close();
            return;
        }

        EtudiantDAO etudiantDAO = new EtudiantDAO(connexion);

        int choix;

        do {

            System.out.println("\n===== GESTION DES ÉTUDIANTS =====");
            System.out.println("1. Afficher les étudiants");
            System.out.println("2. Ajouter un étudiant");
            System.out.println("3. Modifier un étudiant");
            System.out.println("4. Supprimer un étudiant");
            System.out.println("5. Quitter");
            System.out.print("Choisissez une option : ");

            choix = input.nextInt();
            input.nextLine();

            switch (choix) {

                case 1:

                    List<Etudiant> etudiants = etudiantDAO.afficherEtudiants();

                    for (Etudiant etudiant : etudiants) {

                        System.out.println(
                            "ID: " + etudiant.getId() +
                            ", Nom: " + etudiant.getNom() +
                            ", Prénom: " + etudiant.getPrenom() +
                            ", Âge: " + etudiant.getAge() +
                            ", Email: " + etudiant.getEmail()
                        );
                    }

                    break;

                case 2:

                    System.out.print("Entrez le nom : ");
                    String nom = input.nextLine();

                    System.out.print("Entrez le prénom : ");
                    String prenom = input.nextLine();

                    System.out.print("Entrez l'âge : ");
                    int age = input.nextInt();
                    input.nextLine();

                    System.out.print("Entrez l'email : ");
                    String email = input.nextLine();

                    Etudiant nouvelEtudiant = new Etudiant(nom, prenom, age, email);

                    etudiantDAO.ajouterEtudiant(nouvelEtudiant);

                    break;

                case 3:

                    System.out.print("Entrez l'ID : ");
                    int id = input.nextInt();
                    input.nextLine();

                    System.out.print("Nouveau nom : ");
                    String nouveauNom = input.nextLine();

                    System.out.print("Nouveau prénom : ");
                    String nouveauPrenom = input.nextLine();

                    System.out.print("Nouvel âge : ");
                    int nouvelAge = input.nextInt();
                    input.nextLine();

                    System.out.print("Nouvel email : ");
                    String nouvelEmail = input.nextLine();

                    Etudiant etudiantModifie =
                            new Etudiant(
                                id,
                                nouveauNom,
                                nouveauPrenom,
                                nouvelAge,
                                nouvelEmail
                            );

                    etudiantDAO.modifierEtudiant(etudiantModifie);

                    break;

                case 4:

                    System.out.print("Entrez l'ID de l'étudiant à supprimer : ");
                    int idSuppression = input.nextInt();
                    input.nextLine();

                    etudiantDAO.supprimerEtudiant(idSuppression);

                    break;

                case 5:
                    System.out.println("Au revoir !");
                    break;

                default:
                    System.out.println("Choix invalide !");
            }

        } while (choix != 5);

        try {
            connexion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        input.close();
    }
}