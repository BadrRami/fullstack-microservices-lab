import java.util.Scanner;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        Connection connexion = ConnexionDB.getConnexion();

        if (connexion == null) {
            System.out.println("Impossible de se connecter à PostgreSQL.");
            return;
        }

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

            switch (choix) {

                case 1:
                    Etudiant.afficherEtudiants(connexion);
                    break;

                case 2:
                    Etudiant.AjouterEtudiant(connexion);
                    break;

                case 3:
                    Etudiant.ModifierEtudiant(connexion);
                    break;

                case 4:
                    Etudiant.SupprimerEtudiant(connexion);
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