package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;


import metaheuristics.grasp.AbstractGRASP;
import problems.qbf.QBF_Inverse;
import solutions.Solution;

public class QBF_SC extends AbstractGRASP<Integer> {

	
    private String searchType = "best"; // "first" ou "best"
    
    public QBF_SC(Double alpha, Integer iterations, String filename) throws IOException {
        super(new QBF_Inverse(filename), alpha, iterations);
    }
    
    public QBF_SC(Double alpha, Integer iterations, String filename, String searchType) throws IOException {
        super(new QBF_Inverse(filename), alpha, iterations);
        this.searchType = searchType.toLowerCase();
    }

	@Override
	public ArrayList<Integer> makeCL() {
		ArrayList<Integer> _CL = new ArrayList<>();
        for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
            _CL.add(i);
        }
        return _CL;
	}

	@Override
	public ArrayList<Integer> makeRCL() {
		ArrayList<Integer> rcl = new ArrayList<>();
        double bestCost = Double.POSITIVE_INFINITY;
        double worstCost = Double.NEGATIVE_INFINITY;

        for (Integer cand : CL) {
            double cost = ObjFunction.evaluateInsertionCost(cand, sol);
            if (cost < bestCost) bestCost = cost;
            if (cost > worstCost) worstCost = cost;
        }

        double threshold = bestCost + alpha * (worstCost - bestCost);
        for (Integer cand : CL) {
            double cost = ObjFunction.evaluateInsertionCost(cand, sol);
            if (cost <= threshold) {
                rcl.add(cand);
            }
        }

        return rcl;
	}

	@Override
	public void updateCL() {
		 // Todos os elementos fora da solução são candidatos
        CL.clear();
        for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
            if (!sol.contains(i)) {
                CL.add(i);
            }
        }
		
	}

	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> s = new Solution<>();
        s.cost = 0.0;
        return s;
	}

	@Override
	public Solution<Integer> localSearch() {
		boolean improved;

        do {
            improved = false;
            updateCL();

            if (searchType.equals("first")) {
                // FIRST-IMPROVING
                for (Integer candIn : CL) {
                    double delta = ObjFunction.evaluateInsertionCost(candIn, sol);
                    if (delta < -Double.MIN_VALUE) {
                        sol.add(candIn);
                        CL.remove(candIn);
                        ObjFunction.evaluate(sol);
                        improved = true;
                        break;
                    }
                }
                if (improved) continue;

                for (Integer candOut : new ArrayList<>(sol)) {
                    double delta = ObjFunction.evaluateRemovalCost(candOut, sol);
                    if (delta < -Double.MIN_VALUE) {
                        sol.remove(candOut);
                        CL.add(candOut);
                        ObjFunction.evaluate(sol);
                        improved = true;
                        break;
                    }
                }
                if (improved) continue;

                outerLoop:
                for (Integer candIn : CL) {
                    for (Integer candOut : new ArrayList<>(sol)) {
                        double delta = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
                        if (delta < -Double.MIN_VALUE) {
                            sol.remove(candOut);
                            CL.add(candOut);
                            sol.add(candIn);
                            CL.remove(candIn);
                            ObjFunction.evaluate(sol);
                            improved = true;
                            break outerLoop;
                        }
                    }
                }

            } else {
                // BEST-IMPROVING
                Double minDeltaCost;
                Integer bestCandIn = null, bestCandOut = null;

                do {
                    minDeltaCost = Double.POSITIVE_INFINITY;
                    updateCL();

                    for (Integer candIn : CL) {
                        double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
                        if (deltaCost < minDeltaCost) {
                            minDeltaCost = deltaCost;
                            bestCandIn = candIn;
                            bestCandOut = null;
                        }
                    }

                    for (Integer candOut : sol) {
                        double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
                        if (deltaCost < minDeltaCost) {
                            minDeltaCost = deltaCost;
                            bestCandIn = null;
                            bestCandOut = candOut;
                        }
                    }

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
                        improved = true;
                    }

                } while (minDeltaCost < -Double.MIN_VALUE);
            }

        } while (improved);

        return sol;
    
	}

}
