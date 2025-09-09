package problems.qbf.solvers;

import java.io.IOException;
import solutions.Solution;


public class MainQBF_SC {

	public static void main(String[] args) throws IOException {
        long maxTimeMillis = 30 * 60 * 1000; // 30 minutos
        long startTime = System.currentTimeMillis();

        // Parâmetros padrão
        double alpha = 0.05;
        int iterations = 1000;
        String filename = "instances/qbf/qbf400";
        String searchType = "first"; // "first" ou "best"

        // Leitura de parâmetros passados por linha de comando
        if (args.length >= 3) {
            alpha = Double.parseDouble(args[0]);
            iterations = Integer.parseInt(args[1]);
            filename = args[2];
            searchType = args.length > 3 ? args[3].toLowerCase() : searchType;
        }

        System.out.println("Executando QBF_SC com:");
        System.out.println("alpha = " + alpha + ", iterations = " + iterations);
        System.out.println("arquivo = " + filename + ", searchType = " + searchType);

        // Criação do objeto GRASP com busca local
        QBF_SC grasp = new QBF_SC(alpha, iterations, filename, searchType);
        Solution<Integer> bestSol = null;

        // Loop até o tempo máximo definido
        while (System.currentTimeMillis() - startTime < maxTimeMillis) {
            bestSol = grasp.solve();
            if (System.currentTimeMillis() - startTime >= maxTimeMillis) {
                System.out.println("Tempo máximo atingido! Interrompendo execução...");
                break;
            }
        }

        System.out.println("=== Execução finalizada ===");
        System.out.println("Melhor solução encontrada:");
        System.out.println(bestSol);

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        System.out.println("Tempo total = " + totalTime + " segundos");
    }
}
