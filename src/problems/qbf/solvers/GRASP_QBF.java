package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import metaheuristics.grasp.AbstractGRASP;
import problems.qbf.QBF_Inverse;
import solutions.Solution;



/**
 * Metaheuristic GRASP (Greedy Randomized Adaptive Search Procedure) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). Since by default this GRASP considers
 * minimization problems, an inverse QBF function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class GRASP_QBF extends AbstractGRASP<Integer> {
	
	private static Random rng = new Random(0);
	private String searchStrategy = "best";
	
	public void setSearchStrategy(String strategy) {
	    this.searchStrategy = strategy.toLowerCase();
	}

	
	private String constructionMethod = "default"; //para definir o método a ser utilizado - default(do professor)
	// randomplusgreedy e sampledgreedy que serão os métodos
	private int p = 2; //controla a aleatoriedade de ambos os métodos utilizados

	/**
	 * Constructor for the GRASP_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param alpha
	 *            The GRASP greediness-randomness parameter (within the range
	 *            [0,1])
	 * @param iterations
	 *            The number of iterations which the GRASP will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	
	//Construtor do professor
	public GRASP_QBF(Double alpha, Integer iterations, String filename) throws IOException {
		super(new QBF_Inverse(filename), alpha, iterations);
	}
	
	//Construtor para suportar os métodos alternativos
	//Esse construtor utiliza o construtor original mas acrescenta os dois parâmetros criados
	//tanto para definir o método quanto para determinador o p da aleatoriedade
	public GRASP_QBF(Double alpha, Integer iterations, String filename, String method, int p) throws IOException {
        super(new QBF_Inverse(filename), alpha, iterations);
        this.constructionMethod = method.toLowerCase();
        this.p = p;
    }
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeCL()
	 */
	//gera a lista de todos os possíveis candidatos
	@Override
	public ArrayList<Integer> makeCL() {
		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = i;
			_CL.add(cand);
		}
		return _CL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {
		ArrayList<Integer> _RCL = new ArrayList<Integer>();
		if(CL.isEmpty()) return _RCL;
		
		double minCost = Double.POSITIVE_INFINITY;
		double maxCost = Double.NEGATIVE_INFINITY;
		
		//cálculo dos custos de inserção dos candidatos
		for (Integer candidatos : CL) {
			double delta = ObjFunction.evaluateInsertionCost(candidatos, bestSol);
			if (delta < minCost) minCost = delta;
			if (delta > maxCost) maxCost = delta;
		}
		double threshold = minCost + alpha * (maxCost - minCost);
		//adiciona os candidatos acima do threshold
		for (Integer candidatos : CL) {
			double delta = ObjFunction.evaluateInsertionCost(candidatos, bestSol);
			if (delta <= threshold) {
	            _RCL.add(candidatos);
	        }
		}
		return _RCL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#updateCL()
	 */
	@Override
	public void updateCL() {
		// do nothing since all elements off the solution are viable candidates.
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}
	
	/**
	 * Aqui começa o que de fato implementamos
	 */
	
	
	//Esse método dá a possibilidade de escolher entre as três metaheurísticas a do professor e as duas nossas
	@Override
    public Solution<Integer> constructiveHeuristic() {
        switch (constructionMethod) {
            case "randomplusgreedy":
                return constructiveHeuristicRandomPlusGreedy(p);
            case "sampledgreedy":
                return constructiveHeuristicSampledGreedy(p);
            default:
                return super.constructiveHeuristic();
        }
    }
	
	/**
     * Método Random plus greedy
     */
	/**
     * Na fase aleatória desse método, de acordo com o número p definido de iterações, escolhemos o candidato CL
     *  e adionamos à solução, avaliando o custo da solução atual - de modo a introduzir diversidade na solução
     *  inicial. Na fase gulosa, avaliamos a lista CL quanto cada candidato poderia melhorar (ou piorar menos)
     *  a solução se fosse inserido. A ideia é explorar a parte gulosa, escolhendo sempre a melhor opção
     *  localmente para melhorar a solução gradualmente. Finalmente retorna uma solução construída que 
     *  combina aleatoriedade inicial e greediness na fase final.
     */
	
    public Solution<Integer> constructiveHeuristicRandomPlusGreedy(int p) {
    	this.sol = createEmptySol();
        this.CL = makeCL();

        // fase aleatória
        for (int i = 0; i < p && !CL.isEmpty(); i++) {
            int randIndex = rng.nextInt(CL.size());
            Integer cand = CL.remove(randIndex);
            sol.add(cand);
            ObjFunction.evaluate(sol);
        }

        // fase gulosa
        while (!CL.isEmpty()) {
            Integer bestCand = null;
            double bestDelta = Double.POSITIVE_INFINITY;

            for (Integer cand : CL) {
                double delta = ObjFunction.evaluateInsertionCost(cand, sol);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestCand = cand;
                }
            }

            if (bestCand == null) break;
            sol.add(bestCand);
            CL.remove(bestCand);
            ObjFunction.evaluate(sol);
        }

        return sol;
    }
    
    /**
     * Método Sampled greedy
     */
    /**
     Diferente do método anterior, aqui apenas uma pequena amostra dos candidatos são aleatórios e não toda 
     a lista CL de uma vez. Retiramos p elementos aleatórios de CL, calculamos o delta de custo, escolhemos 
     o candidato que mais melhora (ou menos piora) a solução. Os candidatos não escolhidos voltam à lista.
     */
    
    public Solution<Integer> constructiveHeuristicSampledGreedy(int p) {
    	this.sol = createEmptySol();
        this.CL = makeCL();

        //insere a parte aleatória do código
        while (!CL.isEmpty()) {
            // sorteia até p elementos da CL
            ArrayList<Integer> sample = new ArrayList<>();
            for (int i = 0; i < p && !CL.isEmpty(); i++) {
                int randIndex = rng.nextInt(CL.size());
                sample.add(CL.remove(randIndex));
            }

            // escolhe o melhor da amostra
            Integer bestCand = null;
            double bestDelta = Double.POSITIVE_INFINITY;
            for (Integer cand : sample) {
                double delta = ObjFunction.evaluateInsertionCost(cand, sol);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestCand = cand;
                }
            }

            if (bestCand != null) {
                sol.add(bestCand);
                ObjFunction.evaluate(sol);
                sample.remove(bestCand);
            }

            // devolve os candidatos não escolhidos para a CL
            CL.addAll(sample);
        }

        return sol;
    }


	

	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> localSearch() {
		
		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			// Evaluate insertions
			for (Integer candIn : CL) {
				double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
			// Evaluate removals
			for (Integer candOut : sol) {
				double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
			// Evaluate exchanges
			for (Integer candIn : CL) {
				for (Integer candOut : sol) {
					double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
			// Implement the best move, if it reduces the solution cost.
			if (minDeltaCost < -Double.MIN_VALUE) {
				if (bestCandOut != null) {
					sol.remove(bestCandOut);
					CL.add(bestCandOut);
				}
				if (bestCandIn != null) {
					sol.add(bestCandIn);
					CL.remove(bestCandIn);
				}
				ObjFunction.evaluate(sol);
			}
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;
	}

	/**
	 * A main method used for testing the GRASP metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		long maxTimeMillis = 30 * 60 * 1000;
		long startTime = System.currentTimeMillis();
		
		//parâmetros do construtor
		double alpha;
		int iterations;
		String filename;
		String method;
		int p;
		
	    if (args.length >= 3) {
	        alpha = Double.parseDouble(args[0]);
	        iterations = Integer.parseInt(args[1]);
	        filename = args[2];
	        method = args.length > 3 ? args[3] : "default";
	        p = args.length > 4 ? Integer.parseInt(args[4]) : 2;
	    } else {
	        // valores padrão
	        alpha = 0.05;
	        iterations = 1000;
	        filename = "instances/qbf/qbf400";
	        method = "default";   //randomplusgreedy; sampledgreedy; default 
	        p = 2;
	    }
				
		
		GRASP_QBF grasp = new GRASP_QBF(alpha, iterations, filename, method, p);
		Solution<Integer> bestSol = null;

	    while (System.currentTimeMillis() - startTime < maxTimeMillis) {
	        bestSol = grasp.solve(); // cada chamada executa uma iteração ou bloco
	        // dependendo de como solve() funciona, pode ser preciso rodar 1 iteração por vez
	        if (System.currentTimeMillis() - startTime >= maxTimeMillis) {
	            System.out.println("Tempo máximo atingido! Interrompendo execução...");
	            break;
	        }
	    }

		System.out.println("maxVal = " + bestSol);
		
		//GRASP_QBF grasp = new GRASP_QBF(0.05, 1000, "instances/qbf/qbf040");
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		System.out.println("=== Execução finalizada ===");
	}

}
